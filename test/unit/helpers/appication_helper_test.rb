require 'test_helper'

class ApplicationHelperTest < ActionView::TestCase
  context "functions_group_into_alpha" do
    
    setup do
      fs = Library.find_by_name("Clojure Core").namespaces.reduce([]){|coll, ns| coll + ns.functions }
      @groups = functions_group_into_alpha(fs)
    end
    
    should "result in 2 groups when using functions from clojure core" do  
      assert_equal 2, @groups.size
    end
    
    should "have the first function in the group with name '+' as '+'" do
      assert_equal '+', @groups["+"][0].name
    end
  end
end
