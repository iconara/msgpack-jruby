if RUBY_PLATFORM == 'java'
  $: << File.expand_path('../../lib', __FILE__)
end

require 'benchmark'
require 'msgpack'

n = 10000

Benchmark.bm(30) do |x|
  x.report(RUBY_PLATFORM == 'java' ? 'JRuby' : 'Ruby') do
    n.times do
      MessagePack.pack({'x' => ['y', 34, 2**30 + 3, 2.1223423423356, {'hello' => 'world', 5 => [63, 'asjdl']}]})
    end
  end
end