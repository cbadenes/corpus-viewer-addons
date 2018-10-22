package es.gob.minetad.utils;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class RestClient {

    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);



    public static JsonNode get(String url, Integer statusCode) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.get(url).asJson();

        if (statusCode != response.getStatus()){
            throw new RuntimeException("Unexpected status code [" + response.getStatus()+"] -> " + response.getStatusText());
        }

        return response.getBody();

    }

    public static JsonNode get(String url, Map<String,Object> params, Integer statusCode) throws UnirestException {

        HttpResponse<JsonNode> response = Unirest.get(url)
                .queryString(params)
                .asJson();

        if (statusCode != response.getStatus()){
            throw new RuntimeException("Unexpected status code [" + response.getStatus()+"] -> " + response.getStatusText());
        }

        return response.getBody();
    }
}
