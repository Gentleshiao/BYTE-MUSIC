FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/music-service-0.0.1-SNAPSHOT.jar music-service.jar
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "music-service.jar"]
