# frontend description

## glosary

**mpd** ... measurement program definition: JSON document stored in
CouchDB defining the steps for a calibration or measurement.

**container** ... part of a mpd that groups measurement sequences.

## overview

<img src="frontend_i.jpeg" width="400">

1. id of the mpd
2. status of the mpd (_active_ means ready to run)
3. description of the mpd
4. container title
5. container status:
* READY ... container is ready to start
* RUN ... container is running; the tasks of the container are being
  executed
* ERROR ... a task of the container returned an ERROR, task execution
  is interrupted
* MON ... container runs constantly (e.g. monitoring measurements,
  restarts if all tasks in the container are executed)
* SUSPEND ... execution of container tasks is stopped at point
6. number of the container
7. show/hide container content
