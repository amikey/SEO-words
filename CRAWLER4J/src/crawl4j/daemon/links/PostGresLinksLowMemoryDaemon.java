package crawl4j.daemon.links;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.url.WebURL;

public class PostGresLinksLowMemoryDaemon {
	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/crawler4j.properties";
	private static Map<String, Integer> node_locator = new HashMap<String, Integer>(); 
	private static Map<String, Set<String>> in_links_map = new HashMap<String, Set<String>>();
	private static Map<String, Set<String>> out_links_map = new HashMap<String, Set<String>>();
	private static int counter = 0;
	private static int insertion_counter = 0;
	private static String fetching_request = "SELECT URL, LINKS FROM CRAWL_RESULTS WHERE DEPTH >0 ORDER BY DEPTH LIMIT 1000000";
	private static String update_statement ="UPDATE CRAWL_RESULTS SET IN_LINKS_SIZE=?, IN_LINKS=? WHERE URL=?";

	private static Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|ico|png|tiff?|mid|mp2|mp3|mp4"
			+ "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private static String find_node_statement ="SELECT ID FROM NODES WHERE URL=?";
	private static String insert_node_statement ="INSERT INTO NODES (LABEL)"
			+ " VALUES(?)";
	private static String insert_relation_statement ="INSERT INTO EDGES (SOURCE, TARGET)"
			+ " VALUES(?,?)";
	private static String beginning_string = "^\\s*http://([a-z0-9]*\\.)*www.cdiscount.com.*";

	private static Connection con; 

	public static void main(String[] args){

		try {
			instantiate_connection();
		} catch (SQLException e1) {
			e1.printStackTrace();
			System.out.println("Trouble with the POSTGRESQL database");
			System.exit(0);
		}
		try{
			// fetching data from the Postgresql data base and looping over
			looping_over_urls();
		} catch (SQLException e){
			e.printStackTrace();
			System.out.println("Trouble with the POSTGRESQL database");
			System.exit(0);
		}
		//		// we can save content in the crawl_results database
		//		//		savingIncomingLinks();
		//		// creating the relationships in neo4j		
		//		building_relationships();
	}

	private static void instantiate_connection() throws SQLException{
		// instantiating database connection
		// instantiating database connection
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
		con = DriverManager.getConnection(url, user, passwd);
	}

