package es.gob.minetad.metric;

import com.google.common.util.concurrent.AtomicDouble;
import es.gob.minetad.model.Pair;
import es.gob.minetad.model.Permutations;
import es.gob.minetad.model.Ranking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 *   Based on:
 *   Kumar, R., & Vassilvitskii, S. (2010). Generalized distances between rankings.
 *   Proceedings of the 19th International Conference on World Wide Web - WWW ’10,
 *   (3), 571. http://doi.org/10.1145/1772690.1772749
 *
 */
public class ExtendedKendallsTau<T> implements RankingSimilarityMetric<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ExtendedKendallsTau.class);


    public Double similarity(Ranking<T> r1, Ranking<T> r2, SimilarityMeasure<T> similarityMeasure){

        Double distance = distance(r1,r2,similarityMeasure);
        double score = Math.abs(distance - 1);
        return score;

    }

    public Double similarity(Ranking<T> r1, Ranking<T> r2) {
        return similarity(r1, r2, (SimilarityMeasure<T>) new Levenshtein());
    }

    public Double distance(Ranking<T> r1, Ranking<T> r2, SimilarityMeasure<T> similarityMeasure){

        AtomicDouble distance = new AtomicDouble(0.0);
        List<Pair<T,T>> elements = new Permutations<T>().between(r1.getElements(), r2.getElements());

        elements.parallelStream().forEach( pair -> {
            T i = pair.getI();
            T j = pair.getJ();

            if (!r1.exist(i) || !r1.exist(j) || !r2.exist(i) || !r2.exist(j)) {
                distance.addAndGet(1.0);
                return;
            }

            Integer d1 = r1.getPosition(j)-r1.getPosition(i);
            Integer d2 = r2.getPosition(j)-r2.getPosition(i);

            // check pairwise inversions
            if ((d1*d2) < 0 ){

                LOG.debug("Pairwise inversion: " + pair);

                // Element Weights
                Double wi   = elementWeightOf(i, r1, r2);
                Double wj   = elementWeightOf(j, r1, r2);
                LOG.debug("\t -> Element Weight:: [wi=" + wi +",wj=" + wj+"] = " + (wi*wj));

                // Position weights
                Double pi   = costOfMoving(i, r1, r2);
                Double pj   = costOfMoving(j, r1, r2);
                LOG.debug("\t -> Position Weight:: [pi= " + pi + ",pj="+pj+"] = " + (pi*pj));

                // Element Similarities
                Double Dij  = similarityMeasure.similarity(i,j);
                Double sij  = 1.0 - Dij;
                LOG.debug("\t -> Element Similarity = " + sij);

                distance.addAndGet((wi+wj+pi+pj+sij)/5.0);
            }
        });

        return distance.get()/elements.size();
    }


    private Double elementWeightOf(T element, Ranking r1, Ranking r2){
        return (r1.getWeight(element)+r2.getWeight(element))/2.0;
    }

    private Double costOfMoving(T element, Ranking r1, Ranking r2){

        Integer i           = r1.getPosition(element);
        Integer sigmai      = r2.getPosition(element);

        if (i == sigmai) return 0.0;

        Integer maxDisplacement = r1.getElements().size();

        Integer maxWeight = maxDisplacement * (maxDisplacement + 1) / 2;

        Integer a = maxDisplacement + 1 - Math.min(i, sigmai);
        Integer b = maxDisplacement + 1 - Math.max(i, sigmai);

        Integer displacement = 0;
        for (int j = a; j >= b ; j--){
            displacement += j;
        }

        Double weightDisplacement = Double.valueOf(displacement) / maxWeight;
        return weightDisplacement;
//
//        Double pi           = positionWeightOf(i,maxDisplacement);
//        Double psigmai      = positionWeightOf(sigmai,maxDisplacement);
//
//        return Math.abs(pi-psigmai);
    }


//    private static Double positionWeightOf(Integer position, Integer maxDisplacement){
//        if (position == 1) return Double.valueOf(maxDisplacement-1);
//
//        Double accumulatedCost = 0.0;
//
//        for (int j = 1; j<=(position-1); j++){
//            accumulatedCost += swapCostOf(j,maxDisplacement);
//        }
//
//        return accumulatedCost;
//    }

//    private static Double swapCostOf(Integer j, Integer maxDisplacement){
//        return ((maxDisplacement-j)/Double.valueOf(maxDisplacement));
//    }

}
