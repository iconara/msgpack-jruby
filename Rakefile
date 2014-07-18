$: << 'lib'

require 'bundler/setup'
require 'rspec/core/rake_task'
Bundler::GemHelper.install_tasks


task :default => :spec

RSpec::Core::RakeTask.new(:spec) do |r|
  r.rspec_opts = '--tty'
end

task :spec => :compile

spec = eval File.read("msgpack-jruby.gemspec")

require 'rake/javaextensiontask'
Rake::JavaExtensionTask.new('msgpack_jruby', spec) do |ext|
  ext.source_version = ext.target_version = 1.7
  ext.ext_dir = 'ext/java'
  ext.lib_dir = 'lib/ext'
  ext.classpath = Dir['lib/ext/*.jar'].map { |x| File.expand_path x }.join File::PATH_SEPARATOR
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
