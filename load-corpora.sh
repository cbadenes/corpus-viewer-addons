echo "loading CORDIS documents"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisDocuments test

echo "loading CORDIS doctopics"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisDocTopics test

echo "loading CORDIS LDAtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisLDATopicDocs test

echo "loading Wikipedia documents"
mvn -DargLine="-Xmx4096m" -Dtest=LoadWikipediaDocuments test

echo "loading Wikipedia doctopics"
mvn -DargLine="-Xmx4096m" -Dtest=LoadWikipediaDocTopics test

echo "loading Wikipedia LDAtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadWikipediaLDATopicDocs test

echo "loading Patents documents"
mvn -DargLine="-Xmx8096m" -Dtest=LoadPatentsDocuments test

echo "loading Patents doctopics"
mvn -DargLine="-Xmx8096m" -Dtest=LoadPatentsDocTopics test

echo "loading Patents LDAtopicdocs"
mvn -DargLine="-Xmx8096m" -Dtest=LoadPatentsLDATopicDocs test

echo "loading Patents DTMtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadPatentsDTMTopicDocs test

echo "loading Patents CTMtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadPatentsCTMTopicDocs test

echo "Alarm Service"
mvn -DargLine="-Xmx8096m" -Dtest=AlarmServiceTest test

echo "Hierarchical Queries "
mvn -DargLine="-Xmx8096m" -Dtest=HierarchicalQueriesTest test

echo "Topic Groups Queries "
mvn -DargLine="-Xmx8096m" -Dtest=TopicGroupQueriesTest test

echo "Graph Service"
mvn -DargLine="-Xmx8096m" -Dtest=GraphServiceTest test

echo "loading CORDIS similarities"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisSimilarities test

echo "loading Wikipedia similarities"
mvn -DargLine="-Xmx8096m" -Dtest=LoadWikipediaSimilarities test

echo "loading Patents similarities"
mvn -DargLine="-Xmx8096m" -Dtest=LoadPatentsSimilarities test

echo "all corpora are ready"