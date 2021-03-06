package com.searchdexing;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SearchdexingInspector {

	private static List<IndexationInfo> results = new ArrayList<IndexationInfo>();
	private static String request_string ="https://www.google.fr/search?q=";
	// this URL works but gives ajax to play before you parse the DOM
	//	private static String request_string ="https://www.google.fr/#safe=off&q=";
	private static String outputpath = "/home/sduprey/My_Data/My_Outgoing_Data/My_Searchdexing_Inspector/";

	public static String[] to_follow =	{
		"site:www.cdiscount.com/sa-10/",
		"site:www.cdiscount.com/le-sport/r-",
		"site:www.cdiscount.com/telephonie/r-",
		"site:www.cdiscount.com/photo-numerique/r-",
		"site:www.cdiscount.com/high-tech/r-",
		"site:www.cdiscount.com/chaussures/r-",
		"site:www.cdiscount.com/bagages/r-",
		"site:www.cdiscount.com/bijouterie/r-",
		"site:www.cdiscount.com/vin-champagne/r-",
		"site:www.cdiscount.com/au-quotidien/r-",
		"site:www.cdiscount.com/maison/r-",
		"site:www.cdiscount.com/electromenager/r-",
		"site:www.cdiscount.com/juniors/r-",
		"site:www.cdiscount.com/informatique/r-",
		"site:www.cdiscount.com/dvd/r-",
		"site:www.cdiscount.com/auto/r-",
	    "site:www.cdiscount.com/livres-bd/r-"};

	public static void main(String[] args){
		System.setProperty("http.agent", "");
		String outputPathFileName = outputpath+"results_"+new Date().toGMTString()+".csv";
		if (args.length == 1){
			outputPathFileName=args[0];
		}
		make_your_job();
		try {
			print_results(outputPathFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Trouble writing file");
		}
		System.out.println("File written");
	}

	private static void make_your_job(){
		for (String todo : to_follow){
			results.add(gettingIndexationCounter(todo));
		}
	}

	private static void print_results(String outputPathFileName) throws IOException{

		BufferedWriter writer = null;
		// we open the file
		writer=  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPathFileName), "UTF-8"));	
		// we write the header
		writer.write("Request;Count\n");
		// we open the database
		for (IndexationInfo item_to_write : results){	
			String to_write = item_to_write.getRequest();
			int counter = item_to_write.getCount();
			writer.write(to_write+";"+item_to_write.getResult()+"\n");
		}
		writer.close();
	}



	public static IndexationInfo gettingIndexationCounter(String request){
		IndexationInfo info= new IndexationInfo();
		info.setRequest(request);
		// we here fetch up to five paginations
		org.jsoup.nodes.Document doc;
		try{
			request=request.replace("/", "%2F");
			String my_url = request_string+request;
			doc =  Jsoup.connect(my_url)
//					.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
					//.userAgent("Mozilla/5.0 (X11; U; Linux i686; fr; rv:1.8.1.1) Gecko/20060601 Firefox/2.0.0.1")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20110608 SeaMonkey/2.1")
					.ignoreHttpErrors(true)
					.timeout(0)
					.get();

			Elements stats = doc.select("#resultStats");
			for (Element stat : stats) {
//				String text=stat.text().replace("&nbsp;","");
//				text=text.replace("&eacute;","e");
				System.out.println("Request : "+info.getRequest());
				System.out.println("Results : "+stat.text());
				info.setResult(stat.text());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		return info;
	}

	public static int randInt(int min, int max) {
		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	private static class IndexationInfo {
		String request="";
		int count=0;
		private String result="";
		public String getRequest() {
			return request;
		}
		public void setRequest(String request) {
			this.request = request;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
	}
}
