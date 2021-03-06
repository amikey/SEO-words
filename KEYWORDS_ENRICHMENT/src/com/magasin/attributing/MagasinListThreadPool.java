package com.magasin.attributing;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MagasinListThreadPool {
	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/keywords_enrichment.properties";

	private static int fixed_pool_size = 130;	
	// size of keywords to manage for a thread
	private static int size_bucket = 10000;
	

	public static void main(String[] args) {
		// Reading the property of our database
		Properties props = new Properties();
		FileInputStream in = null;      
		try {
			in = new FileInputStream(database_con_path);
			props.load(in);
		} catch (IOException ex) {
			System.out.println("Trouble fetching database configuration");
			ex.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				System.out.println("Trouble fetching database configuration");
				ex.printStackTrace();
			}
		}
		// the following properties have been identified
		String url = props.getProperty("db.url");
		String user = props.getProperty("db.user");
		String passwd = props.getProperty("db.passwd");

		// Instantiating the pool thread
		ExecutorService executor = Executors.newFixedThreadPool(fixed_pool_size);

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
			List<String> tofetch = new ArrayList<String>();
			int local_counter=0;
			while (rs.next()) {
				tofetch.add(rs.getString(1));
				local_counter++;
				if (local_counter == size_bucket){
					// we reset the counter to the initial state
					local_counter=0;
					//					Runnable worker = new ScrapingWorkerThread(con, tofetch);
					// one connection per thread
					con=DriverManager.getConnection(url, user, passwd);
					Runnable worker = new BatchedArboWorkerThread(con, tofetch);
					executor.execute(worker);
					tofetch=new ArrayList<String>();
					
				}
			}
			
			if (tofetch.size() > 0){
				con=DriverManager.getConnection(url, user, passwd);
				Runnable worker = new BatchedArboWorkerThread(con, tofetch);
				executor.execute(worker);
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
//				if (con != null) {
//					con.close();
//				}

			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(MagasinListThreadPool.class.getName());
				lgr.log(Level.WARNING, ex.getMessage(), ex);
			}
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		System.out.println("Finished all threads");

	}


}
