package query;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import es.gob.minetad.model.TestSettings;
import es.gob.minetad.solr.SolrClientFactory;

public class CordisDocumentsTest {

	private static final Logger LOG = LoggerFactory.getLogger(CordisDocumentsTest.class);
  	private static String consultaTexto="Phosphorus (P) is a key and often limiting nutrient for phytoplankton in the ocean. A strong positive feedback exists between marine P availability, primary production and ocean anoxia: increased production leads to ocean anoxia, which, in turn, decreases the burial efficiency of P in sediments and therefore increases the availability of P and production in the ocean. This feedback likely plays an important role in the present-day expansion of low-oxygen waters (“dead zones”) in coastal systems worldwide. Moreover, it contributed to the development of global scale anoxia in ancient oceans. Critically, however, the responsible mechanisms for the changes in P burial in anoxic sediments are poorly understood because of the lack of chemical tools to directly characterize sediment P. I propose to develop new methods to quantify and reconstruct P dynamics in low-oxygen marine systems and the link with carbon cycling in Earth’s present and past. These methods are based on the novel application of state-of-the-art geochemical analysis techniques to determine the burial forms of mineral-P within their spatial context in modern sediments. The new analysis techniques include nano-scale secondary ion mass spectrometry (nanoSIMS), synchotron-based scanning transmission X-ray microscopy (STXM) and laser ablation-inductively coupled plasma-mass spectrometry (LA-ICP-MS). I will use the knowledge obtained for modern sediments to interpret sediment records of P for periods of rapid and extreme climate change in Earth’s history. Using various biogeochemical models developed in my research group, I will elucidate and quantify the role of variations in the marine P cycle in the development of low-oxygen conditions and climate change. This information is crucial for our ability to predict the consequences of anthropogenically-enhanced inputs of nutrients to the oceans combined with global warming.";//"Two major challenges facing systems neuroscience today are (1) to relate computational brain theory with its notions of parallel computation and population-code representation to massively multivariate spatiotemporal brain-activity data as acquired with functional magnetic resonance imaging (fMRI) and cell recording and (2) to relate brain representations in animal models (e.g. nonhuman primates) to human brain representations. This project tackles these challenges with a focus on visual object recognition in human and nonhuman primates. Object recognition is a still poorly understood key problem of computational neuroscience with implications for cortical computation in general. We will test computational models and relate representational content of population codes between human and nonhuman primates by means of a novel multivariate technique called representational similarity analysis (RSA). The core idea of RSA is to characterize a given brain representation by a dissimilarity matrix of stimulus-evoked activity patterns and to visualize and statistically compare such dissimilarity matrices. In contrast to existing approaches, computational models here form an integral component of the analysis of brain-activity data. We will match up representationally homologous regions between human and nonhuman primate and determine which computational models best explain the empirical data (from human and nonhuman primate fMRI) for each brain region. Moreover this project will further develop the technique of RSA and provide an easy-to-use and freely available Matlab toolbox to the community. By richly relating brain theory to data and human to nonhuman primate studies, this project bridges major divides and will contribute to a more integrated systems neuroscience.";
	private static String consultaTopics="t1|19 t4|19 t11|19 t13|19 t17|30 t21|19 t23|99 t27|19 t33|19 t36|19 t37|19 t41|30 t42|30 t45|19 t50|53 t57|76 t59|53";
	private static String consultaTopicDTM="topic0";
	private static TestSettings settings;
    private static SolrClient client;
    private static String docTopicsCollection="cordis-doctopics";
    private static String topicsDocsCollection="cordis-topicdocs";
    private static String patTopicsDocsCollection="patents-topicdocs";
    private static String corporaCollection="corpora";
    private static String topicPercentageQuery="t13";
    private static int modelSize=70;
  
  
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
    public void corporaQuery() throws IOException {
    		    	
    	SolrQuery query = new SolrQuery();
    	query.set("q", "*:*");
    	query.set("fl", "id,name_s,entropy_f");
    	System.out.println("corporaQuery" + Strings.repeat("-",70-"corporaQuery".length()));
    		try {
		    	SolrDocumentList results = client.query(corporaCollection,query).getResults();
		    	
		    	 results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("name_s : "+(String)doc.get("name_s")+", entropy_f : "+(Float)doc.get("entropy_f"));
			   });
		    	
    	}catch (SolrServerException se) {
    		LOG.error(se.getMessage());
    		
		}
    	
    }

    @Test
    public void topicPercentage() throws IOException {	    
    	SolrQuery query = new SolrQuery();
    	query.set("q", "docfreq(listaBO,"+topicPercentageQuery+")");
    	//query.set("fq", "area_s:ES_p AND topicWater_i:0"); // los fq hay q añadirlos con los operadores logicos
    	query.set("fl", "id,name_s,percentage:[percentage],score");
    	query.setSort("score", ORDER.desc);
    	query.setRows(20);
    	SolrQuery queryTopic = new SolrQuery();
    	queryTopic.set("q", "id:13");
    	
    	System.out.println("topicPercentage" + Strings.repeat("-",70-"topicPercentage".length()));
    	try{
	    	SolrDocumentList resultsTopic=	client.query(topicsDocsCollection, queryTopic).getResults();
	    	resultsTopic.iterator().forEachRemaining(doc ->{
	    		System.out.println("Topic : "+ doc.get("words_tfdl")+",\n tfidf :"+ doc.get("wordsTFIDF_tfdl") );
	    	});
		    SolrDocumentList results = client.query(docTopicsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    	System.out.println("Topic Percentage id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s")+", percent : "+(String)doc.get("percentage")+", score : "+(Float)doc.get("score"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    @Test
    public void documentEntropy() throws IOException {	    
    	SolrQuery query = new SolrQuery();
    	query.set("q", "*:*");
    	//query.set("fq", "area_s:ES_p AND topicWater_i:0"); // los fq hay q añadirlos con los operadores logicos
    	query.set("fl", "id,name_s,entropy_f");
    	query.setRows(modelSize);
    	System.out.println("documentEntropy" + Strings.repeat("-",70-"documentEntropy".length()));
    	try{
		    SolrDocumentList results = client.query(docTopicsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Document Entropy id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s")+ ", entropy : "+(Float)doc.get("entropy_f") );
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    
    @Test
    public void topicEntropy() throws IOException {	    
    	SolrQuery query = new SolrQuery();
    	query.set("q", "*:*");
    	query.set("fl", "id,name_s,description_txt,model_s,entropy_f,weight_f");
    	query.setRows(modelSize);
    	System.out.println("topicPercentage" + Strings.repeat("-",70-"topicPercentage".length()));
    	try{
		    SolrDocumentList results = client.query(topicsDocsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Topic Entropy Details: name : "+(String)doc.get("name_s")+", description_txt: "+doc.get("description_txt")+", entropy : "+(Float)doc.get("entropy_f"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
  

    @Test
    public void relevantTopics() throws IOException {	    
    	SolrQuery query = new SolrQuery();
    	query.set("q", "*:*");
    	query.set("fl", "name_s,description_txt");
    	query.setRows(modelSize);
    	System.out.println("relevantTopics" + Strings.repeat("-",70-"relevantTopics".length()));
    	try{
		    SolrDocumentList results = client.query(topicsDocsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Topic Relevant Terms: name : "+(String)doc.get("name_s")+", description_txt: "+doc.get("description_txt"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    
    @Test
    public void weightTerms() throws IOException {	    
    	SolrQuery query = new SolrQuery();
    	query.set("q", "*:*");
    	query.set("fl", "name_s,words_tfdl,wordsTFIDF_tfdl,");	    	
    	System.out.println("weightTerms" + Strings.repeat("-",70-"weightTerms".length()));
    	query.setRows(modelSize);
    	try{
		    SolrDocumentList results = client.query(topicsDocsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Topic Weigth Terms: name : "+(String)doc.get("name_s")+", words_tfdl: "+doc.get("words_tfdl")+", wordsTFIDF_tfdl: "+doc.get("wordsTFIDF_tfdl"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    @Test
    public void topicsMLT() throws IOException {	    
    	SolrQuery query = new SolrQuery();
    	query.setRequestHandler("/mlt");
    	query.set("q", "listaMLT:"+consultaTopics);
    	query.set("df", "listaMLT");
    	query.setSort("score", ORDER.desc);
    	query.setRows(20);
    	System.out.println("topicsMLT" + Strings.repeat("-",70-"topicsMLT".length()));
    	try{
		    SolrDocumentList results = client.query(docTopicsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Topics MLT id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    
    @Test
    public void DocumentsMLT() throws IOException {	    
      	SolrQuery query = new SolrQuery();
    	query.setRequestHandler("/mlt");
    	query.set("q", "text_txt:\""+consultaTexto+"\"");
    	query.set("df", "text_txt");
    	query.setSort("score", ORDER.desc);
    	query.setRows(20);
    	System.out.println("DocumentsMLT" + Strings.repeat("-",70-"DocumentsMLT".length()));
    	try{
		    SolrDocumentList results = client.query(docTopicsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Text MLT id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    @Test
    public void correlation() throws IOException {	    
      	SolrQuery query = new SolrQuery();
    	
    	query.set("q", "*:*");
    	query.set("fl", "id,description_txt,name_s,correlation:[correlation]");
    	query.set("model", "cordis-70:7777/model/topics/");
    	query.setRows(modelSize);
    	System.out.println("correlation" + Strings.repeat("-",70-"correlation".length()));
    	try{
		    SolrDocumentList results = client.query(topicsDocsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("Correlation id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s")+", \ndescription : "+doc.get("description_txt")+", \ncorrelation : "+doc.get("correlation"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	
	
    }
    
    
    @Test
    public void topicDTM() throws IOException {	    
    	
    	for (int year=1992;year<=2005; year++) {
      	SolrQuery query = new SolrQuery();
    	
    	query.set("q", "*:*");
    	query.set("fq", "model_s:dtm");
    	query.set("fq", "label_s:"+year);
    	query.set("fl", "id,name_s,weight_f,label_s,words_tfdl,wordsTFIDF_tfdl");
    	query.setSort("label_s",ORDER.asc);
    	
    	System.out.println("topicDTM" + Strings.repeat("-",70-"topicDTM".length()));
    	try{
		    SolrDocumentList results = client.query(patTopicsDocsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("topicDTM id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s")+", year : "+doc.get("label_s")+", weight : "+doc.get("weight_f"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	}
	
    }
    
    
    @Test
    public void topicTermsDTM() throws IOException {	    
    	
    	for (int year=1992;year<=2005; year++) {
      	SolrQuery query = new SolrQuery();	    	
    	query.set("q", "*:*");
    	query.set("fq", "model_s:dtm");
    	query.set("fq", "label_s:"+year);
    	query.set("fq", "name_s:"+consultaTopicDTM+"*");
    	query.set("fl", "id,name_s,weight_f,label_s,words_tfdl,wordsTFIDF_tfdl");
    	query.setSort("label_s",ORDER.asc);
    	
    	System.out.println("topicDTM" + Strings.repeat("-",70-"topicDTM".length()));
    	try{
		    SolrDocumentList results = client.query(patTopicsDocsCollection,query).getResults();
		    results.iterator().forEachRemaining(doc ->{		    		
		    			System.out.println("topicDTM id : "+(String)doc.get("id")+", name : "+(String)doc.get("name_s")+", year : "+doc.get("label_s")+", weight : "+doc.get("weight_f")+
		    					"\t\n terms and weights  : "+doc.get("words_tfdl")+ "\t\n terms and weights  TFIDF : "+doc.get("wordsTFIDF_tfdl"));
			  });
    	}catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
		}
    	}
	
    }
}
