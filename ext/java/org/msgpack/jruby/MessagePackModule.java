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
  private static final MessagePackFacade facade = new MessagePackFacade();

  public static RubyString pack(RubyObject o) throws IOException {
    return facade.pack(o);
  }

  public static RubyObject unpack(RubyString s) throws IOException {
    return facade.unpack(s);
  }

  public static class Unpacker {
    public void feed(RubyString s) { }
  }

  public static class UnpackError extends StandardError { }
}
