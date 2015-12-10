package xof.spider.daemon;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseException;

import xof.spider.configuration.SpiderConfig;
import xof.spider.database.BDBFrontier;
import xof.spider.filter.SimpleBloomFilter;
import xof.spider.parser.HtmlParser;
import xof.spider.url.CrawlUrl;
import xof.spider.utils.Pair;
import xof.spider.web.WebSpider;

public class DaemonService {
	private final static Logger logger = LoggerFactory.getLogger(DaemonService.class);
	
	private ExecutorService executor;
	private BDBFrontier bdbFrontier;
	private int threadNum;
	private SimpleBloomFilter filter;
	private String fileDirectory;
	private boolean isDebug;
	
	public DaemonService(boolean isDebug){
		this.isDebug = isDebug;
		init();
	}
	
	private boolean init(){
		SpiderConfig config = SpiderConfig.getInstance();
		threadNum = config.THREAD_NUM;
		fileDirectory = config.FILE_DIR;
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

				Pair<String, Boolean> result = getNextCrawlUrl();
				if(result.right){
					try {
						Thread.sleep(1000);
						continue;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(result.left == null){
					continue;
				}
				handleWeb(result.left);
			}
		}
		
		
		private Pair<String, Boolean> getNextCrawlUrl(){
			CrawlUrl crawl = null;
			synchronized (bdbFrontier) {
				crawl = bdbFrontier.getNext();
			}
			if(crawl == null){
				return new Pair<String, Boolean>(null,true);
			}
			String URL = crawl.getOriUrl();
			synchronized (filter) {
				if(!filter.contains(URL)){
					filter.add(URL);
				}
				else{
					return new Pair<String, Boolean>(null, false);
				}
			}
			return new Pair<String, Boolean>(URL, false);
			
			
		}
		
		private void handleWeb(String URL){
			WebSpider spider = new WebSpider(URL);
			String content = null;
			try {
				content = spider.getWebContent();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HtmlParser parser = new HtmlParser(URL, content);
			Set<String> nextUrls = parser.getNextURLs();
			String title = parser.getTitle();
			
			saveHtmlFile(title, fileDirectory,content,URL);
			updateUrls(nextUrls);
		}
		
		private void saveHtmlFile(String fileName,String fileDirectory,String content,String URL){
			if(isDebug) return;
			try {
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(String.format("%s/%s", fileDirectory,fileName)));
				bufferedWriter.write(content);
				bufferedWriter.flush();
				bufferedWriter.close();
			} catch (IOException e) {
				logger.error("CrawlThread failed to save file {} from {}",fileName,URL,e);
			}
			
		}
		
		private void updateUrls(Set<String> urls){
			if(urls == null) return;
			for(String url : urls){
				bdbFrontier.putUrl(new CrawlUrl(url));
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
