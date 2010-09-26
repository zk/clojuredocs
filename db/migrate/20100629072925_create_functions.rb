class CreateFunctions < ActiveRecord::Migration
  def self.up
    create_table :functions do |t|
      t.string  :library
      t.string  :ns
      t.string  :name
      t.string  :file
      t.string  :line
      t.string  :arglists_comp
      t.string  :added
      t.text    :doc
      t.text    :source
      t.integer :weight, :default => 0
      t.timestamps
    end
    
    create_table "function_references", :force => true, :id => false do |t|
        t.column "from_function_id", :integer
        t.column "to_function_id", :integer
    end
  end

  def self.down
    drop_table :functions
    drop_table :function_references
  end
end
