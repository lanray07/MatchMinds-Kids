"""Minimal Amazon RVS proxy. Run behind a TLS reverse proxy; never ship the secret in an APK.
Set AMAZON_SHARED_SECRET, AMAZON_RVS_ENV=production|sandbox and PORT. No request logging.
Production requires rate limiting, request size limits and operational monitoring at the proxy.
"""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.request import urlopen
from urllib.parse import quote
from urllib.error import HTTPError
import json, os

SKU = 'matchminds.premium.lifetime'
def verify(user, receipt):
    secret = os.environ['AMAZON_SHARED_SECRET']
    environment = os.environ.get('AMAZON_RVS_ENV','production')
    if environment not in ('production','sandbox'): raise ValueError('Unknown RVS environment')
    host = 'https://appstore-sdk.amazon.com'
    path = 'version/1.0' if environment == 'production' else 'sandbox/version/1.0'
    url = f'{host}/{path}/verifyReceiptId/developer/{quote(secret,safe="")}/user/{quote(user,safe="")}/receiptId/{quote(receipt,safe="")}'
    try:
        with urlopen(url, timeout=10) as response: data = json.load(response)
    except HTTPError as error:
        if error.code == 410: return {'sku':SKU,'userId':user,'receiptId':receipt,'cancelled':True}
        raise
    if data.get('productId') != SKU: raise ValueError('Unexpected product')
    if data.get('productType') != 'ENTITLED': raise ValueError('Expected one-time entitlement')
    if data.get('receiptId') != receipt: raise ValueError('Receipt mismatch')
    return {'sku': SKU, 'userId': user, 'receiptId': receipt, 'cancelled': data.get('cancelDate') is not None}

class Handler(BaseHTTPRequestHandler):
    def log_message(self, *_): pass
    def do_POST(self):
        try:
            if self.path != '/verify': self.send_error(404); return
            length = int(self.headers.get('Content-Length','0'))
            if not 0 < length <= 4096: self.send_error(413); return
            data = json.loads(self.rfile.read(length))
            user, receipt = data['userId'], data['receiptId']
            if not all(isinstance(x,str) and 0 < len(x) < 1024 for x in (user,receipt)): raise ValueError()
            result = json.dumps(verify(user,receipt)).encode()
            self.send_response(200); self.send_header('Content-Type','application/json'); self.send_header('Content-Length',str(len(result))); self.end_headers(); self.wfile.write(result)
        except Exception:
            self.send_error(503,'Receipt verification unavailable')

if __name__ == '__main__': ThreadingHTTPServer(('127.0.0.1',int(os.environ.get('PORT','8080'))),Handler).serve_forever()
