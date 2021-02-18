FROM maven:3.3-jdk-8-onbuild AS builder
#FROM arm64v8/maven:3-adoptopenjdk-8 AS builder
RUN mkdir -p /app
WORKDIR app
COPY . /app
RUN mvn clean package

FROM java:8
COPY --from=builder /app/target/meemoo-qa-api-1.0-SNAPSHOT-shaded.jar /opt/meemoo-qa-api.jar
CMD ["java","-jar","/opt/meemoo-qa-api.jar"]
