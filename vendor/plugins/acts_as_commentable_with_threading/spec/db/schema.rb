ActiveRecord::Schema.define(:version => 0) do
  create_table "users", :force => true do |t|
    t.timestamps
  end
  
  create_table "commentables", :force => true do |t|
    t.timestamps
  end

  create_table "comments", :force => true do |t|
    t.integer "commentable_id", :default => 0
    t.string "commentable_type", :limit => 15, :default => ""
    t.string "title", :default => ""
    t.text "body", :default => ""
    t.string "subject", :default => ""
    t.integer "user_id", :default => 0, :null => false
    t.integer "parent_id"
    t.integer "lft"
    t.integer "rgt"
    t.timestamps
  end

  add_index "comments", "user_id"
  add_index "comments", "commentable_id"
end
