class Namespace < ActiveRecord::Base
  belongs_to :library
  has_many :functions
  
  def self.versions_of(ns)
    Namespace.find(:all, :include => :library, :conditions => {:name => ns.name, :libraries => {:name => ns.library.name}})
  end
  
  def link_opts(use_current_vs_actual_version = true)
    {:controller => 'main',
     :action     => 'ns',
     :lib        => library.url_friendly_name,
     :version    => (use_current_vs_actual_version && library.current ? nil : library.version),
     :ns         => name}
  end
end
