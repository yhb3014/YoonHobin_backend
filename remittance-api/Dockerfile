FROM gradle:8.14-jdk17 AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle :remittance-api:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /home/gradle/src/remittance-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]