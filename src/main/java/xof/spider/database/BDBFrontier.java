package xof.spider.database;


import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;

import xof.spider.url.CrawlUrl;
import xof.spider.url.Frontier;

public class BDBFrontier extends AbstractFrontier implements Frontier{
	private final static Logger logger = LoggerFactory.getLogger(BDBFrontier.class);
	private StoredMap pendingUrlsDB = null;
	
	public BDBFrontier(String homeDirectory) throws DatabaseException {
		super(homeDirectory);
		init();
	}
	
	public boolean isEmpty(){
		if(pendingUrlsDB == null || pendingUrlsDB.size() == 0)
			return true;
		return false;
	}

	@Override
	public CrawlUrl getNext() {
		CrawlUrl result = null;
		if(!pendingUrlsDB.isEmpty()){
			Set<?> entrys = pendingUrlsDB.entrySet();
			logger.debug("BDBFrontier: entrys {}",entrys);
			@SuppressWarnings("unchecked")
			Entry<String, CrawlUrl> entry = (Entry<String, CrawlUrl>) pendingUrlsDB.entrySet().iterator().next();
			result = entry.getValue();
			delete(entry.getKey());
		}
		return result;
	}

	@Override
	public boolean putUrl(CrawlUrl url) {
		put(url.getOriUrl(), url);
		return true;
	}

	@Override
	public void put(Object key, Object value) {
		if(pendingUrlsDB == null) init();
		pendingUrlsDB.put(key, value);
	}

	@Override
	public Object get(Object key) {
		if(pendingUrlsDB == null){
			init();
			return null;
		}
		return pendingUrlsDB.get(key);
	}

	@Override
	public Object delete(Object key) {
		if(pendingUrlsDB == null){
			init();
			return null;
		}
		return pendingUrlsDB.remove(key);
	}
	
	private void init(){
		EntryBinding keyBinding = new SerialBinding(javaCatalog, String.class);
		EntryBinding valueBinding = new SerialBinding(javaCatalog, CrawlUrl.class);
		pendingUrlsDB = new StoredMap(database, keyBinding, valueBinding, true);
	}
	
//	public String caculateUrl(String url){
//		return url;
//	}
	
	public static void main(String[] args) {
		try {
			BDBFrontier test = new BDBFrontier("db");
//			CrawlUrl url = new CrawlUrl();
//			url.setOriUrl("http://www.1.com");
//			test.putUrl(url);
//			CrawlUrl url1 = new CrawlUrl();
//			url1.setOriUrl("http://www.2.com");
//			test.putUrl(url1);
			logger.debug("first url {}",test.getNext().getOriUrl());
			test.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}




}
