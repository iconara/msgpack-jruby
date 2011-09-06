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
	
	public static RubyString pack(RubyObject o) throws IOException {
		return instance.packObject(o);
	}
	
	public static RubyObject unpack(RubyString s) throws IOException {
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

	public RubyObject unpackString(RubyString blob) throws IOException {
		RubyObject obj = null;
		Unpacker unpacker = new Unpacker();
		unpacker.wrap(blob.getBytes());
		return _unpack(unpacker.iterator().next(), blob.getRuntime());
	}
	
	private static Field cachedMapField = null;
	
	private RubyObject _unpack(MessagePackObject mpo, Ruby runtime) throws IOException {
		if (mpo.isNil()) {
			return null;
		} else if (mpo.isBooleanType()) {
			return RubyBoolean.newBoolean(runtime, mpo.asBoolean());
		} else if (mpo.isIntegerType()) {
			try {
				return RubyFixnum.newFixnum(runtime, mpo.asLong());
			} catch (MessageTypeException mte) {
				return RubyBignum.newBignum(runtime, mpo.asBigInteger());
			}
		} else if (mpo.isFloatType()) {
			return RubyFloat.newFloat(runtime, mpo.asDouble());
		} else if (mpo.isArrayType()) {
			MessagePackObject[] mpoElements = mpo.asArray();
			RubyObject[] elements = new RubyObject[mpoElements.length];
			int count = mpoElements.length;
			for (int i = 0; i < count; i++ ) {
				elements[i] = _unpack(mpoElements[i], runtime);
			}
			return RubyArray.newArray(runtime, elements);
		} else if (mpo.isMapType()) {
			// This is hackish, but the only way to make sure that hashes 
			// keep their order. MessagePack's unpacking subsystem does not 
			// expose the key/value pair array, only a Map constructed from
			// that array. This code first attempts to get that array from a
			// private field, and if that fails it falls back on using the
			// unordered Map.
			RubyHash hash = null;
			try {
				if (cachedMapField == null) {
					cachedMapField = mpo.getClass().getDeclaredField("map");
					cachedMapField.setAccessible(true);
				}
				MessagePackObject[] keyValuePairs = (MessagePackObject[]) cachedMapField.get(mpo);
				int count = keyValuePairs.length;
				hash = RubyHash.newHash(runtime);
				for (int i = 0; i < count; i += 2) {
					hash.put(_unpack(keyValuePairs[i], runtime), _unpack(keyValuePairs[i + 1], runtime));
				}
			} catch (IllegalAccessException iae) {
			} catch (NoSuchFieldException nfe) {
			}
			if (hash == null) {
				Map<MessagePackObject, MessagePackObject> mpoMap = mpo.asMap();
				hash = RubyHash.newHash(runtime);
				for (Map.Entry<MessagePackObject, MessagePackObject> entry : mpoMap.entrySet()) {
					hash.put(_unpack(entry.getKey(), runtime), _unpack(entry.getValue(), runtime));
				}
			}
			return hash;
			
		} else if (mpo.isRawType()) {
			return RubyString.newString(runtime, mpo.asByteArray());
		} else {
			throw runtime.newArgumentError(String.format("Cannot upack type: %s", mpo.getClass().getName()));
		}
	}
}