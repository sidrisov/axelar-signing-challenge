#!/bin/sh

docker stop axelar-signing-service-1 && docker rm -f axelar-signing-service-1
docker stop axelar-signing-service-2 && docker rm -f axelar-signing-service-2

docker run -d \
     --network axelar-network --network-alias signing-service-1 \
     --name axelar-signing-service-1 \
     -p 8081:8080 \
     docker.io/library/records-signing-service:0.0.1-SNAPSHOT

docker run -d \
     --network axelar-network --network-alias signing-service-2 \
     --name axelar-signing-service-2 \
     -p 8082:8080 \
     docker.io/library/records-signing-service:0.0.1-SNAPSHOT
