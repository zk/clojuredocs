class SeeAlsoController < ApplicationController
 
  def vote
    
    if not current_user_session
      render json_fail("No valid user session.")
      return
    end
    
    id = (params[:id] || 0)
    sa = SeeAlso.find_by_id(id)
    
    # bad id passed
    if not sa
      render json_fail("Couldn't find that see also.")
      return
    end
    
    # creator of see also is current user
    if sa.user == current_user
      render json_fail("You can't vote on your own see also.")
      return false
    end
    
    # current user already voted
    if sa.has_voted?(current_user)
      render json_fail("Current user has already voted.")
      return
    end
    
    vote_action = params[:vote_action]
    # no vote action
    if not vote_action
      render json_fail("No vote action found.")
      return
    end
    
    direction = (vote_action == "vote_up")    
    sa.votes << Vote.new(:vote => direction, :user => current_user)
    sa.save
    
    render :json => {:success => true, :vote_score => sa.vote_score}
  end
  
  def lookup
    q = params[:term]
    
    if not q
      render :json => []
      return
    end
    
    name = q + "%"
    @functions = Function.find(:all, :conditions => ['name like ?', name]).sort{|a,b| Levenshtein.distance(q, a.name) <=> Levenshtein.distance(q, b.name)}
    
    if @functions.size > 10
      @functions = @functions[0, 10]
    end
    
    render :json => @functions.map{|f| {:href => f.href, :ns => f.ns, :name => f.name, :examples => f.examples.size, :shortdoc => f.shortdoc }}
  end
  
  def delete
    id = params[:id]
    
    if not id
      render json_fail "No see also specified to delete."
      return
    end
    
    sa = SeeAlso.find_by_id(id)
    if not sa
      render json_fail "No see also found."
      return
    end
    
    if not sa.user == current_user
      render json_fail "You don't own that see also."
      return
    end
    
    if not sa.delete
      render json_fail "Unknown error deleting that see also."
      return
    end
    
    render :json => {:success => true}
  end
  
  def add
    
    if not current_user_session
      redner json_fail "Must be logged in to add see alsos."
      return
    end
    
    id = params[:var_id]
    from_var = Function.find_by_id(id)
    
    if not from_var
      render json_fail "Couldn't find from var."
      return
    end
    
    to_var_fqn = params[:v]
    if not to_var_fqn
      render json_fail "To var not specified."
      return
    end
    
    split = to_var_fqn.split("/")
    ns = split[0]
    name = split[1]

    to_var = Function.find_by_ns_and_name(ns, name)
    
    if not to_var
      render json_fail("Couldn't find to var.")
      return
    end
    
    #if see also already exists between the two vars
    if from_var.see_alsos.map{|s| s.to_function}.index(to_var)
      render json_fail "See also already exists."
      return
    end

    sa = SeeAlso.new
    sa.user = current_user
    sa.to_function = to_var
    from_var.see_alsos << sa
    from_var.save
    
    render :json => {
      :success => true, 
      :to_var => {
        :sa_id => sa.id, 
        :name => to_var.name, 
        :ns => to_var.ns, 
        :href => to_var.href, 
        :shortdoc => to_var.shortdoc
      },
      :content => render_to_string(:partial => "see_also_content", :locals => {:sa => sa})
    }
    
  end
  
end
