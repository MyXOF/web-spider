package xof.spider.filter;

public class UrlFilter {
	private final static String URL_PREFIX_HTTP = "http://";
	private final static String URL_PREFIX_HTTPS = "https://";
	
	public static boolean isLegal(String str){
		if(str == null) return false;
		
		return str.startsWith(URL_PREFIX_HTTP) || str.startsWith(URL_PREFIX_HTTPS);
	}
	
	public static void main(String[] args) {
		System.out.println(UrlFilter.isLegal("http://cidian.youdao.com/index-mac.html"));
		System.out.println(UrlFilter.isLegal("http://learn.tsinghua.edu.cn/MultiLanguage/lesson/student/mainstudent.jsp"));
		System.out.println(UrlFilter.isLegal("http://www.cnblogs.com/luchen927/archive/2011/06/25/2090400.html"));
		System.out.println(UrlFilter.isLegal("https://www.baidu.com/s?wd=java%20httpclient%20timeout&rsv_spt=1&rsv_iqid=0x88bd1f9e000cd723&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&oq=布隆过滤器&rsv_t=1e3asqx29fisgir751gpWB5ucKUOcd4idZ2udkxFz18DE4EgSpHH2ZMfzXGuBmyOogqH&inputT=3693&rsv_pq=a9133efa00025df3&rsv_sug3=32&rsv_sug1=28&sug=java%20httpclient&rsv_n=1&bs=布隆过滤器"));
		System.out.println(UrlFilter.isLegal("http//www.163.com"));
	}

}
