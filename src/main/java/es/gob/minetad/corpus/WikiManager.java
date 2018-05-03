package es.gob.minetad.corpus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.learner.facade.rest.model.ModelParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 *  Community Research and Development Information Service
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class WikiManager extends CorpusManager{

    private static final Logger LOG = LoggerFactory.getLogger(WikiManager.class);

    private static final String CORPUS_URL = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/4tPyd5Ps51sCuRx/download";

    public void create() {

    }

    public void train(){

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new URL(CORPUS_URL).openStream())));

            String endpoint = "http://librairy.linkeddata.es/learner";
            String user     = System.getenv("LIBRAIRY_USER");
            String pwd      = System.getenv("LIBRAIRY_PWD");

            if (Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(pwd)){
                LOG.warn("No librAIry credentials found!");
                return;
            }

            Unirest.delete(endpoint + "/documents").basicAuth(user, pwd).asString();

            String line;
            ObjectMapper jsonMapper = new ObjectMapper();
            AtomicInteger counter = new AtomicInteger();
            Integer maxSize = 1000000;
            while(!Strings.isNullOrEmpty(line = reader.readLine()) && counter.get() <= maxSize ){

                com.fasterxml.jackson.databind.JsonNode json = jsonMapper.readTree(line);

                try{
                    Document document = new Document();
                    document.setId(json.get("url").asText());
                    document.setName(json.get("title").asText());
                    document.setLabels(Collections.emptyList());
                    document.setText(json.get("text").asText());

                    Unirest.post(endpoint + "/documents").basicAuth(user, pwd).body(document).asString();

                    if ((counter.incrementAndGet()) % 500 == 0) LOG.info("Added " + (counter.get()) + " docs");

                }catch (Exception e){
                    LOG.error("Error reading document",e);
                }
            }

            LOG.info((counter.get()) + " documents added");

            LOG.info("Learner ready to train a new model");
            ModelParameters modelParameters = new ModelParameters();
            Map<String, String> parameters = ImmutableMap.of(
                    "algorithm","lda",
                    "topics","200",
                    "language","en",
                    "email","cbadenes@fi.upm.es"
            );
            modelParameters.setParameters(parameters);

            HttpResponse<String> response = Unirest.post(endpoint + "/dimensions").basicAuth(user, pwd).body(modelParameters).asString();

            LOG.info("Training model. " + response.getStatus()+ "::" + response.getStatusText() + " -> " + response.getBody());

        } catch (Exception e) {
            LOG.error("Unexpected Error", e);
        }

    }


    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

        WikiManager manager = new WikiManager();

        // Create a new Topic Model
        manager.train();

    }

}
