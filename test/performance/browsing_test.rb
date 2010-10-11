require 'test_helper'
require 'performance_test_help'

# Profiling results for each test method are written to tmp/performance.
class BrowsingTest < ActionController::PerformanceTest
  def test_homepage
    get '/'
  end
  
  def test_clojure_lib_page
    get '/clojure_core'
  end
end
