FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/gateway-service-0.0.1-SNAPSHOT.jar gateway-service.jar
CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "gateway-service.jar"]
