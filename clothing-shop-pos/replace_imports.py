import os

base_dir = r'c:\Users\Hoangmelinh\Downloads\clothing-shop-pos\clothing-shop-pos\src\main\java\com\sapo\mock\clothing'

replacements = {
    'com.sapo.mock.clothing.controller': 'com.sapo.mock.clothing.auth.controller',
    'com.sapo.mock.clothing.domain.request': 'com.sapo.mock.clothing.auth.dto.request',
    'com.sapo.mock.clothing.domain.response.ResLoginDTO': 'com.sapo.mock.clothing.auth.dto.response.ResLoginDTO',
    'com.sapo.mock.clothing.domain.response.RestResponse': 'com.sapo.mock.clothing.common.dto.response.RestResponse',
    'com.sapo.mock.clothing.domain.response.ResultPaginationDTO': 'com.sapo.mock.clothing.common.dto.response.ResultPaginationDTO',
    'com.sapo.mock.clothing.domain.entity': 'com.sapo.mock.clothing.user.entity',
    'com.sapo.mock.clothing.repository': 'com.sapo.mock.clothing.user.repository',
    'com.sapo.mock.clothing.service': 'com.sapo.mock.clothing.user.service',
    'com.sapo.mock.clothing.util.error': 'com.sapo.mock.clothing.exception',
    'import com.sapo.mock.clothing.domain.response.*;': 'import com.sapo.mock.clothing.common.dto.response.*;',
}

for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original = content
            for old, new in replacements.items():
                content = content.replace(old, new)
            
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f'Updated {filepath}')
