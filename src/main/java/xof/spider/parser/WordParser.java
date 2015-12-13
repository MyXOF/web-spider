package xof.spider.parser;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xof.spider.configuration.SpiderConfig;

public class WordParser {
	private static final Logger logger = LoggerFactory.getLogger(WordParser.class);
	
	private String sourceDirectory;
	private String parseDirectory;
	
	public WordParser(){
		SpiderConfig config = SpiderConfig.getInstance();
		sourceDirectory = config.SOURCE_DATA_FILTER_DIR;
		parseDirectory = config.PARSE_DATA_DIR;
	}
	
	public void service(){
		File dir = new File(sourceDirectory);
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) return;
		
		for(File file : files){
			parseWord(file);
		}
	}
	
	public void parseWord(File input){
		if(input == null){
			logger.warn("WordParser : receive null file input");
			return;
		}
		String fileName = input.getName();
		Document document;
		try {
			document = Jsoup.parse(input,"UTF-8","");
			Element body = document.getElementById("bodyContent");
			if(body == null){
				logger.warn("WordParser : file {} contains no useful text",fileName);
				return;
			}
			String content = body.text();
			Matcher matcher = Pattern.compile("(\\w+(-|'+|[.]))*\\w+").matcher(content);
			
			String parseFilePath = String.format("%s/%s", parseDirectory,fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(parseFilePath));
			while(matcher.find()){
				bufferedWriter.write(matcher.group()+"\n");
				bufferedWriter.flush();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			logger.error("WordParser: fail to open {}",fileName,e);;
		}
	}
	
	public static void main(String[] args) {
		WordParser test = new WordParser();
		test.service();
	}

}
