package load;

import com.google.common.base.Strings;
import es.gob.minetad.doctopic.DTFIndex;
import es.gob.minetad.model.SolrCollection;
import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import es.gob.minetad.utils.ReaderUtils;
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
 * Create a 'topic-doc' collection from a CTM output
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
public class LoadCTMTopicDocs {

    private static final Logger LOG = LoggerFactory.getLogger(LoadCTMTopicDocs.class);

    private static final Integer MULTIPLIER = 10000;

    private String corpus;
    private Integer dimension;


    public LoadCTMTopicDocs(String corpus, Integer dim) {
        this.corpus     = corpus;
        this.dimension  = dim;
    }

    @Test
    public void execute(){
        Path path = Paths.get("corpora", corpus, "ctm_" + dimension);
        try {
            if (!path.toFile().exists()){
                LOG.warn("Directory: " + path.toFile().getAbsolutePath() + " not found!");
                return;
            }

            List<Path> files = Files.walk(path)
                    .filter(s -> s.toString().endsWith(".tsv") && !s.toFile().getName().contains("tfidf"))
                    .sorted()
                    .collect(Collectors.toList());

            // Creating Solr Collection
            SolrCollection collection = new SolrCollection(corpus + "-topicdocs");
            DTFIndex indexer = new DTFIndex(MULTIPLIER);

            for(Path file : files){

                ConcurrentHashMap<String,Topic> topics = new ConcurrentHashMap<>();
                ConcurrentHashMap<String,Topic> topicsByTFIDF = new ConcurrentHashMap<>();

                readFile(file,topics);
                readFile(Paths.get(file.toFile().getAbsolutePath().replace("modelo_","modelo_tfidf_")),topicsByTFIDF);


                for(String topicKey: topics.keySet()){

                    Topic topic = topics.get(topicKey);

                    SolrInputDocument document = new SolrInputDocument();

                    Map<String,Double> words = new HashMap<>();
                    topic.getWords().forEach(tw -> words.put(tw.getWord().getValue(), tw.getScore()));
                    String dtf = indexer.toString(words);

                    Map<String,Double> wordsTFIDF = new HashMap<>();
                    Topic topicTFIDF = topicsByTFIDF.get(topicKey);
                    topicTFIDF.getWords().forEach(tw -> wordsTFIDF.put(tw.getWord().getValue(), tw.getScore()));
                    String dtfTFIDF = indexer.toString(wordsTFIDF);


                    document.addField("id",topic.getId());
                    document.addField("name_s",topic.getName());

                    String description = topic.getWords().stream().sorted((a,b) -> -a.getScore().compareTo(b.getScore())).limit(10).map(e -> e.getWord().getValue()).collect(Collectors.joining(","));
                    document.addField("description_t",description);

                    String descriptionTFIDF = topicTFIDF.getWords().stream().sorted((a,b) -> -a.getScore().compareTo(b.getScore())).limit(10).map(e -> e.getWord().getValue()).collect(Collectors.joining(","));
                    document.addField("descriptionTFIDF_t",descriptionTFIDF);

                    document.addField("label_s",StringUtils.substringBetween(topic.getName(),"_"));
                    document.addField("model_s","ctm");
                    document.addField("weight_f",topic.getWeight());
                    document.addField("entropy_f",topic.getEntropy());
                    document.addField("words_tfdl",dtf);
                    document.addField("wordsTFIDF_tfdl",dtfTFIDF);

                    collection.add(document);


                }
                LOG.info("Doc " + file.toFile().getName() + " parsed");
            }
            collection.commit();


        } catch (Exception e) {
            LOG.info("Unexpected error",e);
        }

    }

    private void readFile(Path path, ConcurrentHashMap<String,Topic> topics) throws IOException {
        BufferedReader reader = ReaderUtils.from(path.toFile().getAbsolutePath());
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
                String key      = id + "_ctm";

                if (!topics.containsKey(key)){
                    Topic topic = new Topic();
                    topic.setId(key);
                    topic.setName("t"+key);
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
    }

}
