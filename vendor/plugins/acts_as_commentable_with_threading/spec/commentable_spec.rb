require File.join(File.dirname(__FILE__), "spec_helper")

describe "A class that is commentable" do
  it "can have many root comments" do
    Commentable.new.comment_threads.should be_a_kind_of(Enumerable)
  end
end