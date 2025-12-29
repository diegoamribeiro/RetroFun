import os
import re

# Headers to look for (from lib/include)
lib_headers = [
    "Nes_Apu.h", "Nes_Vrc6.h", "Blip_Buffer.h", "Nes_Oscs.h", 
    "Nes_Namco.h", "Nonlinear_Buffer.h", "blargg_common.h", 
    "blargg_source.h", "Multi_Buffer.h", "Blip_Synth.h", "apu_snapshot.h"
]

root_dir = "iosApp/iosApp/laines/src"
lib_include_path = "iosApp/iosApp/laines/lib/include"

print(f"Scanning {root_dir}...")

for root, dirs, files in os.walk(root_dir):
    for file in files:
        if file.endswith(".cpp") or file.endswith(".hpp"):
            filepath = os.path.join(root, file)
            
            # Calculate relative path to lib/include
            # This is tricky because "os.path.relpath" gives path FROM start TO end.
            # We want headers to use path FROM current file TO lib/include.
            # e.g. from src/include/mappers/mapper24.hpp to lib/include/Nes_Vrc6.h
            
            # Depth from project root
            # root_dir is iosApp/iosApp/laines/src
            # lib is iosApp/iosApp/laines/lib/include
            
            # Get path from current dir to 'laines' dir
            # Then add 'lib/include'
            
            # rel to 'laines' dir:
            # iosApp/iosApp/laines/src/include/mappers -> ../../../ (to laines)
            
            # Current dir absolute
            abs_current = os.path.abspath(root)
            abs_laines = os.path.abspath("iosApp/iosApp/laines")
            
            rel_to_laines = os.path.relpath(abs_laines, abs_current)
            rel_to_lib = os.path.join(rel_to_laines, "lib/include")
            
            with open(filepath, 'r') as f:
                content = f.read()
            
            new_content = content
            for header in lib_headers:
                # Regex for <Header.h>
                pattern = f'#include <{header}>'
                replacement = f'#include "{rel_to_lib}/{header}"'
                
                if pattern in new_content:
                    print(f"Fixing {filepath}: {pattern} -> {replacement}")
                    new_content = new_content.replace(pattern, replacement)
            
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)

print("Done.")
