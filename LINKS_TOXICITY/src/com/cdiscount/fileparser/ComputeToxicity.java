package com.cdiscount.fileparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class ComputeToxicity {
	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/links_toxicity.properties";
	private static String insert_rate_string ="INSERT INTO TRUST_RATING(SOURCE_URL, SOURCE_DOMAIN, NB_LINKS, NB_DOMAIN_LINKS, LINK_TRUSTFLOW, COMPUTED_TRUST_RATE_LINK, COMPUTED_TRUST_RATE_DOMAIN) VALUES (?,?,?,?,?,?,?)";
	private static int batch_size = 50000;
	private static Map<String, Integer> counting_map = new HashMap<String, Integer>();
	private static Map<String, Integer> urlcounting_map = new HashMap<String, Integer>();
	public static void main(String[] args) throws IOException {
		String csvFile = "/home/sduprey/My_Data/My_Toxic_Links/cdiscount.csv";
		BufferedReader br = null;
		String line = "";
		String header = null;
		String[] column_names = null;
		String cvsSplitBy = ",";
		int nb_line=1;
		// we loop over the file a first time
		try {
			// we loop over the file a first time
			br = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(csvFile), "UTF8"));
			// we skip the first line : the headers
			header = br.readLine();
			column_names= header.split(cvsSplitBy);
			System.out.println(column_names);
			while (((line = br.readLine()) != null)) {
				String[] splitted_line = line.split(cvsSplitBy);
				System.out.println("Reading line number :"+nb_line);
				try {
					String targetURL = splitted_line[0];
					String sourceURL = splitted_line[1];
					populateLinksMap(targetURL,sourceURL);
				} catch (Exception exx) {
					exx.printStackTrace();
				}
				nb_line++;
			}
			//displayFoundLinks();
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// we loop over the file a second time to make the computation

		Connection con = null;
		PreparedStatement pst = null;
		// the csv file variables
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
		nb_line=1;
		int nb_batch=1;
		// last error
		try {
			con = DriverManager.getConnection(url, user, passwd);
			con.setAutoCommit(false);
			pst = con.prepareStatement(insert_rate_string);

			// we loop over the file a second time to make the computation
			br = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(csvFile), "UTF8"));
			// we skip the first line : the headers
			header = br.readLine();
			column_names= header.split(cvsSplitBy);
			System.out.println(column_names);
			while ((line = br.readLine()) != null) {
				String[] splitted_line = line.split(cvsSplitBy);
				System.out.println("Inserting line number :"+nb_line);
				try {
					//String targetURL = splitted_line[0];
					String sourceURL = splitted_line[1];
					String sourceDomain = getDomain(sourceURL);
					Integer links_count = urlcounting_map.get(sourceURL);
					Integer domain_links_count = counting_map.get(sourceDomain);
					int SOURCE_TRUSTFLOW=Integer.valueOf(splitted_line[13]);
					int COMPUTED_TRUST_RATE_LINK=compute_rate(links_count,SOURCE_TRUSTFLOW);
					// INSERT INTO TRUST_RATING(SOURCE_URL, SOURCE_DOMAIN, NB_LINKS, LINK_TRUSTFLOW, COMPUTED_TRUST_RATE_LINK, COMPUTED_TRUST_RATE_DOMAIN) VALUES (?,?,?,?,?,?)
					System.out.println(sourceURL+" : "+sourceDomain+" : "+links_count+" : "+SOURCE_TRUSTFLOW+" : "+COMPUTED_TRUST_RATE_LINK);
					//pst = con.prepareStatement(insert_rate_string);
					pst.setString(1,sourceURL);
					pst.setString(2,sourceDomain);
					pst.setInt(3, links_count);
					pst.setInt(4,domain_links_count);
					pst.setInt(5, SOURCE_TRUSTFLOW);
					pst.setInt(6, COMPUTED_TRUST_RATE_LINK);
					pst.setInt(7, -1);
					pst.addBatch();
					//pst.executeUpdate();
					if (nb_batch == batch_size){
						pst.executeBatch();
						con.commit();
						con.setAutoCommit(false);
						nb_batch=0;
					}
				} catch (Exception exx) {
					exx.printStackTrace();
				}
				nb_line++;
				nb_batch++;
			}



			// We loop over the file a second time to make the computation 

		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}



	}

	private static int compute_rate(int links_count, int SOURCE_TRUSTFLOW){
		int trustflow_rate = 0;
		if (SOURCE_TRUSTFLOW<=10){
			trustflow_rate=6;
		}else if (SOURCE_TRUSTFLOW<=20){
			trustflow_rate=4;
		}else if (SOURCE_TRUSTFLOW<=30){
			trustflow_rate=2;
		}else {
			trustflow_rate=1;
		}

		int link_rate = 0;
		if (links_count<=10){
			link_rate=1;
		}else if (links_count<=20){
			link_rate=2;
		}else if (links_count<=50){
			link_rate=4;
		}else {
			link_rate=6;
		}
		return trustflow_rate+link_rate;
	}

	private static void populateLinksMap(String targetURL, String sourceURL){
		Integer local_count = urlcounting_map.get(sourceURL);
		if (local_count == null){
			local_count = new Integer(1);
			urlcounting_map.put(sourceURL, local_count);
		} else {
			local_count=local_count+1;
			urlcounting_map.put(sourceURL,local_count);
		}


		//		System.out.println("Target URL : "+targetURL);
		String target_domain=getDomain(targetURL);
		//		System.out.println("Target domain : "+target_domain);
		//		System.out.println("Source URL : "+sourceURL);
		String source_domain=getDomain(sourceURL);
		//		System.out.println("Source domain : "+source_domain);

		if (!"www.cdiscount.com".equals(target_domain)){
			System.out.println("Target domain unknown : "+target_domain);
			//	System.exit(0);
		}	
		Integer localdomain_count = counting_map.get(source_domain);
		if (localdomain_count == null){
			localdomain_count = new Integer(1);
			counting_map.put(source_domain, localdomain_count);
		} else {
			localdomain_count=localdomain_count+1;
			counting_map.put(source_domain,localdomain_count);
		}

	}

	private static void displayFoundLinks(){
		System.out.println("Displaying domain links count");
		Iterator it = urlcounting_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String domain_name=(String)pairs.getKey();
			Integer count = (Integer)pairs.getValue();
			System.out.println(domain_name+" : "+count);
		}	
	}

	private static String getDomain(String url){
		String target_domain = null;
		if (url.length()>=1 &&url.charAt(url.length()-1)=='\"')
		{
			url = url.replace(url.substring(url.length()-1), "");
		}
		if (url.length()>=1 &&url.charAt(0)=='\"')
		{
			url = url.substring(1);
		}
		url=url.replaceAll("http://","@");
		url=url.replaceAll("https://","@");
		// we hereby construct different tables 
		StringTokenizer target_protoc_tokenize = new StringTokenizer(url,"@");
		while (target_protoc_tokenize.hasMoreTokens()){
			String underTargetURL=target_protoc_tokenize.nextToken();
			StringTokenizer target_tokenize = new StringTokenizer(underTargetURL,"/");
			if (target_tokenize.hasMoreTokens()){
				target_domain = target_tokenize.nextToken();	
				if (target_domain.length()>=1 &&target_domain.charAt(target_domain.length()-1)=='\"')
				{
					target_domain = target_domain.replace(target_domain.substring(target_domain.length()-1), "");
				}
				if (target_domain.indexOf("?")>=0){
					target_domain=target_domain.substring(0,target_domain.indexOf("?"));
				}
				if (target_domain.indexOf("#")>=0){
					target_domain=target_domain.substring(0,target_domain.indexOf("#"));
				}
				if (target_domain.indexOf(":")>=0){
					target_domain=target_domain.substring(0,target_domain.indexOf(":"));
				}
				if (target_domain.indexOf("&")>=0){
					target_domain=target_domain.substring(0,target_domain.indexOf("&"));
				}
				target_domain=target_domain.trim();
			}	
		}
		return target_domain;
	}

	public static String removeBadChars(String s) {
		if (s == null) return null;
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<s.length();i++){ 
			if (Character.isHighSurrogate(s.charAt(i)) ) continue;	
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}
}
