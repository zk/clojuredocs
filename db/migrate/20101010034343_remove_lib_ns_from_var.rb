class RemoveLibNsFromVar < ActiveRecord::Migration
  def self.up
    remove_column :functions, :library
    remove_column :functions, :ns
  end

  def self.down
  end
end
