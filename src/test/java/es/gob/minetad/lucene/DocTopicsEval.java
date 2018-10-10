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
import es.gob.minetad.utils.TimeUtils;
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
import org.junit.*;
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
    public static final Integer NUM_DOCS        = -1;
    public static final Double MIN_SCORE        = 0.7;


    protected static FSDirectory directory;
    protected static DirectoryReader indexReader;

//    protected static List<Double> SAMPLE_VECTOR   = Arrays.asList(0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.03875,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.05125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.03875,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.05125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.013750000000000002,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125);
    protected static List<Double> SAMPLE_VECTOR   = Arrays.asList(0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.026250000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.013750000000000002,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125,0.00125);
    protected static Integer numTopics            = SAMPLE_VECTOR.size();
    protected static float epsylon                = 1f / numTopics;
    protected static float multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(numTopics).length()+1)).floatValue();
    protected static CRDCIndex crdcIndex          = new CRDCIndex(numTopics,0.001,100000);

    protected static String vector2String(List<Double> vector){
        return DocTopicsUtil.getVectorString(vector, multiplicationFactor, epsylon);
//        return crdcIndex.toString(vector);
    }

    protected static List<Double> string2Vector(String vector){
        return DocTopicsUtil.getVectorFromString(vector, multiplicationFactor, numTopics, epsylon);
//        return crdcIndex.toVector(vector);
    }

    protected static Map<String,Double> string2map(String vector){
        Map<String,Double> vectorMap = new ConcurrentHashMap<>();
        Arrays.stream(vector.split(" ")).parallel().forEach(t -> vectorMap.put(StringUtils.substringBefore(t,"|"),Double.valueOf(StringUtils.substringAfter(t,"|"))/multiplicationFactor));
        return vectorMap;
    }

    @Before
    public void setup() throws IOException {
        File indexFile = new File(INDEX_DIR);
        if (!indexFile.exists()) createIndex(indexFile);
        else directory = FSDirectory.open(indexFile.toPath());
        indexReader      = DirectoryReader.open(directory);
        LOG.info("num docs indexed: " + indexReader.numDocs());
        LOG.info("max docs indexed: " + indexReader.maxDoc());
    }

    protected static void createIndex(File indexFile) throws IOException {
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

    @After
    public void shutdown() throws IOException {
        indexReader.close();
    }

    protected void queryByDTF(String id, IndexSearcher searcher) throws ParseException, IOException {

        String queryString = vector2String(SAMPLE_VECTOR);
        QueryParser parser = new QueryParser(TopicIndexFactory.FIELD_NAME, new DocTopicAnalyzer());
        Query query = parser.parse(queryString);
        search(id+"-byDTF", searcher, query);


    }

    protected void queryByHash(String id, IndexSearcher searcher) throws ParseException, IOException {

        TopicHash topicHash = new TopicHash(SAMPLE_VECTOR);
        String queryStringPositive = topicHash.byInclusion();
        QueryParser parserPositive = new QueryParser(TopicIndexFactory.DOC_POSITIVE_HASH, new StandardAnalyzer());
        Query queryPositive = parserPositive.parse(queryStringPositive);
        search(id+"-byHash", searcher, queryPositive);
    }


    protected Long search(String id, IndexSearcher searcher, Query query) throws IOException {
        List<Double> v1 = SAMPLE_VECTOR;
//        Map<String, Double> m1 = string2map(vector2String(v1));
        String description = "Searching by '" + id + "' ";
        LOG.info(description + Strings.repeat("-",100-description.length()));
        Instant s1 = Instant.now();
        TopDocs results = searcher.search(query, indexReader.numDocs());
        Instant e1 = Instant.now();
        LOG.info("Total Hits: " + results.totalHits);
        TimeUtils.print(s1, e1, "Query");

        Instant s2 = Instant.now();
        List<Score> scoredDocs = Arrays.stream(results.scoreDocs).parallel().map(scoreDoc -> {
            try {
                org.apache.lucene.document.Document docIndexed = indexReader.document(scoreDoc.doc);
                String vectorString = String.format(docIndexed.get(TopicIndexFactory.FIELD_NAME));
                if (Strings.isNullOrEmpty(vectorString)) return new Score(0.0, new Document(), new Document());

                String hash = id.contains("byHash") ? String.format(docIndexed.get(TopicIndexFactory.DOC_POSITIVE_HASH)) : id.contains("exclusion") ? String.format(docIndexed.get(TopicIndexFactory.DOC_NEGATIVE_HASH)).substring(0, 50) + ".." : vectorString;

                List<Double> v2 = string2Vector(vectorString);
                return new Score(JensenShannon.similarity(v1, v2), new Document(hash), new Document(String.format(docIndexed.get(TopicIndexFactory.DOC_ID)) + "-" + scoreDoc.score));
            } catch (Exception e) {
                e.printStackTrace();
                return new Score(0.0, new Document(), new Document());
            }
        }).filter(s -> s.getValue() > MIN_SCORE).collect(Collectors.toList());

        Instant e2 = Instant.now();
        TimeUtils.print(s2,e2,"JSD");


        Instant s3 = Instant.now();
        List<Score> topDocs = scoredDocs.parallelStream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(10).collect(Collectors.toList());
        Instant e3 = Instant.now();
        TimeUtils.print(s3,e3,"Sort");


        TimeUtils.print(s1,e3,"Total");

        topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"] /" + doc.getReference().getId()+"/"));
        return ChronoUnit.MILLIS.between(s1, e3);

    }

}
