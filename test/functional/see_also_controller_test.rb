require 'test_helper'

class SeeAlsoControllerTest < ActionController::TestCase
  
  include Authlogic::TestCase
  setup :activate_authlogic
  
  context "voting on see alsos" do
    context "when not logged in" do
      setup do
        get :vote
      end

      should "respond with a json success: false" do
        resp = JSON.parse(@response.body)
        assert_equal resp["success"], false
      end
    end
    
    context "when logged in" do
      context "with the owner" do
        setup do
          UserSession.create(users(:zkim))
          get :vote, :id => see_alsos(:map_to_dummy).id
        end
        
        should_have_json_prop 'success', false
      end
      
      context "with a non-existent see also" do
        setup do
          UserSession.create(users(:zkim))
          get :vote, :id => 999999
        end
        
        should_have_json_prop 'success', false
      end
      
      context "with a non-owner" do
        context "no vote" do
          setup do
            UserSession.create(users(:marick))
            get :vote, :id => see_alsos(:map_to_dummy).id
          end

          should_have_json_prop 'success', false
        end
        
        context "already voted" do
          setup do
            UserSession.create(users(:zkim))
            get :vote, :id => see_alsos(:map_to_dummy).id, :vote_action => "vote_up"
          end
          
          should_have_json_prop 'success', false
        end
        
        context "vote up" do
          setup do
            UserSession.create(users(:marick))
            get :vote, :id => see_alsos(:map_to_dummy).id, :vote_action => "vote_up"
          end
          
          
          should_have_json_prop 'success', true
          should_have_json_prop 'vote_score', 1
        end
        
        context "vote down" do
          setup do
            UserSession.create(users(:marick))
            get :vote, :id => see_alsos(:map_to_dummy).id, :vote_action => "vote_down"
          end
          
          
          should_have_json_prop 'success', true
          should_have_json_prop 'vote_score', -1
        end
      end
    end
  end
  
  context "looking up vars for voting see also" do
    context "with a missing query" do
      setup do
        get :lookup
      end
      
      should "respond with an empty json list" do
        assert_equal @response.body, "[]"
      end
    end
    
    context "with a no-match query" do
      setup do
        get :lookup, :q => 'no-match'
      end
      
      should "respond with an empty json list" do
        assert_equal @response.body, "[]"
      end
    end
    
    context "with a query matching an existing var name" do
      setup do
        get :lookup, :term => 'ma'
      end

      should "respond with the map var" do
        resp = JSON.parse(@response.body)
        assert_equal resp[0]["name"], "map"
      end
    end
  end
  
  context "deleting see alsos" do
    #sucessful deletion
    setup do
      UserSession.create(users(:zkim))
      get :delete, :id => see_alsos(:map_to_dummy).id
    end
    
    should_have_json_prop 'success', true
    
    #error conditions
    context "with a missing id" do
      setup do
        get :delete
      end
      
      should_have_json_prop 'success', false
    end

    context "with a non-existant id" do
      setup do
        get :delete, :id => 999999
      end
      
      should_have_json_prop 'success', false
    end

    context "with a user that dosen't own the see also" do
      setup do
        UserSession.create(users(:marick))
        get :delete, :id => 1
      end
      
      should_have_json_prop 'success', false
    end
  end
  
  context "adding see alsos" do
    setup do
      UserSession.create(users(:zkim))
      get :add, :var_id => functions(:clojure_core_dummy).id, :v => "clojure.core/+"
    end
    
    
    should_have_json_prop 'success', true
    should "respond with success:true and the to_var" do
      resp = JSON.parse(@response.body)
      assert_equal resp["to_var"]["name"], "+"
      assert resp["content"]
    end
    
    #error conditions
    context "without a current user session" do
      setup do
        get :add
      end
      
      should "respond with success:false" do
        resp = JSON.parse(@response.body)
        assert_equal resp["success"], false
      end
    end
    
    context "without a from var" do
      setup do
        UserSession.create(users(:zkim))
        get :add
      end
      
      should "respond with success:false" do
        resp = JSON.parse(@response.body)
        assert_equal resp["success"], false
      end
    end
    
    context "without a to var" do
      setup do
        UserSession.create(users(:zkim))
        get :add, :var_id => functions(:clojure_core_dummy).id
      end
      
      should "respond with success:false" do
        resp = JSON.parse(@response.body)
        assert_equal resp["success"], false
      end
    end
    
    context "with a badly formatted to var" do
      setup do
        UserSession.create(users(:zkim))
        get :add, :var_id => functions(:clojure_core_dummy).id, :v => "asdf asdf adsf"
      end
      
      should "respond with success:false" do
        resp = JSON.parse(@response.body)
        assert_equal resp["success"], false
      end
    end
    
    context "With an already existent see also relationship" do
      setup do
        UserSession.create(users(:zkim))
        get :add, :var_id => functions(:map).id, :v => "dummy.ns/dummyname"
      end
      
      should "respond with success:false" do
        resp = JSON.parse(@response.body)
        assert_equal resp["success"], false
      end
    end

  end
end
