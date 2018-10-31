docker run -d -P -v $PWD/collections/config:/opt/solr/server/solr -v $PWD/collections/data:/opt/solr/data --name solr -p 8983:8983 solr:7.5
sleep 5
docker exec -it solr solr create_core -c corpora
docker exec -it solr solr create_core -c documents
docker exec -it solr solr create_core -c doctopics
docker exec -it solr solr create_core -c topicdocs
docker logs -f solr