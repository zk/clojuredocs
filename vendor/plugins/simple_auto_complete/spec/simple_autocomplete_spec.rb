
require "spec/spec_helper"

describe SimpleAutocomplete do
  it "has a version" do
    SimpleAutocomplete::VERSION.should =~ /^\d+\.\d+\.\d+$/
  end
end

class UsersController < ActionController::Base
  def a_instance_method
    self.class.a_class_method
  end
end

describe 'Controller extensions' do
  before do
    @c = UsersController.new
    @c.stub!(:params).and_return Hash.new
    @c.stub!(:render)
  end
  
  describe 'regression' do
    it "can use long method/class names" do
      class UserAddress < ActiveRecord::Base
        set_table_name :users
      end
      UserAddress.should_receive(:scoped).with hash_including(:conditions => ['LOWER(full_name) LIKE ?','%hans%'])
      @c.stub!(:params).and_return :q=>'Hans'
      UsersController.autocomplete_for(:user_address,:full_name)
      @c.autocomplete_for_user_address_full_name
    end
  end
  
  describe 'simple autocomplete' do
    before do
      UsersController.autocomplete_for(:user,:name)
    end
    
    it "renders the items inline" do
      @c.should_receive(:render).with {|hash| hash[:inline] =~ /@items.map \{|item| h(item.name)\}.uniq.join(\'\n\')/}
      @c.autocomplete_for_user_name
    end
    
    it "orders ASC by name" do
      User.should_receive(:scoped).with(hash_including(:order => 'name ASC'))
      @c.autocomplete_for_user_name
    end
    
    it "finds by name" do
      @c.stub!(:params).and_return :q=>'Hans'
      User.should_receive(:scoped).with(hash_including(:conditions => ['LOWER(name) LIKE ?','%hans%']))
      @c.autocomplete_for_user_name
    end
  end

  describe "autocomplete with :match" do
    before do
      UsersController.autocomplete_for(:user, :name, :match => [:full_name, :name])
    end

    it "renders the items inline with method" do
      @c.should_receive(:render).with {|hash| hash[:inline] =~ /@items.map \{|item| h(item.full_name)\}.uniq.join(\'\n\')/}
      @c.autocomplete_for_user_name(:match => [:full_name, :name])
    end

    it "orders ASC by match" do
      User.should_receive(:scoped).with(hash_including(:order => 'full_name ASC'))
      @c.autocomplete_for_user_name(:match => [:full_name, :name])
    end

    it "finds by match" do
      @c.stub!(:params).and_return :q=>'Hans'
      User.should_receive(:scoped).with(hash_including(:conditions => ["LOWER(full_name) LIKE ? OR LOWER(name) LIKE ?", "%hans%", "%hans%"]))
      @c.autocomplete_for_user_name(:match => [:full_name, :name])
    end
  end
  
  describe "autocomplete using blocks" do
    it "evaluates the block" do
      x=0
      UsersController.autocomplete_for(:user, :name) do |items|
        x=1
      end
      @c.autocomplete_for_user_name
      x.should == 1
    end
    
    it "passes found items to the block" do
      User.delete_all
      u1 = User.create!(:name => 'xxx')
      User.create!(:name => 'zzz')
      u3 = User.create!(:name => 'xxx')

      UsersController.autocomplete_for(:user, :name) do |items|
        items.to_a.should =~ [u1, u3]
      end

      @c.stub!(:params).and_return :q=>'xxx'
      @c.autocomplete_for_user_name
    end
    
    it "uses block output for render" do
      UsersController.autocomplete_for(:user, :name) do |items|
        items + 'xx'
      end
      User.should_receive(:scoped).and_return 'aa'
      @c.should_receive(:render).with(hash_including(:inline => 'aaxx'))
      @c.autocomplete_for_user_name
    end

    it "has block in controllers scope" do
      UsersController.should_receive(:a_class_method)
      UsersController.autocomplete_for(:user, :name) do |items|
        a_instance_method
      end
      @c.autocomplete_for_user_name
    end
  end
end

describe 'Model extensions' do
  it "warns raises when a needed finder is not defined" do
    lambda{
      class XPost < ActiveRecord::Base
        set_table_name :posts
        autocomplete_for :user, :name
      end
    }.should raise_error(/User does not respond to find_by_autocomplete_name/)
  end

  describe "auto_{association}_{attribute}" do
    it "is blank when associated is not present" do
      Post.new.auto_author_name.should == ''
    end

    it "is the attribute of the associated" do
      Post.new(:author => Author.new(:name => 'xxx')).auto_author_name.should == 'xxx'
    end
  end

  describe "auto_{association}_{attribute}=" do
    before do
      Author.delete_all
      Author.create!(:name => 'Mike')
      @pete = Author.create!(:name => 'Pete')
      Author.create!(:name => '')
    end

    it "does nothing when blank is set" do
      p = Post.new(:auto_author_name => '')
      p.author.should == nil
    end

    it "does nothing when nil is net" do
      p = Post.new(:auto_author_name => nil)
      p.author.should == nil
    end

    it "finds the correct associated and sets it" do
      p = Post.new(:auto_author_name => 'Pete')
      p.author.should == @pete
    end
  end

  describe "find_by_autocomplete_{attribute}" do
    before do
      Author.delete_all
      @author = Author.create!(:name => 'john')
    end

    it "finds when possible" do
      Author.find_by_autocomplete_name('john').should == @author
    end

    it "returns nil when nothing was found" do
      Author.find_by_autocomplete_name('bob').should == nil
    end
  end
  
  describe "add_by_auto_{name}_{attribute}" do
    it "is always nil when associated is not present" do
      Post.new.add_by_auto_tag_name.should == nil
    end
  end

  describe "add_by_auto_{name}_{attribute}=" do
    before do
      Tag.delete_all
      Tag.create!(:name => 'economics')
      @tag = Tag.create!(:name => 'politics')
      Tag.create!(:name => '')
    end

    it "does nothing when blank is set" do
      p = Post.new(:add_by_auto_tag_name => '' )
      p.tags.should be_empty
    end

    it "does nothing when nil is net" do
      p = Post.new(:add_by_auto_tag_name => nil)
      p.tags.should be_empty
    end

    it "finds the correct associated and sets it" do
      p = Post.new(:add_by_auto_tag_name => 'politics')
      p.tags.should == [@tag]
    end
  end
end