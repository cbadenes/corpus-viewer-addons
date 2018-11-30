package inference;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
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
import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;

public class InferenceSimple {
	//pasar como para el epsylon y el factor de multiplicacion

	  private static final Logger LOG = LoggerFactory.getLogger(InferenceSimple.class);
	    private static TestSettings settings;
	    private static SolrClient client;
	   // private static CloudSolrClient client;
	    private static String consultaT="hosphorus (P) is a key and often limiting nutrient for phytoplankton in the ocean. A strong positive feedback exists between marine P availability, primary production and ocean anoxia: increased production leads to ocean anoxia, which, in turn, decreases the burial efficiency of P in sediments and therefore increases the availability of P and production in the ocean. This feedback likely plays an important role in the present-day expansion of low-oxygen waters (“dead zones”) in coastal systems worldwide. Moreover, it contributed to the development of global scale anoxia in ancient oceans. Critically, however, the responsible mechanisms for the changes in P burial in anoxic sediments are poorly understood because of the lack of chemical tools to directly characterize sediment P. I propose to develop new methods to quantify and reconstruct P dynamics in low-oxygen marine systems and the link with carbon cycling in Earth’s present and past. These methods are based on the novel application of state-of-the-art geochemical analysis techniques to determine the burial forms of mineral-P within their spatial context in modern sediments. The new analysis techniques include nano-scale secondary ion mass spectrometry (nanoSIMS), synchotron-based scanning transmission X-ray microscopy (STXM) and laser ablation-inductively coupled plasma-mass spectrometry (LA-ICP-MS). I will use the knowledge obtained for modern sediments to interpret sediment records of P for periods of rapid and extreme climate change in Earth’s history. Using various biogeochemical models developed in my research group, I will elucidate and quantify the role of variations in the marine P cycle in the development of low-oxygen conditions and climate change. This information is crucial for our ability to predict the consequences of anthropogenically-enhanced inputs of nutrients to the oceans combined with global warming.";//"Two major challenges facing systems neuroscience today are (1) to relate computational brain theory with its notions of parallel computation and population-code representation to massively multivariate spatiotemporal brain-activity data as acquired with functional magnetic resonance imaging (fMRI) and cell recording and (2) to relate brain representations in animal models (e.g. nonhuman primates) to human brain representations. This project tackles these challenges with a focus on visual object recognition in human and nonhuman primates. Object recognition is a still poorly understood key problem of computational neuroscience with implications for cortical computation in general. We will test computational models and relate representational content of population codes between human and nonhuman primates by means of a novel multivariate technique called representational similarity analysis (RSA). The core idea of RSA is to characterize a given brain representation by a dissimilarity matrix of stimulus-evoked activity patterns and to visualize and statistically compare such dissimilarity matrices. In contrast to existing approaches, computational models here form an integral component of the analysis of brain-activity data. We will match up representationally homologous regions between human and nonhuman primate and determine which computational models best explain the empirical data (from human and nonhuman primate fMRI) for each brain region. Moreover this project will further develop the technique of RSA and provide an easy-to-use and freely available Matlab toolbox to the community. By richly relating brain theory to data and human to nonhuman primate studies, this project bridges major divides and will contribute to a more integrated systems neuroscience.";
	    private static String collection="cordis-doctopics";
	    private static String model="model";
	    private static String topics="true";
	    private static String url="cordis-70:7777";
	    private static String prefix="t";
	    private static int multiplicationFactor=1000;
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
	    	query.set("qq", ""+consultaT);
	    	query.set("topics", topics);
	    	query.set("url", url);
	    	//query.set("fq", "id:EU92349"); //filtro  sobre los metadatos.
	    	query.set("q","{!frangeext}query($qq)");
	    	query.set("pruebas", true); //parametro que se quitará
	    	query.set("multiplicationFactor", multiplicationFactor+"");
	    	query.addField("jsWeight:[js],score,id,listaBM,name_s");
	    	query.set("epsylon", epsylon+"");
	    	query.setRows(Integer.MAX_VALUE);
	    	query.setSort("score", ORDER.desc);
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
