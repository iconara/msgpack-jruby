# -*- encoding: utf-8 -*-

$:.push File.expand_path('../lib', __FILE__)

require 'msgpack/version'

def mri?
  RUBY_ENGINE == 'ruby'
end

Gem::Specification.new do |s|
  s.name        = 'msgpack-jruby'
  s.version     = MessagePack::VERSION
  s.platform    = 'java'
  s.authors     = ['Theo Hultberg']
  s.email       = ['theo@iconara.net']
  s.homepage    = 'http://github.com/iconara/msgpack-jruby'
  s.summary     = %q{MessagePack implementation for JRuby}
  s.description = %q{JRuby compatible MessagePack implementation that does not use FFI}
  s.license     = 'Apache License 2.0'

  s.rubyforge_project = 'msgpack-jruby'

  s.files         = Dir['lib/**/*.rb', 'lib/**/*.jar']
  s.require_paths = %w(lib)

  s.add_development_dependency 'rake', ['~> 10.0.2']
  s.add_development_dependency 'rake-compiler', ['~> 0.8.3']
  s.add_development_dependency 'rspec', ['~> 2.6.0']

  s.add_development_dependency 'viiite', ['~> 0.1.0']
  s.add_development_dependency 'bson', ['~> 1.5.2']
  s.add_development_dependency 'bson_ext', ['~> 1.5.2'] if mri?
  s.add_development_dependency 'json', ['~> 1.5.0']
  s.add_development_dependency 'msgpack', ['~> 0.4.6'] if mri?

end
