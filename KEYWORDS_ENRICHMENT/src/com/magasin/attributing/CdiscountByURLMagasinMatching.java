package com.magasin.attributing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CdiscountByURLMagasinMatching {

	private static String update_statement ="UPDATE REFERENTIAL_KEYWORDS SET MAGASIN=?,RAYON=? WHERE KEYWORD=?";
	private static String update_pricing_statement ="UPDATE PRICING_KEYWORDS SET MAGASIN=?,RAYON=? WHERE KEYWORD=?";
	private static String request_url = "www.cdiscount.com/sa-10/";
	private static int counter = 1;

	public static void main(String[] args){
		// Getting the database property

		//		Properties props = new Properties();
		//		FileInputStream in = null;      
		//		try {
		//			in = new FileInputStream("database.properties");
		//			props.load(in);
		//		} catch (IOException ex) {
		//			Logger lgr = Logger.getLogger(MagasinListThreadPool.class.getName());
		//			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		//
		//		} finally {
		//			try {
		//				if (in != null) {
		//					in.close();
		//				}
		//			} catch (IOException ex) {
		//				Logger lgr = Logger.getLogger(MagasinListThreadPool.class.getName());
		//				lgr.log(Level.SEVERE, ex.getMessage(), ex);
		//			}
		//		}
		// the following properties have been identified
		//		String url = props.getProperty("db.url");
		//		String user = props.getProperty("db.user");
		//		String passwd = props.getProperty("db.passwd");
		String url="jdbc:postgresql://localhost/KEYWORDSDB";
		String user="postgres";
		String passwd="mogette";


		// The database connection
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;

		try {  
			con = DriverManager.getConnection(url, user, passwd);
			pst = con.prepareStatement("SELECT count(*) FROM REFERENTIAL_KEYWORDS");
			rs = pst.executeQuery();
			int size=0;
			while (rs.next()) {
				size=rs.getInt(1); 
			}	
			pst.close();
			rs.close();
			System.out.println("Numbers of keyword to manage : "+size);
			pst = con.prepareStatement("SELECT KEYWORD FROM REFERENTIAL_KEYWORDS");
			rs = pst.executeQuery();
			//List<String> tofetch = new ArrayList<String>();
			int local_counter=0;
			while (rs.next()) {
				local_counter++;					
				String keyword = rs.getString(1);
				System.out.println(local_counter+": dealing with keyboards : "+keyword);
				// for each keyword we now fetch cdiscount and amazon results
				try{
					keyword=keyword.replace("'", "''");
//					PreparedStatement keyword_st = con.prepareStatement("select url, domain from pricing_keywords where keyword='"+keyword+"' and domain in ('cdiscount.com','amazon.fr') order by search_position desc");
					PreparedStatement keyword_st = con.prepareStatement("select url, domain from pricing_keywords where keyword='"+keyword+"' and domain in ('cdiscount.com') order by search_position desc");
					ResultSet keyword_rs = keyword_st.executeQuery();
					String cdiscount_magasin = "Unknown";
					String cdiscount_rayon = "Unknown";
//					String amazon_magasin = "Unknown";
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
								cdiscount_rayon=results[1];
								System.out.println("Ranking URL : "+ranking_url);
								System.out.println("Keyword : "+keyword);
								System.out.println("Inserting Cdiscount magasin : "+cdiscount_magasin);
								System.out.println("Inserting Cdiscount rayon : "+cdiscount_rayon);
							}
						}
//						if ("amazon.fr".equals(amazon_magasin)){
//							// cdiscount is ranking, we extract the magasin
//							amazon_magasin=URL_Utilities.checkMagasin(ranking_url);
//						}	
					}

					//System.out.println("Amazon magasin"+amazon_magasin);
					
					// updating the two databases with the proper magasin
					PreparedStatement update_keyword_st = con.prepareStatement(update_statement);
					// preparing the statement
					update_keyword_st.setString(1,cdiscount_magasin);
					update_keyword_st.setString(2,cdiscount_rayon);
					update_keyword_st.setString(3,keyword);
					update_keyword_st.executeUpdate();
					update_keyword_st.close();
					
					
					// updating the two databases with the proper magasin
					PreparedStatement update_pricing_keyword_st = con.prepareStatement(update_pricing_statement);
					// preparing the statement
					update_pricing_keyword_st.setString(1,cdiscount_magasin);
					update_pricing_keyword_st.setString(2,cdiscount_rayon);
					update_pricing_keyword_st.setString(3,keyword);
					update_pricing_keyword_st.executeUpdate();
					update_pricing_keyword_st.close();
					
				}catch (SQLException e){
					System.out.println("Trouble with the keyword : "+keyword);
					e.printStackTrace();
				}



				//tofetch.add(rs.getString(1));

				//					if (local_counter == size_bucket){
				//						// we reset the counter to the initial state
				//						local_counter=0;
				//						Runnable worker = new WorkerThread(con, tofetch);
				//						
				//						tofetch=new ArrayList<String>();
				//						// one connection per thread
				//						con=DriverManager.getConnection(url, user, passwd);
				//					}
			}

			//			int nb_bucket = size/size_bucket;
			//
			//			int size_for_connection=nb_bucket/nb_connection;
			//			int connection_nb=1;
			//			// Dispatching all threads with their work to do
			//			for (int i = 0; i < nb_bucket+1; i++) {
			//
			//				Integer[] my_range = new Integer[2];
			//				my_range[0]=i*size_bucket;
			//				my_range[1]=(i+1)*size_bucket;
			//				if (connection_nb*size_for_connection <= i){
			//					con = DriverManager.getConnection(url, user, passwd);
			//					connection_nb++;
			//					System.out.println("Number of connections"+connection_nb);
			//				}
			//
			//				Runnable worker = new WorkerThread(con, my_range,url, user, passwd);
			//				executor.execute(worker);
			//			}

		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(MagasinListThreadPool.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {

			try {
				if (rs != null) {
					rs.close();
				}
				if (pst != null) {
					pst.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(MagasinListThreadPool.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}


	}

}


//	public static void main(String args[]){
//		//Reading the property of our database
//		Properties props = new Properties();
//		FileInputStream in = null;      
//		try {
//			in = new FileInputStream("database.properties");
//			props.load(in);
//		} catch (IOException ex) {
//			Logger lgr = Logger.getLogger(MergingReferential.class.getName());
//			lgr.log(Level.SEVERE, ex.getMessage(), ex);
//		} finally {
//			try {
//				if (in != null) {
//					in.close();
//				}
//			} catch (IOException ex) {
//				Logger lgr = Logger.getLogger(MergingReferential.class.getName());
//				lgr.log(Level.SEVERE, ex.getMessage(), ex);
//			}
//		}
//		// the following properties have been identified
//		String url = props.getProperty("db.url");
//		String user = props.getProperty("db.user");
//		String passwd = props.getProperty("db.passwd");
//		// Instantiating the database
//		Connection con = null;
//		try{
//			con = DriverManager.getConnection(url, user, passwd);
//			// inserting the referential
//			String select_from_referential = "SELECT keyword FROM REFERENTIAL_KEYWORDS";
//			PreparedStatement pst = con.prepareStatement(select_from_referential);
//			ResultSet rs = pst.executeQuery();
//			while (rs.next()) {
//				String keyword = rs.getString(1);
//				System.out.println("Updating magasin for : "+counter +"  "+keyword);
//				counter++;
//				
//// inserting the magasin data for our keyword				
////				// Inserting the keyword in the referential
////				String ref_stm = "INSERT INTO REFERENTIAL_KEYWORDS(KEYWORD,SOURCE,SEARCH_VOLUME,CDS_TREND)"
////						+ " VALUES(?,?,?,?)";
////				pst = con.prepareStatement(ref_stm);
////				pst.setString(1,keyword);
////				pst.setString(2,source);
////				pst.setInt(3,-1);
////				pst.setInt(4,-1);	
////				try{
////					pst.executeUpdate();
////				}catch (Exception e){
////					System.out.println(keyword + " already there");
////					e.printStackTrace();
////				}
//			}
//		} catch (Exception ex) {
//			Logger lgr = Logger.getLogger(MergingReferential.class.getName());
//			lgr.log(Level.SEVERE, ex.getMessage(), ex);
//		} finally {
//			try {
//				if (con != null) {
//					con.close();
//				}
//			} catch (SQLException ex) {
//				Logger lgr = Logger.getLogger(MergingReferential.class.getName());
//				lgr.log(Level.WARNING, ex.getMessage(), ex);
//			}
//		}
//
//	}


