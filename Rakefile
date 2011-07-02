require 'bundler'


Bundler::GemHelper.install_tasks

task :benchmark do
  system %(rvm 1.9.2-p0 spec/benchmark.rb && rvm jruby-1.6.2 spec/benchmark.rb | grep -v user)
end
