begin
  require "levenshtein/levenshtein_fast"	# If compiled by RubyGems.
rescue LoadError
  begin
    require "levenshtein_fast"			# If compiled by the build script.
  rescue LoadError
    $stderr.puts "WARNING: Couldn't find the fast C implementation of Levenshtein.distance. Using the much slower Ruby version instead."
  end
end

# The Levenshtein distance is a metric for measuring the amount
# of difference between two sequences (i.e., the so called edit
# distance). The Levenshtein distance between two sequences is
# given by the minimum number of operations needed to transform
# one sequence into the other, where an operation is an
# insertion, deletion, or substitution of a single element.
#
# More information about the Levenshtein distance algorithm:
# http://en.wikipedia.org/wiki/Levenshtein_distance .

module Levenshtein
  VERSION	= "0.2.0"

  # Returns the Levenshtein distance as a number between 0.0 and
  # 1.0. It's basically the Levenshtein distance divided by the
  # length of the longest sequence.

  def self.normalized_distance(s1, s2, threshold=nil)
    s1, s2	= s2, s1	if s1.length > s2.length	# s1 is the short one; s2 is the long one.

    if s2.length == 0
      0.0	# Since s1.length < s2.length, s1 must be empty as well.
    else
      if threshold
        if d = self.distance(s1, s2, (threshold*s2.length+1).to_i)
          d.to_f/s2.length
        else
          nil
        end
      else
        self.distance(s1, s2).to_f/s2.length
      end
    end
  end

  # Returns the Levenshtein distance between two sequences.
  #
  # The two sequences can be two strings, two arrays, or two other
  # objects. Strings, arrays and arrays of strings are handled with
  # optimized (very fast) C code. All other sequences are handled
  # with generic (fast) C code.
  #
  # The sequences should respond to :length and :[] and all objects
  # in the sequences (as returned by []) should response to :==.

  def self.distance(s1, s2, threshold=nil)
    s1, s2	= s2, s1	if s1.length > s2.length	# s1 is the short one; s2 is the long one.

    # Handle some basic circumstances.

    return 0		if s1 == s2
    return s2.length	if s1.length == 0

    if threshold
      return nil	if (s2.length-s1.length) >= threshold

      a1, a2	= nil, nil
      a1, a2	= s1, s2			if s1.respond_to?(:-) and s2.respond_to?(:-)
      a1, a2	= s1.scan(/./), s2.scan(/./)	if s1.respond_to?(:scan) and s2.respond_to?(:scan)

      if a1 and a2
        return nil	if (a1-a2).length >= threshold
        return nil	if (a2-a1).length >= threshold
      end
    end

    distance_fast_or_slow(s1, s2, threshold)
  end

  def self.distance_fast_or_slow(s1, s2, threshold)	# :nodoc:
    if respond_to?(:levenshtein_distance_fast)
      levenshtein_distance_fast(s1, s2, threshold)	# Implemented in C.
    else
      levenshtein_distance_slow(s1, s2, threshold)	# Implemented in Ruby.
    end
  end

  def self.levenshtein_distance_slow(s1, s2, threshold)	# :nodoc:
    row	= (0..s1.length).to_a

    1.upto(s2.length) do |y|
      prow	= row
      row	= [y]

      1.upto(s1.length) do |x|
        row[x]	= [prow[x]+1, row[x-1]+1, prow[x-1]+(s1[x-1]==s2[y-1] ? 0 : 1)].min
      end

      # Stop analysing this sequence as soon as the best possible
      # result for this sequence is bigger than the best result so far.
      # (The minimum value in the next row will be equal to or greater
      # than the minimum value in this row.)

      return nil	if threshold and row.min >= threshold
    end

    row[-1]
  end
end
