# encoding: ascii-8bit

require 'stringio'
require 'tempfile'
require 'spec_helper'


module MessagePack
  describe Unpacker do
    let :unpacker do
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

    def flatten(struct, results = [])
      case struct
      when Array
        struct.each { |v| flatten(v, results) }
      when Hash
        struct.each { |k, v| flatten(v, flatten(k, results)) }
      else
        results << struct
      end
      results
    end

    describe '#execute/#execute_limit/#finished?' do
      let :buffer do
        buffer1 + buffer2 + buffer3
      end

      it 'extracts an object from the buffer' do
        unpacker.execute(buffer, 0)
        unpacker.data.should == {'foo' => 'bar'}
      end

      it 'extracts an object from the buffer, starting at an offset' do
        unpacker.execute(buffer, buffer1.length)
        unpacker.data.should == {'hello' => {'world' => [1, 2, 3]}}
      end

      it 'extracts an object from the buffer, starting at an offset reading bytes up to a limit' do
        unpacker.execute_limit(buffer, buffer1.length, buffer2.length)
        unpacker.data.should == {'hello' => {'world' => [1, 2, 3]}}
      end

      it 'extracts nothing if the limit cuts an object in half' do
        unpacker.execute_limit(buffer, buffer1.length, 3)
        unpacker.data.should be_nil
      end

      it 'returns the offset where the object ended' do
        unpacker.execute(buffer, 0).should == buffer1.length
        unpacker.execute(buffer, buffer1.length).should == buffer1.length + buffer2.length
      end

      it 'is finished when an object has been decoded' do
        unpacker.execute_limit(buffer, buffer1.length, buffer2.length)
        unpacker.should be_finished
      end

      it 'is not finished when no object could be decoded' do
        unpacker.execute_limit(buffer, buffer1.length, 3)
        unpacker.should_not be_finished
      end
    end

    describe '#each' do
      context 'with a buffer' do
        it 'yields each object in the buffer' do
          objects = []
          unpacker.feed(buffer1)
          unpacker.feed(buffer2)
          unpacker.feed(buffer3)
          unpacker.each do |obj|
            objects << obj
          end
          objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
        end

        it 'returns an enumerator when no block is given' do
          unpacker.feed(buffer1)
          unpacker.feed(buffer2)
          unpacker.feed(buffer3)
          enum = unpacker.each
          enum.map { |obj| obj.keys.first }.should == %w[foo hello x]
        end
      end

      context 'with a StringIO stream' do
        it 'yields each object in the stream' do
          objects = []
          unpacker.stream = StringIO.new(buffer1 + buffer2 + buffer3)
          unpacker.each do |obj|
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
          unpacker.stream = file
          unpacker.each do |obj|
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

      context 'with something that is not a stream' do
        it 'raises TypeError' do
          expect { unpacker.stream = 'hello world' }.to raise_error(TypeError)
        end
      end
    end

    describe '#feed_each' do
      it 'feeds the buffer then runs #each' do
        objects = []
        unpacker.feed_each(buffer1 + buffer2 + buffer3) do |obj|
          objects << obj
        end
        objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
      end

      it 'handles chunked data' do
        objects = []
        buffer = buffer1 + buffer2 + buffer3
        buffer.chars.each do |ch|
          unpacker.feed_each(ch) do |obj|
            objects << obj
          end
        end
        objects.should == [{'foo' => 'bar'}, {'hello' => {'world' => [1, 2, 3]}}, {'x' => 'y'}]
      end
    end

    describe '#fill' do
      it 'is a no-op' do
        unpacker.stream = StringIO.new(buffer1 + buffer2 + buffer3)
        unpacker.fill
        unpacker.each { |obj| }
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

    context 'regressions' do
      it 'handles massive arrays (issue #2)' do
        array = ['foo'] * 10_000
        MessagePack.unpack(MessagePack.pack(array)).should have(10_000).items
      end
    end

    context 'encodings', :encodings do
      let :buffer do
        MessagePack.pack([
          'hello'.force_encoding(::Encoding::BINARY),
          'world'.force_encoding(::Encoding::UTF_8),
          "sk\xC3\xA5l".force_encoding(::Encoding::UTF_8)
        ])
      end

      let :unpacker do
        described_class.new
      end

      it 'decodes binary strings as ASCII-8BIT' do
        unpacker.execute(buffer, 0)
        string = unpacker.data[0]
        string.encoding.should == ::Encoding::BINARY
      end

      it 'decodes UTF-8 strings as UTF-8' do
        unpacker.execute(buffer, 0)
        string = unpacker.data[1]
        string.encoding.should == ::Encoding::UTF_8
        string = unpacker.data[2]
        string.encoding.should == ::Encoding::UTF_8
      end
    end
  end
end