#!/usr/bin/env sh
set -ex

DESTINATION=/home/ubuntu/docker-compose

wget --retry-connrefused \
     --waitretry=1 \
     --read-timeout 20 \
     --timeout 15 \
     -t 10 \
     -qO- "https://github.com/docker/compose/releases/download/1.11.2/docker-compose-`uname -s`-`uname -m`" \
     > $DESTINATION

chmod +x /home/ubuntu/docker-compose
