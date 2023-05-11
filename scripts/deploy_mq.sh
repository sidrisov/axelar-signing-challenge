#!/bin/sh
docker run -d \
     --network axelar-network --network-alias mq --hostname mq \
     --name axelar-mq \
     -p 5672:5672 \
     -p 15672:15672 \
     -e RABBITMQ_DEFAULT_USER=root \
     -e RABBITMQ_DEFAULT_PASS=password \
     rabbitmq:3-management
