require 'test_helper'

class ApplicationHelperTest < ActionView::TestCase
  context "functions_group_into_alpha" do
    
    setup do
      fs = Function.in_library('Clojure Core')
      @groups = functions_group_into_alpha(fs)
    end
    
    should "result in 3 groups when using functions from clojure core" do  
      assert_equal 3, @groups.size
    end
    
    should "have the first function in the group with name '+' as '+'" do
      assert_equal '+', @groups["+"][0].name
    end
  end
end
