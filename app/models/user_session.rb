class UserSession < Authlogic::Session::Base
  auto_register
#  find_by_login_method :find_by_login_or_email
end