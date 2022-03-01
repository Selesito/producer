FROM maven:3.6.3-openjdk-11
RUN mkdir producer
WORKDIR producer
COPY . .
RUN mvn package -Dmaven.test.skip=true
EXPOSE 8081
CMD ["java", "-jar", "target/producer-0.0.1-SNAPSHOT.jar"]
