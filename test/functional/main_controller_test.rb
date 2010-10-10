require 'test_helper'

require 'flexmock/test_unit'

class MainControllerTest < ActionController::TestCase
  
  
  include Authlogic::TestCase
  setup :activate_authlogic
    
  #see test_helper for should_succeed definition
  should_succeed :index
  should_succeed :libs

  context "The function short link action" do
    setup do
      get :function_short_link, {:id => 1}
    end
    
    should respond_with 302 
    
    context "with an invalid function" do
      setup do
        get :function_short_link, :id => -1
      end
      
      should respond_with 404
    end
  end
  
  context "the ns page" do
    should_succeed :ns, {:lib => 'clojure_core', :ns => 'clojure.core'}
    
    context "with an invalid library" do
      setup do
        get :ns, :lib => 'not found', :ns => 'clojure.core'
      end
      
      should respond_with 404
    end
    
    context "with a valid library and an invalid ns" do
      setup do
        get :ns, :lib => 'clojure_core', :ns => 'not found'
      end
      
      should respond_with 404
    end
  end
  
  context "The library page" do
    should_succeed :lib, :lib => "clojure_core"
    
    context "with a library not in the database" do
      setup do
        get :lib, :lib => "not found"
      end
      
      should respond_with 404
    end
  end
  
  context "the quickref pages" do
    should_succeed :quick_ref_shortdesc, :lib => 'Clojure Core'
    context "with a library other than Clojure Core" do
      setup do
        get :quick_ref_shortdesc, :lib => 'not found'
      end
      
      should respond_with 404
    end
    
    should_succeed :quick_ref_vars_only, :lib => 'Clojure Core'
    context "with a library other than Clojure Core" do
      setup do
        get :quick_ref_vars_only, :lib => 'not found'
      end
      
      should respond_with 404
    end
  end
  
  # context "searching" do
  #     context "for the query 'map'" do
  #       setup do
  #         class Array
  #           def total_entries
  #             1
  #           end
  #         end
  #         flexmock(Function).should_receive(:search).once.with_any_args.and_return([functions(:map)])
  #         get :search
  #       end
  #       
  #       should respond_with :success
  #     end
  #   end
end
