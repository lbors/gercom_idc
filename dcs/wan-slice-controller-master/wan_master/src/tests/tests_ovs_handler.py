
import unittest
from ovs_handler import *

class OVSHandlerTests(unittest.TestCase):

    # def setUp(self):
    #     delete_bridge("test-bridge")

    # def tearDown(self):
    #     delete_bridge("test-bridge")
    
    def test_has_bridge(self):
        
        test_cases = [
            ("my-br", True),
            ("no-br", False)
        ]

        for value, expected in test_cases:
            with self.subTest(value=value):
                self.assertEqual(has_bridge(value), expected)

    def test_create_bridge(self):

        with self.subTest("Bridge do not exist and is created"):
            self.assertEqual(create_bridge("test-bridge"), True)
        
        with self.subTest("Bridge already exists and is not created"):
            self.assertEqual(create_bridge("test-bridge"), False)
        
        delete_bridge("test-bridge")

