class AddLibNsVarRels < ActiveRecord::Migration
  def self.up
    add_column :functions, :namespace_id, :integer
    add_column :namespaces, :library_id, :integer

    functions = Function.find(:all)
    namespaces = Namespace.find(:all)

    functions.each do |f|
      ns_id = Namespace.find_by_name_and_version(f.ns, f.version).id rescue nil

      if not ns_id
        ns_id = Namespace.find_by_name(f.ns).id rescue nil
      end

      if ns_id
        f.namespace_id = ns_id
        f.save
      end
    end

    namespaces.each do |n|
      f = Function.find_by_ns_and_version(n.name, n.version)
      if not f
        f = Function.find_by_ns(n.name)
      end

      if f
        l = Library.find_by_name_and_version(f.library, f.version)
        if not l
          l = Library.find_by_name(f.library)
        end

        if l
          n.library_id = l.id
          n.save
        end
      end
    end

  end

  def self.down
  end
end
