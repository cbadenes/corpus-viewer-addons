curl "http://localhost:8983/solr/admin/cores?action=UNLOAD&deleteInstanceDir=true&core=corpora"
curl "http://localhost:8983/solr/admin/cores?action=UNLOAD&deleteInstanceDir=true&core=documents"
curl "http://localhost:8983/solr/admin/cores?action=UNLOAD&deleteInstanceDir=true&core=doctopics"
curl "http://localhost:8983/solr/admin/cores?action=UNLOAD&deleteInstanceDir=true&core=topicdocs"
rm -rf collections/data/**