# Fuel Consumption Estimator

Estimates fuel consumption for ships.

## Assumptions

* All timestamps are given in UTC
* Routes in requests always contain more than one time/distance point 
* Routes in requests are represented as 2-dimensional arrays where each sub array represents a distinct route

## Basic Design
* The project has been set up as a multi-module project with dependencies only on relevant modules
* The project has been designed using functional programming principles. The only place a state is kept is in the write-through cache
* Reactive streams have been used throughout the project. E.g. is has been used to process parsing of csv files in parallel for get a faster through put of the parsing of csv rows   
* IO monads from Cat effects have been used as the concurrency model

## Solution choices made

1. Dao - uses euclidean distance to get the row that has the closes distance between the given variables
1. Parser - parses every type of csv to individual types - for convenience it parses it to the same basic type before persisting it
1. Cache - Here I have implemented a simple write through cache with a hash map as data structure
1. Service - Uses Harvesine distance between the coordinates in km multiplied with a factor 1.852 to get nautical miles  

## Libraries/Tools used

* SBT - multi-module project
* FS2 streams - provides concurrent functional reactive streams 
* Postgres database - for the persistence layer
* Doobie - functional layer for JDBC for the dao layer
* Http4s - functional http server and client
* Circe - functional json library via type classes 
* Cats - as generic functional library
* Uses ScalaTest and Postgres docker container for unit testing.

## How to setup and run the project

1. It is assumed that the project is run on a unix based env. The user will have to modify the bash scripts if there is a wish to run it on Windows
2. Make sure that java version 17 is on the run path 
3. Make sure that bin/bash is on the run path
4. Make sure that psql version 14.5 is on the run path
5. Make sure an instance of postgres version 14.5 is running on localhost port 5432
6. cd into bin and make sure the user has execution right to the scripts  
7. execute `./setup_db.bash` - it sets up and parses the data into the database 
8. execute `./run_server.bash` - it runs the rest server on localhost:8080 
9. execute `./send_request.bash` - it sends a request to the server via curl

## Running tests
1. To run the tests make sure that sbt version 1.7.1 is installed an on the run path
2. To run the tests run `sbt test` from the project root.

## If I had more time to spend

If I had more time to spend I would:
* Spend time on figuring out a way to fill out the missing data points in the data to give a more accurate fuel consumption result that by Euclidean Distance
* Write more unit test - the project is only partially tested and there are for instance no unit tests for the service business logic for instance
* Write some integration tests. 
* Have persisted the individual data types as their own tables and used their additional heuristics in the calculation.
* Have implemented a clearing of the cache - although this may not really be important as usage over 100 years would only give ~ 36500 cache entries.
* More elaborate error handling for the api and more elaborate logging in general - there is only logging of unsuccessful parser attempts 