(defproject hokime "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/tim-brown/hokime"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.github.javaparser/javaparser-symbol-solver-core "3.20.2"]
                 [com.github.javaparser/javaparser-core "3.20.2"]
                 ]
  :repl-options {:init-ns hokime.core})
