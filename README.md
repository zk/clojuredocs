# ClojureDocs Web App

[ClojureDocs](http://clojuredocs.org) is a community powered documentation and examples repository designed to aid clojurists of all skill levels in grokking clojure core and third party libraries.

The alpha version of ClojureDocs was released on July 9th, 2010.  See the [original mailing list post](http://groups.google.com/group/clojure/browse_thread/thread/a97d472679f2cade/810b73543fd6a2a5?q=clojuredocs&lnk=ol&) for more information.

ClojureDocs consists of three main projects: this website, the [library importer](https://github.com/zkim/clojuredocs-analyzer), and the [external API](http://github.com/dakrone/cd-wsapi.git).

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
* Navigate to `http://localhost:3000`


This will give you a pristine working version of ClojureDocs.  Not very interesting without data, so lets import some:

* Download the latest [database export](https://github.com/zkim/clojuredocs/downloads)
* Import the database: `mysql -uroot clojuredocs_development < downloaded_export_file`
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
