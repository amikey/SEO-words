package com.httpinfos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.parsing.utility.XPathUtility;

public class URLListWorkerThread implements Runnable {
	private int batch_size = 100;

	private static String updateStatement ="UPDATE HTTPINFOS_LIST SET STATUS=?, H1=?, TITLE=?, XPATH1=?, XPATH2=?, XPATH3=?, XPATH4=?, XPATH5=?, TO_FETCH=FALSE WHERE ID=?";
	private String[] xpathExpressions;
	private String user_agent;
	private List<ULRId> my_urls_to_fetch = new ArrayList<ULRId>();
	private Connection con;

	public URLListWorkerThread(Connection con ,List<Integer> to_fetch, String my_user_agent, String[] xpathExpressions) throws SQLException{
		this.xpathExpressions = xpathExpressions;
		this.user_agent=my_user_agent;
		this.con = con;
		String my_url="";
		if (to_fetch.size()>0){
			try {
				PreparedStatement pst = null;
				my_url="SELECT URL, ID FROM HTTPINFOS_LIST WHERE TO_FETCH = TRUE and ID in "+to_fetch.toString();
				my_url=my_url.replace("[", "(");
				my_url=my_url.replace("]", ")");
				pst = con.prepareStatement(my_url);
				ResultSet rs = null;
				rs = pst.executeQuery();
				while (rs.next()) {
					String loc_url = rs.getString(1);
					int id = rs.getInt(2);
					ULRId toadd = new ULRId();
					toadd.setId(id);
					toadd.setUrl(loc_url);
					my_urls_to_fetch.add(toadd); 
				}
				pst.close();
				System.out.println(Thread.currentThread()+" initialized with  : "+to_fetch.size() + " fetched URLs");

			}
			catch(SQLException e){
				e.printStackTrace();
				System.out.println("Trouble with thread"+Thread.currentThread()+" and URL : "+my_url);
			}
		}
	}

	public void run() {
		List<ULRId> line_infos = new ArrayList<ULRId>();
		for (ULRId id :my_urls_to_fetch){
			line_infos.add(id);
			if (line_infos.size() !=0 && line_infos.size() % batch_size ==0) {
				runBatch(line_infos);	
				line_infos.clear();
				line_infos = new ArrayList<ULRId>();
			}
		}
		runBatch(line_infos);
		close_connection();
		System.out.println(Thread.currentThread().getName()+" closed connection");
	}

	public void runBatch(List<ULRId> line_infos){
		List<URLInfo> infos=processCommand(line_infos);
		updateStatus(infos);
		System.out.println(Thread.currentThread().getName()+" End");
	}

