# Μητις

<img src="metis.png" alt="metis" id="logo">

[![start with workflow](https://github.com/wactbprot/metis/actions/workflows/main.yml/badge.svg)](https://github.com/wactbprot/metis/actions/workflows/main.yml)

## notes

### polylith

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://github.com/polyfy/polylith)
- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)

You can also get in touch with the Polylith Team via our [forum](https://polylith.freeflarum.com) or on [Slack](https://clojurians.slack.com/archives/C013B7MQHJQ).
 


### emacs

```elisp
((clojure-mode . ((cider-preferred-build-tool . clojure-cli)
                  (cider-clojure-cli-global-options . "-A:dev"))))
```
### start

Start with alias (here `:dev`) dependency
`clj -A:dev`

### codox

```shell
clojure -X:dev:codox
```
### code coverage

```shell
clojure -M:dev:coverage
```

### clj

* https://clojure.org/guides/deps_and_cli
* https://clojure.org/reference/repl_and_main
* https://github.com/seancorfield/dot-clojure

```
$ clj --help
Version: 1.10.2.774

You use the Clojure tools ('clj' or 'clojure') to run Clojure programs
on the JVM, e.g. to start a REPL or invoke a specific function with data.
The Clojure tools will configure the JVM process by defining a classpath
(of desired libraries), an execution environment (JVM options) and
specifying a main class and args. 

Using a deps.edn file (or files), you tell Clojure where your source code
resides and what libraries you need. Clojure will then calculate the full
set of required libraries and a classpath, caching expensive parts of this
process for better performance.

The internal steps of the Clojure tools, as well as the Clojure functions
you intend to run, are parameterized by data structures, often maps. Shell
command lines are not optimized for passing nested data, so instead you
will put the data structures in your deps.edn file and refer to them on the
command line via 'aliases' - keywords that name data structures.

'clj' and 'clojure' differ in that 'clj' has extra support for use as a REPL
in a terminal, and should be preferred unless you don't want that support,
then use 'clojure'.

Usage:
  Start a REPL   clj     [clj-opt*] [-Aaliases] [init-opt*]
  Exec function  clojure [clj-opt*] -X[aliases] [a/fn] [kpath v]*
  Run main       clojure [clj-opt*] -M[aliases] [init-opt*] [main-opt] [arg*]
  Prepare        clojure [clj-opt*] -P [other exec opts]

exec-opts:
 -Aaliases      Use concatenated aliases to modify classpath
 -X[aliases]    Use concatenated aliases to modify classpath or supply exec fn/args
 -M[aliases]    Use concatenated aliases to modify classpath or supply main opts
 -P             Prepare deps - download libs, cache classpath, but don't exec

clj-opts:
 -Jopt          Pass opt through in java_opts, ex: -J-Xmx512m
 -Sdeps EDN     Deps data to use as the last deps file to be merged
 -Spath         Compute classpath and echo to stdout only
 -Spom          Generate (or update) pom.xml with deps and paths
 -Stree         Print dependency tree
 -Scp CP        Do NOT compute or cache classpath, use this one instead
 -Srepro        Ignore the ~/.clojure/deps.edn config file
 -Sforce        Force recomputation of the classpath (don't use the cache)
 -Sverbose      Print important path info to console
 -Sdescribe     Print environment and command parsing info as data
 -Sthreads      Set specific number of download threads
 -Strace        Write a trace.edn file that traces deps expansion
 --             Stop parsing dep options and pass remaining arguments to clojure.main

init-opt:
 -i, --init path     Load a file or resource
 -e, --eval string   Eval exprs in string; print non-nil values
 --report target     Report uncaught exception to "file" (default), "stderr", or "none"

main-opt:
 -m, --main ns-name  Call the -main function from namespace w/args
 -r, --repl          Run a repl
 path                Run a script from a file or resource
 -                   Run a script from standard input
 -h, -?, --help      Print this help message and exit

Programs provided by :deps alias:
 -X:deps mvn-install       Install a maven jar to the local repository cache
 -X:deps git-resolve-tags  Resolve git coord tags to shas and update deps.edn

For more info, see:
 https://clojure.org/guides/deps_and_cli
 https://clojure.org/reference/repl_and_main
```
