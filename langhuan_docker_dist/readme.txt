docker ps -a | grep app
docker compose stop app
docker compose rm -f app
docker compose up -d --no-deps --build app

docker ps -a | grep web
docker compose stop web
docker compose rm -f web
docker compose up -d --no-deps --build web

-----------------------------------


docker-compose down
docker rmi 镜像
docker-compose up -d

