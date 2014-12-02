package Crawler;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Random;

/**
 * This class is used to detect in which language a text is.
 * It's a wrapper for a python script based on Google's compact language detector.
 *
 */
public class DetectLanguage {
    /** name of the python script file*/
    public static String detectScript = "/langDetect.py";
    /** a temp text file to store the string */
    public static String tempFile = "tempFile";


    /**
     * Detects the language of a string
     * @param text String to detect
     * @return a Crawler.Result object with language and reliability
     * @throws Exception If can't find the python script
     */
    public static Result detect(final String text, long id) throws Exception {

        Random rand = new Random();
        String fileLocation = tempFile + String.valueOf(id)+".txt";


        String path = DetectLanguage.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8").replace("/target/classes/","").replace("file:","").replace("/home/gm2597/crawler/BSCrawler/target/BSCrawler-1.0-SNAPSHOT-jar-with-dependencies.jar/langDetect.py","langDetect.py");
        //String decodedPath = "/home/gm2597/crawler/BSCrawler/"; //production
        System.out.println("Script path: "+decodedPath+detectScript);
        File script = new File(decodedPath+detectScript);
        if(!script.exists()){
            throw new Exception("Cannot find langDetect.py at: "+decodedPath+detectScript);
        }

        String lang;
        boolean isReliable;
        File tempFileHandler = new File(fileLocation);

        //write string to file
        try {
            FileUtils.writeStringToFile(tempFileHandler, text, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //pass to python
        ProcessBuilder pb = new ProcessBuilder("python", decodedPath+detectScript, tempFile);
        Process p = null;
        String ret = null;

        try {
            p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            ret = in.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }

        //read return value
        // lang: %s ,reliable: isRealiable@' % detectedLangName)
        lang = ret.substring(8, ret.indexOf(',')-1);
        int scoreInt = Integer.parseInt(ret.substring(ret.indexOf("reliable:")+"reliable:".length()+1, ret.length()));
        if(scoreInt == 1)
            isReliable = true;
        else
            isReliable = false;

        Result rest = new Result(lang, isReliable);

        //delete file
        tempFileHandler.delete();


        return rest;

    }

}