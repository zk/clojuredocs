# ClojureDocs Web App

## Getting Started

## Gems
sudo gem install bundler
bundle install

## Import DB
create the following databases in MySQL: clojuredocs_development, clojuredocs_test
rake db:migrate
rake db:migrate RAILS_ENV=test

## Run Tests
rake test

## Start Server
script/server

## Search (Optional)
http://www.sphinxsearch.com/downloads/sphinx-0.9.9.tar.gz
(in tarball dir)
./configure
make
sudo make install

(in project dir)
rake thinking_sphinx:rebuild


## License
ClojureDocs is licensed under the EPL v1.0 http://opensource.org/licenses/eclipse-1.0.php