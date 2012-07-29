$: << 'lib'

require 'bundler/setup'
require 'msgpack/version'


task :clean do
  rm Dir['ext/java/**/*.class']
end

task :compile do
  classpath = (Dir["lib/ext/*.jar"] + ["#{ENV['MY_RUBY_HOME']}/lib/jruby.jar"]).join(':')
  system %(javac -Xlint:-options -source 1.6 -target 1.6 -cp #{classpath} ext/java/*.java ext/java/org/msgpack/jruby/*.java)
  exit($?.exitstatus) unless $?.success?
end

task :package => :compile do
  class_files = Dir['ext/java/**/*.class'].map { |path| path = path.sub('ext/java/', ''); "-C ext/java '#{path}'" }
  system %(jar cf lib/ext/msgpack_jruby.jar #{class_files.join(' ')})
  exit($?.exitstatus) unless $?.success?
end

task :release do
  version_string = "v#{MessagePack::VERSION}"
  unless %x(git tag -l).include?(version_string)
    system %(git tag -a #{version_string} -m #{version_string})
  end
  system %(gem build msgpack-jruby.gemspec && gem push msgpack-jruby-*.gem && mv msgpack-jruby-*.gem pkg)
end

namespace :benchmark do
  BENCHMARK_RUBIES = ['1.9.2-p0', 'jruby-1.6.5', 'jruby-head']
  BENCHMARK_GEMSET = 'msgpack-jruby-benchmarking'
  BENCHMARK_FILE = 'spec/benchmarks/shootout_bm.rb'

  task :run do
    rubies = BENCHMARK_RUBIES.map { |rb| "#{rb}@#{BENCHMARK_GEMSET}" }
    cmd = %(rvm #{rubies.join(',')} exec viiite run #{BENCHMARK_FILE} | tee benchmark | viiite report --hierarchy --regroup=bench,lib,ruby)
    puts cmd
    system cmd
  end

  task :quick do
    cmd = %(IMPLEMENTATIONS=msgpack viiite run #{BENCHMARK_FILE} | viiite report --hierarchy --regroup=bench)
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