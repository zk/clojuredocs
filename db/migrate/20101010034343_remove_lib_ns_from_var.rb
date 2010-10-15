class RemoveLibNsFromVar < ActiveRecord::Migration
  def self.up
    remove_column :functions, :library
    remove_column :functions, :ns

    Function.reset_column_information
  end

  def self.down
  end
end
