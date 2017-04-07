event-sourcing-experiment
=========================

I've heard lots of excitement about event sourcing as an approach for building truly HA services - able to handle rolling restarts, node shutdowns etc without flinching.  This is a stream-of-consciousness log of my experience trying to write a Kafka-backed CRUD app.

## Get kafka running

`docker-compose up -d` works locally for me (on docker-machine).
