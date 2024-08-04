# syntax=docker/dockerfile:experimental

FROM oven/bun:alpine AS build-web

# Set the working directory in the Docker image
WORKDIR /app

# Copy the source code from github into the Docker image
RUN apk add git --no-cache && git clone https://github.com/Xanonymous-GitHub/sherlock-js-bundle.git /app

# Build the application
RUN bun i --frozen-lockfile && bun --bun run build

FROM gradle:8-jdk21-alpine AS build

# Set the working directory in the Docker image
WORKDIR /app

# Copy the Gradle configuration files
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts gradle.properties ./

# Copy the submodules into the Docker image
COPY --chown=gradle:gradle gumtree ./gumtree

# Load all necessary Gradle dependencies (for caching purposes)
RUN gradle wrapper \
    && ./gradlew --no-daemon dependencies

# Copy the source code into the Docker image
COPY --chown=gradle:gradle src ./src

# Copy the built web application into the Docker image
COPY --from=build-web /app/dist/assets ./src/main/resources/static/assets

# Build the application
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre-alpine AS final

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

# Copy the objectdb configuration file
COPY src/main/resources/objectdb.conf /app/objectdb.conf

ENV SECURITY_KEY=sherlock
ENV ADMIN_PASSWORD=sherlock
ENV OBJECTDB_CONF=/app/objectdb.conf

CMD ["java", "-jar", "/app/app.jar"]
