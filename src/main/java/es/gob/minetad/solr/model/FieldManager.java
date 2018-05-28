package es.gob.minetad.solr.model;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import es.gob.minetad.model.RestResource;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class FieldManager extends RestResource {

    private static final Logger LOG = LoggerFactory.getLogger(FieldManager.class);

    private final String endpoint;
    private final String collection;
    private final String uri;


    public FieldManager(String endpoint, String collection) {
        this.endpoint = endpoint;
        this.collection = collection;
        this.uri = this.endpoint + "/" + collection + "/schema";
    }

    public boolean add(String name, String type, Boolean stored){


        try{
            HttpResponse<JsonNode> response = Unirest.post(uri).body("{ \"add-field\": { \"name\": \""+name+"\", \"type\": \""+type+"\", \"stored\":\""+stored+"\"} }").asJson();
            if (response.getStatus() != 200){
                LOG.warn("Error adding field: " + ((JSONObject)response.getBody().getObject().getJSONObject("error").getJSONArray("details").get(0)).getJSONArray("errorMessages").get(0));
                return false;
            }
            return true;
        }catch (Exception e){
            LOG.error("Error adding field: " + name + " to collection: " + collection, e);
            return false;
        }

    }

    public boolean addType(String name, String type, String position, String analyzer){

        try{
            HttpResponse<JsonNode> response = Unirest.post(uri).body("{ \"add-field-type\": { \"name\": \""+name+"\", \"class\": \""+type+"\", \"positionIncrementGap\":\""+position+"\", \"analyzer\": "+analyzer+"} }").asJson();
            if (response.getStatus() != 200){
                LOG.warn("Error adding field-type: " + ((JSONObject)response.getBody().getObject().getJSONObject("error").getJSONArray("details").get(0)).getJSONArray("errorMessages").get(0));
                return false;
            }
            return true;
        }catch (Exception e){
            LOG.error("Error adding field-type: " + name + " to collection: " + collection, e);
            return false;
        }

    }


    public boolean remove(String name){

        try{
            HttpResponse<JsonNode> response = Unirest.post(uri).body("{\"delete-field\": { \"name\": \""+name+"\"} }").asJson();
            if (response.getStatus() != 200){
                LOG.warn("Error removing field: " + response.getBody());
                return false;
            }
            return true;
        }catch (Exception e){
            LOG.error("Error removing field: " + name + " to collection: " + collection, e);
            return false;
        }

    }

    public boolean removeType(String name){

        try{
            HttpResponse<JsonNode> response = Unirest.post(uri).body("{\"delete-field-type\": { \"name\": \""+name+"\"} }").asJson();
            if (response.getStatus() != 200){
                LOG.warn("Error removing field '" + name + "' :" + response.getBody());
                return false;
            }
            return true;
        }catch (Exception e){
            LOG.error("Error removing field: " + name + " to collection: " + collection, e);
            return false;
        }

    }

}
