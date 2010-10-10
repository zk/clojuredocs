class Library < ActiveRecord::Base
  
  has_many :namespaces
  
  #:nocov:
  define_index do
    indexes :name
    indexes :description
  end
  #:nocov:
  
  def self.versions(name)
    Library.find_all_by_name(name).map{|l| l.version}
  end
  
  def self.versions_of(library)
    Library.find(:all, :conditions => {:name => library.name})
  end
  
  def self.current_version_of(library)
    Library.find_by_name_and_current(library.name, true)
  end
  
end
