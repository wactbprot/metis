# Μητις

<img src="metis.png" alt="metis" id="logo">

[![codox](https://github.com/wactbprot/metis/actions/workflows/main.yml/badge.svg)](https://github.com/wactbprot/metis/actions/workflows/main.yml)

[api documentation](https://wactbprot.github.io/metis/)

## user interface (web site)

The metis frontend is based on [UIkik](https://getuikit.com/). A fresh
metis installation needs libraries: run `./dl.sh` in metis root path
in order to install them.

### url

* container view: `http://localhost:8010/cont/mpd-ref`
* input/output elements: `http://localhost:8010/elem/mpd-ref`


## environment variables

| `var`                	| Description                                                      	| Example                                                                                                              	|
|----------------------	|------------------------------------------------------------------	|----------------------------------------------------------------------------------------------------------------------	|
| `CMP_BUILD_ON_START` 	| mpds to build on server start                                    	| `export CMP_BUILD_ON_START="ppc-gas_dosing"`<br>`export CMP_BUILD_ON_START="se3-servo,se3-cmp_valves,se3-cmp_state"` 	|
| `CMP_DEVHUB_URL`     	| url for device requests <br>(Action: TCP, VXI11, MODBUS EXECUTE) 	| `export CMP_DEVHUB_URL="http://localhost:9009"`<br>`export CMP_DEVHUB_URL="http://a73434:55555"`                     	|
| `CMP_LT_SRV`         	| CouchDB server                                                   	| `export CMP_LT_SRV="127.0.0.1"`<br>`export CMP_LT_SRV="a73434"`                                                      	|

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
