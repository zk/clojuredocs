class AddVersions < ActiveRecord::Migration
  def self.up
    add_column :functions, :version, :string
    add_column :namespaces, :version, :string
    add_column :libraries, :version, :string
  end

  def self.down
    remove_column :functions, :version
    remove_column :namespaces, :version
    remove_column :libraries, :version
  end
end
