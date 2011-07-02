# encoding: ascii-8bit

require_relative 'spec_helper'


describe MessagePack do
  describe '#pack' do
    context 'with constant values' do
      it 'encodes true' do
        MessagePack.pack(true).should == "\xC3"
      end

      it 'encodes false' do
        MessagePack.pack(false).should == "\xC2"
      end

      it 'encodes nil' do
        MessagePack.pack(nil).should == "\xC0"
      end
    end
    
    context 'with numbers' do
      it 'encodes small integers' do
        MessagePack.pack(42).should == "*"
      end

      it 'encodes medium integers' do
        MessagePack.pack(333).should == "\xCD\x01M"
      end

      it 'encodes large integers' do
        MessagePack.pack(2**31 - 1).should == "\xCE\x7F\xFF\xFF\xFF"
      end

      it 'encodes huge integers' do
        MessagePack.pack(2**64 - 1).should == "\xCF\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF"
      end
    
      it 'encodes negative integers' do
        MessagePack.pack(-1).should == "\xFF"
      end

      it 'encodes small floats' do
        MessagePack.pack(3.14).should == "\xCB@\t\x1E\xB8Q\xEB\x85\x1F"
      end

      it 'encodes big floats' do
        MessagePack.pack(Math::PI * 1_000_000_000_000_000_000).should == "\xCBC\xC5\xCC\x96\xEF\xD1\x19%"
      end

      it 'encodes negative floats' do
        MessagePack.pack(-2.1).should == "\xCB\xC0\x00\xCC\xCC\xCC\xCC\xCC\xCD"
      end
    end
    
    context 'with strings' do
      it 'encodes strings' do
        MessagePack.pack('hello world').should == "\xABhello world"
      end

      it 'encodes empty strings' do
        MessagePack.pack('').should == "\xA0"
      end
    end
    
    context 'with symbols' do
      it 'encodes symbols as strings' do
        MessagePack.pack(:symbol).should == "\xA6symbol"
      end
    end
    
    context 'with arrays' do
      it 'encodes arrays with strings' do
        MessagePack.pack(["hello", "world"]).should == "\x92\xA5hello\xA5world"
      end
      
      it 'encodes arrays with mixed values' do
        MessagePack.pack(["hello", "world", 42]).should == "\x93\xA5hello\xA5world*"
      end
      
      it 'encodes arrays of arrays' do
        MessagePack.pack([[[[1, 2], 3], 4]]).should == "\x91\x92\x92\x92\x01\x02\x03\x04"
      end
      
      it 'encodes empty arrays' do
        MessagePack.pack([]).should == "\x90"
      end
    end
    
    context 'with hashes' do
      it 'encodes hashes' do
        MessagePack.pack({'foo' => 'bar'}).should == "\x81\xA3foo\xA3bar"
      end

      it 'encodes hashes with mixed keys and values' do
        MessagePack.pack({'foo' => 'bar', 3 => 'three', 'four' => 4, 'x' => ['y'], :a => :b}).should == "\x85\xA3foo\xA3bar\x03\xA5three\xA4four\x04\xA1x\x91\xA1y\xA1a\xA1b"
      end
      
      it 'encodes hashes of hashes' do
        MessagePack.pack({{'x' => {'y' => 'z'}} => 's'}).should == "\x81\x81\xA1x\x81\xA1y\xA1z\xA1s"
      end
    end
    
    context 'with other things' do
      it 'raises an error' do
        expect { MessagePack.pack(self) }.to raise_error(ArgumentError, /^Cannot pack type:/)
      end
    end
  end
end