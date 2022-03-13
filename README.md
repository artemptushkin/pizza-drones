### Task

The assignment is to implement a monitor tower that monitors the air traffic of n drones used to deliver original Italian pineapple pizzas in Parma.
Each drone sends its position to the monitoring tower, at regular intervals.
The monitoring tower must:
* Be able to manage all drones connected to it simultaneously.
* Store all positions in the filesystem directly (usage of any database management system is not allowed), in a single file, in sequential order.
* Stream positions of all drones in the same order they were received by the control tower.
Bonus exercises/food for thought:
* The monitoring tower is able to provide the stream of the positions of a certain drone.
* The solution should work also with millions of positions stored.
* The system should support more than one monitoring tower.


#### Prerequisites to work with

1. Install [RSocket CLI client](https://github.com/making/rsc)
```shell
brew install making/tap/rsc
```

#### It supports more than one monitoring tower

* Run a tower
```shell
./gradlew :tower-server:bootRun
```

* Run 2 or more drone-clients
```shell
#session 1
./gradlew :drone-client:bootRun --args='--spring.rsocket.server.port=8001'

#session 2
./gradlew :drone-client:bootRun --args='--spring.rsocket.server.port=8002'
```

* Listen to the event stream from RSocket CLI
```shell
rsc --route=api.drones.locations.stream tcp://localhost:7000 --stream
```

* Or listen to the event stream of a particular drone from RSocket CLI
```shell
rsc --route=api.drone.locations.stream tcp://localhost:7000 --data=1 --stream
```

* Expect to receive stream of events, like:
```shell
{"id":5,"timestamp":1647007058,"latitude":44.8,"longitude":10.25}
{"id":1,"timestamp":1647007063,"latitude":44.8,"longitude":10.25}
{"id":5,"timestamp":1647007071,"latitude":44.8,"longitude":10.25}
{"id":5,"timestamp":1647007073,"latitude":44.8,"longitude":10.25}
{"id":1,"timestamp":1647007075,"latitude":44.8,"longitude":10.25}
{"id":110,"timestamp":1647007086,"latitude":44.8,"longitude":10.25}
{"id":110,"timestamp":1647007088,"latitude":44.8,"longitude":10.25}
{"id":1,"timestamp":1647007543,"latitude":44.8,"longitude":10.25}
```

* Expect both clients to get it in the output

### Plan

1. Database layer
* it persists a new record to the end of the file O(1)
* it returns by drone O(1)
* it indexes by drone
  - drone id - array of intervals line index [1, 3, 5]
* it dumps index every N periods ??
* it stores in concurrently saved in-memory queue
* it dumps in-memory-queue into file system every N periods
2. Drone client
* it sends drone event to the server
3. Tower server
* It stores into a database from properties
* It consumer drone events
* It streams all the events from the start
* [Optional] It streams _live_ events
* It streams by drone