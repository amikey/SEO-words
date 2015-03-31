package com.similarity.test;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SimilarityTest {

	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/kriter.properties";

	public static void main(String[] args) throws SQLException{
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

		Connection local_con = DriverManager.getConnection(url, user, passwd);
		List<String> categories = new ArrayList<String>();
		//      categories.add("TARTE");
		//		categories.add("CARTE TUNER TV");
		//		categories.add("PANES - CORDON BLEUS");
		//		categories.add("FANION DE SIGNALISATION");	
		//		categories.add("TIGE A URETRE");
		//		categories.add("COQUE - HOUSSE");
		//      categories.add("SALADE");
		//      categories.add("CABINE D'ESSAYAGE - MIROIR D'ESSAYAGE - RIDEAU DE CABINE");
		      categories.add("COQUE - BUMPER - FACADE TELEPHONE");
		//      categories.add("XBOX 360");

		
		long startTime = System.currentTimeMillis();
	
		//Runnable worker = new SimilarityComputingNoFetchWorkerThread(local_con,categories);
		//worker.run();

		long endTime = System.currentTimeMillis();

		long timeneeded = (endTime-startTime)/(1000*60);
		System.out.println("Needed time in minutes : "+timeneeded);
	}
}
