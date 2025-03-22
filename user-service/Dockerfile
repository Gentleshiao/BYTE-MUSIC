FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "user-service.jar"]