	private static void savingIncomingLinks(){
		System.out.println("Saving the incoming links in the POSTGRESQL database \n");
		Iterator it = in_links_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String url =(String)pairs.getKey();
			Set<String> incoming_links = (Set<String>)pairs.getValue();
			System.out.println("Incoming links : "+incoming_links);
			try {
				insert_links_row(url,incoming_links);
			} catch (SQLException e) {
				System.out.println("Trouble inserting URL : "+url);
			}
		}	
	}

	private static void insert_links_row(String url,Set<String> incoming_links) throws SQLException{
		insertion_counter++;
		System.out.println("Inserting URL number " + insertion_counter + url);
		PreparedStatement st = con.prepareStatement(update_statement);
		// preparing the statement
		st.setInt(1,incoming_links.size());
		st.setString(2,incoming_links.toString());
		st.setString(3,url);
		st.executeUpdate();
	}

	public static void looping_over_urls() throws SQLException{
		// here is the links daemon starting point
		// getting all URLS and out.println links for each URL
		System.out.println("Getting all URLs and outside links from the crawl results database");
		PreparedStatement pst = con.prepareStatement(fetching_request);
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {
			counter++;
			String url_node = rs.getString(1);
			String output_links = rs.getString(2);
			System.out.println("Dealing with URL number :"+counter + " : " +url_node);
			manage_input(url_node,output_links);
		}
	}

	public static void manage_input(String url_node, String output_links){
		// creating the nodes in neo4j
		try {
			create_node(url_node);
			Set<String> parsed_output = parse_nodes_out_links_and_create(output_links);
			relations_insertion(url_node,parsed_output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Trouble creating node : "+url_node);
			e.printStackTrace();
		}
		//		// populating the outgoing map		
		//		populate_out_links(url_node, output_links);		
		//		// populating the incoming map
		//		populate_in_links(url_node, output_links);
	}

	private static Set<String> parse_nodes_out_links_and_create(String output_links) throws SQLException{
		output_links = output_links.replace("[", "");
		output_links = output_links.replace("]", "");
		String[] url_outs = output_links.split(",");
		Set<String> outputSet = new HashSet<String>();
		for (String url_out : url_outs){
			WebURL web_url = new WebURL();
			web_url.setURL(url_out);
			if ((shouldVisit(url_out))){
				url_out=url_out.trim();
				create_node(url_out);
				outputSet.add(url_out);
			}
		}
		return outputSet;
	}

	private static boolean shouldVisit(String url_out) {
		String href = url_out.toLowerCase();
		return !filters.matcher(href).matches() && href.matches(beginning_string);
	}

	private static void building_relationships() throws SQLException{
		System.out.println("Inserting relationships in the Neo4j graph db \n");
		Iterator it = out_links_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String url =(String)pairs.getKey();
			Set<String> outgoing_links = (Set<String>)pairs.getValue();
			System.out.println("Outgoing links : "+outgoing_links);

			relations_insertion(url,outgoing_links);

		}	
	}

	private static void relations_insertion(String url,Set<String> outgoing_links) throws SQLException{
		int total_size = outgoing_links.size();
		int local_counter = 0;
		Integer beginningNode = node_locator.get(url);
		for (String ending_Node_URL : outgoing_links){
			Integer endingNode = node_locator.get(ending_Node_URL);
			if (endingNode != null && !(beginningNode.equals(endingNode))){			
				System.out.println(" Beginning node : " + beginningNode);
				System.out.println(" Ending node : "+endingNode);
				//URI relationshipUri = addRelationship( beginningNode, endingNode, "link","{}");
				//System.out.println("First relationship URI : "+relationshipUri);
				createRelationShip(beginningNode, endingNode);
				local_counter++;
			} else {
				System.out.println("Trouble with url : "+url);
				System.out.println("One node has not been found : "+url+total_size);
			}
		}
		System.out.println("Having inserted "+local_counter+" over "+total_size);
	}

	private static void createRelationShip(Integer beginningNode, Integer endingNode) throws SQLException{
		PreparedStatement insert_st = con.prepareStatement(insert_relation_statement);
		insert_st.setInt(1, beginningNode);
		insert_st.setInt(2,endingNode);
		insert_st.executeUpdate();
	}

	private static void populate_out_links(String url_node, String output_links){
		// we here compute the output links
		output_links = output_links.replace("[", "");
		output_links = output_links.replace("]", "");
		String[] url_outs = output_links.split(",");
		Set<String> outputSet = new HashSet<String>();
		for (String url_out : url_outs){
			try {
				create_node(url_out);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Trouble creating the node : "+url_node);
			}
			outputSet.add(url_out);
		}
		out_links_map.put(url_node, outputSet);
	}

	//	private static void populate_in_links(String url_node, String output_links){
	//		// we here compute the output links
	//		output_links = output_links.replace("[", "");
	//		output_links = output_links.replace("]", "");
	//		String[] url_outs = output_links.split(",");
	//		for (String url_out : url_outs){
	//			Set<String> incomingSet = in_links_map.get(url_out);
	//			if ( incomingSet == null){
	//				// we don't have any entries yes
	//				incomingSet  = new HashSet<String>();
	//				in_links_map.put(url_out, incomingSet);
	//			}
	//			// we add the currently parsed URL
	//			incomingSet.add(url_node);
	//		}
	//	}

	private static Integer find_node(String url_to_search) throws SQLException{
		Integer found_id = null;
		PreparedStatement pst = con.prepareStatement(find_node_statement);
		pst.setString(1, url_to_search);
		ResultSet rs = pst.executeQuery();
		if (rs.next()) {
			found_id = rs.getInt(1);
		}
		return found_id;
	}

	private static void create_node(String url_node) throws SQLException{
		Integer potential_id = find_node(url_node);
		// the node is not present in the database, we create it
		if (potential_id == null){
			PreparedStatement insert_st = con.prepareStatement(insert_node_statement,Statement.RETURN_GENERATED_KEYS);
			//insert_st.setString(1,URL_Utilities.checkMagasin(url_node));
			insert_st.setString(1,url_node);
			insert_st.executeUpdate();
			ResultSet rs = insert_st.getGeneratedKeys();
			int inserted_keys=0;
			if (rs != null && rs.next()) {
				inserted_keys = rs.getInt(1);
			}
			node_locator.put(url_node, inserted_keys);
		} else {
			// the node is already in the database, we just refresh the cache
			node_locator.put(url_node,potential_id);
		}
	}
}
