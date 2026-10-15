import unittest
from localize import validate
class TranslationTests(unittest.TestCase):
    def test_reordered_placeholders_are_valid(self):
        self.assertEqual([],validate({'x':'Milo has ${count} matches for $name'},{'x':'$name: Milo, ${count}'}))
    def test_dropped_tokens_and_brand_are_rejected(self):
        result=validate({'x':'Milo has ${count} matches'},{'x':'matches'})
        self.assertIn('placeholders: x',result);self.assertIn('product-name: x',result)
    def test_missing_and_empty_strings(self):
        self.assertEqual(['missing: x'],validate({'x':'Hello'},{}))
        self.assertEqual(['empty: x'],validate({'x':'Hello'},{'x':''}))
if __name__=='__main__': unittest.main()
