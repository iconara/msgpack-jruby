# encoding: ascii-8bit

require 'spec_helper'


describe MessagePack do
  def self.utf8(str)
    str.force_encoding(Encoding::UTF_8)
  end

  def self.make_hash(size)
    Hash[Array.new(size) { |i| [i.to_s(16).rjust(4, '0'), true] }]
  end

  tests = {
    'constant values' => [
      ['true', true, "\xc3"],
      ['false', false, "\xc2"],
      ['nil', nil, "\xc0"],
    ],
    'numbers' => [
      ['zero', 0, "\x00"],
      ['127', 0x7f, "\x7f"],
      ['128', 0x80, "\xcc\x80"],
      ['256', 0x100, "\xcd\x01\x00"],
      ['23435345', 0x01659851, "\xCE\x01\x65\x98\x51"],
      ['2342347938475324', 0x0008525a60d02d3c, "\xcf\x00\x08\x52\x5a\x60\xd0\x2d\x3c"],
      ['-1', -1, "\xff"],
      ['-33', -33, "\xd0\xdf"],
      ['-129', -129, "\xd1\xff\x7f"],
      ['-8444910', -8444910, "\xd2\xff\x7f\x24\x12"],
      ['-41957882392009710', -41957882392009710, "\xd3\xff\x6a\xef\x87\x3c\x7f\x24\x12"],
      ['small integers', 42, "*"],
      ['medium integers', 333, "\xcd\x01M"],
      ['large integers', 2**31 - 1, ["\xce", "\xd2"].map { |prefix| "#{prefix}\x7f\xff\xff\xff" }],
      ['large negative integers', -2**31, "\xd2\x80\x00\x00\x00"],
      ['large unsigned integers', 2**32 - 1, "\xce\xff\xff\xff\xff"],
      ['huge integers', 2**63 - 1, ["\xcf", "\xd3"].map { |prefix| "#{prefix}\x7f\xff\xff\xff\xff\xff\xff\xff" }],
      ['huge negative integers', -2**63, "\xd3\x80\x00\x00\x00\x00\x00\x00\x00"],
      ['huge unsigned integers', 2**64 - 1, "\xcf\xff\xff\xff\xff\xff\xff\xff\xff"],
      ['negative integers', -1, "\xff"],
      ['1.0', 1.0, "\xca\x3f\x80\x00\x00"],
      ['small floats', 0.03125, "\xca\x3d\x00\x00\x00"],
      ['big floats', Math::PI * 1_000_000_000_000_000_000, "\xcbC\xc5\xcc\x96\xef\xd1\x19%"],
      ['negative floats', -2.1, "\xcb\xc0\x00\xcc\xcc\xcc\xcc\xcc\xcd"],
    ],
    'strings' => [
      ['strings', utf8('hello world'), "\xabhello world"],
      ['non-UTF-8 strings', "ol\xE9".force_encoding(Encoding::ISO_8859_1), "\xa4ol\xc3\xa9"],
      ['empty strings', utf8(''), "\xa0"],
      ['medium strings', utf8('x' * 0xdd), "\xd9\xdd#{'x' * 0xdd}"],
      ['big strings', utf8('x' * 0xdddd), "\xda\xdd\xdd#{'x' * 0xdddd}"],
      ['huge strings', utf8('x' * 0x10000), "\xdb\x00\x01\x00\x00#{'x' * 0x10000}"],
    ],
    'binaries' => [
      ['medium binary', ("\a" * 0x5), "\xc4\x05#{"\a" * 0x5}"],
      ['big binary', ("\a" * 0x100), "\xc5\x01\x00#{"\a" * 0x100}"],
      ['huge binary', ("\a" * 0x10000), "\xc6\x00\x01\x00\x00#{"\a" * 0x10000}"],
    ],
    'arrays' => [
      ['empty arrays', [], "\x90"],
      ['small arrays', [1, 2], "\x92\x01\x02"],
      ['medium arrays', [false] * 0x111, "\xdc\x01\x11#{"\xc2" * 0x111}"],
      ['big arrays', [false] * 0x11111, "\xdd\x00\x01\x11\x11#{"\xc2" * 0x11111}"],
      ['arrays with strings', [utf8('hello'), utf8('world')], "\x92\xa5hello\xa5world"],
      ['arrays with mixed values', [utf8('hello'), utf8('world'), 42], "\x93\xa5hello\xa5world*"],
      ['arrays of arrays', [[[[1, 2], 3], 4]], "\x91\x92\x92\x92\x01\x02\x03\x04"],
    ],
    'hashes' => [
      ['empty hashes', {}, "\x80"],
      ['small hashes', {utf8('foo') => utf8('bar')}, "\x81\xa3foo\xa3bar"],
      ['medium hashes', make_hash(0x20), "\xde\x00\x20#{make_hash(0x20).map { |k, v| "\xa4#{k}\xc3" }.join('')}"],
      ['big hashes', make_hash(0x10000), "\xdf\x00\x01\x00\x00#{make_hash(0x10000).map { |k, v| "\xa4#{k}\xc3" }.join('')}"],
      ['hashes with mixed keys and values', {utf8('foo') => utf8('bar'), 3 => utf8('three'), utf8('four') => 4, utf8('x') => [utf8('y')], utf8('a') => utf8('b')}, "\x85\xa3foo\xa3bar\x03\xa5three\xa4four\x04\xa1x\x91\xa1y\xa1a\xa1b"],
      ['hashes of hashes', {{utf8('x') => {utf8('y') => utf8('z')}} => utf8('s')}, "\x81\x81\xa1x\x81\xa1y\xa1z\xa1s"],
      ['hashes with nils', {utf8('foo') => nil}, "\x81\xa3foo\xc0"]
    ],
    'extensions' => [
      ['micro fixed extensions', MessagePack::ExtensionValue.new(1, "\x00"), "\xd4\x01\x00"],
      ['tiny fixed extensions', MessagePack::ExtensionValue.new(1, "\x00\x00"), "\xd5\x01\x00\x00"],
      ['small fixed extensions', MessagePack::ExtensionValue.new(1, "\x00" * 4), "\xd6\x01\x00\x00\x00\x00"],
      ['medium fixed extensions', MessagePack::ExtensionValue.new(1, "\x00" * 8), "\xd7\x01\x00\x00\x00\x00\x00\x00\x00\x00"],
      ['big fixed extensions', MessagePack::ExtensionValue.new(1, "\x00" * 16), "\xd8\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"],
      ['small variable extensions', MessagePack::ExtensionValue.new(3, "\x00" * 5), "\xc7\x05\x03\x00\x00\x00\x00\x00"],
      ['medium variable extensions', MessagePack::ExtensionValue.new(3, "\x00" * 0x101), "\xc8\x01\x01\x03#{"\x00" * 0x101}"],
      ['big variable extensions', MessagePack::ExtensionValue.new(3, "\x00" * 0x10001), "\xc9\x00\x01\x00\x01\x03#{"\x00" * 0x10001}"],
    ]
  }

  tests.each do |ctx, its|
    context("with #{ctx}") do
      its.each do |desc, unpacked, packeds|
        it("encodes #{desc}") do
          encoded = MessagePack.pack(unpacked)
          Array(packeds).should include(encoded), "expected #{encoded.inspect[0, 100]} to be one of #{Array(packeds).map {|x| x.inspect[0, 100] }*', '}"
        end

        it "decodes #{desc}" do
          Array(packeds).each do |packed|
            decoded = MessagePack.unpack(packed)
            if packed.getbyte(0) == 0xca
              decoded.should be_within(0.00001).of(unpacked)
            else
              if ctx == 'strings'
                decoded.should eql(unpacked.encode(Encoding::UTF_8)), "expected #{decoded.inspect[0, 100]} to equal #{unpacked.inspect[0, 100]}"
              else
                decoded.should eql(unpacked), "expected #{decoded.inspect[0, 100]} to equal #{unpacked.inspect[0, 100]}"
              end
              if ctx == 'strings'
                decoded.encoding.should eql(Encoding::UTF_8)
              elsif ctx == 'binaries'
                decoded.encoding.should eql(Encoding::BINARY)
              end
            end
          end
        end

        it "decodes encoded #{desc} back" do
          if ctx == 'strings'
            MessagePack.unpack(MessagePack.pack(unpacked)).should eql(unpacked.encode(Encoding::UTF_8))
          else
            MessagePack.unpack(MessagePack.pack(unpacked)).should eql(unpacked)
          end
        end

        it "encodes decoded #{desc} back" do
          Array(packeds).each do |packed|
            packeds.should include(MessagePack.pack(MessagePack.unpack(packed)))
          end
        end
      end
    end
  end

  context 'using other names for .pack and .unpack' do
    it 'can unpack with .load' do
      MessagePack.load("\xABhello world").should == 'hello world'
    end

    it 'can pack with .dump' do
      MessagePack.dump('hello world'.force_encoding(::Encoding::UTF_8)).should == "\xABhello world"
    end
  end

  context 'with symbols' do
    it 'encodes symbols as strings' do
      MessagePack.pack(:symbol).should == "\xA6symbol"
    end
  end

  context 'with different external encoding', :encodings do
    before do
      @default_external = Encoding.default_external
      @default_internal = Encoding.default_internal
      Encoding.default_external = Encoding::UTF_8
      Encoding.default_internal = Encoding::ISO_8859_1
    end

    after do
      Encoding.default_external = @default_external
      Encoding.default_internal = @default_internal
    end

    it 'transcodes strings when encoding' do
      input = "sk\xE5l".force_encoding(Encoding::ISO_8859_1)
      MessagePack.pack(input).should == "\xA5sk\xC3\xA5l"
    end
  end

  context 'with other things' do
    it 'raises an error on #pack with an unsupported type' do
      expect { MessagePack.pack(self) }.to raise_error(ArgumentError, /^Cannot pack type:/)
    end
    
    it 'rasies an error on #unpack with garbage' do
      expect { MessagePack.unpack("\xc1") }.to raise_error(MessagePack::UnpackError)
    end

    it 'rasies an error when not all data is consumed by the decoding' do
      pending 'this is how the msgpack gem works' do
        expect { MessagePack.unpack("\xc0\xc0") }.to raise_error(MessagePack::UnpackError)
      end
    end
  end

  context 'extensions' do
    before do
      pending 'Should this remain in v2.0?'
    end

    it 'can unpack hashes with symbolized keys' do
      packed = MessagePack.pack({'hello' => 'world', 'nested' => ['object', {'structure' => true}]})
      unpacked = MessagePack.unpack(packed, :symbolize_keys => true)
      unpacked.should == {:hello => 'world', :nested => ['object', {:structure => true}]}
    end

    it 'can unpack strings with a specified encoding', :encodings do
      packed = MessagePack.pack({'hello' => 'world'})
      unpacked = MessagePack.unpack(packed, :encoding => Encoding::UTF_8)
      unpacked['hello'].encoding.should == Encoding::UTF_8
    end

    it 'can pack strings with a specified encoding', :encodings do
      packed = MessagePack.pack({'hello' => "w\xE5rld".force_encoding(Encoding::ISO_8859_1)}, :encoding => Encoding::UTF_8)
      packed.index("w\xC3\xA5rld").should_not be_nil
    end
  end
end
