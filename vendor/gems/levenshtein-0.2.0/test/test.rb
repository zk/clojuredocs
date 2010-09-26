require "test/unit"
require "levenshtein"

module Levenshtein
  class TestSequence
    def initialize(o)
      @sequence	= o
    end
  
    def length
      @sequence.length
    end
 
    def [](pos)
      @sequence[pos]
    end
  end
  
  class TestElement
    attr_reader :object

    def initialize(o)
      @object	= o
    end
  
    def ==(other)
      @object == other.object
    end
  end
end

class TestLevenshteinString < Test::Unit::TestCase
  def test_erik_veenstra
    assert_equal(7, Levenshtein.distance("erik", "veenstra"))
    assert_equal(7, Levenshtein.distance("veenstra", "erik"))

    assert_in_delta(0.875, Levenshtein.normalized_distance("erik", "veenstra"), 0.01)
    assert_in_delta(0.875, Levenshtein.normalized_distance("veenstra", "erik"), 0.01)
  end

  def test_empty_string
    assert_equal(0, Levenshtein.distance("", ""))
    assert_equal(3, Levenshtein.distance("", "foo"))
    assert_equal(3, Levenshtein.distance("foo", ""))

    assert_in_delta(0.0, Levenshtein.normalized_distance("", ""), 0.01)
    assert_in_delta(1.0, Levenshtein.normalized_distance("", "foo"), 0.01)
    assert_in_delta(1.0, Levenshtein.normalized_distance("foo", ""), 0.01)
  end

  def test_same_string
    assert_equal(0, Levenshtein.distance("", ""))
    assert_equal(0, Levenshtein.distance("foo", "foo"))

    assert_in_delta(0.0, Levenshtein.normalized_distance("", ""), 0.01)
    assert_in_delta(0.0, Levenshtein.normalized_distance("foo", "foo"), 0.01)
  end

  def test_threshold
    assert_equal(3, Levenshtein.distance("foo", "foobar"))
    assert_equal(3, Levenshtein.distance("foo", "foobar", 4))
    assert_equal(nil, Levenshtein.distance("foo", "foobar", 2))

    assert_in_delta(0.5, Levenshtein.normalized_distance("foo", "foobar"), 0.01)
    assert_in_delta(0.5, Levenshtein.normalized_distance("foo", "foobar", 0.66), 0.01)
    assert_equal(nil, Levenshtein.normalized_distance("foo", "foobar", 0.30))
  end

  def test_same_head_and_or_tail
    assert_equal(3, Levenshtein.distance("ab123cd", "abxyzcd"))
    assert_equal(3, Levenshtein.distance("ab123", "abxyz"))
    assert_equal(3, Levenshtein.distance("123cd", "xyzcd"))
    assert_equal(5, Levenshtein.distance("123cd123", "123"))

    assert_in_delta(0.42, Levenshtein.normalized_distance("ab123cd", "abxyzcd"), 0.01)
    assert_in_delta(0.6, Levenshtein.normalized_distance("ab123", "abxyz"), 0.01)
    assert_in_delta(0.6, Levenshtein.normalized_distance("123cd", "xyzcd"), 0.01)
    assert_in_delta(0.625, Levenshtein.normalized_distance("123cd123", "123"), 0.01)
  end
end

class TestLevenshteinArray < Test::Unit::TestCase
  def test_erik_veenstra
    x	= lambda{|s| s.scan(/./).collect{|e| Levenshtein::TestElement.new(e)}}

    assert_equal(7, Levenshtein.distance(x["erik"], x["veenstra"]))
  end
end

class TestLevenshteinArrayOfStrings < Test::Unit::TestCase
  def test_erik_veenstra
    x	= lambda{|s| s.scan(/./)}

    assert_equal(7, Levenshtein.distance(x["erik"], x["veenstra"]))
  end
end

class TestLevenshteinGeneric < Test::Unit::TestCase
  def test_erik_veenstra
    x	= lambda{|s| Levenshtein::TestSequence.new(s.scan(/./).collect{|e| Levenshtein::TestElement.new(e)})}

    assert_equal(7, Levenshtein.distance(x["erik"], x["veenstra"]))
  end
end

class TestLevenshteinSlow < Test::Unit::TestCase
  def test_erik_veenstra
    assert_equal(7, Levenshtein.levenshtein_distance_slow("erik", "veenstra", nil))
  end

  def test_empty_sequence
    assert_equal(0, Levenshtein.levenshtein_distance_slow("", "", nil))
    assert_equal(3, Levenshtein.levenshtein_distance_slow("", "foo", nil))
  end

  def test_same_sequence
    assert_equal(0, Levenshtein.levenshtein_distance_slow("", "", nil))
    assert_equal(0, Levenshtein.levenshtein_distance_slow("foo", "foo", nil))
  end

  def test_threshold
    assert_equal(3, Levenshtein.levenshtein_distance_slow("foo", "foobar", nil))
    assert_equal(nil, Levenshtein.levenshtein_distance_slow("foo", "foobar", 2))
  end
end
