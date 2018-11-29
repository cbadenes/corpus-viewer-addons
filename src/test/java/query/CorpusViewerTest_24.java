package query;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CorpusViewerTest_24 {

	//Atributes
	
	 private static final String COLLECTIONAME    = "CORDIS_150";
	 public  CloudSolrClient client = null;
	 
	 private final int NROWS =10;
	 private static final String FIELD1    = "score";
	 private static final String FIELD2    = "text_t";
	 private static final String FIELDFACET   = "area_s";
	 private static final String textualQuery ="text_t: investigate smarth";
	 private static final String DATESTART  = "date_dt";
	 private static final String DATEND   = "endDate_dt";
	 private static final String start="2016-07-17T00:00:00Z";
	 private static final String end="2018-07-17T00:00:00Z";
	 @Before
	    
	    public  void getConnection() {
	    	
	    	  client = new CloudSolrClient.Builder(Arrays.asList(new String[]{"localhost:9983"}), Optional.empty())
	    	            .withConnectionTimeout(10000)
	    	            .withSocketTimeout(60000)
	    	            .build();

	    	     client.setDefaultCollection(COLLECTIONAME);
	    	     client.connect();
	    	 
	    	        	
	    }
	    @Test
	    public void  iQuery() throws SolrServerException, IOException {
	  	  //  List<Document> docList = new ArrayList<Document>();
	  	   // String vectorResponse="";
	  	    //Solr query

	  	   
	  	       
	  	    SolrQuery idquery = new SolrQuery();
	  	    idquery.setQuery(textualQuery);
	  	    idquery.setRows(NROWS);
	  	    idquery.setFields("id",FIELD1,FIELD2,FIELDFACET,DATESTART,DATEND);
	  	    idquery.setFacet(true);
	  	//Adding the facet field 
	        idquery.addFacetField(FIELDFACET); 
	     // Adding filter query
	        idquery.addFilterQuery(DATESTART+":["+start+ " TO NOW"+ "]");
	  	    idquery.addFilterQuery(DATEND+":["+end+ " TO NOW"+ "]");
	        
	  	  	   
	  	    QueryRequest req = new QueryRequest(idquery);
	        NoOpResponseParser dontMessWithSolr = new NoOpResponseParser();
	  	    dontMessWithSolr.setWriterType("json");
	  	    client.setParser(dontMessWithSolr);
	  	    NamedList<Object> resp = client.request(req);
	  	    String jsonResponse = (String) resp.get("response");
	  	    System.out.println(jsonResponse);
	  	    Assert.assertTrue("Verify that we get back some JSON",
	  	    jsonResponse.startsWith("{\"responseHeader"));
	  	    

	  	 
	  	    }
	  	   @After
	  	    public void shutdown() throws IOException {
	  	    	if(client != null)
	  	    	client.close();
	  	    
	  	    }
	  	   
}
