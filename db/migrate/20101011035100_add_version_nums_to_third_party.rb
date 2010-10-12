class AddVersionNumsToThirdParty < ActiveRecord::Migration
  
  def self.set_lib_version(lib_name, version)
    lib = Library.find_by_name(lib_name)
    if not lib
      puts "Couldn't find #{lib_name}"
      return
    end
    lib.version = version
    lib.current = true
    lib.namespaces.each do |n|
      n.functions.each do |f|
        f.version = lib.version
        f.save
      end
      
      n.version = lib.version
      n.save
    end
    lib.save
    puts "Finished library #{lib_name}"
  end
  
  def self.up
    set_lib_version "circumspec", "0.0.12"
    set_lib_version "clj-sandbox", "0.4.0"
    set_lib_version "clj-ssh", "0.2.0-SNAPSHOT"
    set_lib_version "clj-swing", "0.1.0-SNAPSHOT"
    set_lib_version "enlive", "1.0.0-SNAPSHOT"
    set_lib_version "incanter", "1.2.3-SNAPSHOT"
    set_lib_version "leiningen", "1.2.0"
    set_lib_version "midje", "0.1.1"
    set_lib_version "pallet", "0.2.0-SNAPSHOT"
    set_lib_version "ring", "0.2.3"
    set_lib_version "swank-clojure", "1.2.0"
    set_lib_version "trammel", "0.3.2"
  end

  def self.down
  end
end
