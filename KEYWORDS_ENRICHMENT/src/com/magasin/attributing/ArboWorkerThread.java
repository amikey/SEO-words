package com.magasin.attributing;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ranks.CdiscountInformation;

public class ArboWorkerThread implements Runnable {

	private static String[] real_magasin = {
		"informatique",
		"musique-cd-dvd",
		"musique-instruments",
		//"9782818308318",
		"bricolage-chauffage",
		"culture-multimedia",
		//"traitement-de-l-air-de-l-eau",
		//"grues-treuils-palans",
		//"cdiscount-pro",
		"dvd",
		"livres-bd",
		//"cdiscount",
		"jeux-educatifs",
		//"t-shirts-manches-courtes",//http://www.cdiscount.com/t-shirts-manches-courtes/l-113011101.html
		"cadeaux-noel",
		//"apple-at-md826zm-a",
		//"point-de-vente",
		"juniors",
		"jeux-pc-video-console",
		"high-tech",
		//"sac-porte-travers-mandarina-duck-reference-j6t0",
		"vin-champagne",
		"photo-numerique",
		"animalerie",
		//"scarificateur-%C3%A9lectrique-combi-care-38-e-+-bac",
		"tout-a-moins-de-10-euros",
		//"lit-enfant-mi-hauteur-90-x-200-cm",
		//"op",
		//"9782918653400",
		//"fl%C3%A9chettes-ergo-soft",
		"bagages",
		"jardin-animalerie",
		"electromenager",
		"le-sport",
		"vin-alimentaire",
		"cosmetique",
		"telephonie",
		"arts-loisirs",
		//"filtration-de-l-eau-boissons-glacons",
		//"babygro-doudoune-doublee-polaire-bebe",
		"pret-a-porter",
		"soldes-promotions",
		"outillage",
		"chaussures",
		"destockage",
		//"jean-diesel-safado-8u9-homme",http://www.cdiscount.com/jean-diesel-safado-8u9-homme/f-1133018-mp00040255.html
		"auto",
		"Unknown",
		"maison",
		"boutique-cadeaux",
		"salon-complet",
		"bijouterie",
		"au-quotidien",
		//"disques-durs",
		"jardin",
		"personnalisation-3d"
		//"mammouth---d%C3%A9terre-ton-dinosaure---dig-a-dino"
	};




	private static String request_url = "http://www.cdiscount.com/sa-10/";
	private static String update_statement ="UPDATE REFERENTIAL_KEYWORDS SET MAGASIN=?,RAYON=? WHERE KEYWORD=?";
	private static String update_pricing_statement ="UPDATE PRICING_KEYWORDS SET MAGASIN=?,RAYON=? WHERE KEYWORD=?";

	private List<String> my_keywords_to_fetch;
	private int counter = 0;
	private Connection con;
	public ArboWorkerThread(Connection con, List<String> to_fetch) throws SQLException{
		my_keywords_to_fetch=to_fetch;
		this.con = con;
	}

	public void run() {
		for (int i =0;i<my_keywords_to_fetch.size();i++){		
			String keyword_to_fetch = my_keywords_to_fetch.get(i);
			try{
				//CdiscountInformation info = getScrapingKeywordInfo(keyword_to_fetch);
				CdiscountInformation info = getArboKeywordInfo(keyword_to_fetch);
				insertInfo(keyword_to_fetch,info);
			} catch (Exception e){
				e.printStackTrace();
				System.out.println("Trouble fetching keyword "+keyword_to_fetch);
			}
		}

		System.out.println(Thread.currentThread().getName()+" End");
	}

