# ClojureDocs Web App

## Requirements
* Ruby 1.8.7 & Rails 2.1.5.
* MySQL 5.1
* bundler
* Sphinx 0.9.9
* RVM - not required, but highly suggested.

## Getting Started
* Open up a terminal.
* Clone the repo: `git clone https://github.com/zkim/clojuredocs.git`
* `cd clojuredocs`
* Copy the database template: `cp ./config/database.example.yml ./config/database.yml` 
* Install required gems: `bundle install`
* Create the required databases: `echo 'create database clojuredocs_development; create database clojuredocs_test' | mysql -uroot`
* Run database migrations: `rake db:migrate`
* Start the dev server: `script/server`
* Navigate to "http://localhost:3000"


This will give you a pristine working version of ClojureDocs.  Not very interesting without data, so lets import some:

* Download the latest [database export](https://github.com/zkim/clojuredocs/downloads)
* Import the database: `mysql -uroot clojuredocs_production < downloaded_export_file`
* Refresh `http://localhost:3000`

Enabling search requires sphinx

* Download `http://www.sphinxsearch.com/downloads/sphinx-0.9.9.tar.gz`
* Unzip and cd into the sphinx directory
* `./configure`
* `make`
* `sudo make install`
* Back in the clojuredocs project directory: `rake thinking_sphinx:rebuild`  This will index the database and start the sphinx search daemon.


## License
ClojureDocs is licensed under the EPL v1.0 http://opensource.org/licenses/eclipse-1.0.php