# encoding: ascii-8bit

require 'spec_helper'


module MessagePack
  describe ExtensionValue do
    describe '#to_msgpack' do
      context 'with fixed sized payloads' do
        it 'encodes its payload when it is one byte' do
          described_class.new(1, "\x77").to_msgpack.should == "\xd4\x01\x77"
        end

        it 'encodes its payload when it is two bytes' do
          described_class.new(1, "\x77\x88").to_msgpack.should == "\xd5\x01\x77\x88"
        end

        it 'encodes its payload when it is four bytes' do
          described_class.new(1, "\x77\x88\x99\x00").to_msgpack.should == "\xd6\x01\x77\x88\x99\x00"
        end

        it 'encodes its payload when it is eight bytes' do
          described_class.new(1, "\x77\x88\x99\x00\x11\x22\x33\x44").to_msgpack.should == "\xd7\x01\x77\x88\x99\x00\x11\x22\x33\x44"
        end

        it 'encodes its payload when it is 16 bytes' do
          described_class.new(1, "\x55" * 16).to_msgpack.should == "\xd8\x01" + ("\x55" * 16)
        end
      end

      context 'with small non-fixed sized payload' do
        it 'encodes its payload when it is three bytes' do
          described_class.new(1, "\x55" * 3).to_msgpack.should == "\xc7\x03\x01" + ("\x55" * 3)
        end

        it 'encodes its payload when it is five bytes' do
          described_class.new(1, "\x55" * 5).to_msgpack.should == "\xc7\x05\x01" + ("\x55" * 5)
        end

        it 'encodes its payload when it is 12 bytes' do
          described_class.new(1, "\x55" * 12).to_msgpack.should == "\xc7\x0c\x01" + ("\x55" * 12)
        end
      end

      context 'with variable sized payloads' do
        it 'encodes its payload when it is 20 bytes' do
          described_class.new(1, "\x55" * 20).to_msgpack.should == "\xc7\x14\x01" + ("\x55" * 20)
        end

        it 'encodes its payload when it is 500 bytes' do
          described_class.new(1, "\x55" * 500).to_msgpack.should == "\xc8\x01\xf4\x01" + ("\x55" * 500)
        end

        it 'encodes its payload when it is 100,000 bytes' do
          described_class.new(1, "\x55" * 100_000).to_msgpack.should == "\xc9\x00\x01\x86\xa0\x01" + ("\x55" * 100_000)
        end
      end
    end
  end
end
