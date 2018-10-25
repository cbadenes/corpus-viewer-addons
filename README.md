# Corpus Viewer Addons

`Corpus-Viewer-Addons` provides a set of distributed services to analyze texts from large document corpora in a fast and easy way.

## Features
- [NLP Toolkit](https://github.com/librairy/nlpEN-service) 
- A set of Topic Models trained from internal corpora and packaged as Restful APIs


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

### Run a Topic Model
1. Move into `src/test/docker/models` directory.
1. Run the models by executing `create.sh`

- The above command runs a web service for each model and uses the settings specified within `docker-compose.yml`.
- The HTTP Restful-APIs should be available at: `http://localhost:800[x]/model`

### Configuration
To change configuration, just edit the [docker-compose.yml](src/test/docker/nlp/en/docker-compose.yml) from NLP-EN or the [docker-compose](src/test/docker/models/docker-compose.yml) from Models.

## Services
A Topic Model is described by the following services:
- `/settings` : read the metadata of the model. 
- `/dimensions`: lists the topics discovered by the model.
- `/dimensions/{id}`: explore a topic from its word distribution.
- `/shape`: build a vector with the topic distributions from a given text.
- `/inference`: build a list of topic distributions along with the top 10 words of each topic from a given text.


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



