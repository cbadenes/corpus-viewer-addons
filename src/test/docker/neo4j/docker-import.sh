if [ -f data ] ; then
        rm -rf data/
   else
mkdir -p data/import
cp ../../../../output/graphs/$1/*.* data/import/
docker run -it --rm --name "neo4j-import" \
    --volume=$PWD/data:/data \
    -it neo4j:3.5 /bin/bash -c "/var/lib/neo4j/bin/neo4j-admin import --mode=csv --database=graph.db --nodes /data/import/nodes-header.csv,/data/import/nodes.csv.gz --relationships /data/import/edges-header.csv,/data/import/edges.csv.gz"