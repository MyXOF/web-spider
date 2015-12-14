package xof.spider.examples;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseException;

import xof.spider.database.berkeleydb.BDBFrontier;
import xof.spider.url.CrawlUrl;

public class DBTest {
	private final static Logger logger = LoggerFactory.getLogger(DBTest.class);
	private ExecutorService exec;
	private final int threadNum = 4;
	private BDBFrontier test;
	
	public DBTest(){
		exec = Executors.newFixedThreadPool(threadNum);
		try {
			test = new BDBFrontier("db");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void service() throws DatabaseException{
		BDBFrontier bdbFrontier = new BDBFrontier("db");
		CrawlUrl url1 = new CrawlUrl();
		url1.setOriUrl("http://www.1.com");
		bdbFrontier.putUrl(url1);
		CrawlUrl url2 = new CrawlUrl();
		url2.setOriUrl("http://www.2.com");
		bdbFrontier.putUrl(url2);
		CrawlUrl url3 = new CrawlUrl();
		url3.setOriUrl("http://www.3.com");
		bdbFrontier.putUrl(url3);
		CrawlUrl url4 = new CrawlUrl();
		url4.setOriUrl("http://www.1.com");
		bdbFrontier.putUrl(url4);
		bdbFrontier.close();
		for(int i = 0;i < threadNum;i++){
			exec.execute(new dbTHread(i));
		}
	}
	
	class dbTHread implements Runnable{
		private int threadID;
		public dbTHread(int threadID) {
			this.threadID = threadID;
		}
		
		@Override
		public void run() {
			synchronized (test) {
				logger.debug("dbTHread {} first url {}",threadID,test.getNext().getOriUrl());
			}

			
		}
		
	}
	
	public static void main(String[] args) throws DatabaseException {
		DBTest test = new DBTest();
		test.service();

	}

}
