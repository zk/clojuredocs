require File.join(File.dirname(__FILE__), "spec_helper")

# Specs some of the behavior of awesome_nested_set although does so to demonstrate the use of this gem
describe Comment do
  before do
    @user = User.create!
    @comment = Comment.create!(:body => "Root comment", :user => @user)
  end

  describe "that is valid" do
    it "should have a user" do
      @comment.user.should_not be_nil
    end
    
    it "should have a body" do
      @comment.body.should_not be_nil
    end
  end
    
  it "should not have a parent if it is a root Comment" do
    @comment.parent.should be_nil
  end

  it "can have see how child Comments it has" do
    @comment.children.size.should == 0
  end

  it "can add child Comments" do
    grandchild = Comment.new(:body => "This is a grandchild", :user => @user)
    grandchild.save!
    grandchild.move_to_child_of(@comment)
    @comment.children.size.should == 1
  end    

  describe "after having a child added" do
    before do
      @child = Comment.create!(:body => "Child comment", :user => @user)
      @child.move_to_child_of(@comment)
    end
    
    it "can be referenced by its child" do    
      @child.parent.should == @comment
    end
    
    it "can see its child" do
      @comment.children.first.should == @child
    end
  end
end