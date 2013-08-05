package org.msgpack.jruby;


import java.io.IOException;

import org.jcodings.Encoding;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyNil;
import org.jruby.RubyBoolean;
import org.jruby.RubyBignum;
import org.jruby.RubyInteger;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;


public class RubyObjectPacker {
  private final MessagePack msgPack;

  public RubyObjectPacker(MessagePack msgPack) {
    this.msgPack = msgPack;
  }

  public RubyString pack(IRubyObject o) throws IOException {
    return RubyString.newString(o.getRuntime(), packRaw(o));
  }

  public byte[] packRaw(IRubyObject o) throws IOException {
    BufferPacker packer = msgPack.createBufferPacker();
    write(packer, o);
    return packer.toByteArray();
  }

  private void write(BufferPacker packer, IRubyObject o) throws IOException {
    if (o == null || o instanceof RubyNil) {
      packer.writeNil();
    } else if (o instanceof RubyBoolean) {
      packer.write(((RubyBoolean) o).isTrue());
    } else if (o instanceof RubyBignum) {
      write(packer, (RubyBignum) o);
    } else if (o instanceof RubyInteger) {
      write(packer, (RubyInteger) o);
    } else if (o instanceof RubyFixnum) {
      write(packer, (RubyFixnum) o);
    } else if (o instanceof RubyFloat) {
      write(packer, (RubyFloat) o);
    } else if (o instanceof RubyString) {
      write(packer, (RubyString) o);
    } else if (o instanceof RubySymbol) {
      write(packer, (RubySymbol) o);
    } else if (o instanceof RubyArray) {
      write(packer, (RubyArray) o);
    } else if (o instanceof RubyHash) {
      write(packer, (RubyHash) o);
    } else {
      throw o.getRuntime().newArgumentError(String.format("Cannot pack type: %s", o.getClass().getName()));
    }
  }

  private void write(BufferPacker packer, RubyBignum bignum) throws IOException {
    packer.write(bignum.getBigIntegerValue());
  }

  private void write(BufferPacker packer, RubyInteger integer) throws IOException {
    packer.write(integer.getLongValue());
  }

  private void write(BufferPacker packer, RubyFixnum fixnum) throws IOException {
    packer.write(fixnum.getLongValue());
  }

  private void write(BufferPacker packer, RubyFloat flt) throws IOException {
    packer.write(flt.getDoubleValue());
  }

  private void write(BufferPacker packer, RubyString str) throws IOException {
    Ruby runtime = str.getRuntime();
    if (str.getEncoding() != runtime.getDefaultExternalEncoding()) {
      str = (RubyString) str.encode(runtime.getCurrentContext(), runtime.getEncodingService().getDefaultExternal());
    }
    packer.write(str.getBytes());
  }

  private void write(BufferPacker packer, RubySymbol sym) throws IOException {
    write(packer, (RubyString) sym.to_s());
  }

  private void write(BufferPacker packer, RubyArray array) throws IOException {
    int count = array.size();
    packer.writeArrayBegin(count);
    for (int i = 0; i < count; i++) {
      write(packer, (RubyObject) array.entry(i));
    }
    packer.writeArrayEnd();
  }

  private void write(BufferPacker packer, RubyHash hash) throws IOException {
    int count = hash.size();
    packer.writeMapBegin(count);
    RubyArray keys = hash.keys();
    RubyArray values = hash.rb_values();
    for (int i = 0; i < count; i++) {
      write(packer, (RubyObject) keys.entry(i));
      write(packer, (RubyObject) values.entry(i));
    }
    packer.writeMapEnd();
  }
}
