package query;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.cloud.ClusterState;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;

public class SolrCloudIntTest {
	
    private static final Logger LOG = LoggerFactory.getLogger(SolrCloudIntTest.class);
    private static CloudSolrClient client;
    private static String consultaT="The detector includes scintillators (S1 to S5), a photo-sensor device (F1 to F5) and an intermediate layer (10). At least some parts of the intermediate layer (10) contain material that absorbs electromagnetic radiation (9). The intermediate layer may be implemented in the form of an adhesive containing a carbon black component which attenuates radiation directly passing through the layer, depending of the thickness of the intermediate layer. The thickness may be 2 to 20 micrometres, attenuating radiation by 5 to 20 per cent. Independent claims are included for an adhesive for joining scintillators and photo-sensors of a detector and for a computer tomograph.";
    private static String collection="patentes";
    private static String model="patstat-model";
    private static String prefix="Patstat_750";
    private static int multiplicationFactor=10000; 
  
    @BeforeClass
    public static void setup(){
    
    	client = new CloudSolrClient.Builder(Arrays.asList(new String[]{"localhost:9983"}), Optional.empty())
                .withConnectionTimeout(10000)
                .withSocketTimeout(6000000)
                .build();
        client.setDefaultCollection(collection);
        client.connect();

        ClusterState clusterState = client.getZkStateReader().getClusterState();
        LOG.info("Collection: " + clusterState);
    }
    
    @AfterClass
    public static void shutdown() throws IOException {
    	client.close();
    }
    
    
    @Test
    public void booleanQuery() throws IOException {
    	
    	SolrQuery query = new SolrQuery();
    	query.set("model", model);
    	query.set("prefix", prefix);
    	query.set("qq", ""+consultaT);
    	//query.set("fq", "id:3"); filtro extra sobre los metadatos.
    	query.set("q","{!frangeext}query($qq)");
    	query.set("pruebas", true); //parametro para usar un vector customizado cordis-70
    	query.set("multiplicationFactor", multiplicationFactor+"");
    	query.addField("jsWeight:[js],score,id,listaBM");
    	query.setSort("id", ORDER.asc);
    	search("Boolean",query);
    }
    
 
 
    private static void search(String id,  SolrQuery query) throws IOException {
    	String description = "Searching by '" + id + "' "; 
    	long totalHits=0; 
    	LOG.info(description + Strings.repeat("-",100-description.length()));
    	System.out.println("QUERY : " +query.getQuery());
    	List<Score> topDocss =new ArrayList<>();   
    	Instant startTime = Instant.now();
    	
	    try{
	    SolrDocumentList results = client.query(query).getResults();
	    Instant globalEndTime = Instant.now();
         String globalElapsedTime = ChronoUnit.HOURS.between(startTime, globalEndTime) + "hours "
                 + ChronoUnit.MINUTES.between(startTime, globalEndTime) % 60 + "min "
                 + (ChronoUnit.SECONDS.between(startTime, globalEndTime) % 60) + "secs "
                 + (ChronoUnit.MILLIS.between(startTime, globalEndTime) % 60) + "msecs";
         LOG.info("Total Query Time : " + globalElapsedTime);
         LOG.info("Total Hits: " + results.getNumFound() );
         totalHits= results.getNumFound();
         Instant startTimeJS = Instant.now();
         results.iterator().forEachRemaining(doc ->{
	    		
	    			topDocss.add(new Score(Double.parseDouble((String)doc.get("jsWeight")),new Document("ref"), new Document((String)doc.get("id"))));
		    	
    		});
         List<Score> topDocs=topDocss.stream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(totalHits).collect(Collectors.toList());
     	 topDocs.forEach(doc -> LOG.info("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"]"));
    	
    	Instant globalEndTimedd = Instant.now();
        String globalElapsedTimed= ChronoUnit.HOURS.between(startTimeJS, globalEndTimedd) + "hours "
                + ChronoUnit.MINUTES.between(startTimeJS, globalEndTimedd) % 60 + "min "
                + (ChronoUnit.SECONDS.between(startTimeJS, globalEndTimedd) % 60) + "secs "
                + (ChronoUnit.MILLIS.between(startTimeJS, globalEndTimedd) % 60) + "msecs";
        LOG.info("Total JS Time: " + globalElapsedTimed);
        LOG.info("Total Hits: " + totalHits );
    	
        
    	
    	
	    }catch (SolrServerException se) {
	    	 LOG.error(se.getLocalizedMessage()); 
		}	
	    
	 
    	
      
    }
   
    
    
    
   
    
}


