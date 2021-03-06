package crawl4j.parser.testing;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StandardBaliseParsingTestingClass {
	public static void main(String[] args){

		//String my_url_to_fetch = "http://www.cdiscount.com/electromenager/tous-nos-accessoires/joint-hublot-d-30-30-cm/f-11029-ind3662734065501.html#mpos=2|mp";
		String my_url_to_fetch = "http://www.cdiscount.com/le-sport/vetements-de-sport/kappa-survetement-armor-homme/f-121020526-3025ej0005.html#mpos=1|cd";

		// fetching data using jQuery
		org.jsoup.nodes.Document doc;
		try{
			// we wait between 30 and 70 seconds
			doc =  Jsoup.connect(my_url_to_fetch)
					.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
					.referrer("accounterlive.com")
					.ignoreHttpErrors(true)
					.timeout(0)
					.get();
			Elements resellers = doc.select(".fpSellBy");
			StringBuilder resellerBuilder = new StringBuilder();
			for (Element reseller : resellers){
				if(reseller.getElementsByTag("a") != null){
					resellerBuilder.append(reseller.getElementsByTag("a").text());
				}
			}
			String vendor = resellerBuilder.toString();
			System.out.println(vendor);


		}
		catch (IOException e) {
			e.printStackTrace();
		} 

	}

}
