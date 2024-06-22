FROM gcr.io/distroless/java21-debian12

LABEL org.opencontainers.image.source=https://github.com/minibuildsio/odyssey-api

WORKDIR /usr/app/
COPY build/libs/*-all.jar app.jar
EXPOSE 8080
CMD ["app.jar"]
