FROM eclipse-temurin:27-jdk-ubi10-minimal AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:27-jre-ubi10-minimal

RUN microdnf install -y curl \
    && microdnf clean all

WORKDIR /app
RUN groupadd --system --gid 10001 astra \
	&& useradd --system --uid 10001 --gid astra --create-home astra \
	&& mkdir -p /var/lib/astra-health/audio \
	&& chown -R astra:astra /var/lib/astra-health

COPY --from=build --chown=astra:astra /workspace/target/astra-health-backend-0.0.1-SNAPSHOT.jar /app/app.jar

USER astra
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENV ASTRA_AUDIO_STORAGE_DIR=/var/lib/astra-health/audio
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
	CMD curl --fail --silent http://localhost:8080/actuator/health
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.io.tmpdir=/tmp", "-jar", "/app/app.jar"]
