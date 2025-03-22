FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/eureka-server-0.0.1-SNAPSHOT.jar eureka-service.jar
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "eureka-service.jar"]
