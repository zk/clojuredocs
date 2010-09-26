class ManagementController < ApplicationController
  
  layout 'management'
  require 'chronic'
  
  def index
    @lib = Library.find(:first, :conditions => {:name => params[:lib]})
    @recently_updated = find_recently_updated(100, @lib.name)
  end
  
  def search
    @lib = Library.find(:first, :conditions => {:name => params[:lib]})
    
    low = Chronic.parse(params[:low_date]).utc rescue nil 
    high = Chronic.parse(params[:high_date]).utc rescue nil
    
    @results = Example.find(:all, :conditions => ["updated_at > ? and updated_at < ?", low, high], :order => 'updated_at DESC')
    @results = @results.collect{|e| e.function}.select{|f| f.ns =~ /#{params[:namespaces]}/}
  end
  
  def function
    @function = Function.find_by_id(params[:id])
    @library = Library.find_by_name(@function.library)
    if not @function
      render :text => "Couldn't find var"
      return
    end
    
    render :layout => false
  end
end
