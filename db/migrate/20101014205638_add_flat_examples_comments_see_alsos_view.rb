class AddFlatExamplesCommentsSeeAlsosView < ActiveRecord::Migration
  def self.up
    puts "Dropping views if exists"
    connection.execute "DROP VIEW IF EXISTS flat_examples_view;"
    connection.execute "DROP VIEW IF EXISTS flat_comments_view;"
    connection.execute "DROP VIEW IF EXISTS flat_see_alsos_view;"
    
    #flat_function_view is used by the api to simplify querying
    puts "Adding flat_examples_view view."
    connection.execute <<-EOC
      CREATE VIEW flat_examples_view as 
      SELECT examples.*, 
             libraries.name as library,
             libraries.version as lib_version,
             namespaces.name as ns,
             functions.name as function, 
             libraries.id as library_id,
             namespaces.id as namespace_id
      FROM examples
      LEFT JOIN functions ON examples.function_id = functions.id 
      LEFT JOIN namespaces ON functions.namespace_id = namespaces.id 
      LEFT JOIN libraries ON namespaces.library_id = libraries.id;
    EOC
    
    puts "Adding flat_see_alsos_view view."
    connection.execute <<-EOC
      CREATE VIEW flat_see_alsos_view as 
      SELECT see_alsos.*, 
             libraries.name as library,
             libraries.version as version,
             namespaces.name as ns,
             functions.name as function, 
             libraries.id as library_id,
             namespaces.id as namespace_id
      FROM see_alsos
      LEFT JOIN functions ON see_alsos.from_id = functions.id 
      LEFT JOIN namespaces ON functions.namespace_id = namespaces.id 
      LEFT JOIN libraries ON namespaces.library_id = libraries.id;
    EOC
    
    puts "Adding flat_comments_view view."
    connection.execute <<-EOC
      CREATE VIEW flat_comments_view as 
      SELECT comments.*, 
             libraries.name as library,
             libraries.version as version,
             namespaces.name as ns,
             functions.name as function, 
             libraries.id as library_id,
             namespaces.id as namespace_id
      FROM comments
      LEFT JOIN functions ON comments.commentable_id = functions.id 
      LEFT JOIN namespaces ON functions.namespace_id = namespaces.id 
      LEFT JOIN libraries ON namespaces.library_id = libraries.id
      WHERE comments.commentable_type = 'Function';
    EOC
  end

  def self.down
    connection.execute "DROP VIEW IF EXISTS flat_examples_view;"
    connection.execute "DROP VIEW IF EXISTS flat_comments_view;"
    connection.execute "DROP VIEW IF EXISTS flat_see_alsos_view;"
  end
end
