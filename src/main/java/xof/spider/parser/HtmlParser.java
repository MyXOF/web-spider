package xof.spider.parser;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xof.spider.filter.UrlFilter;

public class HtmlParser {
	private final static Logger logger = LoggerFactory.getLogger(HtmlParser.class);
	private Document document;
	
	public HtmlParser(String URL,String htmlContent){

		document = Jsoup.parse(htmlContent,URL);
	}
	
	public Set<String> getNextURLs(){
        Set<String> result = new HashSet<String>();

		if(document == null) return result;
		
        Elements links = document.select("a[href]");
        
        if(links == null || links.size() == 0){
        	return result;
        }
        
        for(Element link : links){
        	String url = link.attr("abs:href").trim();
        	if(UrlFilter.isLegal(url)){
        		while(true){
        			if(url.endsWith("/") || url.endsWith("#")){
        				url = url.substring(0, url.length()-1);
        				continue;
        			}
        			break;
        		}
        		result.add(url);
        		logger.debug("HtmlParser : get next url {}",url);
        	}
        }    
        return result;
	}
	
	public String getTitle(){
		if(document == null) return null;
		
		 Element title = document.select("title").first();
		 
		 if(title == null || title.text().equals("")){
			 return null;
		 }
		 
		 return title.text();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
