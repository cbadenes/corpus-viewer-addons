package es.gob.minetad.doctopic;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class AccuracyEval {

    private static final Logger LOG = LoggerFactory.getLogger(AccuracyEval.class);

    private static final String SAMPLE_CORPUS       = "src/test/resources/cordis-projects-fp1-h2020_nsf-1984-2018_120.doc_topics.gz";
    private static final Integer SAMPLE_SIZE        = 100;
    private static final Integer NUM_SIMILAR_DOCS   = 20;
    private static final Integer NUM_TOPICS         = 120;

    private static EvaluationFactory evaluationFactory;

    @BeforeClass
    public static void setup() throws IOException {
        evaluationFactory = new EvaluationFactory(SAMPLE_CORPUS, SAMPLE_SIZE, NUM_SIMILAR_DOCS);
    }


    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher testWatcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            String methodName = description.getMethodName();
            String className = description.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            System.err.println("Starting JUnit-test: " + className + " " + methodName);
        }
    };



    @Test
    public void CleanZero() throws IOException {

        float precision = 1e4f;

        // evaluate index
        evaluationFactory.newFrom(new CleanZeroIndex(NUM_TOPICS, precision));
    }

    @Test
    public void cleanZeroAndEpsylon() throws IOException {

        float precision = 1e4f;
        float epsylon = 0.01f;

        // evaluate index
        evaluationFactory.newFrom(new CleanZeroEpsylonIndex(NUM_TOPICS, precision, epsylon));
    }

    @Test
    public void cleanZeroAndEpsylonLMDirichlet() throws IOException {

        float precision = 1e4f;
        float epsylon = 0.12f;

        // evaluate index
        evaluationFactory.newFrom(new CleanZeroEpsylonDirichletIndex(NUM_TOPICS, precision, epsylon));
    }

    @Test
    public void cleanZeroAndEpsylonTF() throws IOException {

        float precision = 1e4f;
        float epsylon   = 0.12f;

        // evaluate index
        evaluationFactory.newFrom(new CleanZeroEpsylonTFIndex(NUM_TOPICS,precision,epsylon));
    }

    @Test
    public void cleanZeroAndEpsylonTermVectorsMap() throws IOException {

        float precision = 1e4f;
        float epsylon = 0.01f;

        // evaluate index
        evaluationFactory.newFrom(new CleanZeroEpsylonTVMIndex(NUM_TOPICS,precision,epsylon));
    }

    @Test
    public void docTopicsLucene2SimilarityGraphArray() throws IOException {

        float precision = 1e4f;
        float epsylon = 0.15f;

        // evaluate index
        evaluationFactory.newFrom(new SimGraphArrayIndex(NUM_TOPICS,precision,epsylon));
    }

    @Test
    public void crdc() throws IOException {

        double threshold    = 0.85;
        int multiplier      = 10000;

        // evaluate index
        evaluationFactory.newFrom(new CRDCIndex(NUM_TOPICS, threshold, multiplier));
    }

    @Test
    public void tSNE() throws IOException {

        double perplexity   = 10.0;
        int multiplier      = 10000;

        // evaluate index
        evaluationFactory.newFrom(new TSNEIndex(NUM_TOPICS,multiplier,perplexity,evaluationFactory.getCorpusPath()));
    }

}
