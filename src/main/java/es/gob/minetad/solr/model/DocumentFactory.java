package es.gob.minetad.solr.model;

import es.gob.minetad.doctopic.TopicHash;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class DocumentFactory {


    public static Document newDocTopic(String vectorString, String fieldName) {
        Document testDoc = new Document();
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);//TYPE_NOT_STORED
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStoreTermVectors(true);
        Field textField = new Field(fieldName, vectorString, fieldType);
        testDoc.add(textField);
        return testDoc;
    }


    public static Document newDocId(String id, String topics) {
        Document luceneDoc = new Document();
        // id
        luceneDoc.add(new TextField(TopicIndexFactory.DOC_ID, id, Field.Store.YES));

        // doc-topic
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);//TYPE_NOT_STORED
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStoreTermVectorPositions(false);
        fieldType.setStoreTermVectorOffsets(false);
        fieldType.setStoreTermVectors(true);

        Field textField = new Field(TopicIndexFactory.FIELD_NAME, topics, fieldType);
        luceneDoc.add(textField);


        return luceneDoc;
    }


    public static Document newDoc(String id, TopicHash hash, String topics) {
        Document luceneDoc = new Document();
        // id
        luceneDoc.add(new TextField(TopicIndexFactory.DOC_ID, id, Field.Store.YES));

        // hash by inclusion
        luceneDoc.add(new TextField(TopicIndexFactory.DOC_POSITIVE_HASH, hash.byInclusion().replace("#"," "), Field.Store.YES));
        // hash by exclusion
        luceneDoc.add(new TextField(TopicIndexFactory.DOC_NEGATIVE_HASH, hash.byExclusion().replace("#"," "), Field.Store.YES));

        // doc-topic
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);//TYPE_NOT_STORED
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setStoreTermVectorPositions(false);
        fieldType.setStoreTermVectorOffsets(false);
        fieldType.setStoreTermVectors(true);

        Field textField = new Field(TopicIndexFactory.FIELD_NAME, topics, fieldType);
        luceneDoc.add(textField);


        return luceneDoc;
    }

}
