package org.msgpack.jruby;


import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyClass;
import org.jruby.RubyString;
import org.jruby.runtime.load.Library;
import org.jruby.runtime.callback.Callback;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.Packer;


public class MessagePackLibrary implements Library {
  public void load(Ruby runtime, boolean wrap) throws IOException {
    MessagePack msgPack = new MessagePack();
    RubyModule msgpackModule = runtime.defineModule("MessagePack");
    msgpackModule.defineModuleFunction("pack", new PackCallback(msgPack));
    msgpackModule.defineModuleFunction("unpack", new UnpackCallback(msgPack));
  }
    
  private static class PackCallback implements Callback {
    private MessagePack msgPack;
    
    public PackCallback(MessagePack msgPack) {
      this.msgPack = msgPack;
    }
    
    public IRubyObject execute(IRubyObject recv, IRubyObject[] args, Block block) {
      try {
        BufferPacker bufferedPacker = msgPack.createBufferPacker();
        Packer packer = new RubyObjectPacker(msgPack, bufferedPacker).write(args[0]);
        return RubyString.newString(recv.getRuntime(), bufferedPacker.toByteArray());
      } catch (IOException ioe) {
        // TODO: how to propagate these to the Ruby runtime?
        return recv.getRuntime().getNil();
      }
    }
    
    public Arity getArity() { return Arity.ONE_REQUIRED; }
  }

  private static class UnpackCallback implements Callback {
    private RubyObjectUnpacker unpacker;
    
    public UnpackCallback(MessagePack msgPack) { 
      this.unpacker = new RubyObjectUnpacker(msgPack);
    }
    
    public IRubyObject execute(IRubyObject recv, IRubyObject[] args, Block block) {
      try {
        return unpacker.unpack((RubyString) args[0]);
      } catch (IOException ioe) {
        // TODO: how to propagate these to the Ruby runtime?
        return recv.getRuntime().getNil();
      }
    }
    
    public Arity getArity() { return Arity.ONE_REQUIRED; }
  }
}