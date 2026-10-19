# syntax=docker/dockerfile:1

# ---- build -------------------------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# BuildKit cache mount keeps the Maven repository between builds; tests are run in CI / locally, not in the image
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package && cp target/online_shopping-*.jar /build/app.jar

# ---- runtime -----------------------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app --home-dir /app app \
    && mkdir -p /app /data/images && chown -R app:app /app /data
WORKDIR /app
COPY --from=build --chown=app:app /build/app.jar app.jar
USER app
ENV TZ=UTC \
    REGGIE_IMAGE_PATH=/data/images/ \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
VOLUME /data/images
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
