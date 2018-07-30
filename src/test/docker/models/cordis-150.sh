docker run -it --rm \
  -p 8001:7777 \
  -e "JAVA_OPTS=-Xmx4096m" \
  -e "NLP_ENDPOINT=nlp-en-service:65111" \
  -e "REST_PATH=/model" \
  --network="en_default" \
   sesiad/cordis-model:0.2-150