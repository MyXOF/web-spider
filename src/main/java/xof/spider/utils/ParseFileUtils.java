package xof.spider.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xof.spider.configuration.SpiderConfig;

public class ParseFileUtils {
	private static final Logger logger = LoggerFactory.getLogger(ParseFileUtils.class);
	private static final int MIN_FILE_SIZE = 1024;
	private static final String IMAGE_FLAG="Image";
	private static final String USER_TALK_FLAG="User";
	private static final String CATEGORY_FLAG="Category";
	private static final String HELP_FLAG="Help";
	private static final String PORTAL_FLAG="Portal";
	private static final String ROUTE_FLAG="Route";
	private static final String LIST_FLAG="List";
	private static final String TALK_FLAG="Talk";
	private static final String TEMPLATE_FLAG = "Template";
	private static final String WIKI_FLAG = "Wiki";

	private String sourceDirectory;
	private int deep;
	
	public ParseFileUtils(String sourceDirectory,int deep){
		this.sourceDirectory = sourceDirectory;
		this.deep = deep;
	}
	
//	public void ListLegalFile(){
//		File[] files = getFile(deep, new File(sourceDirectory));
//		
//		for(File file : files){
//			logger.debug(file.getName());
//		}
//	}
	
	public void dumpLegalFIle(String destDirectory) throws FileNotFoundException, IOException{
		getFile(deep, new File(sourceDirectory),destDirectory);

	}
	
	public void getFile(int deep,File dir,String destDirectory) throws IOException{
		if(deep == 0) {
			getFileList(dir,destDirectory);
		}
		
		File[] directories = dir.listFiles();
		if(directories == null || directories.length == 0) return;
		for(File directory : directories){
			getFile(deep - 1, directory,destDirectory);
		}
	}
	
	private static class MyFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (new File(dir, name).isDirectory()) return false;
			
			else if(name.startsWith(IMAGE_FLAG) || name.startsWith(USER_TALK_FLAG) ||
					name.startsWith(CATEGORY_FLAG) || name.startsWith(HELP_FLAG) ||
					name.startsWith(PORTAL_FLAG) || name.startsWith(ROUTE_FLAG) ||
					name.startsWith(LIST_FLAG) || name.startsWith(TALK_FLAG) ||
					name.startsWith(TEMPLATE_FLAG) || name.startsWith(WIKI_FLAG)) return false;
			
			else if(new File(dir,name).length() <= MIN_FILE_SIZE) return false;
			
			else{
				return true;
			}
		}
	}

	private  void getFileList(File dir,String destDirectory) throws IOException {
		File[] files = dir.listFiles(new MyFileFilter());
		if (files == null || files.length == 0) {
			return;
		} else {
			dumpFile(files, destDirectory);
		}
	}
	
	private void dumpFile(File[] files,String destDirectory) throws IOException{
		for(File file : files){
			String path = String.format("%s/%s", destDirectory,file.getName());
			File dest = new File(path);
			if(!dest.exists()){
				dest.createNewFile();
			}
	        FileChannel in = new FileInputStream( file ).getChannel();
	        FileChannel out = new FileOutputStream(dest ).getChannel();
	        out.transferFrom( in, 0, in.size() );
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		SpiderConfig config = SpiderConfig.getInstance();
		ParseFileUtils test = new ParseFileUtils(config.SOURCE_DATA_DIR,config.SOURCE_DATA_DEEP);
		test.dumpLegalFIle(config.SOURCE_DATA_FILTER_DIR);

	}

}
