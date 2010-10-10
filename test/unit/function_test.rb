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
end
