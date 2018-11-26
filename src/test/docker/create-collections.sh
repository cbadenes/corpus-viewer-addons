docker exec -it solr solr create_collection -c corpora -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-documents -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-doctopics -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-topicdocs -d sesiad-config -shards 1 -replicationFactor 1
