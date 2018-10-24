package es.gob.minetad.doctopic;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static java.util.stream.Collectors.toMap;


public class DocTopicsUtil {
    private DecimalFormat df4;
    private float[][] docTopicValues;
    private String[] docNames;
    private int numdocs = 0;
    private int numtopics = 0;
    static int[] emptyTopic = {};// topics to be cleanned


    public class ValueComparator implements Comparator<Integer> {
        private Map<Integer, Integer> map;
        public ValueComparator(Map<Integer, Integer> map) {
            this.map = map;
        }
        public int compare(Integer a, Integer b) {
            return map.get(a).compareTo(map.get(b));
        }
    }

    public DocTopicsUtil() {
    }

    public float[][] cleanZeros() {
        float[][] docTopicValuesCleanned =  new float[numdocs][numtopics];

        for(int i=0; i < numdocs; i++){
            printDot(i);
            docTopicValuesCleanned[i] = cleanZerosDocTopicVector(docTopicValues[i]);
        }
        System.out.print("\n");
        return docTopicValuesCleanned;
    }

    private float[] cleanZerosDocTopicVector(float[] docTopicValues) {
        float[] docTopicVector = new float[numtopics];

        // find zero
        float min = 1f;
        for(int i=0; i < numtopics; i++){
            if(docTopicValues[i] < min){
                min = docTopicValues[i];
            }
        }

        if(min > 0.01d || min == 0d){
            return docTopicValues;
        }

        // clean zero and get rest
        int num_zeros = 0;
        for(int i=0; i < numtopics; i++){
            if(docTopicValues[i] > min && !isEmptyTopic(i)){
                docTopicVector[i] = docTopicValues[i];
            } else {
                docTopicVector[i] = 0f;
                num_zeros++;
            }
        }

        // complete rest
        float rest = 1;
        for(int i=0; i < numtopics; i++){
            rest-=docTopicVector[i];
        }
        rest = rest/(float)(numtopics - num_zeros);
        for(int i=0; i < numtopics; i++){
            if(docTopicValues[i] > min && !isEmptyTopic(i)){
                docTopicVector[i]+=rest;
            }
        }

        return docTopicVector;
    }

    private static boolean isEmptyTopic(int topicNumber) {
        for(int i=0; i< emptyTopic.length; i++){
            if(emptyTopic[i]==topicNumber){
                return true;
            }
        }
        return false;
    }

    public int inspectTopicFile_CompleteFormat(String fileName) throws IOException {
        InputStream inputStream = fileName.endsWith(".gz")? new GZIPInputStream(new FileInputStream(new File(fileName))) : new FileInputStream(new File(fileName));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        int lines = 0;
        String line;

        int maxtopic = 0;
        while ((line = reader.readLine()) != null) {
            printDot(lines);

            if(lines == 1){
                String[] result = line.split("\\t");
                maxtopic = result.length-2;

            }
            lines++;
        }
        System.out.print("\n");
        reader.close();
        inputStream.close();

        numdocs = lines;
        numtopics = maxtopic;

        // init arrays
        docTopicValues = new float[numdocs][numtopics];
        docNames = new String[numdocs];

        return numdocs;
    }

    private void printDot(int lines) {
//        if(lines%1000==0 && lines > 0){
//            System.out.print(".");
//        }
//        if(lines%90000==0){
//            System.out.print("\n");
//        }
    }

