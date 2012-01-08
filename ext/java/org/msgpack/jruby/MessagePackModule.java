package org.msgpack.jruby;


import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.compiler.ir.operands.StandardError;


public class MessagePackModule {
  private static final MessagePack msgPack = new MessagePack();
  private static final RubyObjectUnpacker unpacker = new RubyObjectUnpacker(msgPack);

  public static RubyString pack(RubyObject o) throws IOException {
    BufferPacker bufferedPacker = msgPack.createBufferPacker();
    RubyObjectPacker packer = new RubyObjectPacker(msgPack, bufferedPacker);
    packer.write(o);
    Ruby runtime = o == null ? Ruby.getGlobalRuntime() : o.getRuntime();
    return RubyString.newString(runtime, bufferedPacker.toByteArray());
  }

  public static RubyObject unpack(RubyString s) throws IOException {
    return unpacker.unpack(s);
  }

  public static class Unpacker {
    public void feed(RubyString s) { }
  }

  public static class UnpackError extends StandardError { }
}
