# encoding: utf-8

require 'java'

$CLASSPATH << File.expand_path('../ext', __FILE__)
Dir[File.expand_path('../ext/*.jar', __FILE__)].each { |path| $CLASSPATH << path }

MessagePack = org.msgpack.jruby.MessagePackModule
