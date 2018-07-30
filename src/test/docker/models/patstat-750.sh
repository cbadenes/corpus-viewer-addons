docker run -it --rm \
  -p 8003:7777 \
  -e "JAVA_OPTS=-Xmx8192m" \
  -e "NLP_ENDPOINT=nlp-en-service:65111" \
  -e "REST_PATH=/model" \
  --network="en_default" \
   sesiad/patstat-model:0.2-750