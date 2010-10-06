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
  
end
