package Crawler;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import sun.management.AgentConfigurationError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.Charset;


/**
 * Wrapper for Apache HTTP Client
 * Created by Gideon on 9/19/14.
 */



public class MyHTTPClient {

    /** a string to hold the data received in the response */
    private String HTMLData;
    private URI finalURL;
    private  CloseableHttpClient httpClient;
    private  HttpContext context;
    private  HttpGet httpget;


    public MyHTTPClient(CloseableHttpClient httpClient, String url) {
        this.httpget = new HttpGet(url);
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();


        fetch();
    }

    //todo: implement pool for client, get last URI, check

    /**
     * Executes the GetMethod and prints some status information.
     */



    public void fetch() {
        try {
            System.out.println("HTTP Request:  " + httpget.getURI());
            CloseableHttpResponse response = httpClient.execute(httpget, context);
            try {
                // get the response body as an array of bytes
                HttpEntity entity = response.getEntity();
                finalURL = httpget.getURI();
                RedirectLocations locations = (RedirectLocations) context.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
                if (locations != null) {
                    finalURL = locations.getAll().get(locations.getAll().size() - 1);
                }

                if (entity != null) {
                    HTMLData = EntityUtils.toString(entity, Charset.defaultCharset());
                }
            } finally {
                response.close();
                System.out.println("Request Finished");
            }
        } catch (Exception e) {
            System.out.println(" - error: " + e);
        }
    }






    /**
     * generates a apache.commons.httpclient and makes the request to the url
     * @param url url to fetch
     */
    /*
    public MyHTTPClient(String url) {

        //set timeout
        HttpConnectionManagerParams cmparams = new HttpConnectionManagerParams();
        cmparams.setSoTimeout(10000);
        HttpConnectionManager manager = new SimpleHttpConnectionManager();
        manager.setParams(cmparams);
        HttpClientParams params = new HttpClientParams();
        params.setSoTimeout(10000);

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient(params,manager);

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
*/
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
