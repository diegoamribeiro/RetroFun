require 'xcodeproj'

project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

project.targets.each do |target|
  puts "Target: #{target.name}"
  target.build_phases.each do |phase|
    puts "  Phase: #{phase.display_name} (#{phase.class})"
    phase.files.each do |build_file|
      file_ref = build_file.file_ref
      if file_ref
        puts "    File: #{file_ref.display_name} (Path: #{file_ref.path}, Class: #{file_ref.class})"
      else
        puts "    File: (nil ref)"
      end
    end
  end
end
