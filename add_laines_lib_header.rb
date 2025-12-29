require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' }
unless target
  puts "Target 'iosApp' not found!"
  exit 1
end

puts "Adicionando laines/lib/include aos Header Search Paths..."

# Novo caminho a adicionar
new_path = '$(SRCROOT)/iosApp/laines/lib/include'

# Adicionar a todas as configurações
target.build_configurations.each do |config|
  existing_paths = config.build_settings['HEADER_SEARCH_PATHS'] || []
  existing_paths = [existing_paths] if existing_paths.is_a?(String)
  
  unless existing_paths.include?(new_path)
    existing_paths << new_path
    puts "  + Adicionado: #{new_path}"
  end
  
  config.build_settings['HEADER_SEARCH_PATHS'] = existing_paths
  puts "✓ Configuração: #{config.name}"
end

project.save
puts "\n✅ Header path adicionado!"
