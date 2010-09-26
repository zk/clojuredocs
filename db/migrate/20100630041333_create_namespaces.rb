class CreateNamespaces < ActiveRecord::Migration
  def self.up
    create_table :namespaces do |t|
      t.string :name
      t.text :doc
      t.string :source_url
      t.string :repo_host
      t.timestamps
    end
  end

  def self.down
    drop_table :namespaces
  end
end
