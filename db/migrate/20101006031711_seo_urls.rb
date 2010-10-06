class SeoUrls < ActiveRecord::Migration
  def self.up
    add_column :libraries, :url_friendly_name, :string
    add_column :functions, :url_friendly_name, :string
  end

  def self.down
    remove_column :libraries, :url_friendly_name
    remove_column :functions, :url_friendly_name
  end
end
