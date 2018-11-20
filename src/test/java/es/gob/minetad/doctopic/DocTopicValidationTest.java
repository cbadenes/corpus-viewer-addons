package es.gob.minetad.doctopic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.WriterUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONArray;
import org.junit.Test;
import org.librairy.service.modeler.facade.rest.model.InferenceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * This class groups the tests that allow to compare the topic distributions generated during the training stage of the model
 * with those obtained by inference for the same corpus.
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopicValidationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopicValidationTest.class);

    static ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void comparison() throws IOException {

        String documents    = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/woBzdYWfJtJ6sfY/download";

        String doctopics    = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/nfmoNWMB3NN7FwE/download";

        JaccardSimilarity similarity = new JaccardSimilarity();
        int size            = 10;

        Map<String,String> docTexts = new HashMap<>();
        BufferedReader docReader = ReaderUtils.from(documents);
        String row;
        while((row = docReader.readLine()) != null){
            JsonNode json = jsonMapper.readTree(row);
            docTexts.put(json.get("id").asText(), json.get("objective").asText());
            if (docTexts.size() >= size) break;
        }
        docReader.close();

        Map<String,List<Double>> docTopics = new HashMap<>();
        BufferedReader docTopicReader = ReaderUtils.from(doctopics);
        while((row = docTopicReader.readLine()) != null){
            String[] values = row.split(",");
            docTopics.put(values[0], Arrays.asList(values).stream().skip(1).map(d -> Double.valueOf(d)).collect(Collectors.toList()));
            if (docTopics.size() >= size) break;
        }
        docTopicReader.close();



        for(String id: docTexts.keySet()){

            if (docTopics.containsKey(id)){

                String text     = docTexts.get(id);
                LOG.info("text -> " + text.replace("\n",""));

                List<Double> v1 = docTopics.get(id);
                List<Index> t1 = top(v1);
                LOG.info("- v1: " + top(v1));

                List<Double> v2 = inference(text);
                List<Index> t2 = top(v2);
                LOG.info("- v2: " + top(v2));

                List<Double> v3 = inference(text);
                List<Index> t3 = top(v3);
                LOG.info("- v3: " + top(v3));


                LOG.info("v1<->v2 = " + similarity.apply(t1.stream().map(i -> String.valueOf(i.getScore())).collect(Collectors.joining(" ")),t2.stream().map(i -> String.valueOf(i.getScore())).collect(Collectors.joining(" "))) +" -> " + JensenShannon.similarity(v1,v2));
                LOG.info("v2<->v3 = " + similarity.apply(t2.stream().map(i -> String.valueOf(i.getScore())).collect(Collectors.joining(" ")),t3.stream().map(i -> String.valueOf(i.getScore())).collect(Collectors.joining(" "))) +" -> " + JensenShannon.similarity(v2,v3));
                LOG.info("v1<->v3 = " + similarity.apply(t1.stream().map(i -> String.valueOf(i.getScore())).collect(Collectors.joining(" ")),t3.stream().map(i -> String.valueOf(i.getScore())).collect(Collectors.joining(" "))) +" -> " + JensenShannon.similarity(v1,v3));


            }


        }

    }

    @Test
    public void generate() throws IOException {

        String documents    = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/woBzdYWfJtJ6sfY/download";

        BufferedWriter writer = WriterUtils.to(Paths.get("cordis.csv.gz").toFile().getAbsolutePath());
        BufferedReader docReader = ReaderUtils.from(documents);
        String row;
        while((row = docReader.readLine()) != null){
            JsonNode json = jsonMapper.readTree(row);
            writer.write(json.get("id").asText() + " " + json.get("id").asText() + " " +json.get("objective").asText().replace("\n","").replaceAll("\\P{Print}", "") + "\n");
        }
        docReader.close();
        writer.close();


    }

    private static List<Double> inference(String text){
        try {
            List<Double> v = new ArrayList<>();
            InferenceRequest request = new InferenceRequest();
            request.setTopics(false);
            request.setText(text);
            HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.post("http://localhost:8080/inferences")
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(jsonMapper.writeValueAsString(request))
                    .asJson();

            JSONArray array = response.getBody().getObject().getJSONArray("vector");
            for(int i=0;i<array.length();i++){
                v.add((Double) array.get(i));
            }
            return v;

        } catch (UnirestException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private static List<Index> top(List<Double> vector){
        return IntStream.range(0, vector.size()).mapToObj(i -> new Index(i, vector.get(i))).sorted((a, b) -> -a.getScore().compareTo(b.getScore())).limit(10).collect(Collectors.toList());
    }

    private static class Index{
        Integer value;
        Double score;

        public Index(Integer value, Double score) {
            this.value = value;
            this.score = score;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        @Override
        public String toString() {
            return "{" +
                    "value=" + value +
                    ", score=" + score +
                    '}';
        }
    }

}
