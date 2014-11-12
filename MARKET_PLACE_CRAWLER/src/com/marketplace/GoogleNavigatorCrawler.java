package com.marketplace;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;


public class GoogleNavigatorCrawler {
	
	private static int min_number_of_wait_times = 50;
	private static int max_number_of_wait_times = 80;
	private static int waiting_times = 1;
	private static String target_name = "cdiscount.com";
	
	public static void main(String[] args) throws InterruptedException{
		 
		String[] keywords = {"sportswear", "karcher"};
		for (String keyword : keywords){
			// checking with the former results
//			RankInfo info = ranking_keyword(keyword);
//			System.out.println(info.getKeyword() + info.getPosition() + info.getUrl());
			RankInfo ajax_info = ajax_ranking_crawler(keyword);
			System.out.println(ajax_info.getKeyword() + ajax_info.getPosition() + ajax_info.getUrl());
		}
	
		
	}
	
	public static RankInfo ajax_ranking_crawler(String keyword) throws InterruptedException{		
		RankInfo info= new RankInfo();
		info.setKeyword(keyword);
		int depth = 1;
		String my_url = "https://www.google.fr/search?q="+keyword+"&start="+Integer.toString(depth*10);
		
		//Thread.sleep(randInt(min_number_of_wait_times,max_number_of_wait_times)*1000);
		System.out.println("Fetching a new page");
//		doc =  Jsoup.connect(
//				)
//				.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
//				.ignoreHttpErrors(true)
//				.timeout(0)
//				.get();

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(my_url);
		// we will follow some of the results
		//builder.crawlRules().click("a").withAttribute("class","mpNext");
		
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.PHANTOMJS, 1));
		// we give ourselves time (fetching additional resellers might be long)
		builder.crawlRules().waitAfterReloadUrl(waiting_times, TimeUnit.SECONDS);
		//builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);
		// adding our in-house parser plugin which will be used to fetch our resellers data
		
//		http://crawljax.com/apidocs/core/com/crawljax/core/plugin/Plugins.html
		// runOnUrlLoadPlugins(CrawlerContext context)
		builder.addPlugin(new OnNewStatePlugin() {
			@Override
			public void onNewState(CrawlerContext context, StateVertex newState) {
				// This will print the DOM when a new state is detected. You should see it in your
				// console.
				String content_to_parse = context.getBrowser().getStrippedDom();

				org.jsoup.nodes.Document doc = Jsoup.parse(content_to_parse);
				
				int nb_results = 0;
				// we do that if the page is a google page
				Elements serps = doc.select("h3[class=r]");
				for (Element serp : serps) {
					Element link = serp.getElementsByTag("a").first();
					String linkref = link.attr("href");
					if (linkref.startsWith("/url?q=")){
						nb_results++;
						linkref = linkref.substring(7,linkref.indexOf("&"));
					}
					if (linkref.contains(target_name)){
//						my_rank=nb_results;
//						my_url=linkref;
//						found=true;
						System.out.println("Yes we did it");
					}			
					//					System.out.println("Link ref: "+linkref);
					//					System.out.println("Title: "+serp.text());
				}
				
				
				
				
//				Elements resellers = doc.select(".slrName");
//				Elements prices = doc.select("p.price");
//
//
//				// converting the elements to String[]
//				String[] reseller_names = new String[resellers.size()];
//				int index=0;
//				for (Element reseller : resellers) {
//					reseller_names[index]=reseller.text();
//					index++;
//				}
//				Double[] price_values = new Double[prices.size()];
//				index=0;
//				for (Element price : prices) {
//					String matching_price = price.text();
//					matching_price=matching_price.replace("\u20ac",".");
//					try {
//						price_values[index]=Double.valueOf(matching_price);
//					} catch (NumberFormatException e){
//						System.out.println("Trouble converting : "+matching_price);
//						price_values[index]=new Double(0);
//					}
//					index++;
//				}
				// filling now the prices map with the found values
			}
			@Override
			public String toString() {
				return "Market Place Plugin";
			}
		});
		return info;
	}
	
	
	
	public static RankInfo ranking_keyword(String keyword){
		RankInfo info= new RankInfo();
		info.setKeyword(keyword);
		// we here fetch up to five paginations
		int nb_depth = 5;
		long startTimeMs = System.currentTimeMillis( );
		org.jsoup.nodes.Document doc;
		int depth=0;
		int nb_results=0;
		int my_rank=50;
		String my_url = "";
		boolean found = false;
		while (depth<nb_depth && !found){
			try{
				// we wait between x and xx seconds
				Thread.sleep(randInt(min_number_of_wait_times,max_number_of_wait_times)*1000);
				System.out.println("Fetching a new page");
				doc =  Jsoup.connect(
						"https://www.google.fr/search?q="+keyword+"&start="+Integer.toString(depth*10))
						.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)")
						.ignoreHttpErrors(true)
						.timeout(0)
						.get();
				Elements serps = doc.select("h3[class=r]");
				for (Element serp : serps) {
					Element link = serp.getElementsByTag("a").first();
					String linkref = link.attr("href");
					if (linkref.startsWith("/url?q=")){
						nb_results++;
						linkref = linkref.substring(7,linkref.indexOf("&"));
					}
					if (linkref.contains(target_name)){
						my_rank=nb_results;
						my_url=linkref;
						found=true;
					}			
					//					System.out.println("Link ref: "+linkref);
					//					System.out.println("Title: "+serp.text());
				}
				if (nb_results == 0){
					System.out.println("Warning captcha");
				}
				depth++;
			}
			catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long taskTimeMs  = System.currentTimeMillis( ) - startTimeMs;
		//System.out.println(taskTimeMs);
		info.setPosition(my_rank);
		info.setUrl(my_url);
		if (nb_results == 0){
			System.out.println("Warning captcha");
		}else {
			System.out.println("Number of links : "+nb_results);
		}
		System.out.println("My rank : "+my_rank+" for keyword : "+keyword);
		System.out.println("My URL : "+my_url+" for keyword : "+keyword);
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

}
