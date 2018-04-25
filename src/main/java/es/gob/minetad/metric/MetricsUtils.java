package es.gob.minetad.metric;

/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval �
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 19.05.13 11:24
 */

// TODO param Epsylon en constructor

import java.util.List;

public class MetricsUtils
{

    private static float math_log_2 = (float) Math.log(2f);

    /**
     * Manhattan distance
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double distL1(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += Math.abs(h1[i] - h2[i]);
        }
        return sum / h1.length;
    }

    public static double distL1(double[] h1, double[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += Math.abs(h1[i] - h2[i]);
        }
        return sum / h1.length;
    }

    /**
     * Euclidean distance
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double distL2(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] - h2[i]) * (h1[i] - h2[i]);
        }
        return Math.sqrt(sum);
    }

    /**
     * Euclidean distance
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double distL2(double[] h1, double[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] - h2[i]) * (h1[i] - h2[i]);
        }
        return Math.sqrt(sum);
    }

    /**
     * Euclidean distance
     *
     * @param h1
     * @param h2
     * @return
     */
    public static float distL2(float[] h1, float[] h2) {
        float sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] - h2[i]) * (h1[i] - h2[i]);
        }
        return (float) Math.sqrt(sum);
    }

    /**
     * Jeffrey Divergence or Jensen-Shannon divergence (JSD) from
     * Deselaers, T.; Keysers, D. & Ney, H. Features for image retrieval:
     * an experimental comparison Inf. Retr., Kluwer Academic Publishers, 2008, 11, 77-107
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double jsd(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] > 0 ? h1[i] * Math.log(2d * h1[i] / (h1[i] + h2[i])) : 0) +
                    (h2[i] > 0 ? h2[i] * Math.log(2d * h2[i] / (h1[i] + h2[i])) : 0);
        }
        return sum;
    }

    public static float jsd(float[] h1, float[] h2) {
        float sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] > 0 ? (h1[i] / 2f) * Math.log((2f * h1[i]) / (h1[i] + h2[i])) : 0) +
                    (h2[i] > 0 ? (h2[i] / 2f) * Math.log((2f * h2[i]) / (h1[i] + h2[i])) : 0);
        }
        return sum;
    }

    public static float jsd(double[] h1, double[] h2) {
        double sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] > 0 ? (h1[i] / 2f) * Math.log((2f * h1[i]) / (h1[i] + h2[i])) : 0) +
                    (h2[i] > 0 ? (h2[i] / 2f) * Math.log((2f * h2[i]) / (h1[i] + h2[i])) : 0);//2f????
        }
        return (float) sum;
    }

    public static float jsd_tuning1(double[] h1, double[] h2, float epsylon, float epsylon_2_2sqrt) {
        // lower bound 1
        double sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            double diff = h1[i] -  h2[i];
            sum +=  (diff > 0 ? diff : -diff);
        }

        if(sum >= epsylon_2_2sqrt){
            return epsylon;
        }

        sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] > 0 ? (h1[i] / 2f) * Math.log((2f * h1[i]) / (h1[i] + h2[i])) : 0) +
                    (h2[i] > 0 ? (h2[i] / 2f) * Math.log((2f * h2[i]) / (h1[i] + h2[i])) : 0);
        }
        return (float) sum;
    }

    public static float jsd_tuning2(short[] h1, short[] h2, float epsylon, float epsylon_2_2sqrt_short, float epsylon_cota2_2) {
        // lower bound 1
        int sum = 0;
        for (int i = 0; i < h1.length; i++) {
            int diff = (int)h1[i] -  (int)h2[i];
            sum +=  (diff >= 0 ? diff : -diff);
        }

        // lower bound 2
        if(sum >= epsylon_cota2_2){//sum > 0 y  epsylon_cota2_1 negativo
            return epsylon;
        }

        float sumF = 0f;
        for (int i = 0; i < h1.length; i++) {
            sumF += (h1[i] > 0 ? ((int)h1[i]) * Math.log((2f * (int)h1[i]) / ((int)h1[i] + (int)h2[i])) : 0) +
                    (h2[i] > 0 ? ((int)h2[i]) * Math.log((2f * (int)h2[i]) / ((int)h1[i] + (int)h2[i])) : 0);
        }
        return (float) sumF/20000f;
    }

    public static float jsd_tuning2(List<Double> h1, List<Double> h2, float epsylon, float epsylon_2_2sqrt_short, float epsylon_cota2_2) {
        // lower bound 1
        int sum = 0;
        for (int i = 0; i < h1.size(); i++) {
            int diff = h1.get(i).intValue() -  h2.get(i).intValue();
            sum +=  (diff >= 0 ? diff : -diff);
        }

        // lower bound 2
        if(sum >= epsylon_cota2_2){//sum > 0 y  epsylon_cota2_1 negativo
            return epsylon;
        }

        float sumF = 0f;
        for (int i = 0; i < h1.size(); i++) {
            sumF += (h1.get(i) > 0 ? h1.get(i).intValue() * Math.log((2f * h1.get(i).intValue()) / (h1.get(i).intValue() + h2.get(i).intValue())) : 0) +
                    (h2.get(i) > 0 ? h2.get(i).intValue() * Math.log((2f * h2.get(i).intValue()) / (h1.get(i).intValue() + h2.get(i).intValue())) : 0);
        }
        return (float) sumF/20000f;
    }

    public static float jsd_tuning3(short[] h1, short[] h2, float epsylon, float epsylon_2_2sqrt_short) {
        // lower bound 1
        int sum = 0;
        for (int i = 0; i < h1.length; i++) {
            int diff = h1[i] -  h2[i];
            sum +=  (diff > 0 ? diff : -diff);
        }

        if(sum >= epsylon_2_2sqrt_short){
            return epsylon;
        }

        float sumF = 0f;
        float math_log_2 = (float) Math.log(2f);
        for (int i = 0; i < h1.length; i++) {
            float math_log_h1_h2 = (float) Math.log(h1[i] + h2[i]);
            sumF += (h1[i] > 0 ? h1[i] * (math_log_2 + Math.log(h1[i]) - math_log_h1_h2) : 0) +
                    (h2[i] > 0 ? h2[i] * (math_log_2 + Math.log(h2[i]) - math_log_h1_h2) : 0);
        }
        return (float) sumF/20000f;
    }

    public static float jsd_tuning4(short[] h1, short[] h2, float[] log_h1, float[] log_h2, float epsylon, float epsylon_2_2sqrt_short) {
        // lower bound 1
        int sum = 0;
        for (int i = 0; i < h1.length; i++) {
            int diff = h1[i] -  h2[i];
            sum +=  (diff > 0 ? diff : -diff);
        }

        if(sum >= epsylon_2_2sqrt_short){
            return epsylon;
        }
        // lower bound 2

        float sumF = 0f;
        //float math_log_2 = (float) Math.log(2f);
        for (int i = 0; i < h1.length; i++) {
            float math_log_h1_h2 = (float) Math.log(h1[i] + h2[i]);
            sumF += (h1[i] > 0 ? h1[i] * (math_log_2 + log_h1[i] - math_log_h1_h2) : 0) +
                    (h2[i] > 0 ? h2[i] * (math_log_2 + log_h2[i] - math_log_h1_h2) : 0);
        }
        return (float) sumF/20000f;
    }

    public static float jsd_tuning5(short[] h1, short[] h2, float[] log_h1, float[] log_h2, float epsylon, float epsylon_2_2sqrt_short, float epsylon_cota2_2) {
        int sum = 0;
        for (int i = 0; i < h1.length; i++) {
            int diff = (int)h1[i] -  (int)h2[i];
            sum +=  (diff > 0 ? diff : -diff);
//        	if(sum >= epsylon_cota2_2){//sum > 0 y  epsylon_cota2_1 negativo
//        		return epsylon;
//        	}
        }
//    	// lower bound 1
//    	if(sum >= epsylon_2_2sqrt_short){
//    		return epsylon;
//    	}
        // lower bound 2
        //if(sum <=epsylon_cota2_1||sum >= epsylon_cota2_2){
        if(sum >= epsylon_cota2_2){//sum > 0 y  epsylon_cota2_1 negativo
            return epsylon;
        }

        float sumF = 0f;
        //float math_log_2 = (float) Math.log(2f);
        for (int i = 0; i < h1.length; i++) {
            if((int)h1[i] == 0 && (int)h2[i] == 0){
                continue;
            }
            float math_log_h1_h2 = 0;
            if(h1[i] > 0 && h2[i] > 0){
                math_log_h1_h2 = (float) Math.log((int)h1[i] + (int)h2[i]);
            } else if(h1[i] > 0){
                math_log_h1_h2 = log_h1[i];
            } else {
                math_log_h1_h2 = log_h2[i];
            }

            sumF += (h1[i] > 0 ? h1[i] * (math_log_2 + log_h1[i] - math_log_h1_h2) : 0) +
                    (h2[i] > 0 ? h2[i] * (math_log_2 + log_h2[i] - math_log_h1_h2) : 0);
        }
        return (float) sumF/20000f;
    }

    public static float jsd_tuning6(short[] h1, short[] h2, float[] log_h1, float[] log_h2, float epsylon, float epsylon_2_2sqrt_short, float epsylon_cota2_2) {
        int sum = 0;
        for (int i = 0; i < h1.length; i++) {
            int diff = (int)h1[i] -  (int)h2[i];
            sum +=  (diff > 0 ? diff : -diff);
        }

        if(sum >= epsylon_cota2_2){//sum > 0 y  epsylon_cota2_1 negativo
            return epsylon*20000f;
        }

        float sumF = 0f;
        for (int i = 0; i < h1.length; i++) {
            if(h1[i] == 0 && h2[i] == 0){
                continue;
            }
            float math_log_h1_h2 = 0;
            if(h1[i] > 0 && h2[i] > 0){
                math_log_h1_h2 = (float) Math.log((int)h1[i] + (int)h2[i]);
            } else if(h1[i] > 0){
                math_log_h1_h2 = log_h1[i];
            } else {
                math_log_h1_h2 = log_h2[i];
            }

            sumF += (h1[i] > 0 ? (int)h1[i] * (math_log_2 + log_h1[i] - math_log_h1_h2) : 0) +
                    (h2[i] > 0 ? (int)h2[i] * (math_log_2 + log_h2[i] - math_log_h1_h2) : 0);
        }
        return sumF;
    }
    public static double tanimoto(int[] h1, int[] h2) {
        double result = 0;
        double tmp1 = 0;
        double tmp2 = 0;

        double tmpCnt1 = 0, tmpCnt2 = 0, tmpCnt3 = 0;

        for (int i = 0; i < h1.length; i++) {
            tmp1 += h1[i];
            tmp2 += h2[i];
        }

        if (tmp1 == 0 && tmp2 == 0) return 0;
        if (tmp1 == 0 || tmp2 == 0) return 100;

        if (tmp1 > 0 && tmp2 > 0) {
            for (int i = 0; i < h1.length; i++) {
                tmpCnt1 += (h1[i] / tmp1) * (h2[i] / tmp2);
                tmpCnt2 += (h2[i] / tmp2) * (h2[i] / tmp2);
                tmpCnt3 += (h1[i] / tmp1) * (h1[i] / tmp1);

            }

            result = (100 - 100 * (tmpCnt1 / (tmpCnt2 + tmpCnt3
                    - tmpCnt1))); //Tanimoto
        }
        return result;
    }

    public static double tanimoto(float[] h1, float[] h2) {
        double result = 0;
        double tmp1 = 0;
        double tmp2 = 0;

        double tmpCnt1 = 0, tmpCnt2 = 0, tmpCnt3 = 0;

        for (int i = 0; i < h1.length; i++) {
            tmp1 += h1[i];
            tmp2 += h2[i];
        }

        if (tmp1 == 0 && tmp2 == 0) return 0;
        if (tmp1 == 0 || tmp2 == 0) return 100;

        if (tmp1 > 0 && tmp2 > 0) {
            for (int i = 0; i < h1.length; i++) {
                tmpCnt1 += (h1[i] / tmp1) * (h2[i] / tmp2);
                tmpCnt2 += (h2[i] / tmp2) * (h2[i] / tmp2);
                tmpCnt3 += (h1[i] / tmp1) * (h1[i] / tmp1);

            }

            result = (100 - 100 * (tmpCnt1 / (tmpCnt2 + tmpCnt3
                    - tmpCnt1))); //Tanimoto
        }
        return result;
    }

    public static double tanimoto(double[] h1, double[] h2) {
        double result = 0;
        double tmp1 = 0;
        double tmp2 = 0;

        double tmpCnt1 = 0, tmpCnt2 = 0, tmpCnt3 = 0;

        for (int i = 0; i < h1.length; i++) {
            tmp1 += h1[i];
            tmp2 += h2[i];
        }

        if (tmp1 == 0 && tmp2 == 0) return 0;
        if (tmp1 == 0 || tmp2 == 0) return 100;

        if (tmp1 > 0 && tmp2 > 0) {
            for (int i = 0; i < h1.length; i++) {
                tmpCnt1 += (h1[i] / tmp1) * (h2[i] / tmp2);
                tmpCnt2 += (h2[i] / tmp2) * (h2[i] / tmp2);
                tmpCnt3 += (h1[i] / tmp1) * (h1[i] / tmp1);

            }

            result = (100 - 100 * (tmpCnt1 / (tmpCnt2 + tmpCnt3 - tmpCnt1))); //Tanimoto
        }
        return result;
    }

    public static double cosineCoefficient(double[] hist1, double[] hist2) {
        double distance = 0;
        double tmp1 = 0, tmp2 = 0;
        for (int i = 0; i < hist1.length; i++) {
            distance += hist1[i] * hist2[i];
            tmp1 += hist1[i] * hist1[i];
            tmp2 += hist2[i] * hist2[i];
        }
        if (tmp1 * tmp2 > 0) {
            return (distance / (Math.sqrt(tmp1) * Math.sqrt(tmp2)));
        } else return 0d;
    }

    public static float distL1(float[] h1, float[] h2) {
        float sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            sum += Math.abs(h1[i] - h2[i]);
        }
        return sum;
    }

    public static double distL1(byte[] h1, byte[] h2) {
        double sum = 0f;
        for (int i = 0; i < h1.length; i++) {
            sum += Math.abs(h1[i] - h2[i]);
        }
        return sum;
    }


    /**
     * Computes the hellinger distance between two normal distributions P and Q.
     *
     * The function used is:
     *
     * H(P,Q)= 1 - SQRT( 2 * standard deviation of P * standard deviation of Q / variance of P + variance of Q) * e^{ -1/4 * ( (mean of P - mean of Q)^{2} / variance of P + variance of Q)}
     *
     * @return the entropy for the given class distribution.
     */
    public static double computeHellingerDist(double P_mean, double P_variance,double Q_mean,double Q_variance)
    {
        double hellinger = 0.0;

        double P_stdev = Math.sqrt(P_variance);
        double Q_stdev = Math.sqrt(Q_variance);

        hellinger = 1- Math.sqrt((2 * P_stdev * Q_stdev)/(P_variance+Q_variance))* Math.exp(-1/4*(Math.pow(P_mean-Q_mean, 2) / (P_variance+Q_variance)));
        return hellinger;
    }

    /**
     * Computes the hellinger distance between two distributions.
     *
     * The function used is:
     *
     * @param dist the class distribution.
     * @return the entropy for the given class distribution.
     */
    public static double computeHellingerDist(double[][] dist)
    {
        double negatives=0;
        double positives=0;

        for(int i=0;i<dist.length;i++)
        {
            negatives+=dist[i][0];
            positives+=dist[i][1];
        }

        double hellinger = 0.0;

        for(int i=0;i<dist.length;i++)
            hellinger+= Math.pow(Math.sqrt(dist[i][1]/positives)-Math.sqrt(dist[i][0]/negatives),2);

        return Math.sqrt(hellinger);
    }


    public static String simplifyText(String text)
    {
        // Se pasa todo a minusculas

        String modifiedText = text.toLowerCase();

        // Ahora se eliminan las tildes

        modifiedText = modifiedText.replace("�", "a");

        modifiedText = modifiedText.replace("�", "e");

        modifiedText = modifiedText.replace("�", "i");

        modifiedText = modifiedText.replace("�", "o");

        modifiedText = modifiedText.replace("�", "o");

        // Se sustituyen las e�es

        modifiedText = modifiedText.replace("�", "xxx");

        return modifiedText;
    }
}