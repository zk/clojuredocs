class Example < ActiveRecord::Base
  acts_as_versioned
  belongs_to :function
  belongs_to :user
  
  def belongs_to?(user)
    return user_id == user.id
  end
end
