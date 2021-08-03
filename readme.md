# Μητις

<img src="metis.png" alt="metis" id="logo">

[![codox](https://github.com/wactbprot/metis/actions/workflows/main.yml/badge.svg)](https://github.com/wactbprot/metis/actions/workflows/main.yml)

[api documentation](https://wactbprot.github.io/metis/)
# requirements

* java (8 or 11)
* redis (with activated keyspace notification)

# installation 

All of the `mp` state is kept in a [redis](https://redis.io) database.

## redis config

Μητις relies on [Keyspace Notifications](https://redis.io/topics/notifications).
Therefore it is necassary to replace in `/etc/redis/redis.conf`:

```shell
notify-keyspace-events ""
```

by

```shell
notify-keyspace-events AK
```
and restart the service:

```shell
# restart
$ sudo systemctl restart redis.service

# check status
$ sudo systemctl status redis.service
```

## frontend (web site)

The metis frontend is based on [UIkit](https://getuikit.com/). A fresh
metis installation needs libraries: run `./dl.sh` in metis root path
in order to install them. See the [frontend documentation](frontend.md). 

### url

* home: http://localhost:8010/
* container view: http://localhost:8010/cont/mpd-ref
* input/output elements: http://localhost:8010/elem/mpd-ref


## environment variables

There are a plenty of methods to set and activate environment variables. E.g.: 
write them to a file called `.metis` in the home directory (`~/`) and load it 
by means of your `~/.bashrc` including the line `source ~/.metis`.

| `var`                 | Description                                                       | Example                                                                                                                                   |
|---------------------- |------------------------------------------------------------------ |-------------------------------------------------------------------------------------------------------------------------------------------|
| `METIS_BUILD_ON_START`| mpds to build on server start                                     | `export METIS_BUILD_ON_START="mpd-ppc-gas_dosing"`<br>`export METIS_BUILD_ON_START="mpd-se3-servo,mpd-se3-cmp_valves,mpd-se3-cmp_state"`  |
| `METIS_DEVHUB_URL`    | url for device requests <br>(Action: TCP, VXI11, MODBUS EXECUTE)  | `export METIS_DEVHUB_URL="http://localhost:9009"`<br>`export METIS_DEVHUB_URL="http://a73434:55555"`                                      |
| `METIS_LTMEM_HOST`    | CouchDB host                                                      | `export METIS_LTMEM_HOST="127.0.0.1"`<br>`export METIS_LTMEM_HOST="a73434"`                                                               |
| `METIS_DEVPROXY_URL`  | DevProxy url                                                      | `export METIS_DEVPROXY_URL="http://localhost:8009"`                                                                                       |
| `CAL_USR`             | For password protected `vl_db_work` and `vl_db`                   | `export CAL_USR="cal"`                                                                                                                    |
| `CAL_PWD`             | see above                                                         | `export CAL_USR="<passwd>"`                                                                                                               |

# notes

## set proxy

```shell
touch ~/.m2/settings.xml 
```
fill with:

```xml
<settings>
  <proxies>
    <proxy>
      <id> ____ </id>
      <host> ____ </host>
      <port> ____ </port>
      <nonProxyHosts>localhost|*.__.__</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
```

## overcome `SSL peer shut down incorrectly` error by:

```shell
export JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
```

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
