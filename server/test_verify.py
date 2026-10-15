import unittest
from unittest.mock import patch
import os, json, io
from urllib.error import HTTPError
from verify import verify, SKU
class VerifyTests(unittest.TestCase):
    def check(self, changes=None):
        value={'productId':SKU,'productType':'ENTITLED','receiptId':'receipt','cancelDate':None}
        value.update(changes or {})
        return patch('verify.urlopen',return_value=io.BytesIO(json.dumps(value).encode()))
    @patch.dict(os.environ,{'AMAZON_SHARED_SECRET':'test-only','AMAZON_RVS_ENV':'production'})
    def test_valid_and_cancelled(self):
        with self.check(): self.assertFalse(verify('user','receipt')['cancelled'])
        with self.check({'cancelDate':123}): self.assertTrue(verify('user','receipt')['cancelled'])
    @patch.dict(os.environ,{'AMAZON_SHARED_SECRET':'test-only','AMAZON_RVS_ENV':'production'})
    def test_wrong_products_and_receipts_are_rejected(self):
        for changes in [{'productId':'wrong'},{'productType':'SUBSCRIPTION'},{'receiptId':'other'}]:
            with self.check(changes), self.assertRaises(ValueError): verify('user','receipt')
    @patch.dict(os.environ,{'AMAZON_SHARED_SECRET':'test-only','AMAZON_RVS_ENV':'production'})
    def test_gone_receipts_revoke_but_server_errors_do_not_grant(self):
        with patch('verify.urlopen',side_effect=HTTPError('https://example.invalid',410,'Gone',{},None)):
            self.assertTrue(verify('user','receipt')['cancelled'])
        with patch('verify.urlopen',side_effect=HTTPError('https://example.invalid',500,'Failure',{},None)),self.assertRaises(HTTPError): verify('user','receipt')
if __name__=='__main__': unittest.main()
