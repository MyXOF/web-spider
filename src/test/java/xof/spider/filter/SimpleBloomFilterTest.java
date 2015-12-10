package xof.spider.filter;

import junit.framework.TestCase;

public class SimpleBloomFilterTest extends TestCase {
	private SimpleBloomFilter filter;

	protected void setUp() throws Exception {
		super.setUp();
		filter = new SimpleBloomFilter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testContainsCrawlUrl() {
//		fail("Not yet implemented");
	}

	public void testContainsString() {
		String url1 = "xuyi556677@163.com";
		String url2 = "xuyi112233@126.com";
		String url3 = "xuyi556677@qq.com";
		String url4 = "xuyi112233@163.com";
		String url5 = "xuyi@163.com";
		String url6 = "xuyi112234@126.com";
		String url7 = "xuyi556677@163.com";
		
		assertTrue(!filter.contains(url1));
		filter.add(url1);
		assertTrue(filter.contains(url1));
		
		assertTrue(!filter.contains(url2));
		filter.add(url2);
		assertTrue(filter.contains(url2));
		
		assertTrue(!filter.contains(url3));
		filter.add(url3);
		assertTrue(filter.contains(url3));
		
		assertTrue(!filter.contains(url4));
		filter.add(url4);
		assertTrue(filter.contains(url4));
		
		assertTrue(!filter.contains(url5));
		filter.add(url5);
		assertTrue(filter.contains(url5));
		
		assertTrue(!filter.contains(url6));
		filter.add(url6);
		assertTrue(filter.contains(url6));
		
		assertTrue(filter.contains(url7));
		filter.add(url7);
		assertTrue(filter.contains(url7));
		
		assertTrue(filter.contains(url1));
	}

}
