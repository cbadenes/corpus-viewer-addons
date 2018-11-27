package load;

import com.google.common.base.Strings;
import es.gob.minetad.doctopic.DTFIndex;
import es.gob.minetad.model.SolrCollection;
import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import es.gob.minetad.utils.ReaderUtils;
import load.patents.LoadPatentsDocuments;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * Create a 'topic-doc' collection from a DTM output
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *  http://localhost:8983/solr/#/topicdocs
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class LoadDTMTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDTMTopicDocs.class);

    private static final Integer MULTIPLIER = 10000;

    private String corpus;
    private Integer dimension;


    public LoadDTMTopicDocs(String corpus, Integer dim) {
        this.corpus     = corpus;
        this.dimension  = dim;
    }

    @Test
    public void execute(){
        Path path = Paths.get("corpora", corpus, "dtm_" + dimension);
        try {
            if (!path.toFile().exists()){
                LOG.warn("Directory: " + path.toFile().getAbsolutePath() + " not found!");
                return;
            }

            List<Path> files = Files.walk(path)
                    .filter(s -> s.toString().endsWith(".tsv"))
                    .sorted()
                    .collect(Collectors.toList());

            // Creating Solr Collection
            SolrCollection collection = new SolrCollection(corpus + "-topicdocs");
            DTFIndex indexer = new DTFIndex(MULTIPLIER);

            for(Path file : files){
                String name = file.toFile().getName();
                boolean tfidf = name.contains("tfidf");
                String year = StringUtils.substringBefore(StringUtils.substringAfterLast(name,"_"),".tsv");
                ConcurrentHashMap<String,Topic> topics = new ConcurrentHashMap<>();

                BufferedReader reader = ReaderUtils.from(file.toFile().getAbsolutePath());
                String line;
                while((line = reader.readLine()) != null){
                    if (Strings.isNullOrEmpty(line)) continue;
                    String[] values = line.split("\t");
                    if (values.length != 5) continue;

                    try{
                        String id       = values[0];
                        Double weight   = Double.valueOf(values[1].replace(",","."));
                        Double entropy  = Double.valueOf(values[2].replace(",","."));
                        String word     = values[3];
                        Double score    = Double.valueOf(values[4].replace(",","."));
                        String key      = id + "_" + year;

                        if (!topics.containsKey(key)){
                            Topic topic = new Topic();
                            topic.setId(id+"_"+year);
                            topic.setName("t"+id+"_"+year);
                            topic.setWeight(weight);
                            topic.setEntropy(entropy);
                            topic.setWords(new ArrayList<>());
                            topics.put(key,topic);
                        }

                        topics.get(key).getWords().add(new TopicWord(new Word(word),score));

                        if (topics.get(key).getWords().size() == 10){
                            String description = topics.get(key).getWords().stream().map(tw -> tw.getWord().getValue()).collect(Collectors.joining(","));
                            topics.get(key).setDescription(description);
                        }
                    }catch (NumberFormatException e){
                        //"invalid line: '" + line+"'"
                        continue;
                    }

                }

                for(String topicKey: topics.keySet()){

                    Topic topic = topics.get(topicKey);

                    SolrInputDocument document = new SolrInputDocument();

                    Map<String,Double> words = new HashMap<>();
                    topic.getWords().forEach(tw -> words.put(tw.getWord().getValue(), tw.getScore()));
                    String dtf = indexer.toString(words);

                    if (!tfidf){
                        document.addField("id",topic.getId());
                        document.addField("name_s",topic.getName());
                        document.addField("description_txt",topic.getDescription());
                        document.addField("label_s",StringUtils.substringAfterLast(topic.getName(),"_"));
                        document.addField("model_s","dtm");
                        document.addField("weight_f",topic.getWeight());
                        document.addField("entropy_f",topic.getEntropy());
                        document.addField("words_tfdl",dtf);
                    }else{
                        Optional<SolrDocument> optDoc = collection.getById(topic.getId());
                        if (!optDoc.isPresent()) continue;
                        SolrDocument indexedDoc = optDoc.get();
                        for(String field : indexedDoc.getFieldNames()){
                            document.addField(field, indexedDoc.getFieldValue(field));
                        }
                        document.addField("wordsTFIDF_tfdl",dtf);
                    }

                    collection.add(document);


                }
                LOG.info("Doc " + file.toFile().getName() + " parsed");
            }
            collection.commit();


        } catch (Exception e) {
            LOG.info("Unexpected error",e);
        }

    }

}
