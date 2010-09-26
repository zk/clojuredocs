class UserController < ApplicationController
  
  layout 'main'
  
  def profile
    
    @user = User.find_by_login(params[:login])
    
    if request.post? and current_user_session and @user.id == current_user.id
      current_user.update_attributes params[:user]
      if current_user.valid?
        current_user.save!
        flash.now[:message] = "Successfully updated your profile."
        @user = current_user
      else
        current_user.email = @user.email
      end
    end
    
    if not @user
      render :template => 'public/404.html', :layout => false, :status => 404
      return
    end
    
    @recent = (@user.comments + @user.examples + @user.see_alsos)
  end
end
