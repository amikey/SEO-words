package com.processing;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PSQLException;

public class RecursiveAmazonEnrichingDatabase {
	public static int my_threshold=0;
	public static void main(String[] args){

		// Reading the property of our database
		Properties props = new Properties();
		FileInputStream in = null;      
		try {
			in = new FileInputStream("database.properties");
			props.load(in);

		} catch (IOException ex) {

			Logger lgr = Logger.getLogger(RecursiveAmazonEnrichingDatabase.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {

			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				Logger lgr = Logger.getLogger(RecursiveAmazonEnrichingDatabase.class.getName());
				lgr.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}

		// the following properties have been identified
		String url = props.getProperty("db.url");
		String user = props.getProperty("db.user");
		String passwd = props.getProperty("db.passwd");
		// Instantiating the database
		Connection con = null;
		PreparedStatement pst = null;
		// the csv file variables
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection(url, user, passwd);
			//DetectorFactory.loadProfile("profiles");
			// we here fetch the base of key words to fetch from Amazon
			pst = con.prepareStatement("SELECT distinct keyword FROM AMAZON_KEYWORDS");
			rs = pst.executeQuery();
			int counter=0;
			while (rs.next()) {
				String full_keyword = rs.getString(1);
				counter++;
				if (counter>=my_threshold){
					System.out.println("Enriching database for word : "+full_keyword);
					StringTokenizer token= new StringTokenizer(full_keyword);				
					while (token.hasMoreElements()) {     					
						String word_to_suggest=(String)token.nextElement();
						List<String> my_suggestions=RequestingClass.fetch(word_to_suggest);
						System.out.println("Suggestion : "+word_to_suggest+" results "+my_suggestions);

						for (String suggestion : my_suggestions){
							/// inserting in the amazon database
							String am_stm = "INSERT INTO AMAZON_KEYWORDS(KEYWORD)"
									+ " VALUES(?)";

							// WE INSERT IN THE RDC DATABASE
//							pst = con.prepareStatement(am_stm);
//							pst.setString(1,suggestion);
//							try {
//								System.out.println("Inserting in AMAZON keyword the suggested word : "+suggestion);
//								pst.executeUpdate();
//							} catch (PSQLException e){
//								// the primary key constraint has been violated
//								e.printStackTrace();
//							}
						}

					}    
				}
			}	
			// updating the enriched database the keyword and all the suggestions from amazon
		} catch (Exception ex) {
			Logger lgr = Logger.getLogger(RecursiveAmazonEnrichingDatabase.class.getName());
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
				//                if (br != null) {
				//                    br.close();
				//                }

			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(RecursiveAmazonEnrichingDatabase.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}

	}
}
