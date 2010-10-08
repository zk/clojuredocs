class SeoUrls < ActiveRecord::Migration
  def self.up
    add_column :libraries, :url_friendly_name, :string
    add_column :functions, :url_friendly_name, :string
    
    
    #Dosen't seem to matter in the other migrations, but this is req'd for save to work
    Library.reset_column_information
    Function.reset_column_information
    
    Library.find(:all).each do |l|
      l.url_friendly_name = l.name.gsub("?","_q").gsub("/","_").gsub(" ","_").downcase
      l.save
    end
    
    Function.find(:all).each do |f|
      f.url_friendly_name = f.name.gsub("?","_q").gsub("/","_").gsub(" ","_").downcase
      f.save
    end
    
  end

  def self.down
    remove_column :libraries, :url_friendly_name
    remove_column :functions, :url_friendly_name
  end
end
