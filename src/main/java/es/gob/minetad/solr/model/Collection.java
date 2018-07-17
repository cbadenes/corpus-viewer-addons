package es.gob.minetad.solr.model;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import es.gob.minetad.model.RestResource;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Collection extends RestResource {

    private static final Logger LOG = LoggerFactory.getLogger(Collection.class);

    public static boolean create(SolrClient client, String name){
        try{
            LOG.info("Creating collection '" + name + "' ..");
//            String uri = endpoint.startsWith("http")? endpoint + "/admin/collections" : "http://" + endpoint + "/admin/collections";
//            Map<String,Object> params = new HashMap<>();
//
//            params.put("action","CREATE");
//            params.put("name",name);
//            params.put("numShards",1);
//            params.put("replicationFactor",1);
//            params.put("wt","json");

            CollectionAdminRequest.createCollection(name, 1,1).process(client);


//            HttpResponse<JsonNode> response = Unirest.get(uri).queryString(params).asJson();
//
//            if (response.getStatus() != 200){
//                LOG.warn("Collection not created: " + response.getBody().getObject().getJSONObject("error").getString("msg"));
//                return false;
//            }
            LOG.info("Collection '"+ name + "' created");
            return true;
        }catch (Exception e){
            LOG.error("Error creating collection",e);
            return false;
        }
    }

    public static boolean remove(String endpoint, String name){

        String uri = endpoint.startsWith("http")? endpoint + "/admin/collections" : "http://" + endpoint + "/admin/collections";
        try{
            Map<String,Object> params = new HashMap<>();

            params.put("action","DELETE");
            params.put("name",name);
            params.put("wt","json");

            HttpResponse<JsonNode> response = Unirest.get(uri).queryString(params).asJson();

            if (response.getStatus() != 200){
                LOG.warn("Collection not deleted: " + response.getBody().getObject().getJSONObject("error").getString("msg"));
                return false;
            }
            LOG.info("Collection '"+ name + "' removed");
            return true;
        }catch (Exception e){
            LOG.error("Error removing collection",e);
            return false;
        }
    }

}
