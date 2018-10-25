package evals;

import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.*;
import es.gob.minetad.solr.SolrClientFactory;
import es.gob.minetad.utils.ReaderUtils;
import es.gob.minetad.utils.WriterUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.AlarmService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AllPairwiseSimilaritiesEval {

    private static final Logger LOG = LoggerFactory.getLogger(AllPairwiseSimilaritiesEval.class);

    private TestSettings settings;
    private SolrClient client;


    @Before
    public void setup(){
        settings    = new TestSettings();
        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
    }

    /**
     * Evaluates the performance of threshold- and density-based approaches with respect to the use of brute-force to discover similar documents in a given corpus
     */
    @Test
    public void execute() throws IOException, SolrServerException {

        String corpus = "cordis-doctopics-70";
        List<Score> similarities = loadSimilarities(corpus);

        List<Integer> alarmTypes = Arrays.asList(0,1,2,3,4,5);

        for (Integer alarmType : alarmTypes){

            // Density-based Approach
            Alarm alarm = AlarmService.getAlarmsBy(alarmType, corpus, client);

            Evaluation evaluation = new Evaluation();

            for(Map.Entry<String,Long> group : alarm.getGroups().entrySet()){

                List<SolrDocument> docs = AlarmService.getDocumentsBy(alarmType, corpus, group.getKey(), client);

                for (SolrDocument doc: docs){
                    String refId = (String) doc.getFieldValue("id");

                    // candidates based on Density-based Approach
                    List<String> candidateList = docs.stream().map(d -> (String) d.getFieldValue("id")).filter(d -> !d.equalsIgnoreCase(refId)).collect(Collectors.toList());

                    // gold-standard based on brute-force similarity list (same size than before)
                    List<String> referenceList = similarities.parallelStream().filter(s -> s.getReference().getId().equalsIgnoreCase(refId) || s.getSimilar().getId().equalsIgnoreCase(refId)).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(candidateList.size()).map(score -> (score.getReference().getId().equalsIgnoreCase(refId)) ? score.getSimilar().getId() : score.getReference().getId()).collect(Collectors.toList());

                    evaluation.addResult(referenceList, candidateList);
                }

            }

            LOG.info("Results for alarm type '" + alarmType +"': " + evaluation);

        }

    }

    public List<Score> loadSimilarities(String corpus) throws IOException {

        File simFile = Paths.get("output", "similarities", corpus.replace("doctopics", "similarities") + ".csv.gz").toFile();

        if (simFile.exists()){
            LOG.info("Loading similarities from: " + simFile.getAbsolutePath());
            BufferedReader reader = ReaderUtils.from(simFile.getAbsolutePath());
            String row;
            List<Score> similarities = new ArrayList<>();
            while((row = reader.readLine()) != null){
                String[] values = row.split(";;");
                Score score = new Score(Double.valueOf(values[0]), new Document(values[1]), new Document(values[2]));
                similarities.add(score);
            }
            return similarities;
        }

        String[] corpusValue = corpus.split("-");
        return calculateSimilarities(corpusValue[0], Integer.valueOf(corpusValue[2]), 0.7);

    }


    public List<Score> calculateSimilarities(String corpus, Integer model, Double threshold) throws IOException {
        LOG.info("calculating similarities in corpus '" + corpus+"' based on model '" + model+ "' ");
        List<Corpus> corpora = settings.getCorpora().stream().filter(c -> c.getName().equalsIgnoreCase(corpus)).collect(Collectors.toList());
        if (corpora.isEmpty()) throw new RuntimeException("Corpus '" + corpus + "' not found");

        String doctopicsPath = corpora.get(0).getModels().get(model).getDoctopics();
        BufferedReader reader = ReaderUtils.from(doctopicsPath);


        List<Document> documents = new ArrayList<>();
        List<Score> similarities = new ArrayList();
        File simFile = Paths.get("output", "similarities", corpus + "-similarities-" + model + ".csv.gz").toFile();
        BufferedWriter writer = WriterUtils.to(simFile.getAbsolutePath());
        AtomicInteger   counter = new AtomicInteger();
        String row;
        while( (row = reader.readLine()) != null){
            String[] values = row.split(",");
            String id       = values[0];
            List<Double> vector = Arrays.stream(values).skip(1).mapToDouble(v -> Double.valueOf(v)).boxed().collect(Collectors.toList());
            Document d1     = new Document(id,vector);

            List<Score> calculatedSimilarities = documents.parallelStream().map(d2 -> new Score(JensenShannon.similarity(d1.getShape(), d2.getShape()), d1, d2)).filter(s -> s.getValue() > threshold).collect(Collectors.toList());
            calculatedSimilarities.forEach(sim -> {
                try {
                    similarities.add(sim);
                    writer.write(sim.getValue()+";;"+sim.getReference().getId()+";;"+sim.getSimilar().getId()+"\n");
                } catch (IOException e) {
                    LOG.warn("Unexpected error",e);
                }
            });

            documents.add(d1);

            if (counter.incrementAndGet() % 100 == 0) LOG.info(counter.get() + " docs analyzed");

        }
        reader.close();
        writer.close();
        LOG.info(similarities.size() + " similarities saved at: " + simFile.getAbsolutePath());
        return similarities;
    }
}
