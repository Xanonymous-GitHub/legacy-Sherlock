# syntax=docker/dockerfile:experimental

FROM gradle:8-jdk21-alpine AS build

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
RUN ./gradlew --no-daemon bootWar

# Get the jetty modules from the ./jetty_modules.ini file outside.
COPY --chown=gradle:gradle jetty_modules.ini ./

# Save the jetty modules as a file for the final stage.
RUN grep -E '^[^;].+' jetty_modules.ini | cut -d';' -f1 | tr '\n' ',' | sed 's/,$//' > MODULES

FROM jetty:12-jre21 AS final

# Set the working directory in the Docker image
WORKDIR /var/lib/jetty

# Copy the built jar file from the build stage
COPY --from=build --chown=jetty:jetty /app/build/out/*.war /usr/local/jetty/webapps/ROOT.war

# Copy the jetty modules from the build stage
COPY --from=build --chown=jetty:jetty /app/MODULES ./MODULES

# Change the home directory to /sherlock
#RUN usermod -d /sherlock jetty

COPY --chown=jetty:jetty docker-entrypoint.sh ./

ENTRYPOINT ["./docker-entrypoint.sh"]
