docker exec -it solr solr create_collection -c corpora -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-documents -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-doctopics -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-topicdocs -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c cordis-similarities -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c wikipedia-documents -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c wikipedia-doctopics -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c wikipedia-topicdocs -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c wikipedia-similarities -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c patents-documents -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c patents-doctopics -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c patents-topicdocs -d sesiad-config -shards 1 -replicationFactor 1
docker exec -it solr solr create_collection -c patents-similarities -d sesiad-config -shards 1 -replicationFactor 1