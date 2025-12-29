require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' }
unless target
  puts "Target 'iosApp' not found!"
  exit 1
end

# Find or create Emulator group
emulator_group = project.main_group.groups.find { |g| g.name == 'Emulator' }
unless emulator_group
    emulator_group = project.main_group.new_group('Emulator')
    emulator_group.path = 'iosApp'  # Path to iosApp folder
end

base_dir = "iosApp/iosApp"

# Add laines files
laines_disk_path = "#{base_dir}/laines"
if Dir.exist?(laines_disk_path)
  # Create laines group WITHOUT setting path (this prevents duplication)
  laines_group = emulator_group.new_group('laines')
  # IMPORTANTE: NÃO definir laines_group.path!
  
  added_count = 0
  Dir.glob("#{laines_disk_path}/**/*.{c,cpp}") do |file_path|
    # Path relative to iosApp/iosApp
    relative_path = file_path.sub("#{base_dir}/", "")
    
    file_ref = laines_group.new_reference(relative_path)
    target.add_file_references([file_ref])
    added_count += 1
  end
  
  puts "✓ Adicionados #{added_count} arquivos .c/.cpp de laines"
end

project.save
puts "\n✅ Arquivos adicionados SEM path duplicado!"
