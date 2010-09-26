# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
  protect_from_forgery # See ActionController::RequestForgeryProtection for details

  filter_parameter_logging :password, :password_confirmation
  helper_method :current_user_session, :current_user

  private
  
  def json_fail(message)
    return {:json => {:success => false, :message => message}}
  end
  
  def current_user_session
    return @current_user_session if defined?(@current_user_session)
    @current_user_session = UserSession.find
  end

  def current_user
    return @current_user if defined?(@current_user)
    @current_user = current_user_session && current_user_session.user
  end

  def redirect_back_or_default(path)
    redirect_to :back
    rescue ActionController::RedirectBackError
    redirect_to path
  end
  
  def find_recently_updated(size, lib = nil)
    recent = []
    
    comments = []
    examples = []
    see_alsos = []
    
    if not lib
      comments = Comment.find(:all, :limit => size, :order => 'updated_at DESC')
      examples = Example.find(:all, :limit => size, :order => 'updated_at DESC')
      see_alsos = SeeAlso.find(:all, :limit => size, :order => 'updated_at DESC')
    else
      comments = Comment.find(:all, 
        :joins => "INNER JOIN functions ON comments.commentable_id = functions.id",
        :conditions => ["functions.library = ?", lib],
        :limit => size, 
        :order => 'updated_at DESC')
      examples = Example.find(:all, 
        :joins => "INNER JOIN functions ON examples.function_id = functions.id",
        :conditions => ["functions.library = ?", lib],
        :limit => size, 
        :order => 'updated_at DESC')
      see_alsos = SeeAlso.find(:all, 
        :joins => "INNER JOIN functions ON see_alsos.from_id = functions.id",
        :conditions => ["functions.library = ?", lib],
        :limit => size, 
        :order => 'updated_at DESC')
    end
    
    recent = (comments + examples + see_alsos).sort{|a,b| b.updated_at <=> a.updated_at}
      
    if recent.size > size
      recent = recent[0, size]
    end
    
    recent
  end
  
  # Scrub sensitive parameters from your log
  # filter_parameter_logging :password
end
