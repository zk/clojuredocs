class SeeAlso < ActiveRecord::Base
  
  acts_as_voteable
  
  belongs_to :from_function, :class_name => "Function", :foreign_key => "from_id"
  belongs_to :user
  belongs_to :to_function, :class_name => "Function", :foreign_key => "to_id"
  
  def has_voted?(user)
    Vote.find(:first, :conditions => {:voteable_type => "SeeAlso", :voteable_id => self.id, :user_id => user.id})
  end
  
  def owner?(user)
    self.user == user
  end
  
  def vote_score
    votes_for - votes_against
  end
end
