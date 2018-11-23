docker run -d -P -v $PWD/collections/config:/opt/solr/server/solr -v $PWD/collections/data:/opt/solr/data --name solr -p 8983:8983 solr:7.5
docker logs -f solr