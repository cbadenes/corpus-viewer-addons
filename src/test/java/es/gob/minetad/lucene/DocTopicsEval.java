package es.gob.minetad.lucene;

import com.google.common.base.Strings;
import es.gob.minetad.doctopic.CRDCIndex;
import es.gob.minetad.doctopic.DocTopicsUtil;
import es.gob.minetad.doctopic.TopicHash;
import es.gob.minetad.metric.JensenShannon;
import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.model.Stats;
import es.gob.minetad.solr.analyzer.DocTopicAnalyzer;
import es.gob.minetad.solr.analyzer.TopicHashAnalyzer;
import es.gob.minetad.solr.model.DocumentFactory;
import es.gob.minetad.solr.model.TopicIndexFactory;
import es.gob.minetad.utils.ParallelExecutor;
import es.gob.minetad.utils.ReaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
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

    public static final String DOCTOPICS_PATH   = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/4FJtpLxM9qa7QiA/download";
    public static final String INDEX_DIR        = "output/doctopics";
    public static final Integer NUM_DOCS        = 100000;


    private static FSDirectory directory;
    private static DirectoryReader indexReader;

//    private static List<Double> SAMPLE_VECTOR   = Arrays.asList(0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.03875,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.05125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.03875,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.05125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.013750000000000002,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125);
    private static List<Double> SAMPLE_VECTOR   = Arrays.asList(0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125);
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
        File indexFile = new File(INDEX_DIR);
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
                    //org.apache.lucene.document.Document luceneDoc = DocumentFactory.newDocId(id, stringTopics);
                    org.apache.lucene.document.Document luceneDoc = DocumentFactory.newDoc(id, new TopicHash(shape), stringTopics);
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
        search("BruteForce-positive", searcher, new MatchAllDocsQuery());
    }

    @Test
    @Ignore
    public void all() throws IOException, ParseException {

        BufferedReader reader = ReaderUtils.from(DOCTOPICS_PATH);
        String line;
        AtomicInteger counter = new AtomicInteger();
        ConcurrentLinkedQueue<Double> timeList = new ConcurrentLinkedQueue<Double>();
        ConcurrentLinkedQueue<Double> hitsList = new ConcurrentLinkedQueue<Double>();
        Map<Long,IndexSearcher> searcherMap = new ConcurrentHashMap<>();
//        ParallelExecutor executor = new ParallelExecutor();
        int num = 0;
        while( (line = reader.readLine()) != null){
            final String row = line;
            num ++;
            if (num == 100) break;
//            executor.submit(() -> {
                Long thId = Thread.currentThread().getId();

                try {
                    IndexSearcher searcher;
                    if (!searcherMap.containsKey(thId)){
                        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
                        searcher  = new IndexSearcher(DirectoryReader.open(dir));
                        searcher.setSimilarity(new BooleanSimilarity());
                        searcherMap.put(thId, searcher);
                    }else{
                        searcher = searcherMap.get(thId);
                    }

                    if (searcher == null){
                        LOG.warn("Error searcher is null");
                        return;
                    }

                    String[] result = row.split(",");
                    String id = result[0];
                    List<Double> shape = new ArrayList<>();
                    for (int i=1; i<result.length; i++){
                        shape.add(Double.valueOf(result[i]));
                    }

                    // Query
                    String queryString = vector2String(shape);
                    if (Strings.isNullOrEmpty(queryString)) return;
                    QueryParser parser = new QueryParser(TopicIndexFactory.FIELD_NAME, new DocTopicAnalyzer());
                    Query query = parser.parse(queryString);
                    Instant startTime = Instant.now();
                    TopDocs results = searcher.search(query, indexReader.numDocs());
                    Instant endTime = Instant.now();
                    long elapsedTime = ChronoUnit.MILLIS.between(startTime, endTime);
                    timeList.add(Double.valueOf(elapsedTime));
                    hitsList.add(Double.valueOf(results.totalHits));
                    LOG.info("" +counter.incrementAndGet() + "/" + indexReader.numDocs() + " time: " + elapsedTime + "msecs, hits: " + results.totalHits);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            });
        }
//        executor.awaitTermination(1, TimeUnit.HOURS);
        LOG.info("Time Stats (msecs): " + new Stats(new ArrayList<>(timeList)));
        LOG.info("Hits Stats: " + new Stats(new ArrayList<>(hitsList)));
    }


    @Test
    public void moreLikeThisQuery() throws ParseException, IOException {

        IndexSearcher searcher = new IndexSearcher(indexReader);

        MoreLikeThis mlt = new MoreLikeThis(indexReader);
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);

        // Topic DTF
