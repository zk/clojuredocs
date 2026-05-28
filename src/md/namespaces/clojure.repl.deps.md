`clojure.repl.deps` (introduced in 1.12.0) provides facilities for dynamically modifying the available
  libraries in the runtime when running at the REPL, without restarting. 

This namespace is intended only for development-time use in a REPL

Functions are automatically referred in the `user` namespace. In other namespaces, you may need to `(require '[clojure.repl.deps :refer [add-lib add-libs sync-deps]])` before use.
