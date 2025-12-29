import os

# Replacements
cpp_replacements = {
    '#include "mappers/': '#include "../include/mappers/',
    '#include "ppu.hpp"': '#include "../include/ppu.hpp"',
    '#include "apu.hpp"': '#include "../include/apu.hpp"',
    '#include "cpu.hpp"': '#include "../include/cpu.hpp"',
    '#include "common.hpp"': '#include "../include/common.hpp"',
    '#include "mapper.hpp"': '#include "../include/mapper.hpp"',
    '#include "cartridge.hpp"': '#include "../include/cartridge.hpp"',
    '#include "joypad.hpp"': '#include "../include/joypad.hpp"',
    '#include "gui.hpp"': '#include "../include/gui.hpp"',
}

mapper_header_replacements = {
    '#include "mapper.hpp"': '#include "../mapper.hpp"',
    '#include "ppu.hpp"': '#include "../ppu.hpp"',
    '#include "cartridge.hpp"': '#include "../cartridge.hpp"',
    '#include "common.hpp"': '#include "../common.hpp"',
}

# Fix mappers/*.cpp
mappers_dir = "iosApp/iosApp/laines/src/mappers"
for root, dirs, files in os.walk(mappers_dir):
    for file in files:
        if file.endswith(".cpp"):
            path = os.path.join(root, file)
            with open(path, 'r') as f: content = f.read()
            
            new_content = content
            for search, replace in cpp_replacements.items():
                if search in new_content:
                    new_content = new_content.replace(search, replace)
            
            # Special regex-like for other mappers
            import re
            new_content = re.sub(r'#include "mappers/mapper(\d+)\.hpp"', r'#include "../include/mappers/mapper\1.hpp"', new_content)

            if new_content != content:
                print(f"Fixed {path}")
                with open(path, 'w') as f: f.write(new_content)

# Fix src/*.cpp
src_dir = "iosApp/iosApp/laines/src"
for file in os.listdir(src_dir):
    if file.endswith(".cpp"):
        path = os.path.join(src_dir, file)
        with open(path, 'r') as f: content = f.read()
        
        new_content = content
        # Fix includes local to src/ which should point to include/
        replacements = {
            '#include "ppu.hpp"': '#include "include/ppu.hpp"',
            '#include "apu.hpp"': '#include "include/apu.hpp"',
            '#include "cpu.hpp"': '#include "include/cpu.hpp"',
            '#include "gui.hpp"': '#include "include/gui.hpp"',
            '#include "common.hpp"': '#include "include/common.hpp"',
            '#include "cartridge.hpp"': '#include "include/cartridge.hpp"',
            '#include "joypad.hpp"': '#include "include/joypad.hpp"',
            '#include "config.hpp"': '#include "include/config.hpp"',
            '#include "mapper.hpp"': '#include "include/mapper.hpp"',
            '#include "mappers/': '#include "include/mappers/',
        }
        for search, replace in replacements.items():
             if search in new_content:
                new_content = new_content.replace(search, replace)

        if new_content != content:
            print(f"Fixed {path}")
            with open(path, 'w') as f: f.write(new_content)

# Fix include/mappers/*.hpp
inc_mappers_dir = "iosApp/iosApp/laines/src/include/mappers"
for root, dirs, files in os.walk(inc_mappers_dir):
    for file in files:
         if file.endswith(".hpp"):
            path = os.path.join(root, file)
            with open(path, 'r') as f: content = f.read()
            
            new_content = content
            for search, replace in mapper_header_replacements.items():
                 if search in new_content:
                    new_content = new_content.replace(search, replace)

            if new_content != content:
                print(f"Fixed {path}")
                with open(path, 'w') as f: f.write(new_content)
