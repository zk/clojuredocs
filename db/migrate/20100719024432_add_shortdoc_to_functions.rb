class AddShortdocToFunctions < ActiveRecord::Migration
  def self.up
    add_column :functions, :shortdoc, :string, :limit => 70
    
    Function.find(:all).each do |f|
      if(f.doc)
        f.shortdoc = f.doc[0, 69]
      else
        f.shortdoc = ""
      end
      
      f.save
    end
  end

  def self.down
    remove_column :functions, :shortdoc
  end
end
