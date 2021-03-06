package crawl4j.parser.testing;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class ParsingTestingClass {
	public static void main(String[] args){

		//String my_url_to_fetch = "http://www.cdiscount.com/electromenager/tous-nos-accessoires/joint-hublot-d-30-30-cm/f-11029-ind3662734065501.html#mpos=2|mp";
		//String my_url_to_fetch = "http://www.cdiscount.com/le-sport/vetements-de-sport/kappa-survetement-armor-homme/f-121020526-3025ej0005.html#mpos=1|cd";
		//String my_url_to_fetch = "http://www.cdiscount.com/animalerie/chiens/lot-de-3-sofas-pour-chien/f-1621004-ifd19945rouge.html";
		//String my_url_to_fetch = "http://www.cdiscount.com/telephonie/r-housse+guidon.html#_his_";
		String my_url_to_fetch = "http://www.cdiscount.com/maison/tapis/rio-tapis-shaggy-anthracite-30-mm-160x230-cm/f-1172512-r252an160230.html";
		
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
			
			Elements titleel = doc.select("title");
			System.out.println(titleel.text());
			
			doc =  Jsoup.connect(my_url_to_fetch)
					.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
					.referrer("accounterlive.com")
					.ignoreHttpErrors(true)
					.timeout(0)
					.get();
			
			Elements titleel2 = doc.select("title");
			System.out.println(titleel2.text());
		}
		catch (IOException e) {
			e.printStackTrace();
		} 

	}

}
