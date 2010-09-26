class CreateLibraryImportLogs < ActiveRecord::Migration
  def self.up
    create_table :library_import_logs do |t|
      t.string :library_import_task_id
      t.string :level
      t.text :message
      t.column :created_at, :datetime, :null => false
    end
  end

  def self.down
    drop_table :library_import_logs
  end
end
