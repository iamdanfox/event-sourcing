#!/usr/bin/env sh
set -e

cd /home/ubuntu/event-sourcing-experiment

# wait for docker to switch on
until docker ps; do sleep 1; done

/home/ubuntu/docker-compose pull --ignore-pull-failures
