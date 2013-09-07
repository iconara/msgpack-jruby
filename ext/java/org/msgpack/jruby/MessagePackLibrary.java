package org.msgpack.jruby;


import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyClass;
import org.jruby.RubyString;
import org.jruby.RubyHash;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.ThreadContext;
import org.jruby.anno.JRubyModule;
import org.jruby.anno.JRubyMethod;


import org.msgpack.MessagePack;


public class MessagePackLibrary implements Library {
  public void load(Ruby runtime, boolean wrap) throws IOException {
    MessagePack msgPack = new MessagePack();
    RubyModule msgpackModule = runtime.defineModule("MessagePack");
    msgpackModule.defineAnnotatedMethods(MessagePackModule.class);
    RubyClass standardErrorClass = runtime.getStandardError();
    RubyClass unpackErrorClass = msgpackModule.defineClassUnder("UnpackError", standardErrorClass, standardErrorClass.getAllocator());
    RubyClass unpackerClass = msgpackModule.defineClassUnder("Unpacker", runtime.getObject(), new Unpacker.UnpackerAllocator(msgPack));
    unpackerClass.defineAnnotatedMethods(Unpacker.class);
  }

  @JRubyModule(name = "MessagePack")
  public static class MessagePackModule {
    private static RubyObjectPacker packer = new RubyObjectPacker(new MessagePack());
    
    @JRubyMethod(module = true, required = 1, optional = 1, alias = {"dump"})
    public static IRubyObject pack(ThreadContext ctx, IRubyObject recv, IRubyObject[] args) throws IOException {
      RubyHash options = (args.length == 2) ? (RubyHash) args[1] : null;
      return packer.pack(args[0], options);
    }
    
    @JRubyMethod(module = true, required = 1, optional = 1, alias = {"load"})
    public static IRubyObject unpack(ThreadContext ctx, IRubyObject recv, IRubyObject[] args) throws IOException {
      Decoder decoder = new Decoder(ctx.getRuntime(), args[0].asString().getBytes());
      return decoder.next();
    }
  }
}
