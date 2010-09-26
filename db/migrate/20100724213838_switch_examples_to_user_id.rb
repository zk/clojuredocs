class SwitchExamplesToUserId < ActiveRecord::Migration
  def self.up
    
    ActiveRecord::Base.record_timestamps = false
    examples = Example.find(:all)
    remove_column :examples, :author
    remove_column :examples, :email
    add_column :examples, :user_id, :integer
    
    examples.each do |pe|
      ne = Example.find(pe.id)
      user = User.find_by_login(pe.author)
      ne.user_id = user.id
      ne.save_without_revision
    end
    
    example_versions = Example.find_by_sql("select * from example_versions")
    remove_column :example_versions, :author
    remove_column :example_versions, :email
    add_column :example_versions, :user_id, :integer
    
    example_versions.each do |pev|
      user = User.find_by_login(pev.author)
      if user
        ActiveRecord::Base.connection.execute("update example_versions set user_id = #{user.id} where id = #{pev.id}")
      end
    end
  end

  def self.down
    
    ActiveRecord::Base.record_timestamps = false
    examples = Example.find(:all)
    
    remove_column :examples, :user_id
    add_column :examples, :author, :string
    add_column :examples, :email, :string
    
    examples.each do |ne|
      pe = Example.find(ne.id)
      user = User.find(ne.user_id)
      pe.author = user.login
      pe.email = user.email
      pe.save_without_revision
    end
    
    example_versions = Example.find_by_sql("select * from example_versions")
    remove_column :example_versions, :user_id
    add_column :example_versions, :author, :string
    add_column :example_versions, :email, :string
    
    example_versions.each do |nev|
      
      if nev.user_id
        user = User.find(nev.user_id)
        ActiveRecord::Base.connection.execute("update example_versions set author = \"#{user.login}\", email = \"#{user.email}\" where id = #{nev.id}")
      end
    end
  end
end
