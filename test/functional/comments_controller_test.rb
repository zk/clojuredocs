require 'test_helper'
require "authlogic/test_case"

class CommentsControllerTest < ActionController::TestCase

  include Authlogic::TestCase
  setup :activate_authlogic
  
  context "Deleting a comment" do
    context "with the correct user logged in, and a valid comment id" do
      setup do
        UserSession.create(users(:zkim))
        get :delete, :id => 1
      end
      
      should respond_with 302
      should_set_the_flash_to "Comment successfully deleted."
    end
    
    context "without a logged in user" do
      setup do
        get :delete, :id => 1
      end
      
      should respond_with 302
      should_set_the_flash_to "There was a problem deleting that comment."
    end
    
    context "without a valid comment id" do
      setup do
        get :delete, :id => 999
      end
      
      should respond_with 302
      should_set_the_flash_to "There was a problem deleting that comment."
    end
  end
end
