load 'deploy' if respond_to?(:namespace) # cap2 differentiator
Dir['vendor/plugins/*/recipes/*.rb'].each { |plugin| load(plugin) }

load 'config/deploy' # remove this line to skip loading any of the default tasks

after "deploy:update", :setup_gems
after "deploy:update", :set_permissions
after "deploy:update", :update_sphinx
after "deploy:setup", :create_database

task :create_database, :role => [:web, :app] do
  run "echo 'create database clojuredocs' | mysql -u#{db_user} -p#{db_pass}"
end

task :set_permissions, :role => [:web, :app] do
  run "chown -R nobody /var/www/clojuredocs/current/tmp"
  run "chown -R nobody /var/www/clojuredocs/current/public"
  run "chmod 0666 /var/www/clojuredocs/current/log/production.log"
end

task :setup_gems, :role => [:web, :app] do
  run("/opt/ruby-enterprise/bin/gem install rcov -v=0.9.8")
  run("/opt/ruby-enterprise/bin/gem install ruby-openid -v=2.1.2")
  run("/opt/ruby-enterprise/bin/gem install rack-openid -v=1.0.3")
  run("/opt/ruby-enterprise/bin/gem install rdiscount -v=1.6.5")
  run("cd #{deploy_to}/current && #{rake} gems:install RAILS_ENV=production")
  run("cd #{deploy_to}/current && #{rake} gems:unpack RAILS_ENV=production")
  run("cd #{deploy_to}/current && #{rake} gems:build RAILS_ENV=production")
end

task :update_sphinx, :role => [:web, :app] do
  run("killall searchd; true")
  run("cd #{deploy_to}/current && #{rake} thinking_sphinx:rebuild RAILS_ENV=production")
end