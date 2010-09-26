class ExamplesController < ApplicationController

  layout "main"

  def new
    
    var_id = params[:var_id]
    body = params[:example][:body] rescue nil
    
    @function = Function.find_by_id var_id
    if not @function
      render json_fail "Couldn't find the var you'd like to add an example to."
      return
    end

    if not current_user_session
      render json_fail "You must be logged in to create an example."
      return
    end

    if (not body) or body == ""
      render json_fail "There was a problem saving your example, is it blank?"
      return
    end
    
    @example = Example.new
    @example.body = body
    @example.user = current_user
    
    if not @example.valid?
      render :json => {:success => false, :message => "Invalid example.", :errors => @example.errors}
      return false
    end
    
    @function.examples << @example
    @function.update_weight
    @function.save
    
    render :json => {:success => true, 
                     :message => "Example added.", 
                     :example => {:body => @example.body,
                                  :function_id => @example.function_id,
                                  :id => @example.id,
                                  :user_id => @example.user_id},
                     :content => render_to_string(:partial => "example", :locals => {:e => @example})}
  end
  
  def update
    
    example_id = params[:example_id]
    body = params[:example][:body] rescue nil
    
    if not current_user_session
      render json_fail "You must be logged in to update an example."
      return
    end
    
    @example = Example.find_by_id(example_id)
    if not @example
      render json_fail "There was a problem finding the example you were trying to update."
      return
    end
    
    if not body or body == ""
      render json_fail "There was a problem updating your example, is it blank?"
      return
    end
    
    # if not @example.user == current_user
    #       render json_fail "Sorry, you don't own the example you were trying up update."
    #       return
    # end
    
    @example.body = body
    @example.user = current_user
    
    if not @example.save
      render json_fail "There was a problem saving the example you updated."
      return
    end
    
    render :json => {:success => true,
                     :message => "Example updated.",
                     :example => {:body => @example.body,
                                  :function_id => @example.function_id,
                                  :id => @example.id,
                                  :user_id => @example.user_id},
                     :content => render_to_string(:partial => "example", :locals => {:e => @example})}
  end

  def delete

    id = params[:id]

    if not id
      render json_fail "Couldn't find the var you'd like to add an example to."
      return
    end

    if not current_user_session
      render json_fail "You must be logged in to delete an example."
      return
    end

    @example = Example.find_by_id_and_user_id(params[:id], current_user.id)
    if not @example
      render json_fail "Sorry, that example dosen't exist anymore, or you don't own it."
      return
    end

    @example.delete
    render :json => {:success => true, :message => "Example deleted."}
  end

  def view_changes

    id = params[:id] || 0

    @example = Example.find_by_id(id)
    if not @example
      flash[:message] = "Couldn't find the example you're looking for."
      redirect_back_or_default "/"
      return
    end

    @versions = @example.versions.reverse
  end
end
