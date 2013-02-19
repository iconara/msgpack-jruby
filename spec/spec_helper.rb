# encoding: utf-8

require 'bundler/setup'
require 'msgpack'


RSpec.configure do |c|
  c.treat_symbols_as_metadata_keys_with_true_values = true
  c.filter_run_excluding :encodings => !(defined? Encoding)
end