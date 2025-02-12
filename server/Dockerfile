FROM --platform=$TARGETOS/$TARGETARCH eclipse-temurin:23-jre-alpine

WORKDIR /usr/app
COPY build/install/server .

EXPOSE 8080

ENTRYPOINT ["/usr/app/bin/server"]
