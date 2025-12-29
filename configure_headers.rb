require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' }
unless target
  puts "Target 'iosApp' not found!"
  exit 1
end

puts "Configurando Header Search Paths..."

# Paths to add (relative to project root)
header_paths = [
  '$(SRCROOT)/iosApp/genesis_plus_gx',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/cart_hw',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/cd_hw',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/input_hw',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/sound',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/m68k',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/z80',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/cd_hw/libchdr/include',
  '$(SRCROOT)/iosApp/genesis_plus_gx/core/cd_hw/libchdr/deps/zlib-1.3.1',
  '$(SRCROOT)/iosApp/laines',
  '$(SRCROOT)/iosApp/laines/src',
  '$(SRCROOT)/iosApp/laines/src/include',
  '$(SRCROOT)/iosApp',
]

# Add header search paths to all configurations
target.build_configurations.each do |config|
  # Get existing paths or create new array
  existing_paths = config.build_settings['HEADER_SEARCH_PATHS'] || []
  existing_paths = [existing_paths] if existing_paths.is_a?(String)
  
  # Add our paths (avoiding duplicates)
  header_paths.each do |path|
    unless existing_paths.include?(path)
      existing_paths << path
      puts "  + #{path}"
    end
  end
  
  # Set back to config
  config.build_settings['HEADER_SEARCH_PATHS'] = existing_paths
  
  puts "\n✓ Configuração: #{config.name}"
end

project.save
puts "\n✅ Header Search Paths configurados!"