    public int loadTopics_CompleteFormat(String fileName) throws UnsupportedEncodingException, IOException {
        InputStream inputStream = fileName.endsWith(".gz")? new GZIPInputStream(new FileInputStream(new File(fileName))) : new FileInputStream(new File(fileName));
        BufferedReader stdInReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));

        int cnt = 0;

        try {
            String line;

            DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
            simbolos.setDecimalSeparator('.');

            while ((line = stdInReader.readLine()) != null) {
                // comment line
                if(line.trim().startsWith("#")){
                    continue;
                }
                String[] result = line.split("\\t");
                docNames[cnt] = result[1].replace("\"", "").replace("/","-").toUpperCase().trim();

                for (int i=0; i<result.length-2; i++){
                    docTopicValues[cnt][i] = Float.valueOf(result[i+2]).floatValue();
                }
                cnt++;
            }
            return cnt;

        } catch (Exception e) {
            System.err.println("Error reading topic file: ");
            e.printStackTrace();
        } finally{
            stdInReader.close();
            inputStream.close();
        }
        return 0;
    }

    public static String getVectorString(float[] topic_vector, float multiplication_factor) {
        String result = "";
        for(int i=0; i<topic_vector.length;i++){
            if((int)(topic_vector[i]*multiplication_factor) > 0){
                result += i + "|" + (int)(topic_vector[i]*multiplication_factor) + " ";
            }
        }
        return result;
    }

    public static String getVectorString(List<Double> topic_vector, float multiplication_factor) {
        String result = "";
        for(int i=0; i<topic_vector.size();i++){
            int freq = (int) (topic_vector.get(i) * multiplication_factor);
            if(freq > 0){
                result += i + "|" + freq + " ";
            }
        }
        return result;
    }

    public static String getVectorString(List<Double> topic_vector, float multiplication_factor, float epsylon) {
        String result = "";
        for(int i=0; i<topic_vector.size();i++){
            int freq = (int) (topic_vector.get(i) * multiplication_factor);
            if(freq > (epsylon*multiplication_factor)){
                result += "t"+i + "|" + freq + " ";
            }
        }
        return result;
    }

    public static List<Double> getVectorFromString(String topic_vector, float multiplication_factor, int size) {

        String[] topics = topic_vector.split(" ");

        Double[] vector = new Double[size];
        Arrays.fill(vector,0.0);
        for(int i=0; i<topics.length;i++){
            int id      = Integer.valueOf(StringUtils.substringAfter(StringUtils.substringBefore(topics[i],"|"),"t"));
            int freq    = Integer.valueOf(StringUtils.substringAfter(topics[i],"|"));
            Double score = Double.valueOf(freq) / Double.valueOf(multiplication_factor);
            vector[id] = score;
        }
        return Arrays.asList(vector);
    }

    public static List<Double> getVectorFromString(String topic_vector, float multiplication_factor, int size, float epsylon) {

        String[] topics = topic_vector.split(" ");

        Double[] vector = new Double[size];
        Arrays.fill(vector,(double)epsylon);
        for(int i=0; i<topics.length;i++){
            int id      = Integer.valueOf(StringUtils.substringAfter(StringUtils.substringBefore(topics[i],"|"),"t"));
            int freq    = Integer.valueOf(StringUtils.substringAfter(topics[i],"|"));
            Double score = Double.valueOf(freq) / Double.valueOf(multiplication_factor);
            vector[id] = score;
        }
        return Arrays.asList(vector);
    }

    public String getVectorStringfromMap(Map<Integer, Integer> termVector) {
        String result = "";

        for (Map.Entry<Integer, Integer> entry : termVector.entrySet()) {
            result += entry.getKey() + "|" + entry.getValue() + " ";
        }
        return result;
    }

    public static String getVectorStringfromMapReduced(Map<Integer, Integer> termVector, float epsylon_cota2_2) {
        String result = "";

        Map<Integer, Integer> resultTermVector = new HashMap<Integer, Integer>();
        resultTermVector.putAll(termVector);

        Map<Integer, Integer> sorted =  resultTermVector
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        float suma = 0f;
        for (Map.Entry<Integer, Integer> entry : sorted.entrySet()) {
            if(suma < epsylon_cota2_2){
                result += entry.getKey() + "|" + entry.getValue() + " ";
                suma += entry.getValue();
            }
        }
        return result;
    }



    // bin format
    public int inspectBinTopicFile(String fileName) throws IOException {
        File file = new File(fileName);
        InputStream inputStream = new FileInputStream(file);

        int lines = 0;
        short maxtopic = 0;
        int numread = 0;

        byte[] lengthFieldBytes = new byte[2];

        // reg size
        while(numread >= 0) {
            // read record length from header
            numread = inputStream.read(lengthFieldBytes,0,2);

            // read data
            if(numread > 0){
                short dataLength = (short)( ((lengthFieldBytes[1] & 0xFF)<<8) | (lengthFieldBytes[0] & 0xFF) );
                dataLength-=2;// minus length field
                byte[] dataBytes = new byte[dataLength];

                numread = inputStream.read(dataBytes, 0, dataLength);
                if(numread == dataLength){
                    maxtopic = readBinLine(dataBytes, dataLength, maxtopic);
                    lines++;
                } else {
                    numread = -1;
                    System.out.println("Error: incomplete line.");
                }
            }
        }

        numdocs = lines;
        numtopics = (short) (maxtopic+1);
        docNames = new String[numdocs];

        // close
        inputStream.close();

        return numdocs;
    }

    private short readBinLine(byte[] lineBytes, short dataLength, short maxtopic) {
        // num non zero topics
        short num_non_zero_topics=(short)( ((lineBytes[1] & 0xFF)<<8) | (lineBytes[0] & 0xFF) );

        short offset = 2;
        for (int i=0; i<num_non_zero_topics; i++){
            byte loNumTopic = lineBytes[i*4 + offset];
            byte hiNumTopic = lineBytes[i*4 +1 + offset];
            short numTopic = (short)( ((hiNumTopic & 0xFF)<<8) | (loNumTopic & 0xFF) );
            if(numTopic > maxtopic){
                maxtopic = numTopic;
            }
        }
        return maxtopic;
    }

    public int loadBinTopics(String fileName, int numdocs, short[][] docTopicValues) throws UnsupportedEncodingException, IOException {
        FileInputStream inputStream = new FileInputStream(new File(fileName));

        byte[] lengthFieldBytes = new byte[2];

        int numread = 0;
        int lines = 0;

        try {
            DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
            simbolos.setDecimalSeparator('.');

            while (numread >= 0) {
                // read record length from header
                numread = inputStream.read(lengthFieldBytes,0,2);

                // read data
                if(numread > 0){
                    short dataLength = (short)( ((lengthFieldBytes[1] & 0xFF)<<8) | (lengthFieldBytes[0] & 0xFF) );
                    dataLength-=2;// minus length field
                    byte[] dataBytes = new byte[dataLength];

                    numread = inputStream.read(dataBytes, 0, dataLength);
                    if(numread == dataLength){
                        readContentBinLine(dataBytes, dataLength, lines, docTopicValues);
                        lines++;
                    } else {
                        numread = -1;
                        System.out.println("Error: incomplete line.");
                    }
                }
            }
            return lines;

        } catch (Exception e) {
            System.err.println("Error reading topic file. ");
            e.printStackTrace();
        } finally{
            inputStream.close();
        }
        return 0;
    }

    private void readContentBinLine(byte[] lineBytes, short dataLength, int numdoc, short[][] docTopicValues) {

        // num non zero topics
        short num_non_zero_topics=(short)( ((lineBytes[1] & 0xFF)<<8) | (lineBytes[0] & 0xFF) );
        short offset = 2;

        for (int i=0; i<num_non_zero_topics; i++){
            // topic number
            byte loNumTopic = lineBytes[i*4 + offset];
            byte hiNumTopic = lineBytes[i*4 + 1 + offset];
            short numTopic = (short)( ((hiNumTopic & 0xFF)<<8) | (loNumTopic & 0xFF) );

            //topic value
            byte loTopicValue = lineBytes[i*4 + 2 + offset];
            byte hiTopicValue = lineBytes[i*4 + 3 + offset];
            short valueTopic = (short)( ((hiTopicValue & 0xFF)<<8) | (loTopicValue & 0xFF) );

            docTopicValues[numdoc][numTopic] = valueTopic;
        }
        short name_offset = (short) (num_non_zero_topics*4 + offset);
        byte [] nameBytes = Arrays.copyOfRange(lineBytes, name_offset, dataLength);
        String nameDoc = new String(nameBytes);

        docNames[numdoc] = nameDoc;
    }

    public String getVectorStringBin(short[] topic_vector) {
        String result = "";
        for(int i=0; i<topic_vector.length;i++){
            if(topic_vector[i] > 0){
                result += i + "|" + (topic_vector[i]) + " ";
            }
        }
        return result;
    }


    // getter & setter
    public int getNumdocs() {
        return numdocs;
    }

    public int getNumtopics() {
        return numtopics;
    }

    public short[][] normalizeDocTopics(short[][] docTopicValues, int divfactor) {
        for(int i=0; i<docTopicValues.length;i++){
            for(int j=0; j<docTopicValues[i].length; j++){
                docTopicValues[i][j] = (short)(docTopicValues[i][j]/divfactor);
            }
        }
        return docTopicValues;
    }

    public void printDocTopicValuesHeaderShort(short[][] docTopicValues) {
        for(int i=0; i<10; i++){
            for(int j=0; j<docTopicValues[i].length; j++){
                if(docTopicValues[i][j] > 0){
                    System.out.println("docTopicValues["+i+"]["+j+"]=" + docTopicValues[i][j]);
                }
            }
            System.out.println("");
        }
    }
    public void printDocTopicValuesHeaderFloat(float[][] docTopicValues, float precision) {
        for(int i=0; i<10; i++){
            for(int j=0; j<docTopicValues[i].length; j++){
                if(docTopicValues[i][j] > 0){
                    System.out.println("docTopicValues["+i+"]["+j+"]="+(int)(docTopicValues[i][j]*precision));
                }
            }
            System.out.println("");
        }
    }
}