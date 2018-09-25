package es.gob.minetad.lucene;

import com.google.common.base.Strings;
import es.gob.minetad.doctopic.DocTopicsUtil;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Combination;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.model.Stats;
import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import es.gob.minetad.solr.model.DocumentFactory;
import es.gob.minetad.solr.model.TopicIndexFactory;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopicsTest {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopicsTest.class);

    public static final String DOCTOPICS_PATH = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/iQx4Zy2dPcY84Sd/download";

    public static final Integer NUM_DOCS = 10000;

    private static FSDirectory directory;
    private static DirectoryReader indexReader;
    
    private static float epsylon;
    private static float multiplicationFactor;

    private static AtomicInteger counter;
    private static Integer numTopics;

    private static List<Double> SAMPLE_VECTOR;


    @BeforeClass
    public static void setup() throws IOException {
        File indexFile = new File("output/doctopics");
        if (indexFile.exists()) indexFile.delete();
        LOG.info("creating a file system directory for index");
        indexFile.getParentFile().mkdirs();
        directory = FSDirectory.open(indexFile.toPath());
        IndexWriterConfig writerConfig = new IndexWriterConfig(new DocTopicAnalyzer());
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writerConfig.setRAMBufferSizeMB(500.0);
        IndexWriter writer = new IndexWriter(directory, writerConfig);

        LOG.info("indexing documents..");
        counter = new AtomicInteger(0);

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
                    numTopics             = shape.size();
                    epsylon               = 1f / numTopics;
                    multiplicationFactor  = Double.valueOf(1*Math.pow(10,String.valueOf(numTopics).length()+1)).floatValue();

                    if (SAMPLE_VECTOR == null) SAMPLE_VECTOR = shape;

                    String stringTopics = DocTopicsUtil.getVectorString(shape, multiplicationFactor, epsylon);
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


        indexReader      = DirectoryReader.open(directory);
        LOG.info("num docs read: " + counter.get());
        LOG.info("num docs indexed: " + indexReader.numDocs());
        LOG.info("max docs indexed: " + indexReader.maxDoc());
        LOG.info("Index created in: " + elapsedTime);
        LOG.info("num topics stored stats: " + new Stats(new ArrayList<Double>(representationLengths)));
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
    public void moreLikeThisQuery() throws ParseException, IOException {

        IndexSearcher searcher = new IndexSearcher(indexReader);

        MoreLikeThis mlt = new MoreLikeThis(indexReader);
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);
        mlt.setAnalyzer(new DocTopicAnalyzer());

        Reader stringReader = new StringReader(DocTopicsUtil.getVectorString(SAMPLE_VECTOR, multiplicationFactor, epsylon));

        Query mltQuery = mlt.like(TopicIndexFactory.FIELD_NAME, stringReader);

        search("MoreLikeThis", searcher, mltQuery);

    }



    private void query(String id, IndexSearcher searcher) throws ParseException, IOException {
        String queryString = DocTopicsUtil.getVectorString(SAMPLE_VECTOR, multiplicationFactor, epsylon);

        QueryParser parser = new QueryParser(TopicIndexFactory.FIELD_NAME, new DocTopicAnalyzer());
        Query query = parser.parse(queryString);

        search(id, searcher, query);

    }

    private void search(String id, IndexSearcher searcher, Query query) throws IOException {
        String description = "Searching by '" + id + "' ";
        LOG.info(description + Strings.repeat("-",100-description.length()));
        Instant startTime = Instant.now();
        TopDocs results = searcher.search(query, counter.get());
        Instant endTime = Instant.now();

        String elapsedTime = ChronoUnit.HOURS.between(startTime, endTime) + "hours "
                + ChronoUnit.MINUTES.between(startTime, endTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTime, endTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTime, endTime) % 60) + "msecs";
        LOG.info("Query Time: " + elapsedTime);
        LOG.info("Total Hits: " + results.totalHits);


        List<Score> topDocs = Arrays.stream(results.scoreDocs).parallel().map(scoreDoc -> {
            try {
                org.apache.lucene.document.Document docIndexed = indexReader.document(scoreDoc.doc);
                return new Score(JensenShannon.similarity(SAMPLE_VECTOR, DocTopicsUtil.getVectorFromString(String.format(docIndexed.get(TopicIndexFactory.FIELD_NAME)), multiplicationFactor, numTopics, epsylon)), new Document("ref"), new Document(String.format(docIndexed.get(TopicIndexFactory.DOC_ID))));
            } catch (IOException e) {
                e.printStackTrace();
                return new Score(0.0, new Document(), new Document());
            }
        }).filter(s -> s.getValue() > 0.5).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).collect(Collectors.toList());
        Instant globalEndTime = Instant.now();
        String globalElapsedTime = ChronoUnit.HOURS.between(startTime, globalEndTime) + "hours "
                + ChronoUnit.MINUTES.between(startTime, globalEndTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTime, globalEndTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTime, globalEndTime) % 60) + "msecs";
        LOG.info("Search Time: " + globalElapsedTime);

        topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"]"));
    }

}
