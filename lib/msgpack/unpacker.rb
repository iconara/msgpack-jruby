# encoding: ascii-8bit

class MessagePack
  class Unpacker
    def initialize
      @unpacker = org.msgpack.Unpacker.new
      @type_mapper = org.msgpack.jruby.TypeMapper.new
    end
    
    def feed(bytes)
      @unpacker.feed(bytes.to_java_bytes)
    end
    
    def each
      while obj = next_object
        yield obj
      end
    end
    
  private
  
    def next_object
      result = @unpacker.next
      if result.finished?
        @type_mapper.to_ruby_object(result.data)
      else
        nil
      end
    end
  end
end

