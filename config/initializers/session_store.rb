# Be sure to restart your server when you modify this file.

# Your secret key for verifying cookie session data integrity.
# If you change this key, all old sessions will become invalid!
# Make sure the secret is at least 30 characters and all random, 
# no regular words or you'll be exposed to dictionary attacks.
ActionController::Base.session = {
  :key         => '_cd-site_session',
  :secret      => '4a0d577861650b391bcd71dbd12c11dd5e811c294f43c07cb352e8ab34c2c96a3292a8404b195ee6389c1c668fa670d2f207b278d9bae2c28b475beab2c2c438'
}

# Use the database for sessions instead of the cookie-based default,
# which shouldn't be used to store highly confidential information
# (create the session table with "rake db:sessions:create")
# ActionController::Base.session_store = :active_record_store
