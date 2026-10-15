import importlib.util
import io
import json
import os
import unittest
from pathlib import Path
from unittest.mock import patch
spec = importlib.util.spec_from_file_location('vercel_verify', Path(__file__).parent/'vercel/api/verify.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class VerifierTests(unittest.TestCase):
    def test_missing_secret_fails_closed(self):
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(RuntimeError): module.verify_receipt('u','r')

    def test_receipt_contract(self):
        response = io.BytesIO(json.dumps({'productId':module.SKU,'productType':'ENTITLED','receiptId':'r','cancelDate':None}).encode())
        with patch.dict(os.environ, {'AMAZON_SHARED_SECRET':'test-only'}), patch.object(module,'urlopen',return_value=response):
            self.assertEqual(module.verify_receipt('u','r'), {'sku':module.SKU,'userId':'u','receiptId':'r','cancelled':False})

    def test_wrong_sku_rejected(self):
        response = io.BytesIO(b'{"productId":"other","productType":"ENTITLED","receiptId":"r"}')
        with patch.dict(os.environ, {'AMAZON_SHARED_SECRET':'test-only'}), patch.object(module,'urlopen',return_value=response):
            with self.assertRaises(ValueError): module.verify_receipt('u','r')

    def test_sandbox_choice_is_a_server_argument(self):
        response=io.BytesIO(json.dumps({'productId':module.SKU,'productType':'ENTITLED','receiptId':'r'}).encode())
        with patch.dict(os.environ, {'AMAZON_SHARED_SECRET':'test-only'}), patch.object(module,'urlopen',return_value=response) as call:
            module.verify_receipt('u','r','sandbox')
            self.assertIn('/sandbox/version/1.0/',call.call_args.args[0])
