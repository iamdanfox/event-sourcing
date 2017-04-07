event-sourcing-experiment
=========================

I've heard lots of excitement about event sourcing as an approach for building truly HA services - able to handle rolling restarts, node shutdowns etc without flinching.  This is a stream-of-consciousness log of my experience trying to write a Kafka-backed CRUD app.

## Get kafka running

I run `docker-compose up` to start containers with full logs, and notice I need to add a `KAFKA_ADVERTISED_PORT: 9092` environment variable.
Wipe the slate clean with `docker rm -f $(docker ps -aq)` then try again: `docker-compose up -d`

`docker ps` show me two containers running, looking good so far!

## Interact with kafka

With kafka alive, I want to put some toy data into it to get a feel for how topics, partitions and consumer groups all work.  I run `docker exec -it eventsourcingexperiment_kafka_1 bash` to get a SSH session.

Typing `kafka-` &lt;tab&gt; shows me a ton of CLI's available:

```
kafka-acls.sh                        kafka-consumer-groups.sh             kafka-producer-perf-test.sh          kafka-server-start.sh                kafka-verifiable-consumer.sh
kafka-broker-api-versions.sh         kafka-consumer-offset-checker.sh     kafka-reassign-partitions.sh         kafka-server-stop.sh                 kafka-verifiable-producer.sh
kafka-configs.sh                     kafka-consumer-perf-test.sh          kafka-replay-log-producer.sh         kafka-simple-consumer-shell.sh
kafka-console-consumer.sh            kafka-mirror-maker.sh                kafka-replica-verification.sh        kafka-streams-application-reset.sh
kafka-console-producer.sh            kafka-preferred-replica-election.sh  kafka-run-class.sh                   kafka-topics.sh
```

I try `kafka-console-producer.sh` and get a wall of usage info. Consult Google.
I find the incredibly obvious <https://kafka.apache.org/quickstart>.

```
$ kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic dan-is-great
Created topic "dan-is-great".
```

Delightful.  I'm slightly surprised that these CLI's don't automatically pull the zookeeper info from an environment variable, but hey ho, I'll survive.
