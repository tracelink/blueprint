FROM adoptopenjdk/openjdk11:ubi-slim as base

FROM base as build
RUN dnf install maven -y
COPY . /tmp/blueprint
WORKDIR /tmp/blueprint
RUN mvn clean package -e

FROM base
COPY --from=build /tmp/blueprint/blueprint-app/target/blueprint*SNAPSHOT.jar /opt/blueprint/blueprint.jar
RUN useradd blueprintuser && \
    chown -R blueprintuser /opt/blueprint/
USER blueprintuser
WORKDIR /opt/blueprint/
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/opt/blueprint/blueprint.jar"]
