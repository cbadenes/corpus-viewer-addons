# Corpus Viewer Addons

`Corpus-Viewer-Addons` provides a set of distributed services to analyze texts from large document corpora in a fast and easy way.

## Features
- [NLP Toolkit](https://github.com/librairy/nlpEN-service) 
- A set of Topic Models trained from internal corpora and packaged as Restful APIs
- A repository focused on search and explore documents from their topic distributions


## Quick Start

### Install Docker and Clone this repo
1. Install [Docker-Engine](https://docs.docker.com/install/) and [Docker-Compose](https://docs.docker.com/compose/install/) 
1. Clone this repo and move into `src/test/docker/` directory.

	```
	git clone https://github.com/cbadenes/corpus-viewer-addons.git
	```

### Run NLP Toolkit
1. Move into `src/test/docker/nlp/en` directory.
1. Run the service by: `docker-compose up -d`
1. You should be able to monitor the progress by: `docker-compose logs -f`

- The above command runs two services: DBpedia Spotlight and librAIry NLP-EN, and uses the settings specified within `docker-compose.yml`.
- The HTTP Restful-API should be available at: `http://localhost:7777/en` 
- More info [here]()

### Run Topic Model
1. Move into `src/test/docker/models` directory.
1. Run the models by: `docker-compose up -d`
1. You should be able to monitor the progress by: `docker-compose logs -f`

- The above command runs a web service for each model and uses the settings specified within `docker-compose.yml`.
- The HTTP Restful-APIs should be available at: `http://localhost:800[x]/model`

#### Model API
A Topic Model is described by the following services:
- `/settings` : read the metadata of the model. 
- `/topics`: lists the topics discovered by the model.
- `/topics/{id}`: get topic details.
- `/topics/{id}/words`: explore a topic from its word distribution.
- `/topics/{id}/neighbours`: explore a topic from its concurrence topics.
- `/inferences`: build a vector with the topic distributions from a given text.

### Run Document Repository
1. Move into `src/test/docker/solr` directory.
1. Create collections by: `create.sh`
1. Manage service by: `start.sh`, `stop.sh` and `clean.sh` scripts"
1. You should be able to monitor the progress by: `docker solr logs -f`

- The above command runs a Solr service and uses the settings specified within `docker-compose.yml`.
- The HTTP Admin console should be available at: `http://localhost:8983`


## Load Documents

In order to be able to index documents it is necessary to have the topic distributions (doctopics) along with the texts. 

You can download the following corpus: CORDIS, Wikipedia or Patstat, by executing the following script:

    ```
	./download-corpora.sh
	```

Once executed, the `/corpora` folder is created with the texts and vectors associated for each corpus.

Then you can run the unit tests to load data (`src/test/java/load`) into the repository:
- `LoadDocuments`: Saves texts and meta-information of each document in the `documents` collection
- `LoadDocTopics`: Saves the topic distribution of each document in the `doctopics` collection 
- `LoadTopicDocs`: Saves the topic info of each model in the `topicdocs` collection

## Search Documents

There are multiple ways to explore the content of the corpus. You can take a look at the queries we've defined in `src/test/java/query`.


## Reference

You can use the following to cite this work:

```
@inproceedings{Badenes-Olmedo:2017:DTM:3103010.3121040,
 author = {Badenes-Olmedo, Carlos and Redondo-Garcia, Jos{\'e} Luis and Corcho, Oscar},
 title = {Distributing Text Mining Tasks with librAIry},
 booktitle = {Proceedings of the 2017 ACM Symposium on Document Engineering},
 series = {DocEng '17},
 year = {2017},
 isbn = {978-1-4503-4689-4},
 pages = {63--66},
 numpages = {4},
 url = {http://doi.acm.org/10.1145/3103010.3121040},
 doi = {10.1145/3103010.3121040},
 acmid = {3121040},
 publisher = {ACM},
 keywords = {data integration, large-scale text analysis, nlp, scholarly data, text mining},
} 

```



