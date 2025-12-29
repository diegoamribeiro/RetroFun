require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

target = project.targets.find { |t| t.name == 'iosApp' }
unless target
  puts "Target 'iosApp' not found!"
  exit 1
end

# Get build phases
sources_phase = target.source_build_phase
resources_phase = target.resources_build_phase

puts "Removendo TODAS as referências a genesis_plus_gx e laines do projeto..."

# Remove from Sources Build Phase
removed_sources = 0
sources_phase.files.to_a.each do |build_file|
  file_ref = build_file.file_ref
  next unless file_ref
  
  path = file_ref.path || ""
  if path.include?("genesis_plus_gx") || path.include?("laines")
    sources_phase.remove_build_file(build_file)
    removed_sources += 1
  end
end
puts "✓ Removidas #{removed_sources} referências da fase Sources"

# Remove from Resources Build Phase
removed_resources = 0
resources_phase.files.to_a.each do |build_file|
  file_ref = build_file.file_ref
  next unless file_ref
  
  path = file_ref.path || ""
  if path.include?("genesis_plus_gx") || path.include?("laines")
    resources_phase.remove_build_file(build_file)
    removed_resources += 1
  end
end
puts "✓ Removidas #{removed_resources} referências da fase Resources"

# Remove from Emulator group (file references)
emulator_group = project.main_group.groups.find { |g| g.name == 'Emulator' }
if emulator_group
  # Remove genesis_plus_gx subgroup
  genesis_group = emulator_group.groups.find { |g| g.name == 'genesis_plus_gx' }
  if genesis_group
    genesis_group.clear
    emulator_group.groups.delete(genesis_group)
    puts "✓ Removido grupo genesis_plus_gx"
  end
  
  # Remove laines subgroup
  laines_group = emulator_group.groups.find { |g| g.name == 'laines' }
  if laines_group
    laines_group.clear
    emulator_group.groups.delete(laines_group)
    puts "✓ Removido grupo laines"
  end
end

project.save
puts "\n✅ Projeto limpo! Todas as referências aos emuladores foram removidas."
puts "Agora vou re-adicionar apenas os arquivos que realmente existem..."
