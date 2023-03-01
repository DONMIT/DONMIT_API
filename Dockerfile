
FROM openjdk:17-alpine

ARG JAR_FILE="build/libs/*.jar"

COPY ${JAR_FILE} app.jar

ENV    PROFILE local

CMD ["java", "-Dspring.profiles.active=${PROFILE}", "-jar","/app.jar"]