package inference;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.SolrDocumentList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import es.gob.minetad.model.Document;
import es.gob.minetad.model.Score;
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;

public class InferenceMultiple {

	  private static final Logger LOG = LoggerFactory.getLogger(InferenceMultiple.class);
	    private static TestSettings settings;
	    private static SolrClient client;
	    private static String collection="cordis-doctopics";
	    private static String model="model";
	    private static String topics="true";
	    private static String url="cordis-70:7777";
	    private static String prefix="t";
	    private static int multiplicationFactor=1000;
	    public static final String SEPARATOR=";";
	    private static double epsylon=0.0142;

	  
	    @BeforeClass
	    public static void setup(){
	    	 settings    = new TestSettings();
	         client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
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
	    	query.set("topics", topics);
	    	query.set("url", url);
	    	//query.set("fq", "id:EU92349"); //filtro sobre los metadatos.
	    	//query.set("q","id:EU92349"); //filtro sobre los metadatos.
	    	query.set("q","{!frangeext}query($qq)");
	    	query.set("pruebas", false); //parametro que se quitará
	    	query.set("multiplicationFactor", multiplicationFactor+"");
	    	query.addField("jsWeight:[js],score,id,listaBM,name_s");
	    	query.set("epsylon", epsylon+"");
	    	query.setRows(Integer.MAX_VALUE);
	    	query.setSort("score", ORDER.desc);
	    	 BufferedReader br = null;
	         
	         try {
	            
	            br =new BufferedReader(new FileReader("/tmp/inference.csv"));
	            String line = br.readLine();
	            while (null!=line) {
	            	query.remove("qq");
	               String [] fields = line.split(SEPARATOR);
	               String consulta=fields[1];
	               if (consulta.startsWith("\"") && consulta.endsWith("\"")) {
	               query.set("qq", ""+consulta.substring(1,consulta.length()-1));
	               }else {
	                query.set("qq", ""+consulta); 
	               }
	               search("Boolean",query);
	               line = br.readLine();
	            }
	            
	         } catch (Exception e) {
	            e.printStackTrace();
	         } finally {
	            if (null!=br) {
	               br.close();
	            }
	    	
	    }
	        
	    }
	        
	    private static void search(String id,  SolrQuery query) throws IOException {
	    	String description = "Searching by '" + id + "' "; 
	    	long totalHits=0; 
	    	LOG.info(description + Strings.repeat("-",100-description.length()));
	    	System.out.println("QUERY : " +query.getQuery());
	    	List<Score> topDocss =new ArrayList<>();   
	    	Instant startTime = Instant.now();
	    			    try{
		    SolrDocumentList results = client.query(collection,query).getResults();
		    Instant globalEndTime = Instant.now();
	         String globalElapsedTime = ChronoUnit.HOURS.between(startTime, globalEndTime) + "hours "
	                 + ChronoUnit.MINUTES.between(startTime, globalEndTime) % 60 + "min "
	                 + (ChronoUnit.SECONDS.between(startTime, globalEndTime) % 60) + "secs "
	                 + (ChronoUnit.MILLIS.between(startTime, globalEndTime) % 60) + "msecs";
	         LOG.info("Total Query Time : " + globalElapsedTime);
	         LOG.info("Total Hits: " + results.size());
	         totalHits= results.size();
	         Instant startTimeJS = Instant.now();
	        results.iterator().forEachRemaining(doc ->{		    		
		    			topDocss.add(new Score(Double.parseDouble((String)doc.get("jsWeight")),new Document("ref"), new Document((String)doc.get("id"))));
			  });
	         List<Score> topDocs=topDocss.stream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).limit(20).collect(Collectors.toList());
	         topDocs.forEach(doc -> System.out.println("- " + doc.getSimilar().getId() + " \t ["+ doc.getValue()+"]"));
	    	
	    	Instant globalEndTimedd = Instant.now();
	        String globalElapsedTimed= ChronoUnit.HOURS.between(startTimeJS, globalEndTimedd) + "hours "
	                + ChronoUnit.MINUTES.between(startTimeJS, globalEndTimedd) % 60 + "min "
	                + (ChronoUnit.SECONDS.between(startTimeJS, globalEndTimedd) % 60) + "secs "
	                + (ChronoUnit.MILLIS.between(startTimeJS, globalEndTimedd) % 60) + "msecs";
	        System.out.println("Total JS Time: " + globalElapsedTimed);
	        System.out.println("Total Hits: " + totalHits );
	        
		    }catch (SolrServerException se) {
		    	 LOG.error(se.getLocalizedMessage()); 
			}	
		    
		 
	    	
	      
	    }

}
