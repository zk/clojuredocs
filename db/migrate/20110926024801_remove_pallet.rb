class RemovePallet < ActiveRecord::Migration
  def self.up
    lib = Library.find_by_name("pallet")

    return if not lib
    
    lib.namespaces.each do |ns|
      ns.functions.each do |f|
        f.delete
      end
      ns.delete
    end
    lib.delete
    
  end

  def self.down
  end
end
