# Add your own tasks in files placed in lib/tasks ending in .rake,
# for example lib/tasks/capistrano.rake, and they will automatically be available to Rake.

require(File.join(File.dirname(__FILE__), 'config', 'boot'))

require 'rake'
require 'rake/testtask'
require 'rake/rdoctask'

require 'tasks/rails'

require 'digest/sha1'

puts ENV['RAILS_ENV']


def sha1(s)
  Digest::SHA1.hexdigest(s) if s
end

if ENV['RAILS_ENV'] != 'production'

  require 'rcov/rcovtask'

  namespace :test do
    namespace :coverage do
      desc "Delete aggregate coverage data."
      task(:clean) { rm_f "coverage.data" }
    end
    desc 'Aggregate code coverage for unit, functional and integration tests'
    task :coverage => "test:coverage:clean"
    %w[unit functional integration].each do |target|
      namespace :coverage do
        Rcov::RcovTask.new(target) do |t|
          t.libs << "test"
          t.test_files = FileList["test/#{target}/*_test.rb"]
          t.output_dir = "test/coverage/#{target}"
          t.verbose = true
          t.rcov_opts << '--rails --aggregate coverage.data --exclude "gems,cc_quick_ref\.rb"'
        end
      end
      task :coverage => "test:coverage:#{target}"
    end
  end

  desc "Anonymize user table."
  namespace :db do
    task :clean => :environment do

      # Need to do this with reflection
      User.find_each do |u|
        u.login             = sha1 u.login
        u.email             = "#{sha1(u.email)}@clojuredocs.org"
        u.crypted_password  = sha1 u.crypted_password
        u.password_salt     = sha1 u.password_salt
        u.persistence_token = sha1 u.persistence_token
        u.current_login_ip  = sha1 u.current_login_ip
        u.last_login_ip     = sha1 u.last_login_ip
        u.openid_identifier = nil
        u.save(false)
      end

    end
  end

end