	private void close_connection(){
		try {
			this.con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// batched update
	private void updateStatus(List<URLInfo> infos){
		System.out.println("Adding to batch : " + infos.size() + "ULRs into database");
		try {
			//Statement st = con.createStatement();
			con.setAutoCommit(false); 
			PreparedStatement st = con.prepareStatement(updateStatement);
			for (int i=0;i<infos.size();i++){
				String H1= infos.get(i).getH1().replace("'", "");
				String TITLE = infos.get(i).getTitle().replace("'", "");
				String[] XPATHRESULTS = infos.get(i).getXpathResults();
				st.setInt(1, infos.get(i).getStatus());
				st.setString(2, H1);
				st.setString(3, TITLE);
				if (XPATHRESULTS != null){
					st.setString(4, XPATHRESULTS[0]);
					st.setString(5, XPATHRESULTS[1]);
					st.setString(6, XPATHRESULTS[2]);
					st.setString(7, XPATHRESULTS[3]);
					st.setString(8, XPATHRESULTS[4]);
				}else {
					st.setString(4, "");
					st.setString(5, "");
					st.setString(6, "");
					st.setString(7, "");
					st.setString(8, "");
				}
				st.setInt(9, infos.get(i).getId());
				//UPDATE HTTPINFOS_LIST SET STATUS=?, H1=?, TITLE=?, XPATH1=?, XPATH2=?, XPATH3=?, XPATH4=?, XPATH5=?, TO_FETCH=FALSE WHERE ID=?";
				//	String batch ="UPDATE HTTPINFOS_LIST SET STATUS="+infos.get(i).getStatus()+", H1='"+H1+"', TITLE='"+TITLE+ "',TO_FETCH=FALSE WHERE ID="+infos.get(i).getId();
				st.addBatch();		
			}      
			//int counts[] = st.executeBatch();
			System.out.println("Beginning to insert : " + infos.size() + "ULRs into database");
			st.executeBatch();
			con.commit();
			st.close();
			System.out.println("Having inserted : " + infos.size() + "ULRs into database");
		} catch (SQLException e){
			e.printStackTrace();
			System.out.println("Trouble inserting batch ");
		}
	}

	// update step by step
	//	private void updateStatus(List<URLInfo> infos){
	//		for (int i=0;i<infos.size();i++){
	//			String H1= infos.get(i).getH1().replace("'", "");
	//			String TITLE = infos.get(i).getTitle().replace("'", "");
	//			String batch ="UPDATE HTTPINFOS_LIST SET STATUS="+infos.get(i).getStatus()+", H1='"+H1+"', TITLE='"+TITLE+ "',TO_FETCH=FALSE WHERE ID="+thread_fetch_ids.get(i);
	//			try{
	//				PreparedStatement insert_st = con.prepareStatement(batch);
	//				insert_st.executeUpdate();
	//			} catch (SQLException e){
	//				System.out.println("Trouble inserting : "+batch);
	//				e.printStackTrace();
	//			}
	//
	//		}      
	//		System.out.println("Inserting : " + infos.size() + "ULRs into database");
	//	}

	private List<URLInfo> processCommand(List<ULRId> line_infos) {
		List<URLInfo> my_fetched_infos = new ArrayList<URLInfo>();
		for(ULRId line_info : line_infos){
			// second method
			URLInfo my_info = new URLInfo();
			my_info.setId(line_info.getId());
			HttpURLConnection connection = null;
			try{
				System.out.println(Thread.currentThread().getName()+" fetching URL : "+line_info.getUrl());
				URL url = new URL(line_info.getUrl());
				connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent",this.user_agent);
				connection.setInstanceFollowRedirects(true);
				connection.setConnectTimeout(3000);
				connection.connect();
				// getting the status from the connection
				my_info.setStatus(connection.getResponseCode());
				// getting the content to parse
				InputStreamReader in = new InputStreamReader((InputStream) connection.getContent());
				BufferedReader buff = new BufferedReader(in);
				String content_line;
				StringBuilder builder=new StringBuilder();
				do {
					content_line = buff.readLine();
					builder.append(content_line);
				} while (content_line != null);
				String html = builder.toString();
				org.jsoup.nodes.Document doc = Jsoup.parse(html);
				Elements h1s = doc.select("h1");
				String conc_h1="";
				for (Element h1 : h1s) {
					conc_h1=conc_h1+h1.text();
				}	
				my_info.setH1(conc_h1);
				Elements titles = doc.select("title");
				String conc_title="";
				for (Element title : titles) {
					conc_title=conc_title+title.text();
				}				
				my_info.setTitle(conc_title);
				String[] xpathResults = new String[5];
				int local_counter = 0;
				for (String xpath : xpathExpressions){
					String content = XPathUtility.parseContent(html, xpath);
					xpathResults[local_counter]=content;
					local_counter++;
				}
				my_info.setXpathResults(xpathResults);
			} catch (Exception e){
				System.out.println("@@@@@@@@@@@@@@@@ Error with "+line_info);
				e.printStackTrace();
			}

			if (connection != null){
				connection.disconnect();
			}
			my_fetched_infos.add(my_info);
		}
		return my_fetched_infos;
	}

	class ULRId{
		private String url="";
		private int id;
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
	}


	class URLInfo{
		private int id;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		private String[] xpathResults;
		private String h1="";
		private String title="";
		private int status=-1;
		public String getH1() {
			return h1;
		}
		public void setH1(String h1) {
			this.h1 = h1;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public String[] getXpathResults() {
			return xpathResults;
		}
		public void setXpathResults(String[] xpathResults) {
			this.xpathResults = xpathResults;
		}
	}

}
