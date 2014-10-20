package com.httpstatus;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StatusListWorkerThread implements Runnable {
	private String user_agent;
	private String description;
	private List<Integer> thread_fetch_ids = new ArrayList<Integer>();
	private List<String> my_urls_to_fetch = new ArrayList<String>();
	private Connection con;

	public StatusListWorkerThread(Connection con ,List<Integer> to_fetch, String my_user_agent, String my_description) throws SQLException{
		this.user_agent=my_user_agent;
		this.description=my_description;
		this.thread_fetch_ids=to_fetch;
		this.con = con;
		PreparedStatement pst  = null;
		if ("".equals(my_description)){
			String my_url="SELECT URL FROM HTTPSTATUS_LIST WHERE TO_FETCH = TRUE and ID in "+to_fetch.toString();
			my_url=my_url.replace("[", "(");
			my_url=my_url.replace("]", ")");
			pst = con.prepareStatement(my_url);
		} else {
			String my_url="SELECT URL FROM HTTPSTATUS_LIST WHERE TO_FETCH = TRUE and DESCRIPTION='"+description+"' and ID in "+to_fetch.toString();
			my_url=my_url.replace("[", "(");
			my_url=my_url.replace("]", ")");
			pst = con.prepareStatement(my_url);
		};
		ResultSet rs = null;
		rs = pst.executeQuery();
		while (rs.next()) {
			my_urls_to_fetch.add(rs.getString(1)); 
		}
	}

	public void run() {
		List<Integer> status=processCommand();
		try {
			updateStatus(status);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName()+" End");
	}

	private void updateStatus(List<Integer> status) throws SQLException{
		Statement st = con.createStatement();       
		con.setAutoCommit(false);      
		for (int i=0;i<status.size();i++){
			String batch ="UPDATE HTTPSTATUS_LIST SET STATUS="+status.get(i)+", TO_FETCH=FALSE WHERE ID="+thread_fetch_ids.get(i);
			st.addBatch(batch);
		}      
		//int counts[] = st.executeBatch();
		st.executeBatch();
		con.commit();
		System.out.println("Inserting : " + status.size() + "ULRs into database");
	}


	private List<Integer> processCommand() {
		List<Integer> my_fetched_status = new ArrayList<Integer>();
		for (int i=0;i<my_urls_to_fetch.size();i++){
			String line=my_urls_to_fetch.get(i);
			// second method
			int status=-1;
			HttpURLConnection connection = null;
			try{
				System.out.println(Thread.currentThread().getName()+" fetching URL : "+line);
				URL url = new URL(line);
				connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("HEAD");
				connection.setRequestProperty("User-Agent",this.user_agent);
				connection.setInstanceFollowRedirects(false);
				connection.setConnectTimeout(30000);
				connection.connect();
				// getting the status from the connection
				status=connection.getResponseCode();

			  } catch (Exception e){
				System.out.println("Error with "+line);
				e.printStackTrace();
			}

			if (connection != null){
				connection.disconnect();
			}
			my_fetched_status.add(status);
		}
		return my_fetched_status;
	}
	

}
