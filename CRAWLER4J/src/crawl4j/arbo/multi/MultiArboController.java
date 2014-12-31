package crawl4j.arbo.multi;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import crawl4j.urlutilities.MultiArboInfo;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class MultiArboController {
	// here we locally merge all cache
	// that is heavy on RAM memory : we here have to limit the depth to avoid out of memory
	// only shallow crawl will go through this step
	// counting the number of inlinks forces us to wait for the very end
	// of the crawl before we update the database	
	private static Map<String, Set<String>> inlinks_cache = new HashMap<String, Set<String>>();
	private static Connection con;
	
	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/crawler4j.properties";

	private static String insert_statement_with_label="INSERT INTO ARBOCRAWL_RESULTS (URL, WHOLE_TEXT, TITLE, H1, SHORT_DESCRIPTION, STATUS_CODE, DEPTH,"
			+ " OUTLINKS_SIZE, INLINKS_SIZE, NB_BREADCRUMBS, NB_AGGREGATED_RATINGS, NB_RATINGS_VALUES, NB_PRICES, NB_AVAILABILITIES, NB_REVIEWS, NB_REVIEWS_COUNT, NB_IMAGES, PAGE_TYPE, LAST_UPDATE)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static void main(String[] args) throws Exception {
		
		instantiate_connection();
		
		System.setProperty("http.agent", "");
		System.out.println("Starting the crawl configuration");		
		String seed = "http://www.cdiscount.com/";
		// we here launch just a few threads, enough for a shallow crawl
		// maximum twenty otherwise the concurrent update of the Map might get really too slow
		// and become a bottleneck rather than a 
		int numberOfCrawlers =  20;	
		// downsizing to test
		//int numberOfCrawlers =  1;
		if (args.length == 2) {
			seed = args[0];
			numberOfCrawlers=Integer.valueOf(args[1]);
		} 
		String rootFolder = "/home/sduprey/My_Data/My_Multi_Arbo_Crawl4j";
		String user_agent_name = "CdiscountBot-crawler";
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(rootFolder);
		config.setUserAgentString(user_agent_name);
		// Politeness delay : none by default
		config.setPolitenessDelay(0);
		// Unlimited number of pages can be crawled.
		config.setMaxPagesToFetch(-1);
		// we crawl up to depth 5
		// to get the navigation we only need to go up to depth 5
		int maxDepthOfCrawling =  1;        
		config.setMaxDepthOfCrawling(maxDepthOfCrawling);
		// we want the crawl not to be reconfigurable : too slow otherwise
		config.setResumableCrawling(false);
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setUserAgentName(user_agent_name);
		// we respect the text robot
		robotstxtConfig.setEnabled(true);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		controller.addSeed(seed);
		System.out.println("Starting the crawl");
		long startTime = System.currentTimeMillis();
		controller.start(MultiArboCrawler.class, numberOfCrawlers);
		long estimatedTime = System.currentTimeMillis() - startTime;
		List<Object> crawlersLocalData = controller.getCrawlersLocalData();

		// listing the work done by each thread
		long totalLinks = 0;
		long totalTextSize = 0;
		int totalProcessedPages = 0;
		for (Object localData : crawlersLocalData) {
			MultiArboCrawlDataCache stat = (MultiArboCrawlDataCache) localData;
			totalLinks += stat.getTotalLinks();
			totalTextSize += stat.getTotalTextSize();
			totalProcessedPages += stat.getTotalProcessedPages();
		}
		System.out.println("Aggregated Statistics:");
		System.out.println(" Processed Pages: " + totalProcessedPages);
		System.out.println(" Total Links found: " + totalLinks);
		System.out.println(" Total Text Size: " + totalTextSize);
		System.out.println(" Estimated time (ms): " + estimatedTime);

		// computing the number of inlinks per pages over the whole crawl
		System.out.println("Computing inlinks hashmap cache to the database");
		for (Object localData : crawlersLocalData) {
			MultiArboCrawlDataCache stat = (MultiArboCrawlDataCache) localData;
			Map<String, MultiArboInfo> local_thread_cache = stat.getCrawledContent();
			updateInLinksThreadCache(local_thread_cache);
		}

		// saving results to the database
		System.out.println("Saving the whole crawl to the database");		
		System.out.println("Saving inlinks hashmap to the database");
		for (Object localData : crawlersLocalData) {
			MultiArboCrawlDataCache stat = (MultiArboCrawlDataCache) localData;
			Map<String, MultiArboInfo> local_thread_cache = stat.getCrawledContent();
			saveDatabaseData(local_thread_cache);
		}
	}

	public static void updateInLinksThreadCache(Map<String, MultiArboInfo> local_thread_cache){
		Iterator<Map.Entry<String, MultiArboInfo>>  it = local_thread_cache.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, MultiArboInfo> pairs = it.next();
			String url = pairs.getKey();
			MultiArboInfo info = pairs.getValue();
			Set<String> outgoingLinks = info.getOutgoingLinks();
			if (outgoingLinks != null){
				updateInLinks(outgoingLinks,url);
			} else {
				System.out.println(" No outgoing links for this URL : "+url);
				System.out.println(" Status code : "+info.getStatus_code());
			}
		}
	}

	public static void updateInLinks(Set<String> outputSet, String sourceURL){
		for (String targetURL : outputSet){
			Set<String> inLinks = inlinks_cache.get(targetURL);
			if (inLinks == null){
				inLinks= new HashSet<String>();
			}
			inLinks.add(sourceURL);
			inlinks_cache.put(targetURL,inLinks);
		}
	}

	public static void saveDatabaseData(Map<String, MultiArboInfo> local_thread_cache){
		try{
			Iterator<Entry<String, MultiArboInfo>> it = local_thread_cache.entrySet().iterator();
			con.setAutoCommit(false);
			PreparedStatement st = con.prepareStatement(insert_statement_with_label);
			int local_counter = 0;
			if (it.hasNext()){
				do {
					local_counter ++;
					//				PreparedStatement st = con.prepareStatement(insert_statement);
					Map.Entry<String, MultiArboInfo> pairs = (Map.Entry<String, MultiArboInfo>)it.next();
					String url=pairs.getKey();
					MultiArboInfo info = pairs.getValue();
					//(URL, WHOLE_TEXT, TITLE, H1, SHORT_DESCRIPTION, STATUS_CODE, DEPTH, OUTLINKS_SIZE, INLINKS_SIZE, NB_BREADCRUMBS, NB_AGGREGATED_RATINGS, NB_RATINGS_VALUES, NB_PRICES, NB_AVAILABILITIES, NB_REVIEWS, NB_REVIEWS_COUNT, NB_IMAGES, LAST_UPDATE)"
					//  1        2        3    4           5                6        7           8              9             10               11                      12            13              14             15            16             17         18       
					st.setString(1,url);
					st.setString(2,info.getText());
					st.setString(3,info.getTitle());
					st.setString(4,info.getH1());
					st.setString(5,info.getShort_desc());
					st.setInt(6,info.getStatus_code());
					st.setInt(7,info.getDepth());
					st.setInt(8,info.getLinks_size());
					Integer nb_inlinks = 0;
					Set<String> inlinksURL = inlinks_cache.get(url);
					if ( inlinksURL != null){
						nb_inlinks = inlinks_cache.get(url).size();
					}
					st.setInt(9,nb_inlinks);
					st.setInt(10,info.getNb_breadcrumbs());
					st.setInt(11,info.getNb_aggregated_rating());
					st.setInt(12,info.getNb_ratings());
					st.setInt(13,info.getNb_prices());
					st.setInt(14,info.getNb_availabilities());
					st.setInt(15,info.getNb_reviews());
					st.setInt(16,info.getNb_reviews_count());
					st.setInt(17,info.getNb_images());
					st.setString(18,info.getPage_type());
					java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
					st.setDate(19,sqlDate);
					//					st.executeUpdate();
					st.addBatch();
				}while (it.hasNext());	
				st.executeBatch();		 
				con.commit();
				System.out.println(Thread.currentThread()+"Committed " + local_counter + " updates");
			}
		} catch (SQLException e){
			e.printStackTrace();  
			if (con != null) {
				try {
					con.rollback();
				} catch (SQLException ex1) {
					ex1.printStackTrace();
				}
			}
		}		
	}
	
	public static void instantiate_connection() throws SQLException{
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
}