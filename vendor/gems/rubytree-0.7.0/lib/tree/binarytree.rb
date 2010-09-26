# binarytree.rb - This file is part of the RubyTree package.
#
# $Revision$ by $Author$ on $Date$
#
# = binarytree.rb - An implementation of the binary tree data structure.
#
# Provides a binary tree data structure with ability to
# store two node elements as children at each node in the tree.
#
# Author:: Anupam Sengupta (anupamsg@gmail.com)
#

# Copyright (c) 2007, 2008, 2009, 2010 Anupam Sengupta
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

require 'tree'

module Tree

  # Provides a Binary tree implementation. This node allows only two child nodes (left and right child).  It also
  # provides direct access to the left or right child, including assignment to the same.
  #
  # This inherits from the {Tree::TreeNode} class.
  #
  # @author Anupam Sengupta
  #
  class BinaryTreeNode < TreeNode

    # Adds the specified child node to the receiver node.  The child node's parent is set to be the receiver.
    #
    # The child nodes are added in the order of addition, i.e., the first child added becomes the left node, and the
    # second child added will be the second node.
    #
    # If only one child is present, then this will be the left child.
    #
    # @param [Tree::BinaryTreeNode] child The child to add.
    #
    # @raise [ArgumentError] This exception is raised if two children are already present.
    def add(child)
      raise ArgumentError, "Already has two child nodes" if @children.size == 2

      super(child)
    end

    # Returns the left child of the receiver node. Note that left Child == first Child.
    #
    # @return [Tree::BinaryTreeNode] The left most (or first) child.
    #
    # @see #rightChild
    def leftChild
      children.first
    end

    # Returns the right child of the receiver node. Note that right child == last child unless there is only one child.
    #
    # Returns +nil+ if the right child does not exist.
    #
    # @return [Tree::BinaryTreeNode] The right child, or +nil+ if the right side child does not exist.
    #
    # @see #leftChild
    def rightChild
      children[1]
    end

    # Sets the left child of the receiver node. If a previous child existed, it is replaced.
    #
    # @param [Tree::BinaryTreeNode] child The child to add as the left-side node.
    #
    # @return [Tree::BinaryTreeNode] The assigned child node.
    #
    # @see #leftChild
    # @see #rightChild=
    def leftChild=(child)
      @children[0] = child
      @childrenHash[child.name] = child if child # Assign the name mapping
    end

    # Sets the right child of the receiver node. If a previous child existed, it is replaced.
    #
    # @param [Tree::BinaryTreeNode] child The child to add as the right-side node.
    #
    # @return [Tree::BinaryTreeNode] The assigned child node.
    #
    # @see #rightChild
    # @see #leftChild=
    def rightChild=(child)
      @children[1] = child
      @childrenHash[child.name] = child if child # Assign the name mapping
    end

    # Returns +true+ if the receiver node is the left child of its parent.
    # Always returns +false+ if it is a root node.
    #
    # @return [Boolean] +true+ if this is the left child of its parent.
    def isLeftChild?
      return false if isRoot?
      self == parent.leftChild
    end

    # Returns +true+ if the receiver node is the right child of its parent.
    # Always returns +false+ if it is a root node.
    #
    # @return [Boolean] +true+ if this is the right child of its parent.
    def isRightChild?
      return false if isRoot?
      self == parent.rightChild
    end

    # Swaps the left and right child nodes of the receiver node with each other.
    #
    # @todo Define the return value.
    def swap_children
      self.leftChild, self.rightChild = self.rightChild, self.leftChild
    end
  end

end
