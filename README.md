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