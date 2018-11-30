chmod -R 777 data/
chmod -R 777 contrib/
chmod -R 777 bin/
mkdir -m 777 logs/
mkdir -m 777 graph/
docker-compose up -d
docker-compose logs -f