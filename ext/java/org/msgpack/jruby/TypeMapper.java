package org.msgpack.jruby;


import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyBoolean;
import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.RubyBignum;
import org.msgpack.MessagePackObject;
import org.msgpack.MessageTypeException;


public class TypeMapper {
	private static Field cachedMapField = null;
	
	public IRubyObject toRubyObject(MessagePackObject mpo) throws IOException {
		return toRubyObject(mpo, Ruby.getGlobalRuntime());
	}
	
	public IRubyObject toRubyObject(MessagePackObject mpo, Ruby runtime) throws IOException {
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
			IRubyObject[] elements = new IRubyObject[mpoElements.length];
			int count = mpoElements.length;
			for (int i = 0; i < count; i++ ) {
				elements[i] = toRubyObject(mpoElements[i], runtime);
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
					hash.put(toRubyObject(keyValuePairs[i], runtime), toRubyObject(keyValuePairs[i + 1], runtime));
				}
			} catch (IllegalAccessException iae) {
			} catch (NoSuchFieldException nfe) {
			}
			if (hash == null) {
				Map<MessagePackObject, MessagePackObject> mpoMap = mpo.asMap();
				hash = RubyHash.newHash(runtime);
				for (Map.Entry<MessagePackObject, MessagePackObject> entry : mpoMap.entrySet()) {
					hash.put(toRubyObject(entry.getKey(), runtime), toRubyObject(entry.getValue(), runtime));
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