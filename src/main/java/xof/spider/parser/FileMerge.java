package xof.spider.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMerge {
	private static final Logger logger = LoggerFactory.getLogger(FileMerge.class);
	public String directory;
	public String destPath;
	public String destName;
	
	public FileMerge(String directory,String destPath,String destName){
		this.directory = directory;
		this.destPath = destPath;
		this.destName = destName;
	}

	public void service() throws IOException{
		File dir = new File(directory);
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) return;
		
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(String.format("%s/%s", destPath,destName)));
		for(File file : files){
			String fileName = file.getName();
			
			Document document = Jsoup.parse(file,"UTF-8","");
			String title = document.select("title").first().text();
			
			Element body = document.getElementById("bodyContent");
			if(body == null){
				logger.warn("WordParser : file {} contains no useful text",fileName);
				continue;
			}
			String content = body.text();
			
			bufferedWriter.write(String.format("%s\n%s\n%s\n", fileName,title,content));
			bufferedWriter.flush();
		}
		
		bufferedWriter.close();
	}
	
	public static void main(String[] args) throws IOException {
		FileMerge test = new FileMerge("C:/Users/Doge/Downloads/source", "C:/Users/Doge/Downloads/result", "result1");
		test.service();

	}

}
