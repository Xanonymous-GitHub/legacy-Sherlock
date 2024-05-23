# syntax=docker/dockerfile:experimental

FROM gradle:8-jdk22-alpine AS build

# Set the working directory in the Docker image
WORKDIR /app

# Copy the Gradle configuration files
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts gradle.properties ./

# Load all necessary Gradle dependencies (for caching purposes)
RUN gradle wrapper \
    && ./gradlew --no-daemon dependencies

# Copy the source code into the Docker image
COPY --chown=gradle:gradle src ./src

# Build the application
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:22-jre-alpine AS final

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
# Only for sherlock: we SHOULD create a home directory for the user, since the app will write to it.
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/sherlock" \
    --shell "/sbin/nologin" \
    --uid "${UID}" \
    appuser
USER appuser

# Set the working directory in the Docker image
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build --chown=appuser:appuser /app/build/out/*.jar ./app.jar

ENV SECURITY_KEY=sherlock
ENV ADMIN_PASSWORD=sherlock

CMD ["java", "-jar", "/app/app.jar"]
