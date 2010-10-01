require 'test_helper'
require "authlogic/test_case"

class ExamplesControllerTest < ActionController::TestCase

  include Authlogic::TestCase
  setup :activate_authlogic
  
  context "creating examples" do
    context "with valid parameters" do
      setup do
        UserSession.create(users(:zkim))
        get :new, :var_id => 1, :example => {:body => "hello world"}
      end
      
      should_have_json_prop 'success', true
      should "have the correct values for the returned example" do
        assert_equal json_resp["example"]["body"], "hello world"
        assert_equal json_resp["example"]["user_id"], 1
        assert_equal json_resp["example"]["function_id"], 1
        assert json_resp['content']
      end
    end
    
    context "with an invalid var id" do
      setup do
        get :new, :var_id => 999
      end
      
      should_have_json_prop 'success', false
    end
    
    context "with no user logged in" do
      setup do
        get :new, :var_id => 1
      end
      
      should_have_json_prop 'success', false
    end
    
    context "with no body" do
      setup do
        UserSession.create(users(:zkim))
        get :new, :var_id => 1
      end
      
      should_have_json_prop 'success', false
    end
    
    context "with an empty body" do
      setup do
        UserSession.create(users(:zkim))
        get :new, :var_id => 1, :example => {:body => ""}
      end
      
      should_have_json_prop 'success', false
    end
  end
  
  context "updating examples" do
    context "with valid info" do
      setup do
        UserSession.create(users(:zkim))
        post :update, :example_id => 1, :example => {:body => "hello world"}
      end
          
      should_have_json_prop 'success', true
    end
    
    context "without a logged-in user" do
      setup do
        post :update
      end
      
      should_have_json_prop 'success', false
    end
    
    context "missing an example" do
      setup do
        UserSession.create(users(:zkim))
        post :update
      end
      
      should_have_json_prop 'success', false
    end
    
    context "missing an example body" do
      setup do
        UserSession.create(users(:zkim))
        post :update, :example_id => 1
      end
      
      should_have_json_prop 'success', false
    end
    
    # context "logged-in user isn't the example's owner" do
    #       setup do
    #         UserSession.create(users(:marick))
    #         post :update, :example_id => 1, :example => {:body => 'hello world'}
    #       end
    #       
    #       should_have_json_prop 'success', false
    #     end
  end
  
  context "viewing changes" do
    context "with a valid examples id" do
      setup do
        get :view_changes, :id => 1
      end

      should assign_to :example
      should assign_to :versions
      should render_template :view_changes
      should respond_with :success
    end
    
    context "with a missing or invalid examples id" do
      setup do
        get :view_changes, :id => 999999
      end
      
      should set_the_flash
      should redirect_to "/"
    end
    
  end

  context "deleting examples" do
    context "with correct owner logged in" do
      setup do
        UserSession.create(users(:zkim))
        get :delete, :id => 1
      end

      should_have_json_prop 'success', true
    end
    
    context "without an example" do
      setup do
        get :delete
      end

      should_have_json_prop 'success', false
    end

    context "without a current user" do
      setup do
        get :delete, :id => 1
      end

      should_have_json_prop 'success', false
    end
    
    context "with the incorrect owner" do
      setup do
        UserSession.create(users(:marick))
        get :delete, :id => 1
      end

      should_have_json_prop 'success', false
    end
  end
end
