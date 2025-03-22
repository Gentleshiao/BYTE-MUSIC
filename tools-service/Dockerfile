FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/tools-service-0.0.1-SNAPSHOT.jar tools-service.jar
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "tools-service.jar"]
