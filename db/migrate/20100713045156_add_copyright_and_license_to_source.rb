class AddCopyrightAndLicenseToSource < ActiveRecord::Migration
  def self.up
    add_column :libraries, :copyright, :string
    add_column :libraries, :license, :string
    
    Library.find(:all).each do |l|
      l.copyright = "&copy; #{l.name}."
      l.save
    end
  end

  def self.down
    remove_column :libraries, :copyright
  end
end
