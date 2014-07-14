package org.msgpack.jruby;

import java.util.Arrays;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyIO;
import org.jruby.RubyModule;
import org.jruby.RubyString;
import org.jruby.internal.runtime.methods.CallConfiguration;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author taichi
 */
public class CoreExtensions {
  public static void load(final Ruby r) {
    List<RubyClass> classes = Arrays.asList(r.getNilClass(), r.getTrueClass(),
        r.getFalseClass(), r.getFixnum(), r.getBignum(), r.getFloat(),
        r.getString(), r.getArray(), r.getHash(), r.getSymbol());
    for (RubyClass rc : classes) {
      rc.addMethod("to_msgpack", new DynamicMethod(rc, Visibility.PUBLIC,
          CallConfiguration.FrameNoneScopeNone) {
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self,
            RubyModule clazz, String name, IRubyObject[] args, Block block) {
          RubyString result = (RubyString) new Encoder(r).encode(self);
          if (0 < args.length) {
            IRubyObject ro = args[0];
            if (ro instanceof RubyIO) {
              RubyIO io = (RubyIO) ro;
              io.write(context, result);
              return context.nil;
            } else if (ro instanceof RubyString) {
              RubyString rs = (RubyString) ro;
              rs.cat(result.getByteList());
              return context.nil;
            } else if (ro instanceof RubyArray) {
              RubyArray ra = (RubyArray) ro;
              ra.append(result);
              return context.nil;
            }
          }
          return result;
        }

        @Override
        public DynamicMethod dup() {
          return this;
        }
      });
    }
  }
}
