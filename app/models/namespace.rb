class Namespace < ActiveRecord::Base
  belongs_to :library
  has_many :functions
  
  def self.versions_of(ns)
    Namespace.find(:all, :conditions => {:name => ns.name})
  end
  
end
