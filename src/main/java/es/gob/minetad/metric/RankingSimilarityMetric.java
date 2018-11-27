/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package es.gob.minetad.metric;


import es.gob.minetad.model.Ranking;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public interface RankingSimilarityMetric<T> {

    Double similarity(Ranking<T> r1, Ranking<T> r2);
}
