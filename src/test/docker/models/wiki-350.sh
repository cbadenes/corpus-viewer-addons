docker run -it --rm \
  --name cv_wiki_350 \
  -p 8000:7777 \
  -e "JAVA_OPTS=-Xmx4096m" \
  -e "NLP_ENDPOINT=nlp-en-service:65111" \
  -e "REST_PATH=/model" \
  --network="en_default" \
   sesiad/wiki-model:0.5-350