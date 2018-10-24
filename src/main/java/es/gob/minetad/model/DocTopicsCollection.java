package es.gob.minetad.model;

import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.doctopic.TopicSummary;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * * Cálculo de entropías normalizadas *de documentos*

 #theta es el vector de “thetas” con dimensión igual al número de tópicos para cada documento
 #Es decir, cada posición del vector representa la importancia del tópico en el documento,
 #y la suma de todos los elementos del vector “theta” debe ser 1.
 doc_entropy = -np.sum(theta * np.log(theta),axis=1)

 #Dividiendo entre el log del número de tópicos normalizamos.
 doc_entropy = doc_entropy/np.log(ntopics)


 * Cálculo de entropía media del corpus

 Lo definimos simplemente como el promedio de las entropías normalizadas de todos los documentos del corpus

 *
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DocTopicsCollection extends SolrCollection {

    private static final Logger LOG = LoggerFactory.getLogger(DocTopicsCollection.class);
    private final Integer numTopics;
    private final float epsylon;
    private final float multiplicationFactor;
    private final CleanZeroEpsylonIndex docTopicIndexer;
    private double entropy;

    public DocTopicsCollection(String name, Integer numTopics) throws IOException, SolrServerException {
        super(name+"-doctopics-"+numTopics);
        this.counter                = new AtomicInteger();
        this.entropy                = 0.0;
        this.numTopics              = numTopics;
        this.epsylon                = 1f / numTopics;
        this.multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(numTopics).length()+1)).floatValue();
        this.docTopicIndexer        = new CleanZeroEpsylonIndex(numTopics, multiplicationFactor, epsylon);
    }


    public void add(String id, List<Double> topicDistribution ) throws IOException, SolrServerException {

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",id);

        String docTopic = docTopicIndexer.toString(topicDistribution);
        document.addField("doctopic",docTopic);

        double docEntropy = -topicDistribution.stream().map(theta -> theta * Math.log(theta)).reduce((a, b) -> a + b).get();
        docEntropy = docEntropy / Math.log(numTopics);
        document.addField("entropy",docEntropy);
        incrementEntropy(docEntropy);

        TopicSummary tsq1 = new TopicSummary(topicDistribution);
        document.addField("hashcodeQ1",tsq1.getHashCodeQ1());
        document.addField("hashtopicsQ1",""+tsq1.getHashTopicsQ1());
        document.addField("hashcodeQ2",tsq1.getHashCodeQ2());
        document.addField("hashtopicsQ2",""+tsq1.getHashTopicsQ2());


        //TODO debug
        document.addField("topics",topicDistribution);

        add(document);

    }

    private synchronized void incrementEntropy(double val){
        this.entropy += val;
    }

    public Integer getSize() {
        return counter.get();
    }

    public Integer getNumTopics() {
        return numTopics;
    }

    public double getEntropy() {
        return entropy;
    }

}
