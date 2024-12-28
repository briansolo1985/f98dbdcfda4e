# Weather Sensor Realtime Streaming Application

### 1. Purpose
The purpose of this project is to demonstrate a web service capable of registering sensors, and ingest metrics sent by the registered sensors. It is assumed that this project is only  a POC aiming to demonstrate the capabilities of the application, the API contract, the web service structure. Also it is assumed that sensors/metrics will be registered/sent on a low level scale, as the current solution is not targeting to resolve scalability. Also security is out of context.  

### 2. Developer setup
JDK 21 is required for this project. You can use any IDEs to import the project but IntelliJ IDEA is recommended.
Once the project is imported, please configure the IDE to use `intellij-java-google-style.xml` for code formatting.

### 3. Build and run
The application can be built by issuing `./mvnw clean install` in the project root directory.
The above command will build the runnable fat jar and also will run the unit and integration tests.
Once the process is done, go to the `target` directory and issue the following command, which will start the application: `java -jar -Xms2g -Xmx2g weather-sensor-rts-0.0.1-SNAPSHOT.jar"`
<br>However it is encouraged to use `./wsrts.sh` command in project root directory. This will perform the above steps in a dockerized build environment, will create a lightweight docker image and starts the containerized web service. You will need Docker installed and Docker daemon running to perform this step.
<br>A full build should take approximately 80 seconds on an Intel Core i7 8850H machine.

### 4. Using the application
After the application has been started via either method, the web service listens on port 8080.
Swagger is added for API documentation and also for providing a convenient way to test the application.
You can reach Swagger UI via `http://localhost:8080/swagger-ui/index.html`
<br><br>List of available API endpoints:
* sensor-controller
    * GET /sensors: the list of registered sensors can be retrieved
    * POST /sensors: a sensor can be registered via this endpoint. The `id` field is mandatory, `country` and `city` fields are optional.
* sensor-metric-controller
  * POST /metrics/sensors/{id}: once a sensor is registered, metrics can be sent and persisted for the given sensor id. Available fields are `temperature, humidity, windSpeed, airPressure` in double format, and an additional `timestamp` filed which is a long type field and represents the number of elapsed milliseconds since epoch (UNIX epoch milliseconds format). If a timestamp is not provided the server's current timestamp will be used for storing the metric.
* metric-query-controller
  * GET /query: the metrics can be queried for given sensor(s).
    * Sensor ids can be specified via the `sensorIds` field. If no value is provided, all sensors data will be queried. 
    * There are two operation modes:
      * `range` parameter is not provided: the latest metric is fetched for the given sensor. The `rangeEnd` parameter is ignored in this case.
      * `range` parameter is provided: the average of the metrics for the given time period will be calculated. <br>The range format is in Java duration format, for example `PT15M` means a window of 15 minutes, `P2D` means a window of 2 days, `P1DT2H3M` means a window of 1 day 2 hours and three minutes. If `rangeEnd` parameter is not provided, the queried period will be calculated from the `current timestamp minus the provided duration` until the `current timestamp`. <br> This can be fin tuned with the `rangeEnd` parameter, which will be used instead of the current timestamp when calculating the query period.
    * The results field set can be filtered via `fields`. If none or illegal field names are provided, the full attribute list is returned.

### 5. Architectural and design considerations
* Spring Boot is used for implementing the web application. Spring Boot make is easy to create web applications in really fast way. Alternatives could be Quarkus and Micronaut, which might perform better or provide lower memory or CPU footprints.
* Tomcat is used as default web server. In a real production application it could be replaced by Jetty, which provide better throughput and less CPU usage.
* Servlet vs Reactive: given this would be a high throughput application, using a reactive approach would seem natural as it provides extremely good throughput when operating on high scales eg with high number of agents. However in real life reactive paradigm increases the complexity of the application, and makes it harder to debug. Since virtual threads were introduced in JDK 21, and the frequently used web server implementations like Tomcat or Jetty supports them, we can rely on virtual threads instead of the reactive approach. In this way by going on the usual, well-known way of programming we can still achieve comparable performance.
* The web application is stateless, state is stored in an external database. This enables the application to be scaled up with increased traffic. The application is released as a docker container, and if integrated properly in containerization management platform like Kubernetes, the scalability problem can be addressed with less effort.
* The web application is three layered: web, service and persistence (repository) layers, separating and encapsulating the related code respectively. In real life those would be separate Maven modules, in this POC that would have been an overkill.
* Test coverage is minimal. I just wanted to demonstrate how unit and integration test would be implemented and which tools would have been used.

### 6. Database
* Choosing the right database is crucial
* In the POC I chose H2 in-memory relational database, which integrates seamlessly with Spring Boot and enables rapid development. However it needs to be emphasized that H2 is not suitable for a production grade application
* Choosing a relational database for this use case is also not optimal:
  * By relaxing the transaction isolation levels and not using constraints like foreign keys we can achieve fairly high throughput for data ingestion. However relation databases has a limit on table sizes, even it being quite large, we would hit that limit at one point.
  * Besides data ingestion, the database also needs to be optimized for querying. Currently the two keys used for filtering are sensorId and timestamp. Both needs to be indexed for performant queries. While RDBMS B-Tree indexes are sufficient for interval queries, at a high scale those wouldn't be performant for many different timestamp values.
  * The database needs to be highly available. If we scale the web application, the database needs to be scaled as well, otherwise the database will be the bottleneck.
  * RDBMS schema is rigid. There can be various sensors with different metrics. A database with flexible schema can be better at this point.
* After highlighting the above points the ideal database should be:
  * Clustered, horizontally scalable
  * Can be optimized for high scale data ingestion as well as date analysis
  * Highly available
  * Provides flexible schema
  * Has Java / Spring Boot integration support
* For this use case a NoSQL database is a decent choice
  * Document store databases: Elasticsearch and MongoDB are well-known and battle tested databases. Both provides clusterization, scalability, high availability and flexible schemas. Both can be used for storing and querying timeseries data. This would be a mediocre solution as those databases are not optimized for timeseries data.
  * Dedicated timeseries databases: InfluxDB or TimescaleDB. Both are optimized for ingesting and storing high scales of timeseries data. The problematic part is the query side. If we need to provide OLAP style querying, these won't be ideal solution, but that's really depends on the query types.
  * Columnar store timeseries database: Apache Druid or Snowflake. These databases provide to ingest high amount of data, and also provides a way for real time analysis.
* In summary for creating a quick proof of concept, H2 database is adequate, however for a production grade solution we would need to use a NoSQL database. Apache Druid seems to be good choice.

