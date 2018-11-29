echo "loading CORDIS documents"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisDocuments test

echo "loading CORDIS doctopics"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisDocTopics test

echo "loading CORDIS LDAtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadCordisLDATopicDocs test

echo "loading PATENTS DTMtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadPatentsDTMTopicDocs test

echo "loading PATENTS CTMtopicdocs"
mvn -DargLine="-Xmx4096m" -Dtest=LoadPatentsCTMTopicDocs test

echo "all corpora are ready"