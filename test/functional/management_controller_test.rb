require 'test_helper'

class ManagementControllerTest < ActionController::TestCase
  should_succeed :index, :lib => 'Clojure Core'
end
