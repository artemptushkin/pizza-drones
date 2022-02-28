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

