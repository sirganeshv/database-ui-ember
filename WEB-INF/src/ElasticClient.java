import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.client.*;
import org.elasticsearch.client.transport.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.action.search.*; 
import org.elasticsearch.transport.client.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import java.util.*;
import java.net.InetAddress;
import java.io.*;
import org.elasticsearch.node.Node;	
import org.elasticsearch.transport.netty4.*;
import org.elasticsearch.transport.*;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.node.InternalSettingsPreparer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.*;

public class ElasticClient {
		
	/*public Client connect() {
		try {
		/*Settings.Builder elasticsearchSettings =
                Settings.builder()
                        .put("http.enabled", "true") 
                        //.put("index.number_of_shards", "1")
                        .put("path.data", new File("E:", "data").getAbsolutePath()) 
                        .put("path.logs", new File("E:", "logs").getAbsolutePath()) 
                        //.put("path.work", new File("E:", "work").getAbsolutePath()) 
                        .put("path.home", "E:");
		Settings settings = elasticsearchSettings.build();*/
		/*Settings settings = Settings.builder()
			.put("cluster.name", "elasticsearch")
			.put("path.home", "E:")
			.build();*/
		/*Settings settings = 
				Settings.builder()
                    .put("transport.type", "netty4")
                    .put("http.type", "netty4")
                    .put("http.enabled", "true")
                    .put("path.home", "elasticsearch-data")
                    .build();*/
		
		// on startup
		
