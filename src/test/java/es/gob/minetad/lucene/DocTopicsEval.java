package es.gob.minetad.lucene;

import com.google.common.base.Strings;
import es.gob.minetad.doctopic.CRDCIndex;
import es.gob.minetad.doctopic.DocTopicsUtil;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.model.Stats;
import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import es.gob.minetad.solr.model.DocumentFactory;
import es.gob.minetad.solr.model.TopicIndexFactory;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopicsEval {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopicsEval.class);

    public static final String DOCTOPICS_PATH = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/iQx4Zy2dPcY84Sd/download";

    public static final Integer NUM_DOCS = -1;

    private static FSDirectory directory;
    private static DirectoryReader indexReader;

    private static List<Double> SAMPLE_VECTOR   = Arrays.asList(0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.03875,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.05125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.03875,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.05125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.013750000000000002,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125);

    private static Integer numTopics            = SAMPLE_VECTOR.size();
    private static float epsylon                = 1f / numTopics;
    private static float multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(numTopics).length()+1)).floatValue();
    private static CRDCIndex crdcIndex          = new CRDCIndex(numTopics,0.001,100000);

    private static String vector2String(List<Double> vector){
        return DocTopicsUtil.getVectorString(vector, multiplicationFactor, epsylon);
//        return crdcIndex.toString(vector);
    }

    private static List<Double> string2Vector(String vector){
        return DocTopicsUtil.getVectorFromString(vector, multiplicationFactor, numTopics, epsylon);
//        return crdcIndex.toVector(vector);
    }

    private static Map<String,Double> string2map(String vector){
        Map<String,Double> vectorMap = new ConcurrentHashMap<>();
        Arrays.stream(vector.split(" ")).parallel().forEach(t -> vectorMap.put(StringUtils.substringBefore(t,"|"),Double.valueOf(StringUtils.substringAfter(t,"|"))/multiplicationFactor));
        return vectorMap;
    }

    @BeforeClass
    public static void setup() throws IOException {
        File indexFile = new File("output/doctopics");
//        if (indexFile.exists()) indexFile.delete();
        if (!indexFile.exists()) createIndex(indexFile);
        else directory = FSDirectory.open(indexFile.toPath());
        indexReader      = DirectoryReader.open(directory);
        LOG.info("num docs indexed: " + indexReader.numDocs());
        LOG.info("max docs indexed: " + indexReader.maxDoc());


//        List<Map.Entry<String, Double>> topSimilars = similarities.entrySet().parallelStream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).collect(Collectors.toList());
//        topSimilars.forEach(entry -> LOG.info("- " + entry.getKey() + " [" + entry.getValue() + "]"));
    }

    private static void createIndex(File indexFile) throws IOException {
        LOG.info("creating a file system directory for index");
        indexFile.getParentFile().mkdirs();
        IndexWriterConfig writerConfig = new IndexWriterConfig(new DocTopicAnalyzer());
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writerConfig.setRAMBufferSizeMB(500.0);
        directory = FSDirectory.open(indexFile.toPath());
        IndexWriter writer = new IndexWriter(directory, writerConfig);

        LOG.info("indexing documents..");
        AtomicInteger counter = new AtomicInteger(0);

        ConcurrentLinkedQueue<Double> representationLengths = new ConcurrentLinkedQueue();
        BufferedReader doctopicsReader = ReaderUtils.from(DOCTOPICS_PATH);
        String line;
        Instant startTime = Instant.now();
        int interval = NUM_DOCS < 0? 500 : NUM_DOCS / 25;
        ParallelExecutor executor = new ParallelExecutor();
        while ((line = doctopicsReader.readLine()) != null) {
            final Integer index = counter.incrementAndGet();
            final String row = line;
            executor.submit(() -> {
                try{
                    String[] result = row.split(",");
                    String id = result[0];
                    List<Double> shape = new ArrayList<>();
                    for (int i=1; i<result.length; i++){
                        shape.add(Double.valueOf(result[i]));
                    }

                    String stringTopics = vector2String(shape);
                    org.apache.lucene.document.Document luceneDoc = DocumentFactory.newDocId(id, stringTopics);
                    representationLengths.add(Double.valueOf(StringUtils.countMatches(stringTopics,"|")));
                    writer.addDocument(luceneDoc);
                    if (index % interval == 0){
                        LOG.info(index + " docs indexed");
                        writer.commit();
                    }
                }catch (Exception e){
                    LOG.error("Unexpected error", e);
                }
            });

            if ((NUM_DOCS > 0) && (index >= NUM_DOCS)) break;
        }
        executor.awaitTermination(1, TimeUnit.HOURS);
        doctopicsReader.close();

        writer.commit();
        writer.close();


        Instant endTime = Instant.now();
        String elapsedTime = ChronoUnit.HOURS.between(startTime, endTime) + "hours "
                + ChronoUnit.MINUTES.between(startTime, endTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTime, endTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTime, endTime) % 60) + "msecs";
        LOG.info("Index created in: " + elapsedTime);
        LOG.info("num topics stored stats: " + new Stats(new ArrayList<Double>(representationLengths)));
        LOG.info("num docs read: " + counter.get());
    }

    @AfterClass
    public static void shutdown() throws IOException {
        indexReader.close();
    }

    @Test
    public void booleanQuery() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BooleanSimilarity());
        query("Boolean", searcher);

    }

    @Test
    public void bm25Query() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        searcher.setSimilarity(new BM25Similarity());
        query("BM25", searcher);
    }

    @Test
    public void bruteForceQuery() throws IOException, ParseException {
        IndexSearcher searcher  = new IndexSearcher(indexReader);
        search("BruteForce", searcher, new MatchAllDocsQuery());
    }


    @Test
    public void moreLikeThisQuery() throws ParseException, IOException {

        IndexSearcher searcher = new IndexSearcher(indexReader);

        MoreLikeThis mlt = new MoreLikeThis(indexReader);
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);
        mlt.setAnalyzer(new DocTopicAnalyzer());

        Reader stringReader = new StringReader(vector2String(SAMPLE_VECTOR));

        Query mltQuery = mlt.like(TopicIndexFactory.FIELD_NAME, stringReader);

        search("MoreLikeThis", searcher, mltQuery);

    }



    private void query(String id, IndexSearcher searcher) throws ParseException, IOException {
        String queryString = vector2String(SAMPLE_VECTOR);

        QueryParser parser = new QueryParser(TopicIndexFactory.FIELD_NAME, new DocTopicAnalyzer());
        Query query = parser.parse(queryString);

        search(id, searcher, query);

    }

    private void search(String id, IndexSearcher searcher, Query query) throws IOException {
        List<Double> v1 = SAMPLE_VECTOR;
        Map<String, Double> m1 = string2map(vector2String(v1));
        String description = "Searching by '" + id + "' ";
        LOG.info(description + Strings.repeat("-",100-description.length()));
        Instant startTime = Instant.now();
        TopDocs results = searcher.search(query, indexReader.numDocs());

        Instant pstartTime = Instant.now();
        List<Score> topDocs = Arrays.stream(results.scoreDocs).parallel().map(scoreDoc -> {
            try {
                org.apache.lucene.document.Document docIndexed = indexReader.document(scoreDoc.doc);
                String vectorString = String.format(docIndexed.get(TopicIndexFactory.FIELD_NAME));
                if (Strings.isNullOrEmpty(vectorString)) return new Score(0.0, new Document(), new Document());

//                Map<String, Double> m2 = string2map(vectorString);
                List<Double> v2 = string2Vector(vectorString);
                return new Score(JensenShannon.similarity(v1, v2), new Document("ref"), new Document(String.format(docIndexed.get(TopicIndexFactory.DOC_ID))));
            } catch (Exception e) {
                e.printStackTrace();
                return new Score(0.0, new Document(), new Document());
            }
        }).filter(s -> s.getValue() > 0.7).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).collect(Collectors.toList());
        topDocs.size();
        Instant pEndTime = Instant.now();
        LOG.info("Similarities Time: " + + ChronoUnit.MINUTES.between(pstartTime, pEndTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(pstartTime, pEndTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(pstartTime, pEndTime) % 1000) + "msecs");
        Instant globalEndTime = Instant.now();
        String globalElapsedTime =
                + ChronoUnit.MINUTES.between(startTime, globalEndTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTime, globalEndTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTime, globalEndTime) % 1000) + "msecs";
        LOG.info("Total Time: " + globalElapsedTime);
        LOG.info("Total Hits: " + results.totalHits);

        topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"]"));
    }

}
