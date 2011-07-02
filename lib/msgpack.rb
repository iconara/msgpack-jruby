# encoding: utf-8

require 'java'
require 'ext/msgpack-0.5.2-devel.jar'


module MessagePack
  import 'java.io.ByteArrayOutputStream'
  import 'org.msgpack.Packer'
  
  def self.pack(obj)
    stream = ByteArrayOutputStream.new
    _pack(obj, Packer.new(stream))
    stream.flush
    bytes = stream.to_byte_array
    String.from_java_bytes(bytes)
  end
  
private
  
  def self._pack(obj, packer)
    case obj
    when nil then packer.pack_nil
    when true then packer.pack_true
    when false then packer.pack_false
    when String, Symbol then packer.pack_string(obj)
    when Bignum then packer.pack_big_integer(obj)
    when Integer then packer.pack_int(obj)
    when Float then packer.pack_double(obj)
    when Array
      packer.pack_array(obj.size)
      obj.each do |e|
        _pack(e, packer)
      end
    when Hash
      packer.pack_map(obj.size)
      obj.each do |k, v|
        _pack(k, packer)
        _pack(v, packer)
      end
    else
      raise ArgumentError, "Cannot pack type: #{obj.class}"
    end
  end
end
