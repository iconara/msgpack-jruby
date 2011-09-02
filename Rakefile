require 'bundler'


Bundler::GemHelper.install_tasks

task :compile do
  exec %(javac -cp lib/ext/msgpack-0.5.2-devel.jar:$MY_RUBY_HOME/lib/jruby.jar -d lib/ext ext/java/org/msgpack/**/*.java)
end

BENCHMARK_RUBIES = ['1.9.2-p290', 'jruby-1.6.2', 'jruby-1.6.4']
BENCHMARK_GEMSET = 'msgpack-jruby-benchmarking'

task :benchmark do
  rubies = BENCHMARK_RUBIES.map { |rb| "#{rb}@#{BENCHMARK_GEMSET}" }
  cmd = %(rvm #{rubies.join(',')} exec viiite run spec/benchmark.rb | tee benchmark | viiite report --hierarchy --regroup=bench,ruby)
  puts cmd
  system cmd
end
