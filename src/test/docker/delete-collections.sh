curl "http://localhost:8983/solr/admin/collections?action=DELETE&name=corpora"
curl "http://localhost:8983/solr/admin/collections?action=DELETE&name=cordis-documents"
curl "http://localhost:8983/solr/admin/collections?action=DELETE&name=cordis-doctopics"
curl "http://localhost:8983/solr/admin/collections?action=DELETE&name=cordis-topicdocs"
curl "http://localhost:8983/solr/admin/configs?action=DELETE&name=corpora"
curl "http://localhost:8983/solr/admin/configs?action=DELETE&name=cordis-documents"
curl "http://localhost:8983/solr/admin/configs?action=DELETE&name=cordis-doctopics"
curl "http://localhost:8983/solr/admin/configs?action=DELETE&name=cordis-topicdocs"
rm -rf data/cordis-*