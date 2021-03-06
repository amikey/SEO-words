package crawl4j.parser.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import crawl4j.continuous.CrawlerUtility;
import crawl4j.urlutilities.URLinfo;
import crawl4j.xpathutility.XPathUtility;

public class AjaxVendorParsingTestingClass {
	public static void main(String[] args){
		XPathUtility.loadXPATHConf();
		// the commented approach doesn't work
		
//		String my_url_to_fetch = "http://www.cdiscount.com/electromenager/tous-nos-accessoires/joint-hublot-d-30-30-cm/f-11029-ind3662734065501.html";
//		//String my_url_to_fetch = "http://www.cdiscount.com/le-sport/vetements-de-sport/kappa-survetement-armor-homme/f-121020526-3025ej0005.html#mpos=1|cd";
//
//		// fetching data using jQuery
//		org.jsoup.nodes.Document doc;
//		try{
//			// we wait between 30 and 70 seconds
//			doc =  Jsoup.connect(my_url_to_fetch)
//					.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
//					.ignoreHttpErrors(true)
//					.timeout(0)
//					.get();
//			//System.out.println(doc.toString());
//			Elements resellers = doc.select(".fpSellBy");
//			StringBuilder resellerBuilder = new StringBuilder();
//			for (Element reseller : resellers){
//				if(reseller.getElementsByTag("a") != null){
//					resellerBuilder.append(reseller.getElementsByTag("a").text());
//				}
//			}
//			String vendor = resellerBuilder.toString();
//			System.out.println(vendor);

		String my_url_to_fetch = "http://www.cdiscount.com/electromenager/tous-nos-accessoires/joint-hublot-d-30-30-cm/f-11029-ind3662734065501.html";
		//document.write("<p class='fpSellBy'>Vendu par et expédié par <a href='#seller'>SEM Boutique</a></p>");		
		//String my_url_to_fetch = "http://www.cdiscount.com/le-sport/vetements-de-sport/kappa-survetement-armor-homme/f-121020526-3025ej0005.html#mpos=1|cd";
		//document.write("<p class='fpSellBy'>Vendu et expédié par <span class='logoCDS'>Cdiscount</span></p>");

		try{
			// we here don't even use JQuery or XPath
			URL page = new URL(my_url_to_fetch);
			HttpURLConnection conn = (HttpURLConnection) page.openConnection();
			conn.connect();
			InputStreamReader in = new InputStreamReader((InputStream) conn.getContent());
			BufferedReader buff = new BufferedReader(in);
			String line;
			StringBuilder contentbuilder = new StringBuilder();
			do {
				line = buff.readLine();
				contentbuilder.append(line);
			} while (line != null);

			String code_source = contentbuilder.toString();
			int cdiscount_index = code_source.indexOf("<p class='fpSellBy'>Vendu et expédié par <span class='logoCDS'>");

			if (cdiscount_index >0){
				System.out.println("Cdiscount");
			}else{
				System.out.println("Market Place");
			}
			
			URLinfo info =new URLinfo();		
			// basic magasin, rayon, page type parsing
			// info has to be instantiated
			info=CrawlerUtility.basicParsing(info,my_url_to_fetch);
			info=CrawlerUtility.advancedTextParsing(info,code_source);
			
			System.out.println("Is Cdiscount best bid ? : "+info.isCdiscountBestBid());
			
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
