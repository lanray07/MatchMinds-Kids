"""Vercel Python entrypoint. Secrets are environment variables, never source."""
from http.server import BaseHTTPRequestHandler
from urllib.request import urlopen
from urllib.parse import quote
from urllib.error import HTTPError
import json
import os

SKU = 'matchminds.premium.lifetime'

def verify_receipt(user, receipt, environment=None):
    secret = os.environ.get('AMAZON_SHARED_SECRET', '').strip()
    if not secret:
        raise RuntimeError('Verifier not configured')
    environment = environment or os.environ.get('AMAZON_RVS_ENV', 'production')
    if environment not in ('production', 'sandbox'):
        raise ValueError('Invalid environment')
    version = 'version/1.0' if environment == 'production' else 'sandbox/version/1.0'
    url = 'https://appstore-sdk.amazon.com/{}/verifyReceiptId/developer/{}/user/{}/receiptId/{}'.format(
        version, quote(secret, safe=''), quote(user, safe=''), quote(receipt, safe=''))
    try:
        with urlopen(url, timeout=10) as response:
            data = json.load(response)
    except HTTPError as error:
        if error.code == 410:
            return {'sku': SKU, 'userId': user, 'receiptId': receipt, 'cancelled': True}
        raise
    if data.get('productId') != SKU or data.get('productType') != 'ENTITLED' or data.get('receiptId') != receipt:
        raise ValueError('Invalid receipt')
    return {'sku': SKU, 'userId': user, 'receiptId': receipt, 'cancelled': data.get('cancelDate') is not None}

class handler(BaseHTTPRequestHandler):
    rvs_environment = 'production'
    def log_message(self, *_):
        pass

    def reply(self, code, data):
        body = json.dumps(data).encode()
        self.send_response(code)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Cache-Control', 'no-store')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        self.reply(200, {'service': 'MatchMinds receipt verification', 'configured': bool(os.environ.get('AMAZON_SHARED_SECRET'))})

    def do_POST(self):
        try:
            length = int(self.headers.get('Content-Length', '0'))
            if not 0 < length <= 4096:
                return self.reply(413, {'error': 'Invalid request size'})
            data = json.loads(self.rfile.read(length))
            user, receipt = data.get('userId'), data.get('receiptId')
            if not all(isinstance(v, str) and 0 < len(v) < 1024 for v in (user, receipt)):
                return self.reply(400, {'error': 'Invalid receipt identifiers'})
            self.reply(200, verify_receipt(user, receipt, self.rvs_environment))
        except Exception:
            # Do not log Amazon URLs, IDs, response bodies or secrets.
            self.reply(503, {'error': 'Receipt verification unavailable'})
