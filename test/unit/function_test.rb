require 'test_helper'

class FunctionTest < ActiveSupport::TestCase
  should have_many(:examples)
  should have_and_belong_to_many(:source_references)
  should have_and_belong_to_many(:used_in)
  
  context "A function" do
    setup do
      @f = Function.find(1)
    end
    
    should "calculate it's href correctly" do
      assert_equal '/v/1', @f.href
    end
  end
  
  context "Getting all libraries" do
    setup do
      @ls = Function.libraries
    end
    
    should "find 1 library" do
      assert_equal 2, @ls.size
    end
  end
  
  context "Getting functions in library Clojure Core" do
    setup do
      @fs = Function.in_library("Clojure Core")
    end
    
    should "find 3 functions" do
      assert (@fs.size == 3)
    end
  end
  
  context "Getting functions in library Clojure Core and ns clojure.core" do
    setup do
      @fs = Function.in_library_and_ns("Clojure Core", "clojure.core")
    end
    
    should "find 2 functions" do
      assert_equal 2, @fs.size
    end
  end
end
