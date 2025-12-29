import sys
import re
import shutil
import os

project_path = "/Users/diegoribeiro/StudioProjects/RetroFun/iosApp/iosApp.xcodeproj/project.pbxproj"
backup_path = project_path + ".backup"

print(f"Backing up to {backup_path}")
shutil.copy(project_path, backup_path)

with open(project_path, 'r') as f:
    lines = f.readlines()

# Patterns to remove
# We want to remove lines that REFERENCE these files in PBXBuildFile and PBXFileReference sections.
# Files: *.txt, *.bor, *.emx, *.msc, *.md, *.T, *.com, *.vcxproj, *.in, *.def, *.rc, *.sln, Makefile, LICENSE, README

extensions = [r"\.txt", r"\.bor", r"\.emx", r"\.msc", r"\.md", r"\.T", r"\.com", r"\.vcxproj", r"\.in", r"\.def", r"\.rc", r"\.sln"]
filenames = ["Makefile", "LICENSE", "README"]

# Helper to check if line matches unwanted file
def is_unwanted(line):
    # Check simple filenames (Makefile, LICENSE, etc)
    # Often appear as: name = Makefile; path = .../Makefile;
    for fname in filenames:
        if f" {fname} " in line or f"/{fname}" in line or f"={fname}" in line:
            # Avoid matching legitimate source files if they somehow have these strings (unlikely for these specific names)
            return True
    
    # Check extensions
    for ext in extensions:
        if re.search(f"{ext}['\";]", line): # Check extension at end of quote or before semicolon
            return True
    return False

# We need to be careful. The PBX structure relies on object IDs.
# If we remove a PBXFileReference, we should also remove the PBXBuildFile that points to it.
# However, parsing the full graph is hard in a simple script.
# But usually, if we remove the lines from PBXBuildFile section (where they are added to targets) and PBXFileReference (where they are defined),
# Xcode might just handle the missing refs or we might leave dangling pointers.
# 
# Better strategy: Filter lines in specific sections.
# Sections: /* Begin PBXBuildFile section */ ... /* End PBXBuildFile section */
# Sections: /* Begin PBXFileReference section */ ... /* End PBXFileReference section */

new_lines = []
removed_count = 0
in_build_file_section = False
in_file_ref_section = False
in_resources_section = False # PBXResourcesBuildPhase

for line in lines:
    if "Begin PBXBuildFile section" in line:
        in_build_file_section = True
    elif "End PBXBuildFile section" in line:
        in_build_file_section = False
    elif "Begin PBXFileReference section" in line:
        in_file_ref_section = True
    elif "End PBXFileReference section" in line:
        in_file_ref_section = False
    
    # Check if we should delete
    if is_unwanted(line):
        removed_count += 1
        # Skip adding this line
        continue
        
    new_lines.append(line)

print(f"Removed {removed_count} lines referencing unwanted files.")

with open(project_path, 'w') as f:
    f.writelines(new_lines)

print("Done.")
