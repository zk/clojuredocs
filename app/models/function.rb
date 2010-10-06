class Function < ActiveRecord::Base
  has_many :examples
  has_many :comments
  has_and_belongs_to_many :source_references, 
    :class_name => "Function", 
    :join_table => "function_references", 
    :foreign_key => "from_function_id",
    :association_foreign_key => "to_function_id"
    
  has_and_belongs_to_many :used_in, 
    :class_name => "Function", 
    :join_table => "function_references", 
    :foreign_key => "to_function_id",
    :association_foreign_key => "from_function_id"
    
  has_many :see_alsos, :foreign_key => "from_id"
  
  acts_as_commentable
  
  #:nocov:
  define_index do
    # fields
    indexes "REPLACE(name, '-', '')", :as => :name
    set_property :enable_star => true
    set_property :min_prefix_len => 1
    #indexes name
    indexes doc
    indexes library
    indexes ns
  end
  #:nocov:
  
  def arglists
    self.arglists_comp.split '|'
  end
  
  def update_weight
    self.weight = examples.count
  end
  
  def href
    "/v/#{id}"
  end
  
  def see_alsos_sorted
    see_alsos.sort{|a,b| b.vote_score <=> a.vote_score}
  end
  
  def self.libraries
    Function.find(:all, :select => 'distinct(library),library').map(&:library).sort
  end
  
  def self.in_library(lib)
    Function.find(:all, :conditions => {:library => lib.name, :version => lib.version}, :select => 'library,ns,name, weight, id', :order => 'name ASC, weight DESC')
  end
  
  def self.in_library_and_ns(lib, ns)
    Function.find(:all, :conditions => {:library => lib.name, :ns => ns, :version => lib.version}, :order => 'name ASC, weight DESC')
  end
  
  def self.versions_of(function)
    Function.find(:all, :conditions => {:library => function.library,
                                        :ns => function.ns,
                                        :name => function.name})
  end
end
