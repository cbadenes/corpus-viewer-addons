package es.gob.minetad.solr.model;

import es.gob.minetad.doctopic.DocTopicsUtil;
import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class TopicIndexFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TopicIndexFactory.class);

    public static final String DOC_ID           = "id";
    public static final String FIELD_NAME       = "doctopics_field";
    public static final float PRECISION         = 1e4f;

    public static IndexReader newTopicIndex(Directory directory, String corpus) throws IOException {
        IndexWriter writer = createIndexWriter(directory);
        if (corpus.toLowerCase().endsWith(".bin")) return indexFromBinDocs(writer, corpus);
        if (corpus.toLowerCase().endsWith(".gz")) return indexFromTxtGZDocs(writer, corpus);
        else throw new RuntimeException("Corpus file extension unknown: " + corpus);
    }

    public static IndexReader loadTopicIndex(Directory directory) throws IOException {
        return DirectoryReader.open(directory);
    }

    private static IndexWriter createIndexWriter(Directory directory) throws IOException {
        IndexWriterConfig writerConfig = new IndexWriterConfig(new DocTopicAnalyzer());
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writerConfig.setRAMBufferSizeMB(500.0);
        IndexWriter writer = new IndexWriter(directory, writerConfig);
        return writer;
    }


    private static IndexReader indexFromBinDocs(IndexWriter writer, String corpus) throws IOException {
        LOG.info("loading documents/topics from bin file ..");
        Instant start = Instant.now();
        DocTopicsUtil docTopicsUtil = new DocTopicsUtil();
        docTopicsUtil.inspectBinTopicFile(corpus);
        int numdocs = docTopicsUtil.getNumdocs();
        int numtopics = docTopicsUtil.getNumtopics();
        short[][] docTopicValues = new short[numdocs][numtopics];
        docTopicsUtil.loadBinTopics(corpus, numdocs, docTopicValues);
        Instant end = Instant.now();
        LOG.info("Corpus parsed in: " + ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");

        int maxDocs = docTopicValues.length;
        start = Instant.now();
        for(int i=0; i<maxDocs; i++){
            String vectorString = docTopicsUtil.getVectorStringBin(docTopicValues[i]);
            Document test = DocumentFactory.newDocTopic(vectorString, FIELD_NAME);
            writer.addDocument(test);

            if (i>0 && i%1000==0) writer.commit();
        }

        end = Instant.now();
        LOG.info("Corpus indexed ("+maxDocs+"docs) in: " + ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");

        Directory indexDirectory = writer.getDirectory();
        writer.close();
        return DirectoryReader.open(indexDirectory);
    }

    private static IndexReader indexFromTxtGZDocs(IndexWriter writer, String corpus) throws IOException {
        LOG.info("loading documents/topics from txt.gz file ..");
        Instant start = Instant.now();
        DocTopicsUtil docTopicsUtil = new DocTopicsUtil();
        docTopicsUtil.inspectTopicFile_CompleteFormat(corpus);
        docTopicsUtil.loadTopics_CompleteFormat(corpus);
        float[][] docTopicValuesCleanned = docTopicsUtil.cleanZeros();
        Instant end = Instant.now();
        LOG.info("Corpus parsed in: " + ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");


        start = Instant.now();
        int maxDocs = docTopicValuesCleanned.length;
        for(int i=0; i<maxDocs; i++){
            // DelimitedTermFrequencyTokenFilter
            String vectorString = docTopicsUtil.getVectorString(docTopicValuesCleanned[i], PRECISION);
            Document test = DocumentFactory.newDocTopic(vectorString, FIELD_NAME);
            writer.addDocument(test);

            if (i>0 && i%1000==0) writer.commit();
        }
        end = Instant.now();
        LOG.info("Corpus indexed ("+maxDocs+"docs) in: " + ChronoUnit.MINUTES.between(start,end) + "min " + (ChronoUnit.SECONDS.between(start,end)%60) + "secs");

        Directory indexDirectory = writer.getDirectory();
        writer.close();
        return DirectoryReader.open(indexDirectory);
    }

}
