# connect
ActiveRecord::Base.establish_connection(
  :adapter => "sqlite3",
  :database => ":memory:"
)

# create model
ActiveRecord::Schema.define(:version => 1) do
  create_table :users do |t|
    t.string :full_name
    t.string :name
  end

  create_table :authors do |t|
    t.string :name
  end

  create_table :posts do |t|
    t.integer :author_id
  end
  
  create_table :tags do |t|
    t.integer :post_id
    t.string  :name
  end
end

class User < ActiveRecord::Base
end

# do not reuse user, since it is used in controller tests!
class Author < ActiveRecord::Base
  find_by_autocomplete :name
end

class Tag < ActiveRecord::Base 
  belongs_to :post
  find_by_autocomplete :name
end

class Post < ActiveRecord::Base
  belongs_to :author
  has_many  :tags
  autocomplete_for :author, :name
  add_by_autocomplete :tag, :name
end