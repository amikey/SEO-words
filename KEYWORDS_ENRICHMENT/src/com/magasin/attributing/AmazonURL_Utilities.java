package com.magasin.attributing;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AmazonURL_Utilities {
	//	Voici le principe de hiérarchie pour les navid (première séquence de chiffres indiquée après identifiant type de page dansl’url ( ex : /v- , /l-, /f-)…
	//	+ 2 chiffres pour chaque sous-strate ( car 99 nœuds max pourune même strate)
	//	 
	//	Root/Home : 1
	//	Root-1/Home magasin = 1XX
	//	Root-2/Rayon : 1XXXX
	//	Root-3/Sous-rayon : 1XXXXXX
	//	Root-4 : Sous-sous rayon : 1XXXXXXXX
	//	Root-5 : 1XXXXXXXXXX
	private static String Vitrine = "^http://([a-z0-9]*\\.)*www.cdiscount.com.*/v-.*";
	private static String FicheProduit  ="^http://([a-z0-9]*\\.)*www.cdiscount.com.*/f-.*";
	private static String ListeProduit = "^http://([a-z0-9]*\\.)*www.cdiscount.com.*/l-.*";
	private static String ListeProduitFiltre = "^http://([a-z0-9]*\\.)*www.cdiscount.com.*/lf-.*";
	private static String PageMarque = "^http://([a-z0-9]*\\.)*www.cdiscount.com.*/m-.*";
	private static String PageConcept = "^http://([a-z0-9]*\\.)*www.cdiscount.com.*/ct-.*";
	private static String SearchDexing = "^http://([a-z0-9]*\\.)*www.cdiscount.com.*/r-.*";

	public static String checkType(String url){
		if (url.matches(Vitrine)){
			return "Vitrine";
		}
		if (url.matches(ListeProduit)){
			return "ListeProduit";
		}
		if (url.matches(FicheProduit)){
			return "FicheProduit";
		}
		if (url.matches(ListeProduitFiltre)){
			return "ListeProduitFiltre";
		}
		if (url.matches(PageMarque)){
			return "PageMarque";
		}
		if (url.matches(PageConcept)){
			return "PageConcept";
		}
		if (url.matches(SearchDexing)){
			return "SearchDexing";
		}	
		return "Unknown";
	}

	public static String checkMagasin(String url){
		String magasin = "Unknown";
		if ("Vitrine".equals(checkType(url))||"ListeProduit".equals(checkType(url))||"FicheProduit".equals(checkType(url))||"SearchDexing".equals(checkType(url))){
			url = url.replace("http://www.cdiscount.com/","");
			StringTokenizer tokenize = new StringTokenizer(url,"/");
			if (tokenize.hasMoreTokens()){
				magasin=tokenize.nextToken();
			}
		}
		if ("ListeProduitFiltre".equals(checkType(url))){
			// it will soon behave as the searchdexing
		}
		return magasin;
	}

	public static String checkRayon(String url){
		String rayon = "Unknown";
		if ("Vitrine".equals(checkType(url))||"ListeProduit".equals(checkType(url))||"FicheProduit".equals(checkType(url))){
			//			int vitrine_index=url.indexOf("/v-");
			//			int listeproduit_index=url.indexOf("/l-");
			//			int ficheproduit_index=url.indexOf("/f-");
			//
			//			int index = Math.max(Math.max(vitrine_index, listeproduit_index),ficheproduit_index);
			//			// we here catch the navid			
			//			String truncated_url=url.substring(0,index);
			//			String navid = url.substring(index+3, url.indexOf("-", index+3));
			//			int navid_length = navid.length();
			//			System.out.println("Navid : "+navid);
			//
			//			// we here slice the truncated url
			url = url.replace("http://www.cdiscount.com/","");
			StringTokenizer tokenize = new StringTokenizer(url,"/");
			List<String> tokenList = new ArrayList<String>();
			while (tokenize.hasMoreTokens()){
				tokenList.add(tokenize.nextToken());
			}
			rayon=tokenList.get(1);
		}
		return rayon;
	}

	public static String getNavid(String url){
		String navid = "Unknown";
		if ("Vitrine".equals(checkType(url))||"ListeProduit".equals(checkType(url))||"FicheProduit".equals(checkType(url))){
			int vitrine_index=url.indexOf("/v-");
			int listeproduit_index=url.indexOf("/l-");
			int ficheproduit_index=url.indexOf("/f-");

			int index = Math.max(Math.max(vitrine_index, listeproduit_index),ficheproduit_index);
			// we here catch the navid			
			navid = url.substring(index+3, url.indexOf("-", index+3));
		}
		return navid;

	}

	public static String checkProduit(String url){
		String produit = "Unknown";
		//		url = url.replace("http://www.cdiscount.com/","");
		//		StringTokenizer tokenize = new StringTokenizer(url,"/");
		//		if (tokenize.hasMoreTokens()){
		//			tokenize.nextToken();
		//		}
		//		if (tokenize.hasMoreTokens()){
		//			tokenize.nextToken();
		//		}
		//		if (tokenize.hasMoreTokens()){
		//			produit = tokenize.nextToken();
		//		}
		return produit;
	}

}