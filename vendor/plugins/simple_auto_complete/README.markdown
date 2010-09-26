 - simple unobstrusive autocomplete
 - JS-library-independent
 - Controller and Model helpers


Install
=======
As Rails plugin:
    script/plugin install git://github.com/grosser/simple_auto_complete.git
Or As Gem:
    sudo gem install simple_autocomplete

Then copy javascripts/css from [example folder](http://github.com/grosser/simple_auto_complete/tree/master/example_js/) OR use your own


Examples
========

Controller
----------
By default, `autocomplete_for` limits the results to 10 entries,
and sorts by the autocomplete field.

    class UsersController < ApplicationController
      autocomplete_for :user, :name
    end


`autocomplete_for` takes a third parameter, an options hash which is used in the find:

    autocomplete_for :user, :name, :limit => 15, :order => 'created_at DESC'

With :match option you can filter for different columns

    # find and order with firstname/lastname
    # return full_name of records for auto-completion
    autocomplete_for :user, :full_name, :match => [:firstname, :lastname]

With a block you can generate any output you need (ERB allowed):

    autocomplete_for :post, :title do |items|
      items.map{|item| "#{item.title} -- #{item.id} <%= Time.now %>"}.join("\n")
    end

The items passed into the block is an ActiveRecord scope allowing further scopes to be chained:

    autocomplete_for :post, :title do |items|
      items.for_user(current_user).map(&:title).join("\n")
    end
      
View
----
    <%= f.text_field :auto_user_name, :class => 'autocomplete', 'data-autocomplete-url'=>autocomplete_for_user_name_users_path %>

Routes
------
    map.resources :users, :collection => { :autocomplete_for_user_name => :get}

JS
--
use any library you like
(includes examples for jquery jquery.js + jquery.autocomplete.js + jquery.autocomplete.css)


    jQuery(function($){//on document ready
      //autocomplete
      $('input.autocomplete').each(function(){
        var $input = $(this);
        $input.autocomplete($input.attr('data-autocomplete-url'));
      });
    });

Records (Optional)
------------------
 - converts an auto_complete form field to an association on assignment
 - Tries to find the record by using `find_by_autocomplete_xxx` on the records model
 - unfound record -> nil
 - blank string -> nil
 - Controller find works independent of this find

Example for a post with autocompleted user name:

    class User
      find_by_autocomplete :name # User.find_by_autocomplete_name('Michael')
    end

    class Post
      has_one :user
      autocomplete_for(:user, :name) #--> f.text_field :auto_user_name
      # OR
      autocomplete_for(:user, :name, :name=>:creator) #--> f.text_field :auto_creator_name (creator must a an User)
    end

    class Group
      has_many :users
      add_by_autocomplete(:user, :name) #--> f.text_field :add_by_auto_user_name
    end


Authors
=======
Inspired by DHH`s 'obstrusive' autocomplete_plugin.

###Contributors (alphabetical)
 - [Bryan Ash](http://bryan-ash.blogspot.com)
 - [David Leal](http://github.com/david)
 - [mlessard](http://github.com/mlessard)
 - [Oliver Azevedo Barnes](http://github.com/oliverbarnes)
 - [Splendeo](http://www.splendeo.es)

[Michael Grosser](http://pragmatig.wordpress.com)  
grosser.michael@gmail.com  
Hereby placed under public domain, do what you want, just do not hold me accountable...
