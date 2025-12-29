require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' }
sources_phase = target.source_build_phase

puts "Removendo duplicatas..."

# Track seen files
seen = {}
to_remove = []

sources_phase.files.each do |build_file|
  file_ref = build_file.file_ref
  next unless file_ref
  
  path = file_ref.path || file_ref.name || ""
  
  # Remove palette.inc (não é código compilável)
  if path.include?("palette.inc")
    to_remove << build_file
    puts "  - Removendo palette.inc"
    next
  end
  
  # Check for duplicates
  if seen[path]
    to_remove << build_file
    puts "  - Removendo duplicata: #{path}"
  else
    seen[path] = true
  end
end

to_remove.each do |f|
  sources_phase.remove_build_file(f)
end

project.save
puts "\n✅ Removidas #{to_remove.size} duplicatas!"
