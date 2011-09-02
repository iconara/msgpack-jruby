# encoding: utf-8

if RUBY_PLATFORM.include?('java')
  # JRuby should use this library, MRI should use the standard gem
  $: << File.expand_path('../../lib', __FILE__)
end

require 'viiite'
require 'msgpack'


OBJECT_STRUCTURE = {'x' => ['y', 34, 2**30 + 3, 2.1223423423356, {'hello' => 'world', 5 => [63, 'asjdl']}]}
ENCODED_DATA = "\x85\xA3foo\xA3bar\x03\xA5three\xA4four\x04\xA1x\x91\xA1y\xA1a\xA1b"
ITERATIONS = 1_000_000

Viiite.bm do |b|
  b.variation_point :ruby, Viiite.which_ruby
  
  b.report(:pack) do
    ITERATIONS.times do
      MessagePack.pack(OBJECT_STRUCTURE)
    end
  end
  
  b.report(:unpack) do
    ITERATIONS.times do
      MessagePack.unpack(ENCODED_DATA)
    end
  end
end