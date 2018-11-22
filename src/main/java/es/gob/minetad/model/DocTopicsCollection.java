package es.gob.minetad.model;

import es.gob.minetad.doctopic.CleanZeroEpsylonIndex;
import es.gob.minetad.doctopic.DocTopicsIndex;
import es.gob.minetad.doctopic.TopicSummary;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
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
    private String metadataCollection;
    
    public DocTopicsCollection(String name, Integer dim) throws IOException, SolrServerException {
        super(name);
        this.counter                = new AtomicInteger();
        this.entropy                = 0.0;
        this.numTopics              = dim;
        this.epsylon                = 1f / numTopics;
        this.multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(numTopics).length()+1)).floatValue();
        this.docTopicIndexer        = new CleanZeroEpsylonIndex(numTopics, multiplicationFactor, epsylon);
    }


    public DocTopicsCollection(String name, Integer dim, String metadataCollection) throws IOException, SolrServerException {
        super(name);
        this.counter                = new AtomicInteger();
        this.entropy                = 0.0;
        this.numTopics              = dim;
        this.epsylon                = 1f / numTopics;
        this.metadataCollection=metadataCollection;
        this.multiplicationFactor   = Double.valueOf(1*Math.pow(10,String.valueOf(numTopics).length()+1)).floatValue();
        this.docTopicIndexer        = new CleanZeroEpsylonIndex(numTopics, multiplicationFactor, epsylon);
    }


    public void add(String id, List<Double> topicDistribution ) throws IOException, SolrServerException {

        SolrInputDocument document = new SolrInputDocument();
        document.addField("id",id);

  //vamos a buscar la inf de los metadatos en la coleccion de documents:
        

        SolrCollection metadata=new SolrCollection(metadataCollection);
        SolrQuery query = new SolrQuery();
       // List<Double> topicDistributionD=null;
        query.set("q", "id:"+id);
        QueryResponse response = metadata.getSolrClient().query(metadataCollection,query);
        
        SolrDocument doc=response.getResults().get(0);
        
       if (doc!=null) {
        	 document.addField("name_s",doc.get("name_s"));
             document.addField("text_txt",doc.get("text_txt"));
             document.addField("instrument_s",doc.get("instrument_s"));
             document.addField("startDate_dt",doc.get("startDate_dt"));
             document.addField("endDate_dt",doc.get("endDate_dt"));
             document.addField("totalCost_f",doc.get("totalCost_f"));
             document.addField("area_s",doc.get("area_s"));
             document.addField("topicWater_i",doc.get("topicWater_i"));
       // }
        
       // topicDistributionD=Arrays.asList(getVectorFromText(doc.get("text_txt").toString()));
       
        metadata.getSolrClient().close();
        
        String docTopic = docTopicIndexer.toString(topicDistribution);
        document.addField("listaBO",docTopic);
        document.addField("listaBM",docTopic);


        double docEntropy = -topicDistribution.stream().map(theta -> theta * Math.log(theta)).reduce((a, b) -> a + b).get();
        docEntropy = docEntropy / Math.log(numTopics);
        document.addField("entropy_f",docEntropy);
        incrementEntropy(docEntropy);

        TopicSummary tsq1 = new TopicSummary(topicDistribution);
        for(int i=0; i<6; i++){
            Integer hashCode = tsq1.getHashCode(i);
            if (hashCode == 0) continue;
            document.addField("hashcode"+i+"_i",hashCode);
            document.addField("hashexpr"+i+"_txt",""+tsq1.getHashTopics(i));
        }

     

        //TODO debug
//        document.addField("topics",topicDistribution);

        add(document);
       }

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

    public DocTopicsIndex getDocTopicIndexer() {
        return docTopicIndexer;
    }
    
    public static Double[] getVectorFromText(String texto) {
    	//	System.out.println("entra x aqui::"+ url);
    //	 System.out.println(texto);
    	texto=texto.replaceAll("\\[", "");
    	texto=texto.replaceAll("\\]", "");
    		String urlModel = "http://localhost:8000/model/inferences";//"http://"+url+"/" + model + "/inferences";
    		String json = "{\"text\":\"" + texto +"\" , \"topics\":"+"false" +"}";
    		Double[] vector = null;
    		String respuesta = null;
    		try {
    			URL obj = new URL(urlModel);
    			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    			con.setRequestProperty("Content-Type", "application/json");
    			con.setDoOutput(true);
    			con.setRequestMethod("POST");
    			OutputStream os = con.getOutputStream();
    			os.write(json.getBytes("UTF-8"));
    			os.close();
    			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    			String inputLine;
    			StringBuffer response = new StringBuffer();

    			while ((inputLine = in.readLine()) != null) {
    				response.append(inputLine);
    			}
    			in.close();
    			String res = response.toString();
    			respuesta = res.substring(res.indexOf("[") + 1, res.indexOf("]"));
    			String[] vectorS = respuesta.split(",");
    			vector = new Double[vectorS.length];
    			for (int i = 0; i < vectorS.length; i++) {
    				vector[i] = Double.parseDouble(vectorS[i]);
    			}

    		} catch (Exception e) {
    			// TODO: handle exception
    			System.out.println("Pues error : "+e.getMessage()+"..."+ e.getLocalizedMessage());
    			//e.printStackTrace();
    		}
    		return vector;

    	}
}
