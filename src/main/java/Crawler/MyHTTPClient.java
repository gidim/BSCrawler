package Crawler;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import sun.management.AgentConfigurationError;
import java.io.IOException;


/**
 * Wrapper for Apache HTTP Client
 * Created by Gideon on 9/19/14.
 */


public class MyHTTPClient {

    /** a string to hold the data received in the response */
    private String HTMLData;
    private URI finalURL;

    /**
     * generates a apache.commons.httpclient and makes the request to the url
     * @param url url to fetch
     */
    public MyHTTPClient(String url) {

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        AgentConfigurationError httpclient;


        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            //get the url after redirect
            finalURL = method.getURI();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            //System.out.println("Downloads HTML: " + new String(responseBody));

            HTMLData = new String(responseBody);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

    }

    /**
     *
     * @return the html data saved by the constructur
     */
    public String getHTMLData() {
        return HTMLData;
    }

    public URI getFinalURL(){
        return finalURL;
    }




}
