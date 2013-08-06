# -*- encoding: utf-8 -*-

$:.push File.expand_path('../lib', __FILE__)

require 'msgpack/version'


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
end
