FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/comment-service-0.0.1-SNAPSHOT.jar comment-service.jar
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "comment-service.jar"]
