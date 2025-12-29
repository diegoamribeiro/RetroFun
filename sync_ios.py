import os
import shutil

SOURCE_DIR = "composeApp/src/androidMain/cpp"
DEST_DIR = "iosApp/iosApp"

FILES_TO_COPY = [
    "ios_bridge.h",
    "ios_bridge.cpp",
    "emulator_context.hpp",
    "emulator_context.cpp",
    "emulator_core.hpp",
    "nes_core.hpp",
    "genesis_core.hpp",
    "osd.h"
]

DIRS_TO_COPY = [
    "laines",
    "genesis_plus_gx"
]

def sync_files():
    print(f"Syncing from {SOURCE_DIR} to {DEST_DIR}...")
    
    if not os.path.exists(DEST_DIR):
        print(f"Destination directory {DEST_DIR} does not exist!")
        return

    # Copy individual files
    for filename in FILES_TO_COPY:
        src = os.path.join(SOURCE_DIR, filename)
        dst = os.path.join(DEST_DIR, filename)
        if os.path.exists(src):
            print(f"Copying {filename}...")
            shutil.copy2(src, dst)
        else:
            print(f"WARNING: Source file {filename} not found!")

    # Copy directories
    for dirname in DIRS_TO_COPY:
        src = os.path.join(SOURCE_DIR, dirname)
        dst = os.path.join(DEST_DIR, dirname)
        
        if os.path.exists(src):
            print(f"Copying directory {dirname}...")
            if os.path.exists(dst):
                shutil.rmtree(dst)
            shutil.copytree(src, dst)
        else:
            print(f"WARNING: Source directory {dirname} not found!")
            
    # Copy shared.h for Genesis if it exists in root
    # (It seems GenesisCore.hpp includes shared.h inside extern "C")
    # Actually shared.h is usually inside genesis_plus_gx/core or root.
    # In Android CMake it's set as include dir. 
    # Let's check listing.
    
    print("Sync complete.")

if __name__ == "__main__":
    sync_files()
