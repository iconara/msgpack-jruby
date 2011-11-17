package org.msgpack.jruby;


import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.RubySymbol;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyBoolean;
import org.jruby.RubyInteger;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyBignum;
import org.jruby.RubyNil;
import org.msgpack.Packer;
import org.msgpack.Unpacker;
import org.msgpack.MessagePackObject;
import org.msgpack.MessageTypeException;
import org.msgpack.object.MapType;


public class MessagePack {
	private static final MessagePack instance = new MessagePack();
	
	private final TypeMapper typeMapper;
	
	private MessagePack() {
		typeMapper = new TypeMapper();
	}
	
	public static RubyString pack(IRubyObject o) throws IOException {
		return instance.packObject(o);
	}
	
	public static IRubyObject unpack(RubyString s) throws IOException {
		return instance.unpackString(s);
	}
	
	public RubyString packObject(IRubyObject o) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		_pack(o, new Packer(stream));
		Ruby runtime = o == null ? Ruby.getGlobalRuntime() : o.getRuntime();
		return RubyString.newString(runtime, stream.toByteArray());
	}
	
	private void _pack(IRubyObject o, Packer packer) throws IOException {
		if (o == null || o instanceof RubyNil) {
			packer.packNil();
		} else if (o instanceof RubyBoolean) {
			packer.packBoolean((Boolean) ((RubyBoolean) o).toJava(Boolean.class));
		} else if (o instanceof RubyBignum) {
			packer.packBigInteger(((RubyBignum) o).getBigIntegerValue());
		} else if (o instanceof RubyInteger) {
			packer.packLong(((RubyInteger) o).getLongValue());
		} else if (o instanceof RubyFixnum) {
			packer.packLong(((RubyFixnum) o).getLongValue());
		} else if (o instanceof RubyFloat) {
			packer.packDouble(((RubyFloat) o).getDoubleValue());
		} else if (o instanceof RubyString) {
			byte[] bytes = ((RubyString) o).getBytes();
			packer.packRaw(bytes.length);
			packer.packRawBody(bytes);
		} else if (o instanceof RubySymbol) {
			_pack((IRubyObject) ((RubySymbol) o).to_s(), packer);
		} else if (o instanceof RubyArray) {
			RubyArray array = (RubyArray) o;
			int count = array.size();
			packer.packArray(count);
			for (int i = 0; i < count; i++) {
				_pack(array.entry(i), packer);
			}
		} else if (o instanceof RubyHash) {
			RubyHash hash = (RubyHash) o;
			int count = hash.size();
			packer.packMap(count);
			RubyArray keys = hash.keys();
			RubyArray values = hash.rb_values();
			for (int i = 0; i < count; i++) {
				_pack(keys.entry(i), packer);
				_pack(values.entry(i), packer);
			}
		} else {
			throw Ruby.getGlobalRuntime().newArgumentError(String.format("Cannot pack type: %s", o.getClass().getName()));
		}
	}

	public IRubyObject unpackString(RubyString blob) throws IOException {
		Unpacker unpacker = new Unpacker();
		unpacker.wrap(blob.getBytes());
		return typeMapper.toRubyObject(unpacker.iterator().next(), blob.getRuntime());
	}
}