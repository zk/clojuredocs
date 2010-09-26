class CommentsController < ApplicationController
  
  def delete
    
    flash[:message] = "There was a problem deleting that comment."
    
    if current_user_session
      @example = Comment.find_by_id_and_user_id(params[:id], current_user.id)
      if @example
        if @example.delete
          flash[:message] = "Comment successfully deleted."
        end
      end
    end
    
    redirect_back_or_default "/"
  end
  
end
