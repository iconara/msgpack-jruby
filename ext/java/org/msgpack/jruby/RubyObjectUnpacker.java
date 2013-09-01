package org.msgpack.jruby;


import java.io.IOException;
import java.math.BigInteger;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.unpacker.MessagePackBufferUnpacker;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.type.BooleanValue;
import org.msgpack.type.IntegerValue;
import org.msgpack.type.FloatValue;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;

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
import org.jruby.runtime.encoding.EncodingService;
import org.jruby.runtime.ThreadContext;
import org.jruby.util.ByteList;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;


public class RubyObjectUnpacker {
  private final MessagePack msgPack;

  public RubyObjectUnpacker(MessagePack msgPack) {
    this.msgPack = msgPack;
  }

  static class CompiledOptions {
    public final boolean symbolizeKeys;
    public final Encoding encoding;

    public CompiledOptions(Ruby runtime) {
      this(runtime, null);
    }

    public CompiledOptions(Ruby runtime, RubyHash options) {
      EncodingService encodingService = runtime.getEncodingService();
      Encoding externalEncoding = null;
      if (options == null) {
        symbolizeKeys = false;
      } else {
        ThreadContext ctx = runtime.getCurrentContext();
        RubySymbol key = runtime.newSymbol("symbolize_keys");
        IRubyObject value = options.fastARef(key);
        symbolizeKeys = value != null && value.isTrue();
        IRubyObject rubyEncoding = options.fastARef(runtime.newSymbol("encoding"));
        externalEncoding = encodingService.getEncodingFromObject(rubyEncoding);
      }
      encoding = (externalEncoding != null) ? externalEncoding : runtime.getDefaultExternalEncoding();
    }
  }

  public IRubyObject unpack(RubyString str, RubyHash options) throws IOException {
    return unpack(str.getRuntime(), str.getBytes(), new CompiledOptions(str.getRuntime(), options));
  }

  public IRubyObject unpack(Ruby runtime, byte[] data) throws IOException {
    return unpack(runtime, data, new CompiledOptions(runtime, null));
  }

  public IRubyObject unpack(Ruby runtime, byte[] data, RubyHash options) throws IOException {
    return unpack(runtime, data, new CompiledOptions(runtime, options));
  }

  IRubyObject unpack(Ruby runtime, byte[] data, CompiledOptions options) throws IOException {
    // MessagePackBufferUnpacker unpacker = new MessagePackBufferUnpacker(msgPack);
    // unpacker.wrap(data);
    // return valueToRubyObject(runtime, unpacker.readValue(), options);
    Decoder decoder = new Decoder(runtime, data, options);
    return decoder.next();
  }

  IRubyObject valueToRubyObject(Ruby runtime, Value value, RubyHash options) throws IOException {
    return valueToRubyObject(runtime, value, new CompiledOptions(runtime, options));
  }

  IRubyObject valueToRubyObject(Ruby runtime, Value value, CompiledOptions options) {
    switch (value.getType()) {
    case NIL:
      return runtime.getNil();
    case BOOLEAN:
      return convert(runtime, value.asBooleanValue());
    case INTEGER:
      return convert(runtime, value.asIntegerValue());
    case FLOAT:
      return convert(runtime, value.asFloatValue());
    case ARRAY:
      return convert(runtime, value.asArrayValue(), options);
    case MAP:
      return convert(runtime, value.asMapValue(), options);
    case RAW:
      return convert(runtime, value.asRawValue(), options);
    default:
      throw runtime.newArgumentError(String.format("Unexpected value: %s", value.toString()));
    }
  }

  private IRubyObject convert(Ruby runtime, BooleanValue value) {
    return RubyBoolean.newBoolean(runtime, value.asBooleanValue().getBoolean());
  }

  private IRubyObject convert(Ruby runtime, IntegerValue value) {
    // TODO: is there any way of checking for bignums up front?
    IntegerValue iv = value.asIntegerValue();
    try {
      return RubyFixnum.newFixnum(runtime, iv.getLong());
    } catch (MessageTypeException mte) {
      return RubyBignum.newBignum(runtime, iv.getBigInteger());
    }
  }

  private IRubyObject convert(Ruby runtime, FloatValue value) {
    return RubyFloat.newFloat(runtime, value.asFloatValue().getDouble());
  }

  private IRubyObject convert(Ruby runtime, ArrayValue value, CompiledOptions options) {
    Value[] elements = value.asArrayValue().getElementArray();
    int elementCount = elements.length;
    IRubyObject[] rubyObjects = new IRubyObject[elementCount];
    for (int i = 0; i < elementCount; i++) {
      rubyObjects[i] = valueToRubyObject(runtime, elements[i], options);
    }
    return RubyArray.newArray(runtime, rubyObjects);
  }

  private IRubyObject convert(Ruby runtime, MapValue value, CompiledOptions options) {
    Value[] keysAndValues = value.asMapValue().getKeyValueArray();
    int kvCount = keysAndValues.length;
    RubyHash hash = RubyHash.newHash(runtime);
    for (int i = 0; i < kvCount; i += 2) {
      Value k = keysAndValues[i];
      Value v = keysAndValues[i + 1];
      IRubyObject kk = valueToRubyObject(runtime, k, options);
      IRubyObject vv = valueToRubyObject(runtime, v, options);
      if (options.symbolizeKeys) {
        kk = runtime.newSymbol(kk.asString().getByteList());
      }
      hash.put(kk, vv);
    }
    return hash;
  }

