# encoding: utf-8

require 'java'

$CLASSPATH << File.expand_path('../ext', __FILE__) << File.expand_path('../ext/msgpack-0.5.2-devel.jar', __FILE__)

MessagePack = org.msgpack.jruby.MessagePack

require 'msgpack/unpacker'