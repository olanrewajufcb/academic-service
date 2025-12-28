
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/academic-service.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
