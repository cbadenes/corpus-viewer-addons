package es.gob.minetad.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class TopicWord {

    private static final Logger LOG = LoggerFactory.getLogger(TopicWord.class);

    private Word word;

    private Double score;

    public TopicWord() {
    }

    public TopicWord(Word word, Double score) {
        this.word = word;
        this.score = score;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicWord topicWord = (TopicWord) o;

        if (word != null ? !word.equals(topicWord.word) : topicWord.word != null) return false;
        return score != null ? score.equals(topicWord.score) : topicWord.score == null;

    }

    @Override
    public int hashCode() {
        int result = word != null ? word.hashCode() : 0;
        result = 31 * result + (score != null ? score.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopicWord{" +
                "word=" + word +
                ", score=" + score +
                '}';
    }
}
