require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Find the target
target = project.targets.find { |t| t.name == 'iosApp' }
unless target
  puts "Target 'iosApp' not found!"
  exit 1
end

# Find or create the group (folder) for the files
# We assume they are in the root of the project folder (which is the default group)
# But strictly speaking they are in iosApp/iosApp subfolder on disk?
# Let's check structure. main group usually points to "iosApp" folder.
# Avoid touching the synced main group directly if it causes issues.
# Create a dedicated group for our emulator files.
# We will create it at the top level or under a known safe group.
emulator_group = project.main_group.groups.find { |g| g.name == 'Emulator' }
unless emulator_group
    emulator_group = project.main_group.new_group('Emulator', 'iosApp') # Path relative to project?
end

# Now use emulator_group instead of main_group for our files
target_group = emulator_group

# List of files to add
files_to_add = [
  'ios_bridge.cpp',
  'ios_bridge.h',
  'emulator_context.cpp',
  'emulator_context.hpp',
  'emulator_core.hpp',
  'nes_core.hpp',
  'genesis_core.hpp',
  'osd.h'
]

# Add files
files_to_add.each do |filename|
  # Check if exists in children
  existing = target_group.files.find { |f| f.path == filename || f.name == filename }
  
  if existing
      # Ensure it's in target
      if filename.end_with?('.cpp') || filename.end_with?('.c')
          target.add_file_references([existing])
      end
      next
  end

  # Add file reference
  file_ref = target_group.new_reference(filename)
  
  # If it's a source file (.cpp), add to build target
  if filename.end_with?('.cpp') || filename.end_with?('.c')
    target.add_file_references([file_ref])
    puts "Added #{filename} to target and group."
  else
    puts "Added #{filename} to group (header)."
  end
end

# Add Folders (Groups) - this is trickier as we need to add all files recursively or as a folder ref
# For LaiNES and GenesisPlusGX, simpler to add as folder references if possible, 
# or recursively add files. 
# Usually for C++ libs, adding the folder reference (blue folder) is NOT enough for compilation sources.
# We must add the FILES.

def add_recursive(group, path, target)
    Dir.glob("#{path}/**/*").each do |file|
        next if File.directory?(file)
        relative_path = file.sub("#{path}/", "")
        
        # Determine strict structure? For now, flatten or keep structure?
        # Simplest: Just add file ref to the parent group relative to the project
        # But for 'laines' folder, we want a 'laines' group.
        
        # Let's simplify: Just look for .cpp/.c files in these dirs and add them
        if file.end_with?('.cpp') || file.end_with?('.c')
             # For compilation, we just need the build file entry. 
             # The group structure is visual.
             # Let's just add them to the main group for now to ensure compilation? 
             # No, that's messy.
             
             # Let's create a subgroup matching the folder name
             # e.g. iosApp/laines -> group 'laines'
             
             # Actually, we can just use new_file(path) on the group and it resolves.
        end
    end
end

# Handling subdirectories simpler:
# Just find all .cpp/.c files in iosApp/iosApp/laines and iosApp/iosApp/genesis_plus_gx
# and add them to the target. Visual grouping is secondary but nice.

base_dir = "iosApp/iosApp"
["laines", "genesis_plus_gx"].each do |dir|
    # Create group
    group = target_group[dir] || target_group.new_group(dir, dir)
    
    # Walk directory
    Dir.glob("#{base_dir}/#{dir}/**/*.{c,cpp,h,hpp}") do |file_path|
        # file_path is like iosApp/iosApp/laines/src/cpu.cpp
        # relative to group (which is at iosApp/iosApp/laines) it is src/cpu.cpp?
        # Xcodeproj is tricky with paths.
        
        # Let's try adding absoluteish path relative to project root
        # Project root is iosApp/
        # File is iosApp/laines/...
        
        real_path = file_path.sub("iosApp/iosApp/", "") # e.g. laines/src/cpu.cpp
        
        # Don't add if already exists
        next if group.find_file_by_path(real_path)

        # Add file
        file_ref = group.new_reference(real_path)
        
        if file_path.end_with?('.c') || file_path.end_with?('.cpp')
            target.add_file_references([file_ref])
            puts "Added source: #{real_path}"
        end
    end
end

project.save
puts "Project saved successfully."
