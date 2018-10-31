echo "downloading CORDIS corpus"
mkdir -p corpora/cordis
curl -o corpora/cordis/documents.jsonl.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/woBzdYWfJtJ6sfY/download
curl -o corpora/cordis/doctopics-70.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/82z5WRNbYftqr2L/download
#curl -o corpora/cordis/doctopics-150.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/idRqeNyxNTW2T4z/download
#
#echo "downloading Wikipedia corpus"
#mkdir -p corpora/wikipedia
#curl -o corpora/wikipedia/documents.jsonl.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/aiAHjQGZT4QD4Yj/download
#curl -o corpora/wikipedia/doctopics-120.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/obxFqEspnTAARSE/download
#curl -o corpora/wikipedia/doctopics-350.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/DxjgdR6zpc977yb/download
#
#echo "downloading PATSTAT corpus"
#mkdir -p corpora/patstat
#curl -o corpora/patstat/documents.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/SEdjnweQG3boBYK/download
#curl -o corpora/patstat/doctopics-250.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/eaf6mSEZ4bQr2WT/download
#curl -o corpora/patstat/doctopics-750.csv.gz https://delicias.dia.fi.upm.es/nextcloud/index.php/s/4FJtpLxM9qa7QiA/download