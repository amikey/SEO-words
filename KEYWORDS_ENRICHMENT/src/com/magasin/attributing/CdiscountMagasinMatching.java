package com.magasin.attributing;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.merging.MergingReferential;

public class CdiscountMagasinMatching {
	//www.cdiscount.com/sa-10/mot-cle.html
	private static String request_url = "www.cdiscount.com/sa-10/";
	private static int counter = 1;
	
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

}
