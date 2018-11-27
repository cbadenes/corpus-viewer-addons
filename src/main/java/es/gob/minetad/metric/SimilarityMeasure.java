/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package es.gob.minetad.metric;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface SimilarityMeasure<T> {

    Double similarity(T el1, T el2);
}
