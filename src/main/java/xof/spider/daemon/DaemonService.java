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
	private boolean threadState[];
	
	public DaemonService(boolean isDebug){
		this.isDebug = isDebug;
		init();
	}
	
	private boolean init(){
		SpiderConfig config = SpiderConfig.getInstance();
		threadNum = config.THREAD_NUM;
		fileDirectory = config.FILE_DIR;
		executor = Executors.newFixedThreadPool(threadNum+1);
	
		threadState = new boolean[threadNum];
		for(int i = 0;i < threadNum;i++){
			threadState[i] = false;
		}
		
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
			threadState[i] = true;
			executor.execute(new CrawlThread(i));
		}		
		executor.execute(new CrawlController());
	}
	
	public void shutdown() throws DatabaseException{
		executor.shutdown();
		bdbFrontier.close();
		logger.info("DaemonService shutdown.");
	}
	
	class CrawlThread implements Runnable{
		private int threadID;
		
		public CrawlThread(int threadID) {
			this.threadID = threadID;
		}
		
		@Override
		public void run() {			
			try {
				while(true){
					Pair<String, Boolean> result = getNextCrawlUrl();
					if(result.right){
						break;
					}
					if(result.left == null){
						continue;
					}
					handleWeb(result.left);
				}
				logger.info("CrawlThread {} exit.",threadID);
			} catch (Exception e) {
				logger.error("CrawlThread {} errord occur at runtime",threadID);
			} finally{
				threadState[threadID] = false;
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
				logger.error("CrawlThread {} errors occur for {}",threadID,URL,e);
				return;
			} catch (IOException e) {
				logger.error("CrawlThread {} errors occur for {}",threadID,URL,e);
				return;
			}catch (Exception e) {
				logger.error("CrawlThread {} errors occur for {}",threadID,URL,e);
				return;
			}
			
			HtmlParser parser = new HtmlParser(URL, content);
			Set<String> nextUrls = parser.getNextURLs();
			String title = parser.getTitle();
			if(title != null){
				saveHtmlFile(title, fileDirectory,content,URL);
			}
			
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
			while(true){
				boolean state = false;
				for(int i = 0;i < threadNum;i++){
					state |= threadState[i];
					if(threadState[i]){
						continue;
					}
					boolean isEmpty = false;
					synchronized (bdbFrontier) {
						isEmpty = bdbFrontier.isEmpty();
					}
					if(!isEmpty){
						threadState[i] = true;
						state |= threadState[i];
						executor.execute(new CrawlThread(i));
					}	
				}
				if(!state){
					break;
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.error("CrawlController failed to sleep",e);
				}
			}
			try {
				shutdown();
			} catch (DatabaseException e) {
				logger.error("CrawlController failed to close daemon thread",e);
			}
			logger.info("CrawlController exit");
		}
		
	}
	
	public static void main(String[] args) {
		DaemonService daemon = new DaemonService(true);
		if(!daemon.init()) return;
		daemon.service();
	}

}
