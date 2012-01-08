package org.msgpack.jruby;


import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyString;


class MessagePackFacade {
  private final MessagePack msgPack;

  public MessagePackFacade() {
    msgPack = new MessagePack();
  }

  public RubyString pack(RubyObject o) throws IOException {
    BufferPacker bufferedPacker = msgPack.createBufferPacker();
    RubyObjectPacker packer = new RubyObjectPacker(msgPack, bufferedPacker);
    packer.write(o);
    Ruby runtime = o == null ? Ruby.getGlobalRuntime() : o.getRuntime();
    return RubyString.newString(runtime, bufferedPacker.toByteArray());
  }

  public RubyObject unpack(RubyString s) throws IOException {
    RubyObjectUnpacker unpacker = new RubyObjectUnpacker(msgPack);
    return unpacker.unpack(s);
  }
}