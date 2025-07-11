#!/bin/bash

# 重启app服务
docker compose stop app
docker compose rm -f app
docker compose up -d --no-deps --build app

# 重启web服务
docker compose stop web
docker compose rm -f web
docker compose up -d --no-deps --build web
