package xof.spider.parser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		config = SpiderConfig.getInstance();
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
		initDatabase();
		
		String fileName = input.getName();
		Document document;
		try {
			document = Jsoup.parse(input,"UTF-8","");
			Element titleElement = document.select("title").first();
			String title = titleElement.text().toLowerCase();
//			cluster.InsertDoc(config.cassandra_keyspace, docID, ByteBuffer.wrap(Gzip.compress(fileName)), ByteBuffer.wrap(Gzip.compress(title)));
//			logger.info("cassandra insert into {} docID:{} file:{} title:{}",config.cassandra_keyspace, docID, fileName, title);
			ByteBuffer fileNameBuffer = ByteBuffer.wrap(Gzip.compress(fileName));
			ByteBuffer titleNameBuffer = ByteBuffer.wrap(Gzip.compress(title));
			Element body = document.getElementById("bodyContent");
			if(body == null){
				logger.warn("WordParser : file {} contains no useful text",fileName);
				return false;
			}
			parseWord(body.text(), docID,fileNameBuffer,titleNameBuffer);
		} catch (IOException e) {
			logger.error("WordParser: fail to open {}",fileName,e);;
		} 
		
		return true;
	}
	
	public boolean parseWord(String content,int docID,ByteBuffer file,ByteBuffer title) throws IOException{
		Matcher matcher = Pattern.compile("\\w+").matcher(content);
		List<String> wordList = new ArrayList<String>();
		Set<String> stopWords = config.stopWords;
		while(matcher.find()){
			String word = matcher.group().toLowerCase();
			if(stopWords.contains(word)) continue;
			wordList.add(word);
		}
		Map<String, Pair<Integer, Integer>> wordInfo = insertSentence(docID, wordList);
		insertWord(wordInfo, docID,file,title);
		return true;
	}
	
	public Map<String, Pair<Integer, Integer>> insertSentence(int docID,List<String> words) throws IOException{
		if(!cluster.checkCf(config.cassandra_keyspace_sentence, "s_"+docID)){
			cluster.createSentenceCf(config.cassandra_keyspace_sentence, "s_"+docID);
			logger.info("cassnadra create sentence cd {}.{}",config.cassandra_keyspace_sentence, "s_"+docID);
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
			cluster.InsertSentence(config.cassandra_keyspace_sentence, "s_"+docID, line, ByteBuffer.wrap(Gzip.compress(builder.toString())));
			logger.info("cassnandra insert into {}.{} line {}, content {}",config.cassandra_keyspace_sentence, "s_"+docID, line,builder.toString());
			line++;
		}
		return wordInfo;
	}
	
	public void insertWord(Map<String, Pair<Integer, Integer>> wordInfo,int docID,ByteBuffer file,ByteBuffer title){
		Iterator<Map.Entry<String, Pair<Integer, Integer>>> entries = wordInfo.entrySet().iterator();
		while (entries.hasNext()) {  
			Map.Entry<String, Pair<Integer, Integer>> entry = entries.next(); 
			String word = "w_"+entry.getKey();
			if(!cluster.checkCf(config.cassandra_keyspace_word, word)){
				cluster.createWordCf(config.cassandra_keyspace_word, word);
			}
		    cluster.InsertWord(config.cassandra_keyspace_word, word, docID, entry.getValue().right, entry.getValue().left,file,title);
		    logger.info("cassandra insert into {}.{} docID {}, weight {} line {}",config.cassandra_keyspace_word, word, docID, entry.getValue().right, entry.getValue().left);
		}  
		
	}
	
	private void initDatabase(){	
//		cluster.createKs(config.cassandra_keyspace_word, config.cassandra_partition_strategy, config.cassandra_replica_factor);
//		cluster.createDocCf(config.cassandra_keyspace);
	}
	
	public static void main(String[] args) {
		FileParser test = new FileParser();
		test.parseFile(new File("Halo~_The_Flood_6866.html"), 0);
		test.parseFile(new File("Halo~_The_Fall_of_Reach_24f4.html"), 1);
		
		test.shutdown();
	}

}
