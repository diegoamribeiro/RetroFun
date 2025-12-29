require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' }
unless target
  puts "Target 'iosApp' not found!"
  exit 1
end

resources_phase = target.resources_build_phase
sources_phase = target.source_build_phase

puts "Scanning Resources build phase..."
files_to_remove = []

resources_phase.files.each do |build_file|
    file_ref = build_file.file_ref
    unless file_ref
        puts "Warning: Build file without file_ref"
        next
    end
    
    path = file_ref.path || ""
    name = file_ref.name || ""
    display_name = file_ref.display_name || ""
    
    # Enable this line to see what's in there
    puts "Resource: Name='#{name}', Path='#{path}', Display='#{display_name}'"
    
    # Check if it's from genesis_plus_gx or laines
    
    # Check if it's from genesis_plus_gx or laines
    # We can check the full path hierarchy or just the name/path substring
    # Xcodeproj paths can be relative.
    
    is_emulator_file = path.include?("genesis_plus_gx") || path.include?("laines")
    
    # We generally want to remove ALL emulator files from RESOURCES.
    # They should only be in SOURCES (if .cpp/.c).
    # ROMs/Images should be kept, but they are unlikely to be deeply nested in genesis_plus_gx folder unless it's a test ROM.
    
    # Specific bad extensions/names from the error log
    if is_emulator_file
        puts "Marking for removal from resources: #{name} (#{path})"
        files_to_remove << build_file
    elsif name == "CMakeLists.txt" || name == "Makefile" || name.include?(".vcxproj") || name == "LICENSE" || name == ".put_lib_files_here"
        puts "Marking junk file for removal: #{name}"
        files_to_remove << build_file
    end
end

if files_to_remove.empty?
    puts "No files found to remove from Resources."
else
    files_to_remove.each do |f|
        resources_phase.remove_build_file(f)
    end
    puts "Removed #{files_to_remove.size} files from Resources phase."
end

# Also check for Duplicate Sources if any
# (Optional, but good practice)
seen_sources = {}
dupes_removed = 0
sources_phase.files.each do |build_file|
    file_ref = build_file.file_ref
    next unless file_ref
    
    key = file_ref.path
    if seen_sources[key]
        puts "Removing duplicate source: #{key}"
        sources_phase.remove_build_file(build_file)
        dupes_removed += 1
    else
        seen_sources[key] = true
    end
end
if dupes_removed > 0
    puts "Removed #{dupes_removed} duplicate sources."
end

project.save
puts "Project saved."
