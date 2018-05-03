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

public class CordisManager extends CorpusManager{

    private static final Logger LOG = LoggerFactory.getLogger(CordisManager.class);

    private static final String CORPUS_URL = "https://delicias.dia.fi.upm.es/nextcloud/index.php/s/hTu0RCHOVyh1ZAW/download";

    /**
     * Creates a jsonl.gz file from a MySQL dump
     */
    public void create() {


        Connection connect = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager.getConnection("jdbc:mysql://localhost/cordis?user=root&password=oeg");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select * from cordis.total_projects");

            // Time Zone
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz);

            // Corpus File
            File outputFile = new File("/tmp/corpus.jsonl.gz");
            if (outputFile.exists()) outputFile.delete();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile, false))));

            ObjectMapper jsonMapper = new ObjectMapper();
            AtomicInteger counter = new AtomicInteger(1);
            while(resultSet.next()) {

                CordisElement project = new CordisElement();
                project.setId(resultSet.getString("ProjectID"));
                project.setTitle(resultSet.getString("title"));
                project.setObjective(resultSet.getString("objective"));
                project.setInstrument(resultSet.getString("instrument"));
                project.setStartDate(df.format(resultSet.getDate("startDate")));
                project.setEndDate(df.format(resultSet.getDate("endDate")));
                project.setTopicWater(resultSet.getInt("topic_water"));
                project.setTotalCost(resultSet.getInt("totalCost"));
                project.setArea(resultSet.getString("area"));

                writer.write(jsonMapper.writeValueAsString(project)+"\n");

                LOG.info("added project ["+counter.getAndIncrement()+"] '" + project.getTitle() + "'");
            }

            LOG.info("Total Projects: " + counter.get());

            writer.close();

        } catch (Exception e) {
            LOG.error("Unexpected Error", e);
        } finally {
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                LOG.error("Unexpected Error", e);
            }
            if (statement != null) try {
                statement.close();
            } catch (SQLException e) {
                LOG.error("Unexpected Error", e);
            }
            if (connect!= null) try {
                connect.close();
            } catch (SQLException e) {
                LOG.error("Unexpected Error", e);
            }
        }
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

            String line = null;
            ObjectMapper jsonMapper = new ObjectMapper();
            AtomicInteger counter = new AtomicInteger(1);
            AtomicInteger emptyCounter = new AtomicInteger(1);
            while(!Strings.isNullOrEmpty(line = reader.readLine())){

                CordisElement project = jsonMapper.readValue(line,CordisElement.class);

                if (Strings.isNullOrEmpty(project.getObjective())){
                    LOG.warn("Empty project '" + project.getTitle() + "' ["+emptyCounter.getAndIncrement()+"]");
                    continue;
                }

                try{
                    Document document = new Document();
                    document.setId(project.getId());
                    document.setName(project.getTitle());
                    document.setLabels(Strings.isNullOrEmpty(project.getInstrument())? Arrays.asList(new String[]{"UNKNOWN"}) : Arrays.asList(new String[]{project.getInstrument().replace(" ","_")}));
                    document.setText(project.getObjective());

                    Unirest.post(endpoint + "/documents").basicAuth(user, pwd).body(document).asString();

                    if ((counter.getAndIncrement()) % 500 == 0) LOG.info("Added " + (counter.get()-1) + " docs");

                }catch (Exception e){
                    LOG.error("Error reading document",e);
                }
            }

            LOG.info((counter.get()-1) + " documents added. Checking Learner to create a new model .. ");

            Boolean indexed = false;

            while(!indexed){
                Thread.sleep(2000);
                HttpResponse<JsonNode> response = Unirest.get(endpoint + "/documents").basicAuth(user, pwd).asJson();
                int size = response.getBody().getObject().getInt("size");
                LOG.info(size + " documents indexed");
                indexed = size == (counter.get()-1);
            }

            LOG.info("Learner ready to train a new model");
            ModelParameters modelParameters = new ModelParameters();
            Map<String, String> parameters = ImmutableMap.of(
                    "algorithm","llda",
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

        CordisManager cordis = new CordisManager();

        // Create a jsonl.gz from MySQL dump
//        cordis.create();

        // Create a new Topic Model
        cordis.train();

    }

    private class CordisElement {

        private String id;

        private String title;

        private String objective;

        private String instrument;

        private String startDate;

        private String endDate;

        private Integer totalCost;

        private String area;

        private Integer topicWater;

        public CordisElement() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getObjective() {
            return objective;
        }

        public void setObjective(String objective) {
            this.objective = objective;
        }

        public String getInstrument() {
            return instrument;
        }

        public void setInstrument(String instrument) {
            this.instrument = instrument;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public Integer getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(Integer totalCost) {
            this.totalCost = totalCost;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public Integer getTopicWater() {
            return topicWater;
        }

        public void setTopicWater(Integer topicWater) {
            this.topicWater = topicWater;
        }
    }

}
