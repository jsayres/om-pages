require 'autoprefixer-rails'

environment = :production
css_dir = "../public/css"
sass_dir = "scss"
http_stylesheets_path = "/css"

fonts_dir = "../public/fonts"
http_fonts_path = "/fonts"


add_import_path "bower_components/bootstrap-sass-official/assets/stylesheets"
add_import_path "bower_components/font-awesome/scss"

on_stylesheet_saved do |file|
  css = File.read(file)
  File.open(file, 'w') do |io|
    io << AutoprefixerRails.process(css)
  end
end
