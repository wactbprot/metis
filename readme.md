# Μητις

<img src="metis.png" alt="metis" id="logo">

[![codox](https://github.com/wactbprot/metis/actions/workflows/main.yml/badge.svg)](https://github.com/wactbprot/metis/actions/workflows/main.yml)

[api documentation](https://wactbprot.github.io/metis/)

# notes

## emacs

```elisp
((clojure-mode . ((cider-preferred-build-tool . clojure-cli)
                  (cider-clojure-cli-global-options . "-A:dev"))))
```
## start

Start with alias (here `:dev`) dependency
`clj -A:dev`

## codox

```shell
clojure -X:dev:codox
```
## code coverage

```shell
clojure -M:dev:coverage
```
