package es.gob.minetad.lucene;

import com.google.common.base.Strings;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import es.gob.minetad.solr.model.TopicIndexFactory;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.TimeUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class BruteForceEval extends DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(BruteForceEval.class);


    @Test
    public void bruteForceQuery() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        List<Double> v1 = SAMPLE_VECTOR;
//        Map<String, Double> m1 = string2map(vector2String(v1));
        String description = "Searching by Brute-Force' ";
        LOG.info(description + Strings.repeat("-",100-description.length()));
        Instant s1 = Instant.now();
        TopDocs results = searcher.search(new MatchAllDocsQuery(), indexReader.numDocs());
        Instant e1 = Instant.now();
        LOG.info("Total Hits: " + results.totalHits);
        TimeUtils.print(s1, e1, "Query");

        Instant s2 = Instant.now();

        Map<String,Double> scoreDocs = new ConcurrentHashMap<>();


        ParallelExecutor executor = new ParallelExecutor();
        for(ScoreDoc scoreDoc : results.scoreDocs){

            executor.submit(() -> {
                org.apache.lucene.document.Document docIndexed = null;
                try {
                    docIndexed = indexReader.document(scoreDoc.doc);
                    String vectorString = String.format(docIndexed.get(TopicIndexFactory.FIELD_NAME));
                    if (Strings.isNullOrEmpty(vectorString)) return;

                    List<Double> v2 = string2Vector(vectorString);
                    double simScore = JensenShannon.similarity(v1, v2);
                    if (simScore < 0.98) return;
                    scoreDocs.put(String.format(docIndexed.get(TopicIndexFactory.DOC_ID)) + "[" + vectorString+"]",simScore);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executor.awaitTermination(1, TimeUnit.HOURS);
        Instant e2 = Instant.now();
        TimeUtils.print(s2,e2,"JSD");


        Instant s3 = Instant.now();
        List<Score> topDocs = scoreDocs.entrySet().parallelStream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).map(entry -> new Score(entry.getValue(), new Document(), new Document(entry.getKey()))).collect(Collectors.toList());
        Instant e3 = Instant.now();
        TimeUtils.print(s3,e3,"Sort");


        TimeUtils.print(s1,e3,"Total");

        topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"]"));

    }

}
