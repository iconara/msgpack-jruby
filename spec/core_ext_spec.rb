require 'spec_helper'

describe "CoreExtensions" do
  coreapis = [NilClass, TrueClass, FalseClass, Fixnum, Bignum, Float, String, Array, Hash, Symbol]
  coreapis.each do |clazz|
    context clazz do
      it "has #to_msgpack" do
        clazz.instance_methods(false).include?(:to_msgpack).should == true
      end
    end
  end
end
