package query;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;

public class CorpusViewerTest_24 {

	//Atributes
	private static final Logger LOG = LoggerFactory.getLogger(CorpusViewerTest_24.class);
	 private static final String COLLECTIONAME    = "cordis-documents";
	 private final int NROWS =10;
	 private static final String FIELD1    = "score";
	 private static final String FIELD2    = "text_t";
	 private static final String FIELDFACET   = "area_s";
	 private static final String textualQuery ="text_txt:*investigate*smarth*";
	 private static final String DATESTART  = "startDate_dt";
	 private static final String DATEND   = "endDate_dt";
	 private static final String start="1890-07-17T00:00:00Z";
	 private static final String end="2018-07-17T00:00:00Z";
	 private static TestSettings settings;
	 private static SolrClient client;
	 
	  
	    @BeforeClass
	    public static void setup(){
	    	 settings    = new TestSettings();
	         client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
	    }
	    
	 
	 
	 @Test
	    public void  iQuery() throws SolrServerException, IOException {
	  

		 	Instant startTime = Instant.now();
		 	long totalHits=0; 
	  	    SolrQuery idquery = new SolrQuery();
	  	    idquery.set("q","*:*");
	  	    //idquery.set("fq",textualQuery);
	  	    idquery.setRows(NROWS);
	  	    idquery.setFields("id,name_s",FIELD1,FIELD2,FIELDFACET,DATESTART,DATEND);
	  	    idquery.setFacet(true);
	  	    idquery.addFacetField(FIELDFACET); 
	        idquery.addFilterQuery(DATESTART+":["+start+ " TO NOW"+ "]");
	  	 //   idquery.addFilterQuery(DATEND+":["+end+ " TO NOW"+ "]");
	        
	  	    SolrDocumentList results = client.query(COLLECTIONAME,idquery).getResults();
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
		    		
	        	 LOG.info("Metadatos & Textual Query   id : "+(String)doc.get("id")+ ", name : "+ (String)doc.get("name_s"));
			    	
	    		});
	  	    
	  	    

	  	 
	  	    }
	  	   @After
	  	    public void shutdown() throws IOException {
	  	    	if(client != null)
	  	    	client.close();
	  	    
	  	    }
	  	   
}
