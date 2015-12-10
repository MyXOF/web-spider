package xof.spider.daemon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseException;

import xof.spider.configuration.SpiderConfig;
import xof.spider.database.BDBFrontier;
import xof.spider.filter.SimpleBloomFilter;
import xof.spider.url.CrawlUrl;

public class DaemonService {
	private final static Logger logger = LoggerFactory.getLogger(DaemonService.class);
	
	private ExecutorService executor;
	private BDBFrontier bdbFrontier;
	private int threadNum;
	private SimpleBloomFilter filter;
	
	public DaemonService(){
		init();
	}
	
	private boolean init(){
		SpiderConfig config = SpiderConfig.getInstance();
		threadNum = config.THREAD_NUM;
		executor = Executors.newFixedThreadPool(threadNum+1);
	
		try {
			bdbFrontier = new BDBFrontier(config.DB_DIR);
			String[] Urls = config.URL.trim().split(",");
			if(Urls == null || Urls.length == 0){
				logger.warn("DaemonService: there is no url to start");
			}
			for(String url : Urls){
				bdbFrontier.putUrl(new CrawlUrl(url));
			}
		} catch (DatabaseException e) {
			logger.error("DaemonService: failed to init database",e);
			return false;
		}
		
		filter = new SimpleBloomFilter();
		
		return true;
	}
	
	public void service(){
		for(int i = 0;i < threadNum;i++){
			executor.execute(new CrawlThread(i));
		}		
		executor.execute(new CrawlController());
	}
	
	class CrawlThread implements Runnable{
		private int threadID;
		
		public CrawlThread(int threadID) {
			this.threadID = threadID;
		}
		
		@Override
		public void run() {
			
			while(true){
				CrawlUrl crawl = null;
				synchronized (bdbFrontier) {
					crawl = bdbFrontier.getNext();
				}
				if(crawl == null){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				
				String URL = crawl.getOriUrl();
				synchronized (filter) {
					if(!filter.contains(URL)){
						filter.add(URL);
					}
					else{
						continue;
					}
				}
				
				
		    	CloseableHttpClient httpclient = HttpClients.createDefault();
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
		            String responseBody = httpclient.execute(httpget, responseHandler);


		            
		            Document document = Jsoup.parse(responseBody,URL);
		            Element title = document.select("title").first();
		            Elements links = document.select("a[href]");
		            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(title.text()));
		            
		            for (Element link : links) {
		            	String urlNext = link.attr("abs:href").trim();
		            	bdbFrontier.putUrl(new CrawlUrl(urlNext));
		            }
		            
		            bufferedWriter.write(responseBody);
		            bufferedWriter.flush();
		            bufferedWriter.close();
		        } catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
		            try {
						httpclient.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
			}
		}
		
	}
	
	class CrawlController implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
