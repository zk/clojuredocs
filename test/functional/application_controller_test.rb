require 'test_helper'

class ApplicationControllerTest < ActionController::TestCase
  
  include Authlogic::TestCase
  setup :activate_authlogic

  context "getting the current user" do
    context "when a user is logged in" do
      setup do
        UserSession.create(users(:zkim))
        @app_controller = ApplicationController.new
      end
      
      should "return the current user" do
        assert_not_nil @app_controller.send(:current_user_session)
        assert_equal @app_controller.send(:current_user).login, "zkim"
      end
    end
    
    context "when a user is not logged in" do
      setup do
        @app_controller = ApplicationController.new
      end
      
      should "return nil" do
        assert_nil @app_controller.send :current_user_session
        assert_nil @app_controller.send :current_user
      end
    end
    
  end
  
  context "getting recently updated items" do
    setup do
      @app_controller = ApplicationController.new
    end
    
    context "from all libraries" do
      should "give recent updates of the types SeeAlso, Example, Comment" do
        recent = @app_controller.send(:find_recently_updated, 10)
        assert_equal recent.size, 4
        assert recent[0].instance_of? SeeAlso
        assert recent[1].instance_of? Example
        assert recent[2].instance_of? Comment
      end
    end
    
  end
end
