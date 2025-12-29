import os
import shutil

ANDROID_CPP_DIR = "composeApp/src/androidMain/cpp"
IOS_APP_DIR = "iosApp/iosApp"

GENESIS_SRC = os.path.join(ANDROID_CPP_DIR, "genesis_plus_gx")
GENESIS_DST = os.path.join(IOS_APP_DIR, "genesis_plus_gx")

LAINES_SRC = os.path.join(ANDROID_CPP_DIR, "laines")
LAINES_DST = os.path.join(IOS_APP_DIR, "laines")

def clean_dir(path):
    if os.path.exists(path):
        print(f"Removing {path}...")
        shutil.rmtree(path)

def copy_genesis_core():
    print("Copying Genesis Plus GX Core...")
    os.makedirs(GENESIS_DST, exist_ok=True)
    
    # Copy 'core' directory
    src_core = os.path.join(GENESIS_SRC, "core")
    dst_core = os.path.join(GENESIS_DST, "core")
    if os.path.exists(src_core):
        shutil.copytree(src_core, dst_core)
        print("Copied core/")
    
    # Copy shared.h if in root (verify location first)
    # Actually shared.h is usually in 'core' or root?
    # Based on grep earlier: genesis_plus_gx/sdl/gx_vstudio/deps/zlib... implies deep structure
    # and grep for shared.h showing 'include "shared.h"' 
    
    # Also need to cleanup 'core' itself if it has junk?
    # Core usually purely source.
    
    # REMOVE platform-specifics from core if any (usually none)
    # But wait, earlier log showed `genesis_plus_gx/core/cd_hw/libchdr...`
    # We need that.
    
    # Do NOT copy 'sdl', 'psp2', 'gcw0', 'gx', 'libretro' folders from root
    # We only copied 'core'. so we are safe!
    # Wait, does 'core' depend on headers in root?
    # Check Android CPP folder listing for genesis_plus_gx root
    
    root_files = [f for f in os.listdir(GENESIS_SRC) if os.path.isfile(os.path.join(GENESIS_SRC, f))]
    for f in root_files:
        if f.endswith(".h"):
            shutil.copy2(os.path.join(GENESIS_SRC, f), os.path.join(GENESIS_DST, f))
            print(f"Copied root header: {f}")

def copy_laines():
    print("Copying LaiNES...")
    os.makedirs(LAINES_DST, exist_ok=True)
    
    # LaiNES usually has src/
    src_src = os.path.join(LAINES_SRC, "src")
    dst_src = os.path.join(LAINES_DST, "src")
    
    if os.path.exists(src_src):
        shutil.copytree(src_src, dst_src)
        print("Copied src/")
        
    # Exclude SConstruct, etc.

def main():
    # 1. Clean existing
    clean_dir(GENESIS_DST)
    clean_dir(LAINES_DST)
    
    # 2. Copy filtered
    if os.path.exists(GENESIS_SRC):
        copy_genesis_core()
    else:
        print("Genesis source missing!")

    if os.path.exists(LAINES_SRC):
        copy_laines()
    else:
        print("LaiNES source missing!")
        
    # 3. Also Re-copy the top-level C++ files to be sure
    top_files = ["ios_bridge.cpp", "ios_bridge.h", "emulator_context.cpp", "emulator_context.hpp", "emulator_core.hpp", "nes_core.hpp", "genesis_core.hpp", "osd.h"]
    for f in top_files:
        src = os.path.join(ANDROID_CPP_DIR, f)
        dst = os.path.join(IOS_APP_DIR, f)
        if os.path.exists(src):
            shutil.copy2(src, dst)
            print(f"Refreshed {f}")

    print("Cleanup complete. Junk files removed from disk.")

if __name__ == "__main__":
    main()
