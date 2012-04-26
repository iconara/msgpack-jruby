# encoding: ascii-8bit

require_relative '../spec_helper'
require 'stringio'
require 'tempfile'


describe ::MessagePack::Unpacker do
  subject do
    described_class.new
  end
  
  let :buffer1 do
    MessagePack.pack(:foo => 'bar')
  end
    
  let :buffer2 do
    MessagePack.pack(:hello => {:world => [1, 2, 3]})
  end
    
  let :buffer3 do
    MessagePack.pack(:x => 'y')
  end
  
  describe '#execute/#execute_limit/#finished?' do
    let :buffer do
      buffer1 + buffer2 + buffer3
    end
    
    it 'extracts an object from the buffer' do
      subject.execute(buffer, 0)
      subject.data.should == {'foo' => 'bar'}
    end

    it 'extracts an object from the buffer, starting at an offset' do
      subject.execute(buffer, buffer1.length)
      subject.data.should == {'hello' => {'world' => [1, 2, 3]}}
    end

    it 'extracts an object from the buffer, starting at an offset reading bytes up to a limit' do
      subject.execute_limit(buffer, buffer1.length, buffer2.length)
      subject.data.should == {'hello' => {'world' => [1, 2, 3]}}
    end
    
    it 'extracts nothing if the limit cuts an object in half' do
      subject.execute_limit(buffer, buffer1.length, 3)
      subject.data.should be_nil
    end

    it 'returns the offset where the object ended' do
      subject.execute(buffer, 0).should == buffer1.length
      subject.execute(buffer, buffer1.length).should == buffer1.length + buffer2.length
    end
    
    it 'is finished if #data returns an object' do
      subject.execute_limit(buffer, buffer1.length, buffer2.length)
      subject.should be_finished
      
      subject.execute_limit(buffer, buffer1.length, 3)
      subject.should_not be_finished
    end
  end
  
  describe '#each' do
    context 'with a buffer' do
      it 'yields each object in the buffer' do
        objects = []
        subject.feed(buffer1)
        subject.feed(buffer2)
        subject.feed(buffer3)
        subject.each do |obj|
          objects << obj
        end
        objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
      end
      
      it 'returns an enumerator when no block is given' do
        subject.feed(buffer1)
        subject.feed(buffer2)
        subject.feed(buffer3)
        enum = subject.each
        enum.map { |obj| obj.keys.first }.should == %w[foo hello x]
      end
    end
    
    context 'with a StringIO stream' do
      it 'yields each object in the stream' do
        objects = []
        subject.stream = StringIO.new(buffer1 + buffer2 + buffer3)
        subject.each do |obj|
          objects << obj
        end
        objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
      end
    end
    
    context 'with a File stream' do
      it 'yields each object in the stream' do
        objects = []
        file = Tempfile.new('msgpack')
        file.write(buffer1)
        file.write(buffer2)
        file.write(buffer3)
        file.open
        subject.stream = file
        subject.each do |obj|
          objects << obj
        end
        objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
      end
    end
    
    context 'with a stream passed to the constructor' do
      it 'yields each object in the stream' do
        objects = []
        unpacker = described_class.new(StringIO.new(buffer1 + buffer2 + buffer3))
        unpacker.each do |obj|
          objects << obj
        end
        objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
      end
    end
  end
  
  describe '#feed_each' do
    it 'feeds the buffer then runs #each' do
      objects = []
      subject.feed_each(buffer1 + buffer2 + buffer3) do |obj|
        objects << obj
      end
      objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
    end
  end
  
  describe '#fill' do
    it 'is a no-op' do
      subject.stream = StringIO.new(buffer1 + buffer2 + buffer3)
      subject.fill
      subject.each { |obj| }
    end
  end
  
  describe '#reset' do
    context 'with a buffer' do
      it 'is unclear what it is supposed to do'
    end
    
    context 'with a stream' do
      it 'is unclear what it is supposed to do'
    end
  end
end
