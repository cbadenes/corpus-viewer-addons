echo "downloading CORDIS corpus"
mkdir -p corpora/cordis
curl -o corpora/cordis/documents.jsonl.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/woBzdYWfJtJ6sfY/download
#curl -o corpora/cordis/bows.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/yHNCie2M8yTN4mY/download
curl -o corpora/cordis/doctopics-70.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/aMBsQaTM4oBi3Ga/download
#curl -o corpora/cordis/doctopics-150.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/WWHprbHxWigBMEC/download
#
#echo "downloading Wikipedia corpus"
#mkdir -p corpora/wikipedia
#curl -o corpora/wikipedia/documents.jsonl.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/SggPgAJwqrSGZsN/download
#curl -o corpora/wikipedia/bows.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/oxMStbmYrXpkFmS/download
#curl -o corpora/wikipedia/doctopics-120.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/qJRqT3mnyPjTHjE/download
#curl -o corpora/wikipedia/doctopics-350.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/QMjTxEoHr5CTALx/download
#
#echo "downloading PATSTAT corpus"
#mkdir -p corpora/patstat
#curl -o corpora/patstat/documents.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/zc3eeoanyZRac4G/download
#curl -o corpora/patstat/bows.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/RqwBJ8PnGsT5Rgw/download
#curl -o corpora/patstat/doctopics-250.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/mG5Lwsii2CosERa/download
#curl -o corpora/patstat/doctopics-750.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/kTD8QEagJEyff3z/download