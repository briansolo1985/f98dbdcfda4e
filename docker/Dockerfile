FROM bellsoft/liberica-openjdk-alpine:21 AS builder

COPY .mvn /wsrts/.mvn
COPY src /wsrts/src
COPY mvnw /wsrts/
COPY pom.xml /wsrts/

WORKDIR /wsrts

RUN ./mvnw clean install

FROM bellsoft/liberica-openjdk-alpine:21

EXPOSE 8080

COPY --from=builder /wsrts/target/weather-sensor-rts-0.0.1-SNAPSHOT.jar /wsrts/

WORKDIR /wsrts

ENTRYPOINT ["java", "-jar", "-Xms2g", "-Xmx2g", "weather-sensor-rts-0.0.1-SNAPSHOT.jar"]
