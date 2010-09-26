# Include hook code here
require 'acts_as_voteable'
ActiveRecord::Base.send(:include, Juixe::Acts::Voteable)