  private IRubyObject convert(Ruby runtime, RawValue value, CompiledOptions options) {
    RubyString string = RubyString.newString(runtime, value.asRawValue().getByteArray());
    string.setEncoding(options.encoding);
    string.callMethod(runtime.getCurrentContext(), "encode!");
    return string;
  }






  private static class Decoder {
    private final Ruby runtime;
    private final byte[] buffer;
    private final CompiledOptions options;
    private final Encoding binaryEncoding;
    private final Encoding utf8Encoding;

    private int offset;

    public Decoder(Ruby runtime, byte[] buffer, CompiledOptions options) {
      this.runtime = runtime;
      this.buffer = buffer;
      this.options = options;
      this.offset = 0;
      this.binaryEncoding = runtime.getEncodingService().getAscii8bitEncoding();
      this.utf8Encoding = UTF8Encoding.INSTANCE;
    }

    private int consumeUnsignedByte() {
      return consumeSignedByte() & 0xff;
    }

    private int consumeSignedByte() {
      int b = buffer[offset];
      offset++;
      return b;
    }

    private long consumeUnsignedInteger(int size) {
      long n = 0;
      for (int i = size - 1; i > 0; i--) {
        long x = consumeUnsignedByte();
        n |= x << (i * 8);
      }
      n |= consumeUnsignedByte();
      return n;
    }

    private long consumeSignedInteger(int size) {
      long n = 0;
      for (int i = size - 1; i > 0; i--) {
        long x = consumeSignedByte();
        n |= x << (i * 8);
      }
      n |= consumeSignedByte();
      return n;
    }

    private IRubyObject consumeRubyUnsignedInteger(int size) {
      if (size == 8 && (buffer[offset] & 0x80) == 0x80) {
        return consumeBignum(1);
      } else {
        return runtime.newFixnum(consumeUnsignedInteger(size));
      }
    }

    private IRubyObject consumeRubySignedInteger(int size) {
      if (size == 8 && (buffer[offset] & 0x80) == 0x80) {
        return consumeBignum(-1);
      } else {
        return runtime.newFixnum(consumeSignedInteger(size));
      }
    }

    private IRubyObject consumeBignum(int sign) {
      byte[] bytes = new byte[8];
      System.arraycopy(buffer, offset, bytes, 0, 8);
      offset += 8;
      return RubyBignum.newBignum(runtime, new BigInteger(sign, bytes));
    }

    private IRubyObject consumeString(int size, Encoding encoding) {
      byte[] bytes = new byte[size];
      System.arraycopy(buffer, offset, bytes, 0, size);
      offset += size;
      ByteList byteList = new ByteList(bytes, encoding);
      return runtime.newString(byteList);
    }

    private IRubyObject consumeRubyFloat(int size) {
      if (size == 4) {
        return runtime.newFloat(Float.intBitsToFloat((int) consumeUnsignedInteger(4)));
      } else {
        return runtime.newFloat(Double.longBitsToDouble(consumeUnsignedInteger(8)));
      }
    }

    private IRubyObject consumeArray(int size) {
      IRubyObject[] elements = new IRubyObject[size];
      for (int i = 0; i < size; i++) {
        elements[i] = next();
      }
      return runtime.newArray(elements);
    }

    private IRubyObject consumeHash(int size) {
      RubyHash hash = RubyHash.newHash(runtime);
      for (int i = 0; i < size; i++) {
        hash.fastASet(next(), next());
      }
      return hash;
    }

    public IRubyObject next() {
      int b = consumeUnsignedByte();
      switch (b) {
      case 0xc0: return runtime.getNil();
      case 0xc2: return runtime.newBoolean(false);
      case 0xc3: return runtime.newBoolean(true);
      case 0xc4: return consumeString((int) consumeUnsignedInteger(1), binaryEncoding);
      case 0xc5: return consumeString((int) consumeUnsignedInteger(2), binaryEncoding);
      case 0xc6: return consumeString((int) consumeUnsignedInteger(4), binaryEncoding);
      case 0xca: return consumeRubyFloat(4);
      case 0xcb: return consumeRubyFloat(8);
      case 0xcc: return consumeRubyUnsignedInteger(1);
      case 0xcd: return consumeRubyUnsignedInteger(2);
      case 0xce: return consumeRubyUnsignedInteger(4);
      case 0xcf: return consumeRubyUnsignedInteger(8);
      case 0xd0: return consumeRubySignedInteger(1);
      case 0xd1: return consumeRubySignedInteger(2);
      case 0xd2: return consumeRubySignedInteger(4);
      case 0xd3: return consumeRubySignedInteger(8);
      case 0xd9: return consumeString((int) consumeUnsignedInteger(1), utf8Encoding);
      case 0xda: return consumeString((int) consumeUnsignedInteger(2), utf8Encoding);
      case 0xdb: return consumeString((int) consumeUnsignedInteger(4), utf8Encoding);
      case 0xdc: return consumeArray((int) consumeUnsignedInteger(2));
      case 0xdd: return consumeArray((int) consumeUnsignedInteger(4));
      case 0xde: return consumeHash((int) consumeUnsignedInteger(2));
      case 0xdf: return consumeHash((int) consumeUnsignedInteger(4));
      default:
        if (b <= 0x7f) {
          return runtime.newFixnum(b);
        } else if ((b & 0xe0) == 0xe0) {
          return runtime.newFixnum(b - 0x100);
        } else if ((b & 0xe0) == 0xa0) {
          return consumeString(b & 0x1f, utf8Encoding);
        } else if ((b & 0xf0) == 0x90) {
          return consumeArray(b & 0x0f);
        } else if ((b & 0xf0) == 0x80) {
          return consumeHash(b & 0x0f);
        }
      }
      return runtime.getNil();
    }
  }
}
