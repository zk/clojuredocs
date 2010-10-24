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
    
    examples = @user.examples
    examples_map = @user.examples.reduce({}) {|coll, e| coll.merge({e.function_id => e})}
    Example.find_by_sql(["select * from example_versions where user_id = ?", @user.id]).each do |e|
      if not examples_map[e.function_id]
        examples << e
        examples_map = examples_map.merge({e.function_id => e})
      end
    end
        
    @recent = (@user.comments + examples + @user.see_alsos).uniq.sort{|a,b| b.updated_at <=> a.updated_at}
  end
end
