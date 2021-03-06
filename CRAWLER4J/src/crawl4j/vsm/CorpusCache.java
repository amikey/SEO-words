package crawl4j.vsm;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CorpusCache {
	private static String database_con_path = "/home/sduprey/My_Data/My_Postgre_Conf/crawler4j.properties";
	private static String select_totalcount_statement="select nb_total_documents from CORPUS_WORDS_METADATA where thema='TOTAL_NUMBER_DOCUMENTS'";
	private static String select_word_statement="select word, nb_documents from CORPUS_WORDS";

	private static Map<String, Double> corpus_idf = new HashMap<String, Double>();
	private static int nb_total_documents = 1;
	private static int nb_semantic_hits_threshold = 20;

	private static String semantic_hit_separator = " ";

	public static void load(){
		Connection con = null;
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
		try{
			con = DriverManager.getConnection(url, user, passwd);
			// getting all words from the corpus table
			PreparedStatement pst_total = con.prepareStatement(select_totalcount_statement);
			ResultSet rs_total = pst_total.executeQuery();

			if (rs_total.next()) {
				nb_total_documents = rs_total.getInt(1);	
				System.out.println("Total number of documents found");
			}	
			pst_total.close();

			// getting all words from the corpus table
			PreparedStatement pst = con.prepareStatement(select_word_statement);
			ResultSet rs = pst.executeQuery();
			int counter=0;
			while (rs.next()) {
				counter++;
				String word = rs.getString(1);
				int nb_document = rs.getInt(2);
				Double idf = Math.log10((double)nb_total_documents/(double)nb_document);
				corpus_idf.put(word, idf);
				System.out.println("Appending "+counter+" word "+word + " with idf : " +idf);
			}	
		} catch (Exception e){
			System.out.println("Error instantiating either database or solr server");
			e.printStackTrace();
		}
	}

	public static Double getIDF(String word){
		Double idf = corpus_idf.get(word);
		if (idf == null){
			System.out.println("Warning Warning Warning the word is not in the corpus for word : "+word);
			System.out.println("We act as if it were found just once for word : "+word);
			idf = (double)nb_total_documents;
		}
		return idf;
	}

	public static Map<String, Integer> computePageTFVector(String text){
		return RemoveStopWordsUtility.removeStopWords(text);
	}

	public static Map<String, Double> computePageTFIDFVector(String pageText){
		Map<String,Integer> cleanedTfMap = cleanTFMap(computePageTFVector(pageText));
		Map<String, Double> tfidfMap = addTFIDF(cleanedTfMap);
		return tfidfMap;
	}

	public static Double computeTFSIDFimilarity(String text1, String text2) {
		if (("".equals(text1))&&("".equals(text2))){
			return (double) 1;
		}
		if (("".equals(text1))&&(!"".equals(text2))){
			return (double) 0;
		}
		if ((!"".equals(text1))&&("".equals(text2))){
			return (double) 0;
		}
		Map<String, Double> firstMap = addTFIDF(computePageTFVector(text1));
		Map<String, Double> secondMap = addTFIDF(computePageTFVector(text2));
		if ((firstMap.size() == 0) && secondMap.size() ==0){
			return (double) 1;
		}
		if ((firstMap.size() != 0) && secondMap.size() ==0){
			return (double) 0;
		}
		if ((firstMap.size() == 0) && secondMap.size() !=0){
			return (double) 0;
		}
		return cosine_tfidfsimilarity(firstMap,secondMap);
	}

	public static Map<String, Integer> cleanTFMap(Map<String, Integer> to_clean){
		Iterator<Entry<String, Integer>> clean_it = to_clean.entrySet().iterator();
		while (clean_it.hasNext()) {
			Map.Entry<String, Integer> pairs = clean_it.next();
			String word=pairs.getKey();
			if (word.matches(".*\\d+.*")){
				clean_it.remove();
			}
		}
		return to_clean;
	}

	public static Map<String, Double> addTFIDF(Map<String, Integer> v1){
		Map<String, Double> output = new HashMap<String, Double>();
		Iterator<Entry<String, Integer>> it = v1.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> pairs = it.next();
			String word=pairs.getKey();
			Integer count = pairs.getValue();
			Double wordIdf = getIDF(word);
			output.put(word, count*wordIdf);
		}	
		return output;
	}

	public static Double computeTFSimilarity(String text1, String text2) {
		if (("".equals(text1))&&("".equals(text2))){
			return (double) 1;
		}
		if (("".equals(text1))&&(!"".equals(text2))){
			return (double) 0;
		}
		if ((!"".equals(text1))&&("".equals(text2))){
			return (double) 0;
		}
		Map<String, Integer> map1 = computePageTFVector(text1);
		Map<String, Integer> map2 = computePageTFVector(text2);

		if ((map1.size() == 0) && map2.size() ==0){
			return (double) 1;
		}
		if ((map1.size() != 0) && map2.size() ==0){
			return (double) 0;
		}
		if ((map1.size() == 0) && map2.size() !=0){
			return (double) 0;
		}
		return cosine_tfsimilarity(map1 , map2);
	}

	public static double cosine_tfidfsimilarity(Map<String, Double> v1, Map<String, Double> v2) {
		Set<String> both = new HashSet<String>(v1.keySet());
		both.retainAll(v2.keySet());
		double sclar = 0, norm1 = 0, norm2 = 0;
		for (String k : both) sclar += v1.get(k) * v2.get(k);
		for (String k : v1.keySet()) norm1 += v1.get(k) * v1.get(k);
		for (String k : v2.keySet()) norm2 += v2.get(k) * v2.get(k);
		return sclar / Math.sqrt(norm1 * norm2);
	}

	public static double cosine_tfsimilarity(Map<String, Integer> v1, Map<String, Integer> v2) {
		Set<String> both = new HashSet<String>(v1.keySet());
		both.retainAll(v2.keySet());
		double sclar = 0, norm1 = 0, norm2 = 0;
		for (String k : both) sclar += v1.get(k) * v2.get(k);
		for (String k : v1.keySet()) norm1 += v1.get(k) * v1.get(k);
		for (String k : v2.keySet()) norm2 += v2.get(k) * v2.get(k);
		return sclar / Math.sqrt(norm1 * norm2);
	}

	public static String formatTFIDFMapWithWeights(Map<String, Double> tfIdfMap){
		Map<String, Double> tfIdfMapSortedMap = sortByValueDescendingly( tfIdfMap );
		return tfIdfMapSortedMap.toString();
	}

	public static String formatTFIDFMapBestHits(Map<String, Double> tfIdfMap){
		Map<String, Double> tfIdfMapSortedMap = sortByValueDescendingly( tfIdfMap );
		String[] orderedKeysTenHits=getOrderedKeysBestHits(tfIdfMapSortedMap);
		return StringUtils.join(orderedKeysTenHits,semantic_hit_separator);
	}

	public static String formatTFIDFMapBestHitsJSON(Map<String, Double> tfIdfMap){
		Map<String, Double> tfIdfMapSortedMap = sortByValueDescendingly( tfIdfMap );
		String orderedKeysTenHits=getOrderedKeysBestHitsJSON(tfIdfMapSortedMap);
		return orderedKeysTenHits;
	}

	@SuppressWarnings("unchecked")
	public static String formatTFMapJSON(Map<String, Integer> tfMap){
		String[] keys=getKeys(tfMap);
		JSONArray termsArray = new JSONArray();
		for (String key : keys){
			termsArray.add(key);
		}
		return termsArray.toJSONString();
	}

	public static String formatTFMap(Map<String, Integer> tfMap){
		String[] keys=getKeys(tfMap);
		return StringUtils.join(keys,semantic_hit_separator);
	}

	public static String formatTFIDFMap(Map<String, Double> tfIdfMap){
		Map<String, Double> tfIdfMapSortedMap = sortByValueDescendingly( tfIdfMap );
		String[] orderedKeys=getKeys(tfIdfMapSortedMap);
		return StringUtils.join(orderedKeys,semantic_hit_separator);
	}

	@SuppressWarnings("unchecked")
	public static String getOrderedKeysBestHitsJSON(Map<String, Double> tfIdfMapSortedMap){
		JSONArray tfidfsArray = new JSONArray();
		if (tfIdfMapSortedMap.size() <= nb_semantic_hits_threshold){
			Iterator<Entry<String, Double>> it = tfIdfMapSortedMap.entrySet().iterator();
			// we go forward as the Map has been sorted descending
			while (it.hasNext()){
				Map.Entry<String, Double> pairs = (Map.Entry<String, Double>)it.next();
				JSONObject tfidfObject = new JSONObject();
				tfidfObject.put("term", pairs.getKey());
				tfidfObject.put("frequency", pairs.getValue());
				tfidfsArray.add(tfidfObject);
			}
		} else {
			// we limit the number of hits up to ten
			Iterator<Entry<String, Double>> it = tfIdfMapSortedMap.entrySet().iterator();
			// we go forward as the Map has been sorted descending
			int counter = 0;
			while (it.hasNext() && counter <nb_semantic_hits_threshold){
				Map.Entry<String, Double> pairs = (Map.Entry<String, Double>)it.next();
				JSONObject tfidfObject = new JSONObject();
				tfidfObject.put("term", pairs.getKey());
				tfidfObject.put("frequency", pairs.getValue());
				tfidfsArray.add(tfidfObject);
				counter ++;
			}
		}
		return tfidfsArray.toJSONString();
	}

	public static String[] getOrderedKeysBestHits(Map<String, Double> tfIdfMapSortedMap){
		String[] ordered_keys =null;
		if (tfIdfMapSortedMap.size() <= nb_semantic_hits_threshold){
			ordered_keys = new String[tfIdfMapSortedMap.size()];
			Iterator<Entry<String, Double>> it = tfIdfMapSortedMap.entrySet().iterator();
			// we go forward as the Map has been sorted descending
			int counter = 0;
			while (it.hasNext()){
				Map.Entry<String, Double> pairs = (Map.Entry<String, Double>)it.next();
				ordered_keys[counter] = pairs.getKey();
				counter ++;
			}
		} else {
			// we limit the number of hits up to ten
			ordered_keys = new String[nb_semantic_hits_threshold];
			Iterator<Entry<String, Double>> it = tfIdfMapSortedMap.entrySet().iterator();
			// we go forward as the Map has been sorted descending
			int counter = 0;
			while (it.hasNext() && counter <nb_semantic_hits_threshold){
				Map.Entry<String, Double> pairs = (Map.Entry<String, Double>)it.next();
				ordered_keys[counter] = pairs.getKey();
				counter ++;
			}
		}
		return ordered_keys;
	}

	public static <V> String[] getKeys(Map<String, V> tfIdfMapSortedMap){
		String[] ordered_keys = new String[tfIdfMapSortedMap.size()];
		Iterator<Entry<String, V>> it = tfIdfMapSortedMap.entrySet().iterator();
		// we go forward as the Map has been sorted descending
		int counter = 0;
		while (it.hasNext()){
			Map.Entry<String, V> pairs = (Map.Entry<String, V>)it.next();
			ordered_keys[counter] = pairs.getKey();
			counter ++;
		}
		return ordered_keys;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescendingly( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				// we sort the Map descendingly
				int toreturn =-(  o1.getValue()).compareTo( o2.getValue() );
				return toreturn;
			}
				});
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscendingly( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				// we sort the Map ascendingly
				int toreturn =(o1.getValue()).compareTo( o2.getValue() );
				return toreturn;
			}
				});
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}
}
