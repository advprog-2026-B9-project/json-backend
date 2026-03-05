# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew build -x test --no-daemon > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine AS runner

ARG USER_NAME=jsonuser
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

RUN addgroup -g ${USER_GID} ${USER_NAME} \
    && adduser -h /opt/app -D -u ${USER_UID} -G ${USER_NAME} ${USER_NAME}

USER ${USER_NAME}
WORKDIR /opt/app

COPY --from=builder --chown=${USER_UID}:${USER_GID} /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xmx512M", "-Xms256M", "-jar", "app.jar"]

EXPOSE 8080