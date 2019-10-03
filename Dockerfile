FROM maven:3.6.0-jdk-11-slim

COPY src /home/money-transfer/src
COPY pom.xml /home/money-transfer
RUN mvn -f /home/money-transfer/pom.xml clean install -DskipTests=true
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "/home/money-transfer/target/money-transfer-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]