	private void insertInfo(String keyword,CdiscountInformation info) throws SQLException{
		counter++;
		System.out.println(Thread.currentThread().getName()+"Inserting keyword number : "+counter+"  "+keyword);
		PreparedStatement st = con.prepareStatement(update_statement);
		// preparing the statement
		st.setString(1,info.getMagasin());
		st.setString(2,info.getRayon());
		st.setString(3,keyword);

		st.executeUpdate();
		st.close();


		PreparedStatement update_pricing_keyword_st = con.prepareStatement(update_pricing_statement);
		// preparing the statement
		update_pricing_keyword_st.setString(1,info.getMagasin());
		update_pricing_keyword_st.setString(2,info.getRayon());
		update_pricing_keyword_st.setString(3,keyword);
		update_pricing_keyword_st.executeUpdate();
		update_pricing_keyword_st.close();
		// if the row has not been updated, we have to insert it !
	}

	private CdiscountInformation getArboKeywordInfo(String keyword){
		CdiscountInformation info =new CdiscountInformation();
		try{
			keyword=keyword.replace("'", "''");
			//			PreparedStatement keyword_st = con.prepareStatement("select url, domain from pricing_keywords where keyword='"+keyword+"' and domain in ('cdiscount.com','amazon.fr') order by search_position desc");
			PreparedStatement keyword_st = con.prepareStatement("select url, domain from pricing_keywords where keyword='"+keyword+"' and domain in ('cdiscount.com') order by search_position desc");
			ResultSet keyword_rs = keyword_st.executeQuery();
			String cdiscount_magasin = "Unknown";
			String cdiscount_rayon = "Unknown";
			//			String amazon_magasin = "Unknown";
			while (keyword_rs.next()) {
				String ranking_url = keyword_rs.getString(1);
				//System.out.println(ranking_url);
				String domain = keyword_rs.getString(2);
				//System.out.println(domain);
				if ("cdiscount.com".equals(domain)){
					// cdiscount is ranking, we extract the magasin
					if ("Unknown".equals(cdiscount_magasin)||"Unknown".equals(cdiscount_rayon)){
						String[] results=URL_Utilities.checkMagasinAndRayonAndProduct(ranking_url);
						cdiscount_magasin=results[0];
						if (!check_proper_magasin(cdiscount_magasin)){
							cdiscount_magasin="Unknown";
						};				
						info.setMagasin(cdiscount_magasin);
						cdiscount_rayon=results[1];			
						info.setRayon(cdiscount_rayon);
						System.out.println(Thread.currentThread()+"Ranking URL : "+ranking_url);
						System.out.println(Thread.currentThread()+"Keyword : "+keyword);
						System.out.println(Thread.currentThread()+"Inserting Cdiscount magasin : "+cdiscount_magasin);
						System.out.println(Thread.currentThread()+"Inserting Cdiscount rayon : "+cdiscount_rayon);
					}
				}
				//				if ("amazon.fr".equals(amazon_magasin)){
				//					// cdiscount is ranking, we extract the magasin
				//					amazon_magasin=URL_Utilities.checkMagasin(ranking_url);
				//				}	
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		return info;
	}

	// the arborescence is not always reliable
	private boolean check_proper_magasin(String totest){
		boolean found = false;
		for (int l=0;l<real_magasin.length;l++){
			if (real_magasin[l].equals(totest)){
				found=true;
			}
		}
		if (!found){
			System.out.println("We here got a little problem");
		}
		return found;
	}
	private CdiscountInformation getScrapingKeywordInfo(String keyword) throws IOException{
		keyword=keyword.replace(" ", "+");
		String my_url = request_url+keyword+".html";
		URL url = new URL(my_url);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
		// we here want to be redirected to the proper magasin
		connection.setInstanceFollowRedirects(true);
		connection.connect();	
		System.out.println(connection.getResponseCode());	
		String redirected_url =connection.getURL().toString(); 
		System.out.println(redirected_url);
		CdiscountInformation info =new CdiscountInformation();
		String magasin =URL_Utilities.checkMagasin(redirected_url);
		info.setMagasin(magasin);
		String rayon =URL_Utilities.checkRayon(redirected_url);
		info.setRayon(rayon);
		String produit =URL_Utilities.checkProduit(redirected_url);
		info.setProduit(produit);
		return info;
	}
}
