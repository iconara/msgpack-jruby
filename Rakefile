require 'bundler'


Bundler::GemHelper.install_tasks

task :compile do
  exec %(javac -cp lib/ext/msgpack-0.5.2-devel.jar:$MY_RUBY_HOME/lib/jruby.jar -d lib/ext ext/java/org/msgpack/**/*.java)
end

task :benchmark do
  system %(rvm 1.9.2-p0 spec/benchmark.rb && rvm jruby-1.6.2 spec/benchmark.rb | grep -v user)
end
