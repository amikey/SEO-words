package crawl4j.corpus.amazon;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import crawl4j.corpus.CORPUSinfo;

public class CrawlDataManagement {

	private int totalProcessedPages;
	private long totalLinks;
	private long totalTextSize;
	private HttpSolrServer solr_server;
	private Map<String, CORPUSinfo> crawledContent = new HashMap<String, CORPUSinfo>();

	public CrawlDataManagement() {
		//		Properties props = new Properties();
		//		FileInputStream in = null;      
		//		try {
		//			in = new FileInputStream("database.properties");
		//			props.load(in);
		//		} catch (IOException ex) {
		//			Logger lgr = Logger.getLogger(BenchmarkingController.class.getName());
		//			lgr.log(Level.FATAL, ex.getMessage(), ex);
		//		} finally {
		//			try {
		//				if (in != null) {
		//					in.close();
		//				}
		//			} catch (IOException ex) {
		//				Logger lgr = Logger.getLogger(BenchmarkingController.class.getName());
		//				lgr.log(Level.FATAL, ex.getMessage(), ex);
		//			}
		//		}
		// the following properties have been identified
		//		String url = props.getProperty("db.url");
		//		String user = props.getProperty("db.user");
		//		String passwd = props.getProperty("db.passwd");

		//		try{
		//			con = DriverManager.getConnection(url, user, passwd);
		//			solr_server = new HttpSolrServer("http://localhost:8983/solr");
		//		} catch (Exception e){
		//			System.out.println("Error instantiating either database or solr server");
		//			e.printStackTrace();
		//		}
	}

	public int getTotalProcessedPages() {
		return totalProcessedPages;
	}

	public void setTotalProcessedPages(int totalProcessedPages) {
		this.totalProcessedPages = totalProcessedPages;
	}

	public void incProcessedPages() {
		this.totalProcessedPages++;
	}

	public long getTotalLinks() {
		return totalLinks;
	}

	public void setTotalLinks(long totalLinks) {
		this.totalLinks = totalLinks;
	}

	public long getTotalTextSize() {
		return totalTextSize;
	}

	public void setTotalTextSize(long totalTextSize) {
		this.totalTextSize = totalTextSize;
	}

	public void incTotalLinks(int count) {
		this.totalLinks += count;
	}

	public void incTotalTextSize(int count) {
		this.totalTextSize += count;
	}

	public void updateSolrData() {
		try{
			Iterator<Entry<String, CORPUSinfo>> it = crawledContent.entrySet().iterator();
			int local_counter = 0;
			if (it.hasNext()){
				local_counter++;
				do {
					local_counter ++;
					Map.Entry<String, CORPUSinfo> pairs = it.next();
					String url=(String)pairs.getKey();
					CORPUSinfo info = (CORPUSinfo)pairs.getValue();
					SolrInputDocument doc = new SolrInputDocument();
					doc.addField("id",url.replace("http://www-history.mcs.st-andrews.ac.uk/",""));
					doc.addField("url",url);
//					doc.addField("whole_text",info.getText());
//					doc.addField("title",info.getTitle());
//					doc.addField("links_size",info.getLinks_size());
//					doc.addField("links",info.getOut_links());
//					doc.addField("h1",info.getH1());
//					doc.addField("short_description",info.getShort_description());
//					doc.addField("birth_date",info.getBirth_date());
//					doc.addField("birth_place",info.getBirth_location());
//					doc.addField("death_date",info.getDeath_date());
//					doc.addField("death_place",info.getDeath_location());
					java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
					doc.addField("last_update", sqlDate.toString());	
					try{
						solr_server.add(doc);
					}catch (Exception e){
						System.out.println("Trouble inserting : "+url);
						e.printStackTrace();  
					}
				}while (it.hasNext());	
				solr_server.commit();
				System.out.println(Thread.currentThread()+"Committed " + local_counter + " updates");
			}
		} catch (Exception e){
			//System.out.println("Line already inserted : "+nb_lines);
			e.printStackTrace();  
		}
	}

	// we here perform upsert to keep up to date our crawl referential
	public void updateData(){
		updateSolrData();
		// clear cache
		crawledContent.clear();
	}

	public Map<String, CORPUSinfo> getCrawledContent() {
		return crawledContent;
	}

	public void setCrawledContent(Map<String, CORPUSinfo> crawledContent) {
		this.crawledContent = crawledContent;
	}
}