	/*	return new ElasticClient().elasticSearchTestNode().client();
		/*Node node = new Node(settings);
		node.start();
		return node.client();*/
		}
	/*	catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		/*NodeClient client = null;
		Settings settings = Settings.builder()
			.put("cluster.name", "elasticsearch").build();       
		
		try {
			client = new Node(settings);
		} catch (Exception uhe) {
			System.out.println("error");
		}
		
		return client;*/
	}
	
	/*public Node elasticSearchTestNode() throws NodeValidationException {
		Node node = new MyNode(
				Settings.builder()
						.put("transport.type", "netty4")
						.put("http.type", "netty4")
						.put("http.enabled", "true")
						.put("cluster.name", "elasticsearch")
						.put("path.home", "elasticsearch-data")
						//.put("path.home", "C:\\Users\\ganesh-pt1936\\Downloads\\elasticsearch-6.2.3")
						.build(),
				Arrays.asList(Netty4Plugin.class));
		node.start();
		return node;
	}

	private static class MyNode extends Node {
		public MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
			super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
		}
	}*/
		
	
	public void insert(int id,String name,String message) {
		try {
			NodeClient client = (NodeClient)new ElasticClient().connect();
			IndexResponse response = client.prepareIndex("twitter", "tweet", String.valueOf(id))
			.setSource(jsonBuilder()
						.startObject()
							.field("user", name)
							.field("postDate", new Date())
							.field("message", message)
						.endObject()
					  )
			.get();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public int insertLog(JSONObject json,int lastInsertedRecordID,Client client) {
		NodeClient client = (NodeClient)new ElasticClient().connect();
		IndexResponse response = null;
		String eventID;
		String eventProvider;
		String eventType;
		String timestamp;
		JSONArray rows = (JSONArray)json.get("row");
		JSONArray cols = (JSONArray)json.get("col");
		if(rows.size() > 0) {
			BulkProcessor bulkProcessor = BulkProcessor.builder(
				client,  
				new BulkProcessor.Listener() {
					@Override
					public void beforeBulk(long executionId,
										   BulkRequest request) {  } 

					@Override
					public void afterBulk(long executionId,
										  BulkRequest request,
										  BulkResponse response) { } 

					@Override
					public void afterBulk(long executionId,
										  BulkRequest request,
										  Throwable failure) { } 
			}).build();
			JSONObject firstRow = (JSONObject)rows.get(0);
			lastInsertedRecordID = Integer.parseInt(String.valueOf(firstRow.get("recordID")));
			System.out.println(lastInsertedRecordID);
			try {
				for(int i = 0;i < rows.size();i++) {
					JSONObject row = (JSONObject)rows.get(i);
					eventID = String.valueOf(row.get("eventID"));
					eventProvider = String.valueOf(row.get("eventProvider"));
					eventType = String.valueOf(row.get("eventType"));
					timestamp = String.valueOf(row.get("timestamp"));
					/*response = client.prepareIndex("logs", "log", String.valueOf(i))
					.setSource(jsonBuilder()
								.startObject()
									.field("eventID", eventID)
									.field("eventProvider", eventProvider)
									.field("eventType", eventType)
									.field("timestamp",timestamp)
								.endObject()
							  )
					.get();*/
					bulkProcessor.add(new IndexRequest("logs", "log", String.valueOf(row.get("recordID")))
						.source(jsonBuilder()
								.startObject()
									.field("eventID", eventID)
									.field("eventProvider", eventProvider)
									.field("eventType", eventType)
									.field("timestamp",timestamp)
								.endObject()));
				}
				bulkProcessor.flush();
				bulkProcessor.close();
			}
			
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return lastInsertedRecordID;
	}
	
	
	/*public void get(int id) {
		TransportClient client = (NodeClient)new ElasticClient().connect();
		GetResponse response = client.prepareGet("logs", "log", String.valueOf(id)).get();
		System.out.println(response);
		//return response;
	}
	
	
	public void delete(int id) {
		TransportClient client = new ElasticClient().connect();
		DeleteResponse response = client.prepareDelete("twitter", "tweet", String.valueOf(id)).get();
		System.out.println(response);
	}*/
	
	public long getTotalNumberOfRecords(int[] idList,String filterCol,String filterStr) {
		NodeClient client = (NodeClient)new ElasticClient().connect();
		String ids = String.valueOf(idList[0]);
		for(int i = 1;i < idList.length;i++)
			ids = ids + " " + idList[i];
		System.out.println("Total number of records are ");
		System.out.println(ids);
		if(filterStr != null && !filterStr.equals("")) {
			return client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)).must(QueryBuilders.matchPhrasePrefixQuery(filterCol,filterStr)))
				.setExplain(true)
				.get().getHits().getTotalHits();
		}
		else {
			return client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)))
				.setExplain(true)
				.get().getHits().getTotalHits();
		}
	}
		
		
	public JSONObject createJSONObject(JSONArray hits) {
		JSONObject json = new JSONObject();
		JSONArray cols = new JSONArray();
		JSONArray rows = new JSONArray();
		if(hits.size() > 0 ) {
			JSONObject firstHit = (JSONObject) hits.get(0);
			JSONObject firstSource = (JSONObject) firstHit.get("_source");
			Set keys = firstSource.keySet();
			Iterator a = keys.iterator();
			while(a.hasNext()) {
				String key = (String)a.next();
				cols.add(key);
			}
			json.put("col",cols);
			for(int i = 0; i < hits.size();i++) {
				JSONObject hit = (JSONObject) hits.get(i);
				JSONObject source = (JSONObject) hit.get("_source");
				rows.add(source);
			}
			json.put("row",rows);
		}
		return json;
	}

	
	public JSONObject search(int[] idList,String filterCol,String filterStr,String sortCol,Boolean isAscending,int paginatedBy,int start) {
		System.out.println("in search method");
		NodeClient client = (NodeClient)new ElasticClient().connect();
		String ids = String.valueOf(idList[0]);
		for(int i = 1;i < idList.length;i++)
			ids = ids + " " + idList[i];
		System.out.println(ids);
		try {
			JSONParser parser = new JSONParser();
			if(filterStr != null && !(filterStr.equals("")) && sortCol != null) {
				SearchResponse response = client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				//.setQuery(QueryBuilders.matchQuery("eventID",ids))
				//.setQuery(QueryBuilders.matchQuery("eventID", "105 1"))
				//.setQuery(QueryBuilders.queryStringQuery(filterStr))
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)).must(QueryBuilders.matchPhrasePrefixQuery(filterCol,filterStr)))
				//.setQuery(QueryBuilders.fuzzy(filterStr))
				//QueryBuilders.fuzzyQuery("name", "kimzhy");
				//.setQuery(QueryBuilders.matchPhrasePrefixQuery("eventID", filterStr))
				//.setQuery(QueryBuilders.matchQuery("lastname", "Harding"))		// Query
				//.setPostFilter(QueryBuilders.rangeQuery("age").from(30).to(40))     // Filter
				.addSort(sortCol+".keyword",( isAscending ? SortOrder.ASC : SortOrder.DESC))
				.setFrom(start)
				.setSize(paginatedBy)
				.setExplain(true)
				.get();
				JSONObject resp = (JSONObject) parser.parse(response.toString());
				JSONObject hitss = (JSONObject) resp.get("hits");
				JSONArray hits = (JSONArray) hitss.get("hits");
				return createJSONObject(hits);
			}
			else if(filterStr != null && !(filterStr.equals(""))) {
				SearchResponse response = client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)).must(QueryBuilders.matchPhrasePrefixQuery(filterCol,filterStr)))
				.setFrom(start).setSize(paginatedBy)
				.setExplain(true).get();
				JSONObject resp = (JSONObject) parser.parse(response.toString());
				JSONObject hitss = (JSONObject) resp.get("hits");
				JSONArray hits = (JSONArray) hitss.get("hits");
				return createJSONObject(hits);

			}			
			else if(sortCol != null) {
				SearchResponse response = client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)))
				.addSort(sortCol+".keyword",( isAscending ? SortOrder.ASC : SortOrder.DESC))
				.setFrom(start).setSize(paginatedBy).setExplain(true).get();
				
				JSONObject resp = (JSONObject) parser.parse(response.toString());
				JSONObject hitss = (JSONObject) resp.get("hits");
				JSONArray hits = (JSONArray) hitss.get("hits");
				return createJSONObject(hits);
			}
			else {
				System.out.println("into the search area");
				SearchResponse response = client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)))
				.setFrom(start).setSize(paginatedBy).setExplain(true).get();
				
				JSONObject resp = (JSONObject) parser.parse(response.toString());
				JSONObject hitss = (JSONObject) resp.get("hits");
				JSONArray hits = (JSONArray) hitss.get("hits");
				return createJSONObject(hits);
				
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	/*public static void main(String[] args) {
		ElasticClient elasticClient = new ElasticClient();
		//new ElasticClient().insert(4,"mandu","Mandu from java");
		//elasticClient.get(3);
		//elasticClient.delete(1);
		elasticClient.search();
	}*/
}