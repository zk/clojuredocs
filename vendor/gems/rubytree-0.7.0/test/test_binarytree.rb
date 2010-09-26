#!/usr/bin/env ruby

# test_binarytree.rb - This file is part of the RubyTree package.
#
# $Revision$ by $Author$ on $Date$
#
# Copyright (c) 2006, 2007, 2008, 2009, 2010 Anupam Sengupta
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
# - Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
#
# - Redistributions in binary form must reproduce the above copyright notice, this
#   list of conditions and the following disclaimer in the documentation and/or
#   other materials provided with the distribution.
#
# - Neither the name of the organization nor the names of its contributors may
#   be used to endorse or promote products derived from this software without
#   specific prior written permission.
#
#   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

require 'test/unit'
require 'tree/binarytree'

module TestTree
  # Test class for the binary tree node.
  class TestBinaryTreeNode < Test::Unit::TestCase

    # Setup the test data scaffolding.
    def setup
      @root = Tree::BinaryTreeNode.new("ROOT", "Root Node")

      @left_child1  = Tree::BinaryTreeNode.new("A Child at Left", "Child Node @ left")
      @right_child1 = Tree::BinaryTreeNode.new("B Child at Right", "Child Node @ right")

    end

    # Tear down the test data scaffolding.
    def teardown
      @root.remove!(@left_child1)
      @root.remove!(@right_child1)
      @root = nil
    end

    # Test initialization of the binary tree.
    def test_initialize
      assert_not_nil(@root, "Binary tree's Root should have been created")
    end

    # Test the add method.
    def test_add
      @root.add  @left_child1
      assert_same(@left_child1, @root.leftChild, "The left node should be left_child1")
      assert_same(@left_child1, @root.firstChild, "The first node should be left_child1")

      @root.add @right_child1
      assert_same(@right_child1, @root.rightChild, "The right node should be right_child1")
      assert_same(@right_child1, @root.lastChild, "The first node should be right_child1")

      assert_raise ArgumentError do
        @root.add Tree::BinaryTreeNode.new("The third child!")
      end

      assert_raise ArgumentError do
        @root << Tree::BinaryTreeNode.new("The third child!")
      end
    end

    # Test the leftChild method.
    def test_leftChild
      @root << @left_child1
      @root << @right_child1
      assert_same(@left_child1, @root.leftChild, "The left child should be 'left_child1")
      assert_not_same(@right_child1, @root.leftChild, "The right_child1 is not the left child")
    end

    # Test the rightChild method.
    def test_rightChild
      @root << @left_child1
      @root << @right_child1
      assert_same(@right_child1, @root.rightChild, "The right child should be 'right_child1")
      assert_not_same(@left_child1, @root.rightChild, "The left_child1 is not the left child")
    end

    # Test leftChild= method.
    def test_leftChild_equals
      @root << @left_child1
      @root << @right_child1
      assert_same(@left_child1, @root.leftChild, "The left child should be 'left_child1")

      @root.leftChild = Tree::BinaryTreeNode.new("New Left Child")
      assert_equal("New Left Child", @root.leftChild.name, "The left child should now be the new child")
      assert_equal("B Child at Right", @root.lastChild.name, "The last child should now be the right child")

      # Now set the left child as nil, and retest
      @root.leftChild = nil
      assert_nil(@root.leftChild, "The left child should now be nil")
      assert_nil(@root.firstChild, "The first child is now nil")
      assert_equal("B Child at Right", @root.lastChild.name, "The last child should now be the right child")
    end

    # Test rightChild= method.
    def test_rightChild_equals
      @root << @left_child1
      @root << @right_child1
      assert_same(@right_child1, @root.rightChild, "The right child should be 'right_child1")

      @root.rightChild = Tree::BinaryTreeNode.new("New Right Child")
      assert_equal("New Right Child", @root.rightChild.name, "The right child should now be the new child")
      assert_equal("A Child at Left", @root.firstChild.name, "The first child should now be the left child")
      assert_equal("New Right Child", @root.lastChild.name, "The last child should now be the right child")

      # Now set the right child as nil, and retest
      @root.rightChild = nil
      assert_nil(@root.rightChild, "The right child should now be nil")
      assert_equal("A Child at Left", @root.firstChild.name, "The first child should now be the left child")
      assert_nil(@root.lastChild, "The first child is now nil")
    end

    # Test isLeftChild? method.
    def test_isLeftChild_eh
      @root << @left_child1
      @root << @right_child1

      assert(@left_child1.isLeftChild?, "left_child1 should be the left child")
      assert(!@right_child1.isLeftChild?, "left_child1 should be the left child")

      # Now set the right child as nil, and retest
      @root.rightChild = nil
      assert(@left_child1.isLeftChild?, "left_child1 should be the left child")

      assert(!@root.isLeftChild?, "Root is neither left child nor right")
    end

    # Test isRightChild? method.
    def test_isRightChild_eh
      @root << @left_child1
      @root << @right_child1

      assert(@right_child1.isRightChild?, "right_child1 should be the right child")
      assert(!@left_child1.isRightChild?, "right_child1 should be the right child")

      # Now set the left child as nil, and retest
      @root.leftChild = nil
      assert(@right_child1.isRightChild?, "right_child1 should be the right child")
      assert(!@root.isRightChild?, "Root is neither left child nor right")
    end

    # Test swap_children method.
    def test_swap_children
      @root << @left_child1
      @root << @right_child1

      assert(@right_child1.isRightChild?, "right_child1 should be the right child")
      assert(!@left_child1.isRightChild?, "right_child1 should be the right child")

      @root.swap_children

      assert(@right_child1.isLeftChild?, "right_child1 should now be the left child")
      assert(@left_child1.isRightChild?, "left_child1 should now be the right child")
      assert_equal(@right_child1, @root.firstChild, "right_child1 should now be the first child")
      assert_equal(@left_child1, @root.lastChild, "left_child1 should now be the last child")
      assert_equal(@right_child1, @root[0], "right_child1 should now be the first child")
      assert_equal(@left_child1, @root[1], "left_child1 should now be the last child")
    end
  end
end
