class AddIndexesToLibNsFunction < ActiveRecord::Migration
  def self.up
    add_index :functions, :namespace_id, :name => 'namespace_id_idx'
    add_index :namespaces, :library_id, :name => 'library_id_idx'
    add_index :examples, :function_id, :name => 'function_id_idx'
    add_index :see_alsos, :from_id, :name => 'from_id_idx'
    add_index :see_alsos, :to_id, :name => 'to_id_idx'
  end

  def self.down
  end
end
