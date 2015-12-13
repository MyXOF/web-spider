package xof.spider.examples;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParserTest {
	public static void main(String[] args) throws IOException{
		File input = new File("007_Stage_cbaf.html"); 
//		File input = new File("007_(disambiguation).html"); 
		Document document = Jsoup.parse(input,"UTF-8","");
		Element body = document.getElementById("bodyContent");
		String content = body.text();
		System.out.println(content);
		Matcher matcher = Pattern.compile("(\\w+(-|'+|[.]))*\\w+").matcher(content);
		while(matcher.find()){
			System.out.println(matcher.group().toLowerCase());
		}
	}
}
