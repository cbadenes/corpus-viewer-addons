declare -a arr=("librairy/swagger"
"librairy/nlp-service-facade"
"librairy/nlpES-service"
"librairy/nlpEN-service"
"cbadenes/Mallet"
"librairy/modeler-service-facade"
"librairy/modelerTopics-service"
"librairy/learner-service-facade"
"librairy/learnerTopics-service"
"librairy/graph-service-facade"
"librairy/graph-service"
"librairy/loader"
"cbadenes/corpus-viewer-addons"
)

for i in "${arr[@]}"
do
   echo "#### Project: $i"
   docker run -it --rm -v `pwd`/dependencies:/root/.m2 cbadenes/java-api-toolkit /bin/bash -c "git clone https://github.com/"$i" ./tmp; mvn -f ./tmp/pom.xml clean install"
done

# docker run -it --rm -v `pwd`/dependencies:/root/.m2 cbadenes/java-api-toolkit /bin/bash -c "git clone https://github.com/librairy/swagger ./tmp; mvn -f ./tmp/pom.xml clean install"