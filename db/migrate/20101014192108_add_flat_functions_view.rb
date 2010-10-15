class AddFlatFunctionsView < ActiveRecord::Migration
  def self.up

    connection.execute "DROP VIEW IF EXISTS flat_functions_view;"

    
    #flat_function_view is used by the api to simplify querying
    puts "Adding flat_function_view view."
    connection.execute <<-EOC
      CREATE VIEW flat_functions_view as 
      SELECT functions.*, 
             libraries.name as library, 
             namespaces.name as ns, 
             libraries.id as library_id 
      FROM functions 
      LEFT JOIN namespaces ON functions.namespace_id = namespaces.id 
      LEFT JOIN libraries ON namespaces.library_id = libraries.id;
    EOC
  end

  def self.down
    connection.execute "DROP VIEW IF EXISTS flat_functions_view;"
  end
end
