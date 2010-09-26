class ActsAsCommentableUpgradeMigrationGenerator < Rails::Generator::Base
  def manifest
    record do |m| 
      m.migration_template 'migration.rb', 'db/migrate'
    end
  end
  
  def file_name
    "acts_as_commentable_upgrade_migration"
  end
end
