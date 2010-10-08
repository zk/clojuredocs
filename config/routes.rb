#removed '.' from sparators to make urls play nicely with clojure names.
ActionController::Routing::SEPARATORS =  %w( / ; , ? )

ActionController::Routing::Routes.draw do |map|
  # The priority is based upon order of creation: first created -> highest priority.

  # Sample of regular route:
  #   map.connect 'products/:id', :controller => 'catalog', :action => 'view'
  # Keep in mind you can assign values other than :controller and :action

  # Sample of named route:
  #   map.purchase 'products/:id/purchase', :controller => 'catalog', :action => 'purchase'
  # This route can be invoked with purchase_url(:id => product.id)

  # Sample resource route (maps HTTP verbs to controller actions automatically):
  #   map.resources :products

  # Sample resource route with options:
  #   map.resources :products, :member => { :short => :get, :toggle => :post }, :collection => { :sold => :get }

  # Sample resource route with sub-resources:
  #   map.resources :products, :has_many => [ :comments, :sales ], :has_one => :seller
  
  # Sample resource route with more complex sub-resources
  #   map.resources :products do |products|
  #     products.resources :comments
  #     products.resources :sales, :collection => { :recent => :get }
  #   end

  # Sample resource route within a namespace:
  #   map.namespace :admin do |admin|
  #     # Directs /admin/products/* to Admin::ProductsController (app/controllers/admin/products_controller.rb)
  #     admin.resources :products
  #   end

  # You can have the root of your site routed with map.root -- just remember to delete public/index.html.
  map.root :controller => "main", :action => "index"
  
  map.connect '/examples_style_guide', :controller => 'main', :action => 'examples_style_guide'
  
  map.connect '/profile/:login', :controller => 'user', :action => 'profile'
  
  map.connect '/search_autocomplete', :controller => 'main', :action => 'search_autocomplete'  
  map.connect '/search', :controller => 'main', :action => 'search'
  map.connect '/search/:lib', :controller => 'main', :action => 'search'
  map.connect '/ac_search', :controller => 'main', :action => 'lib_search'
  map.connect '/ac_search/:lib', :controller => 'main', :action => 'lib_search'  
  
  map.connect '/examples/new', :controller => 'examples', :action => 'new'
  map.connect '/examples/update', :controller => 'examples', :action => 'update'
  map.connect '/examples/delete', :controller => 'examples', :action => 'delete'
  map.connect '/examples/view_changes/:id', :controller => 'examples', :action => 'view_changes'

  map.connect '/comments/delete', :controller => 'comments', :action => 'delete'
  
  map.connect '/logout', :controller => 'user_sessions', :action => 'destroy'
  map.connect '/login', :controller => 'user_sessions', :action => 'new'
  map.connect '/login/dologin', :controller => 'user_sessions', :action => 'create'
  map.connect '/login/login_to_migrate', :controller => 'user_sessions', :action => 'login_to_migrate'  
  map.connect '/login/migrate', :controller => 'user_sessions', :action => 'migrate_existing'
  map.connect '/create_new_openid_user', :controller => 'user_sessions', :action => 'create_new_openid_user'
  
  map.connect '/quickref/:lib', :controller => 'main', :action => 'quick_ref_shortdesc'
  map.connect '/quickref/shortdesc/:lib', :controller => 'main', :action => 'quick_ref_shortdesc'
  map.connect '/quickref/varsonly/:lib', :controller => 'main', :action => 'quick_ref_vars_only'
  
  map.connect '/feed/recent_updates', :controller => 'feed', :action => 'recent_updates'
  
  map.connect '/see_also/lookup', :controller => 'see_also', :action => 'lookup'
  map.connect '/see_also/delete', :controller => 'see_also', :action => 'delete'
  map.connect '/see_also/vote', :controller => 'see_also', :action => 'vote'
  map.connect '/see_also/add', :controller => 'see_also', :action => 'add'
  
  map.connect '/v/:id', :controller => 'main', :action => 'function_short_link'
  
  map.connect '/libs', :controller => 'main', :action => 'libs'
  
  map.connect '/management/search/:lib', :controller => 'management', :action => 'search'
  map.connect '/management/:lib/function', :controller => 'management', :action => 'function' 
  map.connect '/management/:lib', :controller => 'management'
  
  version_regex = /\d+\.[^\/]*/
  
  map.connect '/:lib', :controller => 'main', :action => 'lib'
  map.connect '/:lib/:version', :controller => 'main', :action => 'lib', :requirements => {:version => version_regex}
  map.connect '/:lib/:version/:ns', :controller => 'main', :action => 'ns', :requirements => {:version => version_regex}
  map.connect '/:lib/:version/:ns/:function', :controller => 'main', :action => 'function', :requirements => {:version => version_regex}
  
  map.connect '/:lib', :controller => 'main', :action => 'lib', :version => nil
  map.connect '/:lib/:ns', :controller => 'main', :action => 'ns', :version => nil
  map.connect '/:lib/:ns/:function', :controller => 'main', :action => 'function', :version => nil
  

  # See how all your routes lay out with "rake routes"

  # Install the default routes as the lowest priority.
  # Note: These default routes make all actions in every controller accessible via GET requests. You should
  # consider removing or commenting them out if you're using named routes and resources.
end
