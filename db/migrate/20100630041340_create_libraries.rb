class CreateLibraries < ActiveRecord::Migration
  def self.up
    create_table :libraries do |t|
      t.string :name
      t.string :description
      t.string :site_url
      t.string :source_base_url
      t.timestamps
    end
  end

  def self.down
    drop_table :libraries
  end
end
