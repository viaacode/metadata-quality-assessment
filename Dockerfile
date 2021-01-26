FROM maven:3.3-jdk-8-onbuild

FROM java:8
COPY --from=0 /usr/src/app/target/meemoo-qa-api-1.0-SNAPSHOT-shaded.jar /opt/meemoo-qa-api.jar
CMD ["java","-jar","/opt/meemoo-qa-api.jar"]
