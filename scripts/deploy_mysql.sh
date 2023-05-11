#!/bin/sh
docker run -d \
     --network axelar-network --network-alias mysql \
     --name axelar-mysql \
     -p 3306:3306 \
     -e MYSQL_ROOT_PASSWORD=password \
     -e MYSQL_DATABASE=axelar_db \
     mysql:latest
