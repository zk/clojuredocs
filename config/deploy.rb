
default_run_options[:pty] = true

set :application, "ClojureDocs"
set :use_sudo, false
set :repository, "git@github.com:zkim/clojuredocs.git"
set :scm, "git"
set :user, "root"
set :deploy_via, :remote_cache
set :branch, "master"
set :deploy_to, "/var/www/clojuredocs"
set :rake, "/opt/ruby-enterprise/bin/rake"
set :db_user, "root"
set :db_pass, "clojuredocs"

role :web, "173.45.232.155"
role :app, "173.45.232.155"
role :db, "173.45.232.155", :primary => true

deploy.task :restart, :roles => :app do
  run "touch #{current_path}/tmp/restart.txt"
end



