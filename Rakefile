require 'bundler'


Bundler::GemHelper.install_tasks

task :clean do
  rm Dir['lib/ext/**/*.class']
end

task :compile do
  system %(javac -source 1.6 -target 1.6 -cp lib/ext/msgpack-0.6.5-SNAPSHOT.jar:$MY_RUBY_HOME/lib/jruby.jar -d lib/ext ext/java/org/msgpack/**/*.java)
end

namespace :benchmark do
  BENCHMARK_RUBIES = ['1.9.2-p0', 'jruby-1.6.5', 'jruby-head']
  BENCHMARK_GEMSET = 'msgpack-jruby-benchmarking'

  task :run do
    rubies = BENCHMARK_RUBIES.map { |rb| "#{rb}@#{BENCHMARK_GEMSET}" }
    cmd = %(rvm #{rubies.join(',')} exec viiite run spec/benchmark.rb | tee benchmark | viiite report --hierarchy --regroup=bench,lib,ruby)
    puts cmd
    system cmd
  end

  task :quick do
    cmd = %(IMPLEMENTATIONS=msgpack viiite run spec/benchmark.rb | viiite report --hierarchy --regroup=bench)
    puts cmd
    system cmd
  end

  task :setup do
    rubies = BENCHMARK_RUBIES.map { |rb| "#{rb}@#{BENCHMARK_GEMSET}" }
    rubies.each do |ruby_version|
      cmd = %(rvm-shell #{ruby_version} -c 'bundle check || bundle install')
      puts cmd
      system cmd
    end
  end
end