# CoWin 

This project develop a basic working component of required to develop India's famous Cowin application. Objective of this application is to develop core APIs and demonstrate capabilities required and potential challenges.

## Content

  * [Requirements](#requirements)
    + [Use Cases](#use-cases)
    + [Non-functional Requirements](#non-functional-requirements)
  * [Architecture](#architecture)
    + [Application Architecture](#application-architecture)
    + [Data Architecture](#data-architecture)
    + [Solution Design Decisions](#solution-design-decisions)
    + [API Documentation](#api-documentation)
  * [How to Build and Run?](#how-to-build-and-run)
    + [Build Source Code](#build-project)
    + [Build Docker Container](#build-docker-container)
    + [Run Docker Container](#run-container) 
  * [Performance Test](#performance-test)
    + [Performance Goals](#performance-goals)
    + [Scaling to 4500 Transactions per second](#scaling-to-4500-transactions-per-second)
  * [List of Tasks](#project-tasks)



## Requirements

### Use Cases

1. Search for Available Slots : Finds available slots for given location and date
2. Reserve a slot for the given location, date and user. Once booked user needs to be notified and available slots to be decremented
3. Admin - Register Vax centers, add/modify slots
4. View number of bookings per day across vax centers, Generate drill-down reports

### Non-functional Requirements

1. Scale for 3000 requests per second
2. Latency <30 ms
3. Application state needs to be consistent
4. A single user should not be allowed more than 1 booking
5. Application needs to be secured

## Architecture

### Application Architecture

Key functionality of this application is ability to search and book a slot. It is expected as slots open up at certain time during the day, burst of user requests are expected and slot booking needs to be fair and quick.

A live dashboard and constantly updated available number of slots needs to be transparently available to the users. Once successfully booked, users can be notified through sms/whatsapp.

![Application Architecture](./resources/image/Application_Architecture.png "Application Architecture")

### Internal Data Structure

Ensuring number of accepted bookings are exactly same as available slots even during highly concurrent asynchronous bookings.

#### Logic for booking a slot

```
Add to Queue
	-	Find out vax center id and slot start time
	- 	Add to the queue with (center_id + slot_start_time) as key, Object [center_id,slot_start_time, citizen id]
	
Queue Consumer
	-  Consumes one message at at time from a queue
	-  Performs Validation - Has the citizen already booked a slot or recently vaccinated?
		- If yes, send a rejection message. move to the next message
		- If no, check if any slots are available. If yes, book one and send confirmation message
			- If not slots are available , stop reading further message. Do not acknowledge
```

#### Considerations for Possible solutions

##### TPS 

30000 per second read
3000 per second write 

Redis and Postgres exhibits similar performance numbers for this kind of throughput. Details [here](https://www.cybertec-postgresql.com/en/postgresql-vs-redis-vs-memcached-performance/)

##### Data Volume

1 person record = 1 KB (estimated)
1.4 billion = 1.4 Billion KB = 1.4 TB of data
Postgres can handle this data volume. 
Redis needs to be periodically written back to persistent store

##### Queue Count
Number of Vaccination centers * Dates
(19,101 PINs spanning 154,725 post offices)
Assuming 100,000*30 = 3,000,000
Assuming 50 KB per queue = 150 GB of live data

Redis would turn to be quite costly.

##### Possible Architecture Options

Combination of Message Queues and Database

| Sl No	   | Option | Pros | Cons | Notes |
| -------- | -------  | -------   | -------      | ------- |
| 1		   | Message Queues - RabbitMQ, ActiveMQ   |Fast, Can scale  | Needs distributed version | |
| 2		   | Redis Streams   | Fast, support append-only|  Costly Scalability| |
| 3 	   | ACID-compliant Database   | Fast, Simple|Performance can be slower for higher number of transactions | |
| 4 	   | Key-Value NoSQL   | Fast, Scalable| May need to manage transactions | |


### Data Architecture

Logic data model consists of few entities with ability to assign vaccination slots for given vax center, date/time and ability for a person to book or cancel.

![Data Model](resources/image/Data_Model.png "Data Model")


 
### Solution Design Decisions


| Sl No	   | Decision | Rationale | Alternatives | Notes |
| -------- | -------  | -------   | -------      | ------- |
| 1		   | Language - Java   | Easy to start , enterprise grade| Python, Golang, NodeJs | |
| 2		   | Microservice, EDA as architecture patterns | Horizontal Scalability| Monolith | |
| 3		   | Redis as In-Memory database | Distributed cache, Low latency, fast , high throughput| RDBMS, NoSQL | Data needs to be persisted |


### API Documentation

Following APIs would be implemented as designed part of this exercise :

[APIs to build using OpenAPI Specs](https://documenter.getpostman.com/view/28972773/2s9XxyRtkk)

Note : API URI pattern is designed for readibility than resource, follows more of [this pattern](https://cloud.google.com/apis/design/naming_convention)

![OpenAPI Documentation](./resources/image/OpenAPI.png "API Documentation")


## How to Build and Run?


Code is developed using java and build using Maven. Docker containers can be built and can be run.

### Build Project

Clone project from git (https://github.com/souravt/cowin-backend) and then build project using Maven. Java (>17) and Maven needs to be installed.

```
mvn clean install
```


### Build Docker Container

Run following command from project root folder to build docker container .

``` 
docker build . --tag cowin-api
```

### Run Container

Run following command from project root folder to build docker container :

```
docker run -p 9091:9091 --cpu-period=50000 --cpu-quota=25000 --memory=1024m --name cowin-api cowin-api 
```

Run a curl command to test :

```
curl http://localhost:9091/ping
```

One should receive a response "Pong". Bingo!




## Performance Test

While developing this application and ensuring basics like unit testing, static code analysis and consistent application architecture, once of the most crucial aspect is ability to scale and perform without any performance degradation.

A performance test suite using JMeter is designed up-front for continuous validation.

Test Script  : ![Performance Test Script](./resources/CoWin_API.jmx "Performance Test Script")

### Performance Goals

1. Zero server internal error (except of building back pressure)
2. Response time 99% percentile <50 milliseconds
3. Ability to horizontally scale

### Scaling to 4500 Transactions per second

Transaction/s : 4576
Response Time (Avg): 5.8 ms
Errors : None
CPU : 0.28%
Memory: 422 MB


![Performance Test Results](./resources/image/Test_Result_20000.png "Performance Test Results")


 
## Project Tasks
- [x] Setup Environment (Source Control, IDE, Build, Project Scaffolding)
- [x] Design APIs using OpenAPI
- [x] Develop working APIs for registration, search for available slots and slot booking
- [x] Build, Deploy and Run 
- [x] Build performance test suit to benchmark performance
- [] Redis integration
- [] Messaging integration
- [] Add Authentication
- [x] Logging
- [] Error handling
- [] Notification confirmation
- [] Isolate services to scale


 


