import os
import re

base_dir = r'c:\Users\Hoangmelinh\Downloads\clothing-shop-pos\clothing-shop-pos\src\main\java\com\sapo\mock\clothing'

for root, dirs, files in os.walk(base_dir):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original = content
            content = re.sub(r'import com\.sapo\.mock\.clothing\.domain\.response\..*;', 
                             'import com.sapo.mock.clothing.common.dto.response.*;', 
                             content)
            
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f'Updated {filepath}')
