class Library < ActiveRecord::Base
  has_many :namespaces

  #:nocov:
  define_index do
    indexes :name
    indexes :description
  end
  #:nocov:
  
end
