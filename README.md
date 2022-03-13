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

1. JDK 17+

3. Install [RSocket CLI client](https://github.com/making/rsc)
```shell
brew install making/tap/rsc
```

#### It supports more than one monitoring tower

* Run 2 tower-servers
```shell
#session 1
./gradlew :tower-server:bootRun --args='--spring.rsocket.server.port=7001 --server.port=8081'

#session 2
./gradlew :tower-server:bootRun --args='--spring.rsocket.server.port=7002 --server.port=8082'
```

* Run 2 drones, one per server
```shell
#session 1
./gradlew :drone-client:bootRun --args='--spring.rsocket.server.port=8001 --tower-server.port=7001'

#session 2
./gradlew :drone-client:bootRun --args='--spring.rsocket.server.port=8002 --tower-server.port=7002'
```

* Listen to the event stream of all the drones from RSocket CLI
```shell
#session 1
rsc --route=api.drones.locations.stream tcp://localhost:7001 --stream

#session 2
rsc --route=api.drones.locations.stream tcp://localhost:7002 --stream
```

* Or listen to the event stream of a particular drone from RSocket CLI
```shell
rsc --route=api.drone.locations.stream tcp://localhost:7001 --data=1 --stream
```

* Expect to receive stream of events in each tower-server session, like:
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

### Technical stack

1. [RSocket](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#rsocket) to get light live stream events from drone
2. [JGroups](http://www.jgroups.org/) to share events within the tower-server cluster