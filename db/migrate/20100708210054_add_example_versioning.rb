class AddExampleVersioning < ActiveRecord::Migration
  def self.up
    Example.create_versioned_table
    
    examples = Example.find(:all)
    examples.each do |e|
      e.updated_at = Time.now
      e.save
    end
  end

  def self.down
    Example.drop_versioned_table
  end
end
