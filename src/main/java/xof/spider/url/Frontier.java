package xof.spider.url;

public interface Frontier {
	public CrawlUrl getNext();
	public boolean putUrl(CrawlUrl url);
}
