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
    indexes version
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
    Function.find(:all, :conditions => {:library => function.library_.name,
                                        :ns => function.ns,
                                        :name => function.name})
  end
  
  # not ready to make the leap to a has_many / belongs_to yet, so this
  # will have to do for now
  def library_
    Library.find_by_name_and_version(library, version)
  end

  def link_opts(use_current_vs_actual_version = true)
    {:controller => 'main',
     :action     => 'function',
     :lib        => library_.url_friendly_name,
     :version    => (use_current_vs_actual_version && library_.current ? nil : version),
     :ns         => ns,
     :function   => url_friendly_name}
  end
  
  def all_versions_examples
    Function.versions_of(self).reduce([]) do |coll, f|
      coll + f.examples
    end
  end
  
  def all_versions_see_alsos
    Function.versions_of(self).reduce([]) do |coll, f|
      coll + f.see_alsos
    end.sort{|a,b| b.vote_score <=> a.vote_score}
  end
  
  def stable_version
    stable_lib = Library.find_by_name_and_current(library, true)
    if not stable_lib
      return nil
    end
    
    Function.find(:first, :conditions => {:library => library,
                                          :ns => ns,
                                          :name => name,
                                          :version => stable_lib.version})
  end
end
