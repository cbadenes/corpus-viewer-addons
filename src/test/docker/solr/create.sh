docker run -d -P -v $PWD/collections/config:/opt/solr/server/solr -v $PWD/collections/data:/opt/solr/data --name solr -p 8983:8983 solr:7.5
sleep 5
docker exec -it solr solr create_core -c corpora
docker exec -it solr solr create_core -c cordis-docs
docker exec -it solr solr create_core -c cordis-doctopics-70
docker exec -it solr solr create_core -c cordis-topicdocs-70
docker exec -it solr solr create_core -c cordis-doctopics-150
docker exec -it solr solr create_core -c cordis-topicdocs-150
docker exec -it solr solr create_core -c wiki-docs
docker exec -it solr solr create_core -c wiki-doctopics-120
docker exec -it solr solr create_core -c wiki-topicdocs-120
docker exec -it solr solr create_core -c wiki-doctopics-350
docker exec -it solr solr create_core -c wiki-topicdocs-350
docker exec -it solr solr create_core -c patstat-docs
docker exec -it solr solr create_core -c patstat-doctopics-250
docker exec -it solr solr create_core -c patstat-topicdocs-250
docker exec -it solr solr create_core -c patstat-doctopics-750
docker exec -it solr solr create_core -c patstat-topicdocs-750
docker logs -f solr