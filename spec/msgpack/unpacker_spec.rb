# encoding: ascii-8bit

require_relative '../spec_helper'

require 'stringio'


describe MessagePack::Unpacker do
  context 'with a buffer' do
    it 'unpacks objects fed through #feed' do
      pending
      subject.feed(MessagePack.pack('hello' => 'world') + MessagePack.pack('foo' => 42))
      objects = []
      subject.each do |obj|
        objects << obj
      end
      objects.should == [{'hello' => 'world'}, {'foo' => 42}]
    end
  end
  
  context 'with a stream' do
    it 'unpacks objects from the stream' do
      pending
      stream = StringIO.new(MessagePack.pack('hello' => 'world') + MessagePack.pack('foo' => 42))
      objects = []
      described_class.new(stream).each do |obj|
        objects << obj
      end
      objects.should == [{'hello' => 'world'}, {'foo' => 42}]
    end
  end
end