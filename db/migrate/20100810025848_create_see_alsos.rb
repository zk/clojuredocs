class CreateSeeAlsos < ActiveRecord::Migration
  def self.up
    create_table :see_alsos do |t|
      t.integer :from_id
      t.integer :to_id
      t.integer :user_id
      t.integer :weight, :default => 0
      t.timestamps
    end
  end

  def self.down
    drop_table :see_alsos
  end
end
