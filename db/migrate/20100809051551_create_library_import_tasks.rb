class CreateLibraryImportTasks < ActiveRecord::Migration
  def self.up
    create_table :library_import_tasks do |t|
      t.integer :library_id
      t.integer :vars_found
      t.integer :references_found
      t.integer :vars_removed
      t.string  :status
      t.timestamps
    end
  end

  def self.down
    drop_table :library_import_tasks
  end
end
