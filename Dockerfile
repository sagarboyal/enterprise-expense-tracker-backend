FROM openjdk:17-alpine
WORKDIR /app
COPY target/enterpriseexpensemanagementsystem-0.0.1-SNAPSHOT.jar trex-app.jar
EXPOSE 8080
CMD [ "java", "-jar", "trex-app.jar"]
