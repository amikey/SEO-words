package com.similarity.computing;

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

public class SimilarityComputingThreadPool {

	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/kriter.properties";
	private static int list_fixed_pool_size = 250;
	private static int list_size_bucket = 25;
	public static String select_distinct_cat4 = "select distinct categorie_niveau_4 from CATALOG";
	
	public static void main(String[] args) {
		System.out.println("Number of threads for list crawler : "+list_fixed_pool_size);
		System.out.println("Bucket size for list crawler : "+list_size_bucket);
		// it would be best to use a property file to store MD5 password
		//		// Getting the database property
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
		//the following properties have been identified
		String url = props.getProperty("db.url");
		String user = props.getProperty("db.user");
		String passwd = props.getProperty("db.passwd");

		System.out.println("You'll connect to the postgresql KRITERDB database as "+user);
		// Instantiating the pool thread
		ExecutorService executor = Executors.newFixedThreadPool(list_fixed_pool_size);
		// The database connection
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {  
			con = DriverManager.getConnection(url, user, passwd);
			// getting the number of URLs to fetch
			System.out.println("Requesting all distinct categories");
			pst = con.prepareStatement(select_distinct_cat4);
			rs = pst.executeQuery();
			// dispatching to threads
			int local_count=0;
			int global_count=0;
			List<String> thread_list = new ArrayList<String>();
			while (rs.next()) {
				String cat4 = rs.getString(1);
				if(local_count<list_size_bucket ){
					thread_list.add(cat4);		
					local_count++;
				}
				if (local_count==list_size_bucket){
					// one new connection per task
					System.out.println("Launching another thread with "+local_count+" Categories to fetch");
					Connection local_con = DriverManager.getConnection(url, user, passwd);
					Runnable worker = new SimilarityComputingWorkerThread(local_con,thread_list);
					executor.execute(worker);		
					// we initialize everything for the next thread
					local_count=0;
					thread_list = new ArrayList<String>();
					thread_list.add(cat4);
				}
				global_count++;
			}
			rs.close();
			pst.close();
			// we add one for the euclidean remainder
			// there might be a last task with the euclidean remainder
			if (thread_list.size()>0){
				// one new connection per task
				System.out.println("Launching another thread with "+local_count+ " Categories to fetch");
				Connection local_con = DriverManager.getConnection(url, user, passwd);
				Runnable worker = new SimilarityComputingWorkerThread(local_con,thread_list);
				executor.execute(worker);
			}
			System.out.println("We have : " +global_count + " Categories status to fetch according to the Kriter database \n");
		} catch (SQLException ex) {
			ex.printStackTrace();
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
				ex.printStackTrace();
			}
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		System.out.println("Finished all threads");
	}
}
