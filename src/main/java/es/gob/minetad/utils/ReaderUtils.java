package es.gob.minetad.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class ReaderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderUtils.class);


    public static BufferedReader from(String path) throws IOException {

        InputStreamReader inputStreamReader;
        if (path.startsWith("http")){
            inputStreamReader = new InputStreamReader(new GZIPInputStream(new URL(path).openStream()));
        }else if (path.endsWith(".gz")){
            inputStreamReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)));
        } else{
            inputStreamReader = new InputStreamReader(new FileInputStream(path));
        }

        return new BufferedReader(inputStreamReader);
    }

}
