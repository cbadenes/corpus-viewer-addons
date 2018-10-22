docker run -it --rm \
  --name cv_cordis_150 \
  -p 8000:7777 \
  -e "JAVA_OPTS=-Xmx4096m" \
  -e "NLP_ENDPOINT=nlp-en-service:65111" \
  -e "REST_PATH=/model" \
  --network="en_default" \
   sesiad/cordis-model:0.5-150