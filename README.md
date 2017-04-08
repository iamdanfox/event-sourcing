event-sourcing-experiment
=========================

I've heard lots of excitement about event sourcing as an approach for building truly HA services - able to handle rolling restarts, node shutdowns etc without flinching.  This is a stream-of-consciousness log of my experience trying to write a Kafka-backed CRUD app.

## Get kafka running

I run `docker-compose up` to start containers with full logs, and notice I need to add a `KAFKA_ADVERTISED_PORT: 9092` environment variable.
Wipe the slate clean with `docker rm -f $(docker ps -aq)` then try again: `docker-compose up -d`

`docker ps` show me two containers running, looking good so far!

## Interact with kafka

With kafka alive, I want to try out the CLIs - figure these will be useful for debugging ETE tests.  I run `docker exec -it eventsourcingexperiment_kafka_1 bash` to get a SSH session.

Typing `kafka-` &lt;tab&gt; shows me a ton of CLI's available:

```
kafka-acls.sh                        kafka-consumer-groups.sh             kafka-producer-perf-test.sh          kafka-server-start.sh                kafka-verifiable-consumer.sh
kafka-broker-api-versions.sh         kafka-consumer-offset-checker.sh     kafka-reassign-partitions.sh         kafka-server-stop.sh                 kafka-verifiable-producer.sh
kafka-configs.sh                     kafka-consumer-perf-test.sh          kafka-replay-log-producer.sh         kafka-simple-consumer-shell.sh
kafka-console-consumer.sh            kafka-mirror-maker.sh                kafka-replica-verification.sh        kafka-streams-application-reset.sh
kafka-console-producer.sh            kafka-preferred-replica-election.sh  kafka-run-class.sh                   kafka-topics.sh
```

I try `kafka-console-producer.sh` and get a wall of usage info. Consult Google.
I find the incredibly obvious <https://kafka.apache.org/quickstart>.  Only difference is that all the CLIs are already on the classpath (so no `bin/` prefix necessary) and my zookeeper is accessible at the hostname `zookeeper` thanks to docker (not localhost).

```
$ kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic dan-is-great
Created topic "dan-is-great".

$ kafka-topics.sh --list --zookeeper zookeeper:2181
dan-is-great
```

Delightful.  I'm slightly surprised that these CLI's don't automatically pull the zookeeper info from an environment variable, but hey ho, I'll survive.

## Some toy data

Now that I've got the CLIs working, I want to try some toy data. Still SSH'd into my kafka container, I start the producer CLI:

```
kafka-console-producer.sh --broker-list localhost:9092 --topic dan-is-great
```

If you're following along at this point, I'd recommend using `<yourname>-is-great`.  Treat yourself.

I type `Hello` <enter>, `world` <enter>.  Nothing seems to have exploded yet and no warning lines in the original console.  Time to see if I can consume these!  I fire up a new shell and docker exec into `eventsourcingexperiment_kafka_1` again:

```
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic dan-is-great --from-beginning
```

The CLI spits out two lines and then holds:
```
Hello
world
```

Lovely. `Ctrl-C` to kill this consumer and then re-run produces exactly the same values.  I mash a few keys into the producer and watch them appear on the consumer CLI.  Slightly surprised at the latency... this doesn't seem to be blazing on my docker containers.

# Time for Java

The whole point of this thing was to try to build a CRUD app using event sourcing.  I think I'm going to build something to store recipes. I start by adding an API project in my `settings.gradle`:

```
include 'eventsrc'
include 'eventsrc-api'
```

I'll use jackson and the jax-rs annotations to define some server interfaces and value types.  `./gradlew eclipse` gets my IDE going.  This server interface can be re-used to create strongly typed http clients using feign.

```java
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
public interface RecipeService {

    @GET
    @Path("recipes/:id")
    RecipeResponse getRecipe(@PathParam("id") RecipeId id);

    @POST
    @Path("recipes")
    RecipeResponse createRecipe(CreateRecipe create);
}
```

The GET endpoint is pretty straightforward.  I'm just wrapping the id literal because I've got fed up of having unknown strings and longs everywhere.  It returns a full `RecipeResponse` object which includes the `RecipeId` to make things nice and RESTful.

The `createRecipe` endpoint is not idempotent - every time you call it, we'll make up an ID and store the recipe.  The body of this POST request is another `CreateRecipe` object.

I'm using Google's immutables annotation processor to make these value classes to avoid a bit of boilerplate:

```java
@Value.Immutable
@JsonSerialize
@JsonDeserialize(as = ImmutableRecipe.class)
public interface RecipeResponse {

    RecipeId id();

    String contents();
}
```

A quick serialization test ensures that my Jackson annotations work and Immutables is still generating sensible code!

```java
@Test
public void round_trip_serialization() throws Exception {
    RecipeResponse original = ImmutableRecipeResponse.builder()
            .id(RecipeId.fromString("some-id"))
            .contents("my recipe")
            .build();
    ObjectMapper mapper = new ObjectMapper();
    String string = mapper.writeValueAsString(original);
    assertThat(mapper.readValue(string, RecipeResponse.class), is(original));
}
```

# Integration test (Docker TDD)

I need to write some Java to put events into a Kafka topic using a 'Kafka producer'.
A bit of Googling suggests that I need the [`kafka-clients` Jar](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients).  
I wonder what version I should pick - matching my docker image seems like a good idea.  
Unfortunately, I'm using `latest` which isn't an immutable tag.
I don't really understand the versioning scheme, but 0.10.1.1 seems [pretty recent](https://hub.docker.com/r/wurstmeister/kafka/tags/).
I update the docker-compose.yml and add the jar to my `eventsrc.gradle`:

```groovy
dependencies {
    compile 'org.apache.kafka:kafka-clients:0.10.1.1'
}
```
