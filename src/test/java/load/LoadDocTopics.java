package load;

import es.gob.minetad.doctopic.DocTopicIndexFactory;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.doctopic.TopicSummary;
import es.gob.minetad.model.CorporaCollection;
import es.gob.minetad.model.Corpus;
import es.gob.minetad.model.SolrCollection;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *  Create a 'doctopics' collection for a given Corpus
 *
 *  It Requires a Solr server running:
 *    1. move into: src/test/docker/solr
 *    2. create (or start) a container: ./start.sh
 *    3. create collections: ./create-collections.sh
 *
 *  http://localhost:8983/solr/#/doctopics
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 *
 */
public abstract class LoadDocTopics extends LoadData{

    private static final Logger LOG = LoggerFactory.getLogger(LoadDocTopics.class);
    protected DocTopicsIndex docTopicIndexer;
    protected SolrCollection documentCollection;
    protected double entropy;
    protected int size;

    public LoadDocTopics(String corpus, Integer max, Integer offset) {
        super(corpus, "doctopics", max, offset);
        try {
            this.docTopicIndexer        = DocTopicIndexFactory.newFrom(numTopics);
            this.entropy                = 0.0;
            this.size                   = 0;
            this.documentCollection =new SolrCollection(name+"-documents");
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
        }
    }

    protected abstract SolrInputDocument newDocTopic(String id);

    protected SolrInputDocument newDocument(String row){

        String[] values = row.split(",");
        String id = values[0];
        List<Double> topicDistribution = new ArrayList<>();
        for(int i=1;i<values.length;i++){
            topicDistribution.add(Double.valueOf(values[i]));
        }

        SolrInputDocument document = newDocTopic(id);

        String docTopic = docTopicIndexer.toString(topicDistribution);
        document.addField("listaBO",docTopic);
        document.addField("listaBM",docTopic);

        double docEntropy = -topicDistribution.stream().map(theta -> theta * Math.log(theta)).reduce((a, b) -> a + b).get();
        docEntropy = docEntropy / Math.log(numTopics);
        document.addField("entropy_f",docEntropy);
        incrementEntropy(docEntropy);


        TopicSummary tsq1 = new TopicSummary(topicDistribution);
        for(int i=1; i<6; i++){
            Integer hashCode = tsq1.getHashCode(i);
            if (hashCode == 0) continue;
            document.addField("hashcode"+i+"_i",hashCode);
            document.addField("hashexpr"+i+"_txt",""+tsq1.getHashTopics(i).replace("|"," "));
        }

        return document;

    }

    protected void handleComplete(){
        CorporaCollection corporaCollection;
        try {
            corporaCollection = new CorporaCollection();
            Corpus corpus = new Corpus(name+"-"+numTopics);
            double corpusEntropy = (Double.valueOf(this.entropy) / Double.valueOf(this.size));
            corpus.setEntropy(corpusEntropy);
            corporaCollection.add(corpus);

            corporaCollection.commit();
        } catch (Exception e) {
            LOG.error("Unexpected error",e);
        }
    }

    private synchronized void incrementEntropy(double val){
        this.entropy += val;
        this.size++;
    }


}
