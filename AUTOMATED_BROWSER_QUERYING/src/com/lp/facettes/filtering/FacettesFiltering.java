package com.lp.facettes.filtering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.facettes.data.AdvancedFacettesInfo;
import com.facettes.data.URLFacettesData;
import com.facettes.utility.FacettesUtility;

public class FacettesFiltering {

	private static String path = "/home/sduprey/My_Programs/phantomjs-1.9.8-linux-x86_64/bin/phantomjs";

	public static void main(String[] args){
		URLFacettesData to_fetch = new URLFacettesData();

		//String urlToSearch = "http://www.cdiscount.com/juniors/figurines/gormiti/l-1206714.html";
		String urlToSearch = "http://www.cdiscount.com/maison/lampes/lampes-led/l-1170231.html";
		//		String urlToSearch = "http://www.cdiscount.com/maison/linge-maison/linge-de-decoration/plaids-et-couvre-lits/l-117620403.html";
		to_fetch.setUrl(urlToSearch);


		String my_user_agent= "CdiscountBot-crawler";
		org.jsoup.nodes.Document doc = null;
		try {
			doc =  Jsoup.connect(urlToSearch)
					.userAgent(my_user_agent)
					.ignoreHttpErrors(true)
					.timeout(0)
					.get();
		}catch (IOException e){
			e.printStackTrace();
		}
		List<AdvancedFacettesInfo> standardFacettes = new ArrayList<AdvancedFacettesInfo>();
		if (doc != null){
			standardFacettes = FacettesUtility.extract_facettes_infos(doc, to_fetch);
		}
		System.out.println("Standard facettes number : "+standardFacettes.size());		

		// Create a new instance of the Firefox driver
		// Notice that the remainder of the code relies on the interface, 
		// not the implementation.
		//		File phantomjs = new File(System.getProperty("java.io.tmpdir")+File.separator+"phantomjs-1.9.7");
		File phantomjs = new File(path);

		DesiredCapabilities dcaps = new DesiredCapabilities();

		dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs.getAbsolutePath());

		//PhantomJSDriver driver = new PhantomJSDriver(dcaps);
		//WebDriver driver = new PhantomJSDriver();
		WebDriver driver = new FirefoxDriver();

		// And now use this to visit Google
		driver.get(to_fetch.getUrl());
		// Alternatively the same thing can be done like this
		// driver.navigate().to("http://www.google.com");


//		List<WebElement> categories = driver.findElements(By.xpath("//*[@id='mvFilter']/form//div[@class='mvFTitle noSel']"));
//		
//		for (WebElement category : categories){
//			if (category.isDisplayed()){
//				category.click();
//			}
//		}
		
		// Radio Button: Check Monday using XPATH locator.

		//WebElement menuArrow = driver.findElement(By.cssSelector("div.mvFacets.jsFCategory"));
		WebElement menuArrow = driver.findElement(By.xpath("//*[@id='mvFilter']/form//div[text()='Vendeur']"));
		menuArrow.click();
		WebElement checkBoxFacetMarketPlaceFiltering = null;
		try{
			checkBoxFacetMarketPlaceFiltering = driver.findElement(By.xpath("//input[@value='f/368/c le marche']"));
			checkBoxFacetMarketPlaceFiltering.click();
		} catch (org.openqa.selenium.ElementNotVisibleException e){
			System.out.println("The facet was already displayed, we undisplay it");
			try{
				menuArrow.click();
				checkBoxFacetMarketPlaceFiltering.click();
			} catch (Exception e2){
				System.out.println("Trouble checking the market place facet");
			}
		}

		String htmlPageSourceCode = driver.getPageSource();
		//Close the browser
		driver.quit();
		// Check the title of the page
		System.out.println("Page source code : " + htmlPageSourceCode);
		List<AdvancedFacettesInfo> marketPlaceFacettes = FacettesUtility.extract_facettes_from_source_code(htmlPageSourceCode, to_fetch);
		System.out.println("Market place facettes number : "+marketPlaceFacettes.size());				


		List<AdvancedFacettesInfo> merged_facettes = FacettesUtility.merge_facettes(marketPlaceFacettes,standardFacettes);

		System.out.println("Merged facettes size : "+merged_facettes.size());
	}
}
