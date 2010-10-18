class User < ActiveRecord::Base
  
  acts_as_authentic do |c|
    #c.openid_required_fields = [:nickname, :email]    
  end

  acts_as_tagger

  def self.find_by_login_or_email(login)
    User.find_by_login(login) || User.find_by_email(login)
  end
  
  def authority
    col = 0
    see_alsos.map{|sa| sa.vote_score}.each do |score|
      col += score
    end
    
    col
  end
  
  private 
  def map_openid_registration(reg)
    self.email = reg["email"] if email.blank?
    self.login = reg["nickname"] if login.blank?
  end
  
  has_many :examples
  has_many :comments
  has_many :see_alsos
  
end
