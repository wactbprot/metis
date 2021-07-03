# Μητις

<img src="metis.png" alt="metis" id="logo">

[![codox](https://github.com/wactbprot/metis/actions/workflows/main.yml/badge.svg)](https://github.com/wactbprot/metis/actions/workflows/main.yml)

[api documentation](https://wactbprot.github.io/metis/)

## user interface (web site)

The metis frontend is based on [UIkit](https://getuikit.com/). A fresh
metis installation needs libraries: run `./dl.sh` in metis root path
in order to install them.

### url

* container view: `http://localhost:8010/cont/mpd-ref`
* input/output elements: `http://localhost:8010/elem/mpd-ref`


## environment variables

| `var`                	| Description                                                      	| Example                                                                                                              	|
|----------------------	|------------------------------------------------------------------	|----------------------------------------------------------------------------------------------------------------------	|
| `METIS_BUILD_ON_START` 	| mpds to build on server start                                    	| `export METIS_BUILD_ON_START="mpd-ppc-gas_dosing"`<br>`export METIS_BUILD_ON_START="mpd-se3-servo,mpd-se3-cmp_valves,mpd-se3-cmp_state"` 	|
| `METIS_DEVHUB_URL`     	| url for device requests <br>(Action: TCP, VXI11, MODBUS EXECUTE) 	| `export METIS_DEVHUB_URL="http://localhost:9009"`<br>`export METIS_DEVHUB_URL="http://a73434:55555"`                     	|
| `METIS_LTMEM_HOST`         	| CouchDB host                                                   	| `export METIS_LTMEM_HOST="127.0.0.1"`<br>`export METIS_LTMEM_HOST="a73434"`                                                      	|
| `METIS_DEVPROXY_URL`         	| DevProxy url                                                   	| `export METIS_DEVPROXY_URL="127.0.0.1:8009"`                                                     	|

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
