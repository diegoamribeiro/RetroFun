import os

# Define replacements
mappers_replacements = {
    '#include "mappers/': '#include "../include/mappers/',
    '#include "ppu.hpp"': '#include "../include/ppu.hpp"',
    '#include "apu.hpp"': '#include "../include/apu.hpp"',
    '#include "cpu.hpp"': '#include "../include/cpu.hpp"',
    '#include "common.hpp"': '#include "../include/common.hpp"',
    '#include "mapper.hpp"': '#include "../include/mapper.hpp"',
    '#include "cartridge.hpp"': '#include "../include/cartridge.hpp"',
}

mappers_header_replacements = {
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
            with open(path, 'r') as f:
                content = f.read()
            
            new_content = content
            for search, replace in mappers_replacements.items():
                new_content = new_content.replace(search, replace)
            
            if new_content != content:
                print(f"Modifying {path}")
                with open(path, 'w') as f:
                    f.write(new_content)

# Fix include/mappers/*.hpp
include_mappers_dir = "iosApp/iosApp/laines/src/include/mappers"
for root, dirs, files in os.walk(include_mappers_dir):
    for file in files:
        if file.endswith(".hpp"):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            new_content = content
            for search, replace in mappers_header_replacements.items():
                new_content = new_content.replace(search, replace)
            
            if new_content != content:
                print(f"Modifying {path}")
                with open(path, 'w') as f:
                    f.write(new_content)

print("Done.")
