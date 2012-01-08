package org.msgpack.jruby;


import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.unpacker.MessagePackBufferUnpacker;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.type.IntegerValue;

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


public class RubyObjectUnpacker {
  private final MessagePack msgPack;

  public RubyObjectUnpacker(MessagePack msgPack) {
    this.msgPack = msgPack;
  }

  public RubyObject unpack(RubyString str) {
    MessagePackBufferUnpacker unpacker = new MessagePackBufferUnpacker(msgPack);
    unpacker.wrap(str.getBytes());
    Ruby runtime = str.getRuntime();
    Value value = unpacker.iterator().next();
    return (RubyObject) valueToRubyObject(runtime, value);
  }

  private IRubyObject valueToRubyObject(Ruby runtime, Value value) {
    switch (value.getType()) {
    case NIL:
      return runtime.getNil();
    case BOOLEAN:
      return RubyBoolean.newBoolean(runtime, value.asBooleanValue().getBoolean());
    case INTEGER:
      // TODO: is there any way of checking for bignums up front?
      IntegerValue iv = value.asIntegerValue();
      try {
        return RubyFixnum.newFixnum(runtime, iv.getLong());
      } catch (MessageTypeException mte) {
        return RubyBignum.newBignum(runtime, iv.getBigInteger());
      }
    case FLOAT:
      return RubyFloat.newFloat(runtime, value.asFloatValue().getDouble());
    case ARRAY:
      Value[] elements = value.asArrayValue().getElementArray();
      int elementCount = elements.length;
      IRubyObject[] rubyObjects = new IRubyObject[elementCount];
      for (int i = 0; i < elementCount; i++) {
        rubyObjects[i] = valueToRubyObject(runtime, elements[i]);
      }
      return RubyArray.newArray(runtime, rubyObjects);
    case MAP:
      Value[] keysAndValues = value.asMapValue().getKeyValueArray();
      int kvCount = keysAndValues.length;
      RubyHash hash = RubyHash.newHash(runtime);
      for (int i = 0; i < kvCount; i += 2) {
        Value k = keysAndValues[i];
        Value v = keysAndValues[i + 1];
        hash.put(valueToRubyObject(runtime, k), valueToRubyObject(runtime, v));
      }
      return hash;
    case RAW:
      return RubyString.newString(runtime, value.asRawValue().getByteArray());
    default:
      throw runtime.newArgumentError(String.format("Unexpected value: %s", value.toString()));
    }
  }
}
