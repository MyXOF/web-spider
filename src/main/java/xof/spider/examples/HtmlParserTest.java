package xof.spider.examples;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParserTest {
	public static void main(String[] args) throws IOException{
		File input = new File("a.html"); 
		Document document = Jsoup.parse(input,"UTF-8","");
		Elements links = document.select("p");
		for(Element link:links){
			System.out.println(link.text());
		}
	}
}
