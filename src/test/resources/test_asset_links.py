"""Check the asset dependency graph after moves, without a running server."""
from pathlib import Path
import re
import unittest

RESOURCES = Path(__file__).resolve().parents[2] / 'main/resources'
STATIC = RESOURCES / 'static'

class AssetLinksTest(unittest.TestCase):
    def test_template_assets_exist(self):
        for page in (RESOURCES / 'templates').rglob('*.html'):
            for path in re.findall(r'@\{(/(?:js|css|img)/[^}]+)\}', page.read_text()):
                with self.subTest(page=page.name, asset=path):
                    self.assertTrue((STATIC / path.lstrip('/')).is_file())

    def test_stylesheet_imports_exist(self):
        for sheet in STATIC.rglob('*.css'):
            for path in re.findall(r'@import\s+url\([\'"]([^\'"]+)[\'"]\)', sheet.read_text()):
                if '://' not in path:
                    with self.subTest(sheet=sheet.name, dependency=path):
                        self.assertTrue((sheet.parent / path).is_file())

    def test_javascript_module_imports_exist(self):
        for script in STATIC.rglob('*.js'):
            imports = re.findall(r'import\s+(?:[^;]*?from\s+)?[\'"]([^\'"]+)[\'"]', script.read_text())
            for path in imports:
                with self.subTest(script=script.name, dependency=path):
                    self.assertTrue((script.parent / path).is_file())

if __name__ == '__main__':
    unittest.main()
