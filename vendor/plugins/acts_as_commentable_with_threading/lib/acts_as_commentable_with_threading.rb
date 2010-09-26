require 'activerecord'
require 'awesome_nested_set'
ActiveRecord::Base.class_eval do
  include CollectiveIdea::Acts::NestedSet
  #include Juixe::Acts::Voteable   #<-- uncomment this if you have installed and wish to use the acts_as_voteable plugin
end
require 'comment'

# ActsAsCommentableWithThreading
module Acts #:nodoc:
  module CommentableWithThreading #:nodoc:

    def self.included(base)
      base.extend ClassMethods  
    end

    module ClassMethods
      def acts_as_commentable
        has_many :comment_threads, :class_name => "Comment", :as => :commentable, :dependent => :destroy, :order => 'created_at ASC'
        include Acts::CommentableWithThreading::InstanceMethods
        extend Acts::CommentableWithThreading::SingletonMethods
      end
    end
    
    # This module contains class methods
    module SingletonMethods
      # Helper method to lookup for comments for a given object.
      # This method is equivalent to obj.comments.
      def find_comments_for(obj)
        commentable = ActiveRecord::Base.send(:class_name_of_active_record_descendant, self).to_s
       
        Comment.find(:all,
          :conditions => ["commentable_id = ? and commentable_type = ?", obj.id, commentable],
          :order => "created_at DESC"
        )
      end
      
      # Helper class method to lookup comments for
      # the mixin commentable type written by a given user.  
      # This method is NOT equivalent to Comment.find_comments_for_user
      def find_comments_by_user(user) 
        commentable = ActiveRecord::Base.send(:class_name_of_active_record_descendant, self).to_s
        
        Comment.find(:all,
          :conditions => ["user_id = ? and commentable_type = ?", user.id, commentable],
          :order => "created_at DESC"
        )
      end
    end
    
    # This module contains instance methods
    module InstanceMethods
      
      # Helper method to display only root threads, no children/replies
      def root_comments
        self.comment_threads.find(:all, :conditions => {:parent_id => nil})
      end
      
      # Helper method to sort comments by date
      def comments_ordered_by_submitted
        Comment.find(:all,
          :conditions => ["commentable_id = ? and commentable_type = ?", id, self.class.name],
          :order => "created_at DESC"
        )
      end
      
      # Helper method that defaults the submitted time.
      def add_comment(comment)
        comments << comment
      end
    end
    
  end
end

ActiveRecord::Base.send(:include, Acts::CommentableWithThreading)