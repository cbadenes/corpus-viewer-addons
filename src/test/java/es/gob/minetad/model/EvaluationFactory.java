package es.gob.minetad.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.solr.analyzer.TopicAnalyzer;
import es.gob.minetad.solr.model.DocumentFactory;
import es.gob.minetad.solr.model.TopicIndexFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class EvaluationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluationFactory.class);

    private List<Document> sample;

    private Map<String,List<String>> similarDocuments;

    private final String corpusPath;
    private final Integer sampleSize;
    private final Integer numSimilarDocs;

    public EvaluationFactory(String corpusPath, Integer sampleSize, Integer numSimilarDocs) throws IOException {
        this.corpusPath = corpusPath;
        this.sampleSize = sampleSize;
        this.numSimilarDocs = numSimilarDocs;

        ObjectMapper jsonMapper = new ObjectMapper();
        File sampleFile = new File("src/test/resources/sample-"+sampleSize+".json");
        File sampleMapFile = new File("src/test/resources/sample-map-"+numSimilarDocs+".json");

        if (sampleFile.exists()){
            LOG.info("Loading corpus from json files");
            sample = jsonMapper.readValue(sampleFile, new TypeReference<List<Document>>(){});
            similarDocuments = jsonMapper.readValue(sampleMapFile, new TypeReference<HashMap<String,List<String>>>(){});
            return;

        }

        InputStream inputStream = new GZIPInputStream(new FileInputStream(new File(corpusPath)));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        String line;

        LOG.info("loading documents from corpus");
        List<Document> documents = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        while ((line = reader.readLine()) != null) {
            // comment line
            if(line.trim().startsWith("#")){
                continue;
            }
            String[] result = line.split("\\t");
            String id = result[1].replace("\"", "").replace("/","-").toUpperCase().trim();
            List<Double> shape = new ArrayList<>();
            for (int i=2; i<result.length; i++){
                shape.add(Double.valueOf(result[i]));
            }
            Document document = new Document();
            document.setId(id);
            document.setShape(shape);
            documents.add(document);
//            if (counter.getAndIncrement() == 1000) break;
        }
        reader.close();


        // list of sample documents
        LOG.info("taking a sample");
        sample = documents.stream().limit(sampleSize).collect(Collectors.toList());
        jsonMapper.writeValue(sampleFile, sample);


        // map of similarities
        similarDocuments = new ConcurrentHashMap<>();
        LOG.info("building a map with similar documents");
        sample.parallelStream().forEach( d1 -> {
            List<String> simDocs = documents.stream().map(d2 -> new Score(JensenShannon.similarity(d1.getShape(), d2.getShape()), d1, d2)).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(numSimilarDocs).map(s -> s.getSimilar().getId()).collect(Collectors.toList());
            similarDocuments.put(d1.getId(), simDocs);
        });
        jsonMapper.writeValue(sampleMapFile, similarDocuments);

        LOG.info("corpus and sample are ready!");


    }

    public void newFrom(DocTopicsIndex docTopicsIndex) throws IOException {
        LOG.info("creating a base-directory for index");
        File indexFile = new File("target/index-" + docTopicsIndex.id());
        if (indexFile.exists()) indexFile.delete();
        FSDirectory directory = FSDirectory.open(indexFile.toPath());

        LOG.info("initializing index config");
        IndexWriterConfig writerConfig = new IndexWriterConfig(new TopicAnalyzer());
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writerConfig.setRAMBufferSizeMB(500.0);
        IndexWriter writer = new IndexWriter(directory, writerConfig);

        LOG.info("indexing documents..");
        AtomicInteger counter = new AtomicInteger(1);
        long start = System.currentTimeMillis();
        List<Double> representationLengths = new ArrayList<>();

        InputStream inputStream = new GZIPInputStream(new FileInputStream(new File(corpusPath)));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {

            if(line.trim().startsWith("#")){
                continue;
            }
            String[] result = line.split("\\t");
            String id = result[1].replace("\"", "").replace("/","-").toUpperCase().trim();
            List<Double> shape = new ArrayList<>();
            for (int i=2; i<result.length; i++){
                shape.add(Double.valueOf(result[i]));
            }

            String stringTopics = docTopicsIndex.toString(shape);
            org.apache.lucene.document.Document luceneDoc = DocumentFactory.newDocId(id, stringTopics);
            representationLengths.add(Double.valueOf(StringUtils.countMatches(stringTopics,"|")));
            writer.addDocument(luceneDoc);
            if (counter.getAndIncrement() % 500 == 0) writer.commit();

//            if (counter.get() == 1000) break;
        }
        bufferedReader.close();

        writer.commit();
        writer.close();
        Double indexTime = Double.valueOf(System.currentTimeMillis())- Double.valueOf(start);

        int maxSize = counter.get();

        IndexReader reader      = DirectoryReader.open(directory);
        IndexSearcher searcher  = new IndexSearcher(reader);
        searcher.setSimilarity(docTopicsIndex.metric());//new BooleanSimilarity() //new LMDirichletSimilarity((float) 1)



        LOG.info("num docs indexed: " + reader.numDocs());
        LOG.info("max docs indexed: " + reader.maxDoc());

        Evaluation evaluation = new Evaluation();
        Evaluation evaluationAt10 = new Evaluation();
        // iterate on sample documents to evaluate accuracy on similar documents and elapsed time
        LOG.info("iterating on sample documents to evaluate the accuracy of the algorithm..");
        start = System.currentTimeMillis();
        Double matchedDocs = 0.0;
        counter = new AtomicInteger(1);
        List<Double> totalHits = new ArrayList<>();
        for(Document d1 : sample){

            if ((sampleSize >= 10) && (counter.getAndIncrement() % (sampleSize/10) == 0 )) LOG.info(counter.get()-1 + "/" + sampleSize + " docs evaluated");


            try {

                List<Double> d1Shape = docTopicsIndex.toVector(docTopicsIndex.toString(d1.getShape()));
                // prepare query
                QueryParser parser = new QueryParser(TopicIndexFactory.FIELD_NAME, new TopicAnalyzer());
                String queryString = docTopicsIndex.toString(d1.getShape());

                Query query = parser.parse(queryString);
                TopDocs results = searcher.search(query, maxSize);

                matchedDocs += results.totalHits;
                totalHits.add(Double.valueOf(results.totalHits));

                List<String> simDocs = Arrays.stream(results.scoreDocs).parallel().map(sd -> {
                    try {

                        org.apache.lucene.document.Document docIndexed = reader.document(sd.doc);

                        Document d2 = new Document();
                        d2.setId(String.format(docIndexed.get(TopicIndexFactory.DOC_ID)));
                        d2.setShape(docTopicsIndex.toVector(String.format(docIndexed.get(TopicIndexFactory.FIELD_NAME))));
                        Score score = new Score(docTopicsIndex.similarity(d1Shape, d2.getShape()), d1, d2);
                        return score;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return new Score(Double.MIN_VALUE, null, null);
                    }
                }).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(numSimilarDocs).map(s -> s.getSimilar().getId()).collect(Collectors.toList());

                evaluation.addResult(similarDocuments.get(d1.getId()), simDocs);
                evaluationAt10.addResult(similarDocuments.get(d1.getId()).subList(0,10), simDocs.subList(0,10));

            } catch (ParseException e) {
                LOG.warn("Parsing error",e);
            } catch (IOException e) {
                LOG.warn("IO Error",e);
            } catch (Exception e){
                LOG.error("Unexpected error",e);
            }
        }
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;

        LOG.info("Evaluation Results: ");

        DecimalFormat formatter = new DecimalFormat("###,###.###");
        LOG.info("\t - index num docs: " + formatter.format(reader.numDocs()));

        // index size
        Path folder = directory.getDirectory().toAbsolutePath();
        long size = Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
        String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = (int) (Math.log10(size) / 3);
        double unitValue = 1 << (unitIndex * 10);

        String readableSize = new DecimalFormat("#,##0.#")
                .format(size / unitValue) + " "
                + units[unitIndex];
        LOG.info("\t - index size : " + readableSize);


        // index time
        LOG.info("\t - index time: " + formatter.format(indexTime) + " msecs");

        // index location
        LOG.info("\t - index location (path): " + indexFile.getAbsolutePath());

        // field stats
        LOG.info("\t - index field (stats): " + new Stats(representationLengths));

        // take time ratio
        Double ratioTime = Double.valueOf(elapsedTime) / Double.valueOf(sampleSize);
        LOG.info("\t - query time (avg): " + formatter.format(ratioTime) + " msecs");


        double avgMatchedDocs = Double.valueOf(matchedDocs) / Double.valueOf(sampleSize);
        double ratioMatchedDocs = 100.0 * (avgMatchedDocs / reader.numDocs());
        LOG.info("\t - query matched docs (avg): " + formatter.format(avgMatchedDocs) + " ("+formatter.format(ratioMatchedDocs)+"%)");

        LOG.info("\t - query matched docs (stats): " + new Stats(totalHits));

        LOG.info("\t - query similarityMetric: " + searcher.getSimilarity(true).getClass().getName());

        // accuracy
        LOG.info("\t - evaluation@"+numSimilarDocs+": " + evaluation);
        LOG.info("\t - evaluation@10: " + evaluationAt10);
    }


    public String getCorpusPath() {
        return corpusPath;
    }
}
