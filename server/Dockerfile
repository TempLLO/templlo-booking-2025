FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} eureka-server.jar

EXPOSE 19090

ENTRYPOINT ["java","-jar","/eureka-server.jar"]