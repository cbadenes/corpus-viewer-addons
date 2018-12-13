package query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;

public class TraversalGraphTest {

	  private static final Logger LOG = LoggerFactory.getLogger(TraversalGraphTest.class);
	  private static TestSettings settings;
	  private static SolrClient client;
	    

	
	  private static final String COLLECTION      = "cordis-documents";
	//  private static final String ZK ="localhost:9983";
	    
	    private static final String Q1 ="\"totalCost_f:[50000000 TO *]\"";
	    private static final String Q12 ="\"startDate_dt:[2008-01-17T00:00:00Z TO NOW]\"";
	    private static final int ROWS=200;
	    //Fields in the traversal graph
	    private static final String FL ="\"id,instrument_s\"";
	    private static final String SORT="\"id asc\"";
	    private static final String WALK = "\"id->id\"";
	    private static final String GATHER="\"instrument_s\"";
	    private static final String FILE ="C:\\Users\\eliana.vallejo\\Documents\\graph.gexf";
	
	    //Fields graph query parser
	    //For this case, the query can´t be in quotes.
	    private static final String Q2 ="totalCost_f:[50000000 TO *]";
	    private static final String Q21 ="startDate_dt:[2008-01-17T00:00:00Z TO NOW]";
	    private static final String FROM = "instrument_s";
	    private static final String TO = "id";
			    
	    @BeforeClass
	  public static void setup(){
	        settings    = new TestSettings();
	        client      = SolrClientFactory.create(settings.get("solr.url"), settings.get("solr.mode"));
	
	    }
	    
	    @AfterClass
	    public static void shutdown() throws IOException {
	    	client.close();
	    }
	    

	    public String getClause() { 
	    	
	    	String cexpr="gatherNodes(cordis-documents, search(cordis-documents, q="+Q12+",fl="+
	    	FL+",rows="+ROWS+",sort="+SORT+"), walk="+WALK+", gather="+GATHER+", scatter=\"leaves,branches\", trackTraversal=true )";
	         return cexpr;
	    }
	    
	    @Test
	    public void getTraversalGraph()  {
	    	  System.out.println("\r\n Traversal Graph \r\n ");
	    	try {
	    		ModifiableSolrParams params = new ModifiableSolrParams();
	    		String expr =getClause();
	    		params.add("expr", expr);
	    		QueryRequest query = new QueryRequest(params);
			    query.setPath("/"+COLLECTION+"/graph");
			    query.setResponseParser(new InputStreamResponseParser("xml"));
			    query.setMethod(SolrRequest.METHOD.POST);
			    NamedList<Object> genericResponse = client.request(query);
			    InputStream stream = (InputStream)genericResponse.get("stream");
			    String xml = IOUtils.toString(stream, "UTF-8");
			    System.out.println(xml);
		
			    //write graph
		    	 
			    	    File targetFile = new File(FILE);
			    	     FileOutputStream fop = new FileOutputStream(targetFile);
			    		// if file doesn't exists, then create it
			    		if (targetFile.exists()) {
			    			targetFile.delete();
			    			targetFile.createNewFile();
			    		}else {
			    			targetFile.createNewFile();
			    		}
			    	    
			    	    fop.write(xml.getBytes());
			    	    fop.close();
			    
	    	}catch(Exception e){
	    		System.out.println(e.getMessage());
	    	}
	    	

	    }
	    
	    @Test
	   //OTHER METHOD FOR GRAPH 
	    public void graphQueryParser() throws SolrServerException, IOException {
	    	
	    	String filterQ="{!graph from="+FROM+" to="+TO+"}"+FROM+","+TO;
	    	SolrQuery query = new SolrQuery();
	    	 query.setQuery(Q21);
		  	 query.setRows(ROWS);
		  	 query.setParam("fl", filterQ);
	
		  	 QueryResponse rsp = client.query(COLLECTION,query);
		  	  System.out.println("\r\n Graph Query Parser \r\n ");
		        if ((rsp.getResults() == null) || (rsp.getResults().isEmpty())) {
		            LOG.info("No documents found");
		            return;
		        }
		        //
		      	SolrDocumentList docList= rsp.getResults();
			  	JSONObject returnResults = new JSONObject();
			  	Map<Integer, Object> solrDocMap = new HashMap<Integer, Object>();
			  	int counter = 1;
			  	for(Map singleDoc : docList)
			  	{
			  	  solrDocMap.put(counter, new JSONObject(singleDoc));
			  	  counter++;
			  	}
			  	returnResults.put("docs", solrDocMap);
			  	System.out.println(returnResults);

		        LOG.info("Found " + rsp.getResults().size() + " documents: ");
		      
	
		       for(SolrDocument doc: rsp.getResults()){
		            LOG.info("Document: " + doc);
		            System.out.println("Document: " + doc);
		        }
		  	 
	    }
	    


}
