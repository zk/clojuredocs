class UserSessionsController < ApplicationController
  layout 'main', :only => [:new, :create_new_openid_user, :migrate_existing]

  def new
    @user_session = UserSession.new

    if request.post?
      
      if params[:user_session] and params[:user_session][:openid_identifier] and not params[:user_session][:openid_identifier].match(/https?:\/\//)
        params[:user_session][:openid_identifier] = "http://" + params[:user_session][:openid_identifier]
      end

      if (params[:user_session] && params[:user_session][:openid_identifier]) or params[:for_session] 

        @user_session = UserSession.new(params[:user_session])

        if @user_session.openid_identifier
          session[:openid_identifier] = @user_session.openid_identifier
        end

        @user_session.save do |result|
          if result
            flash[:notice] = "Login successful!"
            redirect_to "/"
          elsif @user_session.errors[:openid_identifier].empty?
            redirect_to :action => 'create_new_openid_user', :openid_identifier => session[:openid_identifier]
          end
        end
      else
        @user_session = UserSession.create(params[:user_session])
        if @user_session.save
          redirect_to "/login/migrate"
        else
          flash.now[:migrate_error] = "Couldn't find a user with that login / password combination."
        end
      end

    end
  end

  def create

  end

  def create_new_openid_user

    @user = User.new

    if request.post?
      @user = User.new(params[:user])

      @user.save do |result|
        if result
          redirect_to "/"
          return
        end

        #        raise @user.to_yaml
      end
    end

  end

  def destroy
    if current_user_session
      current_user_session.destroy
    end
    flash[:notice] = "Logout successful!"
    redirect_back_or_default "/"
  end

  def migrate_existing

    @user = User.new

    if request.post?
      @user = current_user

      if params[:user_session] and params[:user_session][:openid_identifier]
        @user.openid_identifier = params[:user_session][:openid_identifier]
      end

      @user.save do |result|
        if result
          flash[:notice] = "Successfully updated profile."
          #current_user.crypted_password = nil
          #current_user.password_salt = nil
          #current_user.save
          redirect_to root_url
          return
        else

        end
      end
    end

  end

  def login_to_migrate
    @user_session = UserSession.create(params[:user_session])
    if @user_session.save
      redirect_to "/login/migrate"
    else
      redirect_back_or_default "/"
    end
  end
end
