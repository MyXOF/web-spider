package xof.spider.parser;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xof.spider.filter.UrlFilter;

public class HtmlParser {
//	private final static Logger logger = LoggerFactory.getLogger(HtmlParser.class);
//	private String URL;
//	private String htmlContent;
	private Document document;
	
	public HtmlParser(String URL,String htmlContent){
//		this.URL = URL;
//		this.htmlContent = htmlContent;
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
        		result.add(url);
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
