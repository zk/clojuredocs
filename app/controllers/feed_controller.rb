class FeedController < ApplicationController
  
  def recent_updates
    
    @recent = (Comment.find(:all, :limit => 50, :order => 'updated_at DESC') + 
               Example.find(:all, :limit => 50, :order => 'updated_at DESC') +
               SeeAlso.find(:all, :limit => 50, :order => 'updated_at DESC')).sort{|a,b| b.updated_at <=> a.updated_at}[0, 50]
    
    render :layout => false
  end
end
