class CreateExamples < ActiveRecord::Migration
  def self.up
    create_table :examples do |t|
      t.text :body
      t.string :author
      t.string :email
      t.integer :function_id
      t.timestamps
    end
  end

  def self.down
    drop_table :examples
  end
end
