require 'rubygems'
require 'rake/gempackagetask'
require 'spec'
require 'spec/rake/spectask'

PLUGIN = "acts_as_commentable_with_threading"
NAME = "acts_as_commentable_with_threading"
GEM_VERSION = "1.0.0"
AUTHOR = "Evan Light"
EMAIL = "evan@triple-dog-dare.com"
SUMMARY = "Plugin/gem that provides threaded comment functionality"

load 'acts_as_commentable_with_threading.gemspec'
spec = ACTS_AS_COMMENTABLE_WITH_THREADING

Rake::GemPackageTask.new(spec) do |pkg|
  pkg.gem_spec = spec
end

Spec::Rake::SpecTask.new do |t|
  t.spec_files = FileList['spec/**_spec.rb']
end

task :install => [:package] do
  sh %{sudo gem install pkg/#{NAME}-#{GEM_VERSION}}
end

task :default => :spec