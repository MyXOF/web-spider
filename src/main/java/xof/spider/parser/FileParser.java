package xof.spider.parser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xof.spider.configuration.SpiderConfig;
import xof.spider.database.cassandra.CassandraCluster;
import xof.spider.utils.Gzip;
import xof.spider.utils.Pair;

public class FileParser {
	private static final Logger logger = LoggerFactory.getLogger(FileParser.class);
	
	private String sourceDirectory;
	private CassandraCluster cluster;
	private SpiderConfig config;
	private final int SENTENCE_COUNT = 15;
	
	public FileParser(){
		SpiderConfig config = SpiderConfig.getInstance();
		sourceDirectory = config.SOURCE_DATA_FILTER_DIR;
		cluster = CassandraCluster.getInstance();
		config = SpiderConfig.getInstance();
	}
	
	public void service(){
		File dir = new File(sourceDirectory);
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) return;
		int docID = 0;
		initDatabase();
		for(File file : files){
			if(file.isDirectory()){
				continue;
			}
			if(parseFile(file,docID)){
				docID++;
			}
		}
	}
	
	public void shutdown(){
		cluster.close();
	}
	
	public boolean parseFile(File input,int docID){
		if(input == null){
			logger.warn("WordParser : receive null file input");
			return false;
		}
		String fileName = input.getName();
		Document document;
		try {
			document = Jsoup.parse(input,"UTF-8","");
			Element titleElement = document.select("title").first();
			String title = titleElement.text();
			cluster.InsertDoc(config.cassandra_keyspace, docID, ByteBuffer.wrap(Gzip.compress(fileName)), ByteBuffer.wrap(Gzip.compress(title)));
			
			Element body = document.getElementById("bodyContent");
			if(body == null){
				logger.warn("WordParser : file {} contains no useful text",fileName);
				return false;
			}
			parseWord(body.text(), docID);
		} catch (IOException e) {
			logger.error("WordParser: fail to open {}",fileName,e);;
		} 
		
		return true;
	}
	
	public boolean parseWord(String content,int docID) throws IOException{
		Matcher matcher = Pattern.compile("(\\w+(-|'+|[.]))*\\w+").matcher(content);
		List<String> wordList = new ArrayList<String>();
		while(matcher.find()){
			wordList.add(matcher.group());
		}
		Map<String, Pair<Integer, Integer>> wordInfo = insertSentence(docID, wordList);
		insertWord(wordInfo, docID);
		return true;
	}
	
	public Map<String, Pair<Integer, Integer>> insertSentence(int docID,List<String> words) throws IOException{
		if(!cluster.checkCf(config.cassandra_keyspace, "s_"+docID)){
			cluster.createSentenceCf(config.cassandra_keyspace, "s_"+docID);
		}
		Map<String, Pair<Integer, Integer>> wordInfo = new HashMap<String, Pair<Integer,Integer>>();
		int line = 1;
		int len = words.size();
		boolean flag = true;
		while(flag){
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for(int i = 0;i < SENTENCE_COUNT;i++){
				int index = (line-1) * SENTENCE_COUNT + i;
				if(index >= len){
					flag = false;
					break;
				}
				String word = words.get(index);
				builder.append(word);
				builder.append(" ");
				if(wordInfo.containsKey(word)){
					wordInfo.get(word).right++;
				}
				else{
					wordInfo.put(word, new Pair<Integer, Integer>(line, 1));
				}
				count++;
			}
			if(count == 0){
				break;
			}
			cluster.InsertSentence(config.cassandra_keyspace, "s_"+docID, line, ByteBuffer.wrap(Gzip.compress(builder.toString())));
			line++;
		}
		return wordInfo;
	}
	
	public void insertWord(Map<String, Pair<Integer, Integer>> wordInfo,int docID){
		Iterator<Map.Entry<String, Pair<Integer, Integer>>> entries = wordInfo.entrySet().iterator();
		while (entries.hasNext()) {  
			Map.Entry<String, Pair<Integer, Integer>> entry = entries.next(); 
			String word = "w_"+entry.getKey();
			if(!cluster.checkCf(config.cassandra_keyspace, word)){
				cluster.createWordCf(config.cassandra_keyspace, word);
			}
		    cluster.InsertWord(config.cassandra_keyspace, word, docID, entry.getValue().right, entry.getValue().left);
		}  
		
	}
	
	private void initDatabase(){	
		cluster.createKs(config.cassandra_keyspace, config.cassandra_partition_strategy, config.cassandra_replica_factor);
		cluster.createDocCf(config.cassandra_keyspace);
	}
	
	public static void main(String[] args) {
		FileParser test = new FileParser();
		test.service();
		test.shutdown();
	}

}
