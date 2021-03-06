package xof.spider.examples;


import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ClientProtocolException, IOException
    {
    	CloseableHttpClient httpclient = HttpClients.createDefault();
        String URL = "http://www.163.com";
        try {
            HttpGet httpget = new HttpGet(URL);
            System.out.println("executing request " + httpget.getURI());
 
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
 
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = new String(httpclient.execute(httpget, responseHandler).getBytes("UTF-8"));

            //print the content of the page
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
            
//            Document document = Jsoup.parse(responseBody,URL);
//            Element title = document.select("title").first();
//            Elements links = document.select("a[href]");
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(title.text()));
            
//            for (Element link : links) {
//            	System.out.println(link.attr("abs:href").trim());
//            }
            
//            bufferedWriter.write(responseBody);
//            bufferedWriter.flush();
//            bufferedWriter.close();
        } finally {
            httpclient.close();
        }
    }
}
