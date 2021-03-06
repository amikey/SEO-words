package com.magasin.attributing;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.ranks.CdiscountInformation;

public class ScrapingWorkerThread implements Runnable {
	private static String request_url = "http://www.cdiscount.com/sa-10/";
	private static String update_statement ="UPDATE REFERENTIAL_KEYWORDS SET MAGASIN=?,RAYON=?,PRODUIT=? WHERE KEYWORD=?";

	private List<String> my_keywords_to_fetch;
	private int counter = 0;
	private Connection con;
	public ScrapingWorkerThread(Connection con, List<String> to_fetch) throws SQLException{
		my_keywords_to_fetch=to_fetch;
		this.con = con;
	}

	public void run() {
		for (int i =0;i<my_keywords_to_fetch.size();i++){		
			String keyword_to_fetch = my_keywords_to_fetch.get(i);
			try{
				CdiscountInformation info = getKeywordInfo(keyword_to_fetch);
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
		st.setString(3,info.getProduit());
		st.setString(4,keyword);

		st.executeUpdate();
		st.close();
		// if the row has not been updated, we have to insert it !
	}

	private CdiscountInformation getKeywordInfo(String keyword) throws IOException{
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