//        mlt.setAnalyzer(new DocTopicAnalyzer());
        //Reader stringReader = new StringReader(vector2String(SAMPLE_VECTOR));
//        Query mltQuery = mlt.like(TopicIndexFactory.FIELD_NAME, stringReader);

        // Topic Hash
        mlt.setAnalyzer(new StandardAnalyzer());
        TopicHash topicHash = new TopicHash(SAMPLE_VECTOR);
        Reader stringReaderPositive = new StringReader(topicHash.byInclusion());
        Query mltQueryPositive = mlt.like(TopicIndexFactory.DOC_POSITIVE_HASH, stringReaderPositive);
        search("MoreLikeThis-positive", searcher, mltQueryPositive);

        Reader stringReaderNegative = new StringReader(topicHash.byExclusion());
        Query mltQueryNegative = mlt.like(TopicIndexFactory.DOC_NEGATIVE_HASH, stringReaderNegative);
        search("MoreLikeThis-negative", searcher, mltQueryNegative);


    }



    private void query(String id, IndexSearcher searcher) throws ParseException, IOException {
        // Topic DTF
        //String queryString = vector2String(SAMPLE_VECTOR);
//        QueryParser parser = new QueryParser(TopicIndexFactory.FIELD_NAME, new DocTopicAnalyzer());

        // Topic Hash
        TopicHash topicHash = new TopicHash(SAMPLE_VECTOR);
        String queryStringPositive = topicHash.byInclusion();
        QueryParser parserPositive = new QueryParser(TopicIndexFactory.DOC_POSITIVE_HASH, new StandardAnalyzer());
        Query queryPositive = parserPositive.parse(queryStringPositive);
        search(id+"-positive", searcher, queryPositive);

        String queryStringNegative = topicHash.byExclusion();
        QueryParser parserNegative = new QueryParser(TopicIndexFactory.DOC_NEGATIVE_HASH, new StandardAnalyzer());
        Query queryNegative = parserNegative.parse(queryStringNegative);
        search(id+"-negative", searcher, queryNegative);



    }

    private Long search(String id, IndexSearcher searcher, Query query) throws IOException {
        List<Double> v1 = SAMPLE_VECTOR;
//        Map<String, Double> m1 = string2map(vector2String(v1));
        String description = "Searching by '" + id + "' ";
        LOG.info(description + Strings.repeat("-",100-description.length()));
        Instant startTime = Instant.now();
        TopDocs results = searcher.search(query, indexReader.numDocs());
        Instant queryEndTime = Instant.now();
        LOG.info("Total Hits: " + results.totalHits);
        LOG.info("Query Time: " + + ChronoUnit.MINUTES.between(startTime, queryEndTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTime, queryEndTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTime, queryEndTime) % 1000) + "msecs");
        Instant pstartTime = Instant.now();
        List<Score> topDocs = Arrays.stream(results.scoreDocs).parallel().map(scoreDoc -> {
            try {
                org.apache.lucene.document.Document docIndexed = indexReader.document(scoreDoc.doc);
                String vectorString = String.format(docIndexed.get(TopicIndexFactory.FIELD_NAME));
                if (Strings.isNullOrEmpty(vectorString)) return new Score(0.0, new Document(), new Document());

                String hash = id.contains("positive")?String.format(docIndexed.get(TopicIndexFactory.DOC_POSITIVE_HASH)):String.format(docIndexed.get(TopicIndexFactory.DOC_NEGATIVE_HASH));

                List<Double> v2 = string2Vector(vectorString);
                return new Score(JensenShannon.similarity(v1, v2), new Document(hash), new Document(String.format(docIndexed.get(TopicIndexFactory.DOC_ID))+"-"+scoreDoc.score));
            } catch (Exception e) {
                e.printStackTrace();
                return new Score(0.0, new Document(), new Document());
            }
        }).filter(s -> s.getValue() > 0.7).sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).collect(Collectors.toList());
        topDocs.size();
        Instant pEndTime = Instant.now();
        LOG.info("Comparison Time: " + + ChronoUnit.MINUTES.between(pstartTime, pEndTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(pstartTime, pEndTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(pstartTime, pEndTime) % 1000) + "msecs");
        Instant endTime = Instant.now();
        String globalElapsedTime =
                + ChronoUnit.MINUTES.between(startTime, endTime) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTime, endTime) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTime, endTime) % 1000) + "msecs";
        LOG.info("Total Time: " + globalElapsedTime);
        topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"] /" + doc.getReference().getId()+"/"));
        return ChronoUnit.MILLIS.between(startTime, endTime);

    }

}
