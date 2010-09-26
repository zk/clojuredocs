$:.unshift(File.dirname(__FILE__) + '/../lib')

require 'rubygems'
require 'active_record' 


plugin_test_dir = File.dirname(__FILE__)

ActiveRecord::Base.logger = Logger.new(File.join(plugin_test_dir, "debug.log"))

ActiveRecord::Base.configurations = YAML::load(IO.read(File.join(plugin_test_dir, "db", "database.yml")))
ActiveRecord::Base.establish_connection(ENV["DB"] || "sqlite3mem")
ActiveRecord::Migration.verbose = false
load(File.join(plugin_test_dir, "db", "schema.rb"))

require File.join(plugin_test_dir, '..', 'init')

class User < ActiveRecord::Base
  has_many :comments
end

class Commentable < ActiveRecord::Base
  acts_as_commentable
end