class AddVersions < ActiveRecord::Migration
  
  def self.up
    
    add_column :functions, :version, :string
    add_column :namespaces, :version, :string
    add_column :libraries, :version, :string
    add_column :libraries, :current, :boolean
    
    cclib = Library.find_by_name("Clojure Core")
    cclib.version = "1.2.0"
    cclib.current = true
    cclib.save
    
    cont_lib = Library.find_by_name("Clojure Contrib")
    cont_lib.version = "1.2.0"
    cont_lib.current = true
    cont_lib.save
    
    Function.find_all_by_library("Clojure Core").each do |f|
      f.version = "1.2.0"
      f.save
    end
    
    Function.find_all_by_library("Clojure Contrib").each do |f|
      f.version = "1.2.0"
      f.save
    end
    
    Namespace.find(:all, :conditions => ["name LIKE ?", "clojure.%"]).each do |n|
      n.version = "1.2.0"
      n.save
    end
  end

  def self.down
    remove_column :functions, :version
    remove_column :namespaces, :version
    remove_column :libraries, :version
    remove_column :libraries, :current
  end
end
