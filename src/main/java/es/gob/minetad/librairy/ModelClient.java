package es.gob.minetad.librairy;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.gob.minetad.model.RestResource;
import es.gob.minetad.model.Topic;
import es.gob.minetad.model.TopicWord;
import es.gob.minetad.model.Word;
import org.apache.hadoop.util.hash.Hash;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ModelClient extends RestResource {

    private static final Logger LOG = LoggerFactory.getLogger(ModelClient.class);
    private final Object endpoint;

    public ModelClient(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<Topic> getTopics(){



        try {
            HttpResponse<JsonNode> response = Unirest.get(endpoint + "/dimensions").asJson();

            if (response.getStatus() == 200){

                List<Topic> topicList = new ArrayList<>();

                Iterator<Object> iterator = response.getBody().getObject().getJSONArray("dimensions").iterator();

                while(iterator.hasNext()){
                    JSONObject jsonObject = (JSONObject) iterator.next();
                    Topic topic = new Topic();
                    topic.setId(String.valueOf(jsonObject.getInt("id")));
                    topic.setName(jsonObject.getString("name"));
                    topic.setDescription(jsonObject.getString("description"));
                    topicList.add(topic);
                }


                List<Topic>topics = topicList.parallelStream().map( topic -> {
                    Integer offset      = 0;
                    Integer maxWords    = 500;
                    Boolean completed   = false;

                    List<TopicWord> topicWords = new ArrayList<>();

                    while(!completed){
                        Map<String,Object> params = new HashMap<>();
                        params.put("maxWords",maxWords);
                        params.put("offset",offset);
                        HttpResponse<JsonNode> resp = null;
                        try {
                            resp = Unirest.get(endpoint + "/dimensions/"+topic.getId()).queryString(params).asJson();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (resp.getStatus() != 200){
                            LOG.error("Error reading topic: " + topic.getId() + " -> " + resp.getStatus() + ": " + resp.getStatusText());
                            break;
                        }

                        Iterator<Object> wit = resp.getBody().getObject().getJSONArray("elements").iterator();
                        AtomicInteger counter = new AtomicInteger();
                        while(wit.hasNext()){
                            JSONObject tw = (JSONObject) wit.next();
                            TopicWord topicWord = new TopicWord();
                            topicWord.setWord(new Word(tw.getString("value")));
                            topicWord.setScore(tw.getDouble("score"));
                            topicWords.add(topicWord);
                            counter.incrementAndGet();
                        }

                        completed = counter.get() < maxWords;

                        offset += maxWords;


                    }
                    topic.setWords(topicWords);
                    return topic;
                }).collect(Collectors.toList());

                return topics;
            }

        } catch (UnirestException e) {
            LOG.error("Error reading topics from " + endpoint, e);
        }

        return Collections.emptyList();
    }

}
