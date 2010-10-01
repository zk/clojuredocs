require 'test_helper'

class UserControllerTest < ActionController::TestCase
  
  include Authlogic::TestCase
  setup :activate_authlogic
  
  should_succeed :profile, :login => 'zkim'
  
  context "With a non-existant login" do
    setup do
      get :profile, :login => 'not found'
    end
    
    should respond_with 404
  end
  
  context "A login with a space in it" do
    setup do
      get :profile, :login => 'Brian Marick'
    end
    
    should respond_with :success
  end
  
  context "Where the current user and the profile page user are the same" do
    setup do

      UserSession.create(users(:zkim))
      get :profile, :login => 'zkim'
    end
    
    should respond_with :success
  end
  
  context "updating a user with a valid email" do
    setup do
      UserSession.create(users(:zkim)).user
      post :profile, :login => "zkim", :user => {:email => "new_email@asdf.com"}
    end
    
    should respond_with :success
    should assign_to :user
    should "update the email address correctly" do
      assert_equal User.find(1).email, "new_email@asdf.com"
    end
  end
  
  context "updating a user with an invalid email" do
    setup do
      UserSession.create(users(:zkim)).user
      post :profile, :login => "zkim", :user => {:email => "bob"}
    end
    
    should respond_with :success
    should assign_to :user
    should "not update the email address" do
      assert_equal User.find(1).email, "zachary.kim@gmail.com"
    end
  end
end
