package query;

import es.gob.minetad.doctopic.DTFIndex;
import es.gob.minetad.model.Pair;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.utils.WriterUtils;
import load.LoadDTMTopicDocs;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Generates the files needed to visualize a dynamic topic model
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DTMServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DTMServiceTest.class);


    private static final String COLLECTION = "patents-topicdocs";


    private DTFIndex indexer;
    private TestSettings settings;
    private SolrClient client;

    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
        indexer     = new DTFIndex(LoadDTMTopicDocs.MULTIPLIER);
    }


    @Test
    public void execute() throws IOException, SolrServerException {


        Pair<List<String>, Long> range = getRange();
        List<String> years = range.getI();
        if (years.isEmpty()){
            LOG.warn("No Dynamic Topics found in " + COLLECTION);
            return;
        }
        LOG.info("years: " + years);

        Long topics  = range.getJ();
        LOG.info("topics: " + topics);


        // data_topic_x
        List<String> topicDescriptionList = new ArrayList<>();
        for(int i=0; i<topics;i++){
            Path path = Paths.get("output", "dtm", COLLECTION, "data_topic_" + i + ".tsv");
            BufferedWriter writer = WriterUtils.to(path.toFile().getAbsolutePath());
            LOG.info("creating data_topic_"+i+".tsv file");
            try{
                String topicId = i+"_"+years.get(0)+"_dtm";

                List<SolrDocument> topicdocs = getTopicDoc(topicId);
                if (topicdocs.isEmpty()){
                    LOG.warn("No TopicDoc found by id: " + topicId);
                    continue;
                }
                SolrDocument topicdoc = topicdocs.get(0);


                // File Header
                List<String> header = new ArrayList<>();
                header.add("date");

                String descriptionWords = ((List<String>) topicdoc.getFieldValue("description_t")).get(0);

                String[] words = descriptionWords.split(",");
                Arrays.stream(words).forEach(w -> header.add(w));

                String description = "Topic " + i+":" + descriptionWords;
                topicDescriptionList.add(description);
                // write header
                writer.write(header.stream().collect(Collectors.joining("\t")));
                writer.write("\n");

                for(String year: years){

                    List<Double> scoreList =new ArrayList<>();

                    String tId = i+"_"+year+"_dtm";
                    Map<String, Double> topicWords = getTopicDocWords(tId);

                    for(int j=0;j<words.length;j++){
                        String w = words[j];
                        Double s = topicWords.containsKey(w)? topicWords.get(w) : 0.0;
                        scoreList.add(s);
                    }

                    writer.write(year+"\t");
                    writer.write(scoreList.stream().map(d -> String.valueOf(d)).collect(Collectors.joining("\t")));
                    writer.write("\n");

                }

            }finally {
                writer.close();
            }
        }

        // data_topics
        LOG.info("creating data_topics.tsv file");
        Path topicsPath = Paths.get("output", "dtm", COLLECTION, "data_topics.tsv");
        BufferedWriter topicsWriter = WriterUtils.to(topicsPath.toFile().getAbsolutePath());
        try{
            // header
            topicsWriter.write("date\t");
            topicsWriter.write(topicDescriptionList.stream().collect(Collectors.joining("\t")));
            topicsWriter.write("\n");

            for(String year: years){

                List<Double> scoreList = new ArrayList<>();
                for(int i=0; i<topics;i++){
                    String id = i+"_"+year+"_dtm";
                    Double score = getTopicWeight(id, true);
                    scoreList.add(score);
                }
                topicsWriter.write(year+"\t");
                topicsWriter.write(scoreList.stream().map(d -> String.valueOf(d)).collect(Collectors.joining("\t")));
                topicsWriter.write("\n");

            }


        }finally {
            topicsWriter.close();
        }


    }


    public Pair<List<String>,Long> getRange() throws IOException, SolrServerException {
        String fieldName = "label_s";
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/terms");
        query.setTerms(true);
        query.setTermsLimit(1000);
        query.addTermsField(fieldName);
        query.setTermsMinCount(1);

        List<TermsResponse.Term> terms = client.query(COLLECTION,query).getTermsResponse().getTerms(fieldName);

        List<String> years = terms.stream().map(t -> t.getTerm()).sorted().collect(Collectors.toList());
        Long topics = terms.stream().map(t -> t.getFrequency()).reduce((a, b) -> a < b ? a : b).get();
        return new Pair(years,topics);
    }


    public List<SolrDocument> getTopicDoc(String id) throws IOException, SolrServerException {
        String fieldName = "id";
        SolrQuery query = new SolrQuery();
        query.set("q",fieldName+":"+id);
        query.setFields("id","description_t","words_tfdl");
        query.setRows(1);

        QueryResponse response = client.query(COLLECTION, query);
        return response.getResults();
    }

    public Double getTopicWeight(String id, boolean normalize) throws IOException, SolrServerException {
        String fieldName = "id";
        SolrQuery query = new SolrQuery();
        query.set("q",fieldName+":"+id);
        query.setFields("weight_f");
        query.setRows(1);

        QueryResponse response = client.query(COLLECTION, query);
        if (response.getResults().isEmpty()) return 0.0;
        Float value = (Float) response.getResults().get(0).getFieldValue("weight_f");
        return normalize? Double.valueOf(value)*100.0 : Double.valueOf(value);
    }


    public Map<String,Double> getTopicDocWords(String id) throws IOException, SolrServerException {

        List<SolrDocument> topicdocs = getTopicDoc(id);
        if (topicdocs.isEmpty()) return new HashMap<>();

        String topicWords = (String) topicdocs.get(0).getFieldValues("words_tfdl").iterator().next();

        return indexer.toMap(topicWords,false);
    }

}
