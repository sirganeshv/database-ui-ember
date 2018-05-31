//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.transport.TransportAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.client.Client;
//import org.elasticsearch.client.transport.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.action.search.*; 
//import org.elasticsearch.transport.client.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
//import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.node.Node;	
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import java.util.*;
import java.net.InetAddress;
import java.io.*;
import java.util.regex.Pattern;  
import java.util.regex.Matcher; 
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.cluster.repositories.delete.DeleteRepositoryRequest;
import org.elasticsearch.snapshots.SnapshotInfo;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
//import org.elasticsearch.common.collect.ImmutableList;
//import org.elasticsearch.common.settings.ImmutableSettings;

/*import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.*;*/
import net.minidev.json.*; 
import net.minidev.json.parser.*;

public class ElasticClient {
	private Logger logger = LogManager.getLogger(DatabaseServlet.class.getName());
	public int insertLog(JSONObject json,int lastInsertedRecordID,Node node) {
		NodeClient client = (NodeClient)node.client();
		//TransportClient client = (TransportClient)node.client();
		IndexResponse response = null;
		String eventID;
		String eventProvider;
		String eventType;
		String timestamp;
		String securityID;
		String accountName;
		String accountDomain;
		String logonID;
		String message;
		JSONArray rows = (JSONArray)json.get("row");
		JSONArray cols = (JSONArray)json.get("col");
		//GetIndexRequest request = new GetIndexRequest();
		IndicesExistsRequest request = new IndicesExistsRequest();
		request.indices("logs");
		boolean exists = client.admin().indices().exists(request).actionGet().isExists();
		if(exists)
			client.admin().indices().open(new OpenIndexRequest("logs"));
		else
			client.admin().indices().create(new CreateIndexRequest("logs"));
		System.out.println("Opened");
		if(rows != null && rows.size() > 0) {
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
			}).setBulkActions(10000).build();
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
					/*securityID = String.valueOf(row.get("securityID"));
					accountName = String.valueOf(row.get("accountName"));
					accountDomain = String.valueOf(row.get("accountDomain"));
					logonID = String.valueOf(row.get("logonID"));*/	
					message = String.valueOf(row.get("message"));
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
					//Pattern pattern = Pattern.compile("^\\n[a-zA-Z\\s]+:\\s+\\S+\\n");
					XContentBuilder jsonStr = jsonBuilder()
									.startObject()
									.field("eventID", eventID)
									.field("eventProvider", eventProvider)
									.field("eventType", eventType)
									.field("timestamp",timestamp);
					StringBuilder keyValuePairs = new StringBuilder();
					Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s]+:\\t+[a-zA-Z0-9 ]+");
					Matcher matcher = pattern.matcher(message);
					boolean found = false;    
					while (matcher.find()) {    
						/*System.out.println("I found the text "+matcher.group()+" starting at index "+    
						matcher.start()+" and ending at index "+matcher.end());  */
						String parameter = matcher.group();
						String param[] = parameter.split(":");
						String key = param[0].trim();
						String value = param[1].trim();
						keyValuePairs.append(key).append(" : ").append(value).append(System.getProperty("line.separator"));
						found = true;    
					}    
					
					if(found == true)
						jsonStr.field("parsedFields",keyValuePairs);
					else
						jsonStr.field("parsedFields","-");
					bulkProcessor.add(new IndexRequest("logs", "log", String.valueOf(row.get("recordID")))
						.source(jsonStr.endObject()));
									/*.field("securityID",securityID)
									.field("accountName",accountName)
									.field("accountDomain",accountDomain)
									.field("logonID",logonID)*/
									/*.field("message",message)
								.endObject()));*/
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
	
	public long getTotalNumberOfRecords(int[] idList,String filterCol,String filterStr,Node node) {
		NodeClient client = (NodeClient)node.client();
		//TransportClient client = (TransportClient)node.client();
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

	
	public JSONObject search(int[] idList,String filterCol,String filterStr,String sortCol,Boolean isAscending,int paginatedBy,int start,Node node) {
		System.out.println("in search method");
		NodeClient client = (NodeClient)node.client();
		IndicesExistsRequest request = new IndicesExistsRequest();
		request.indices("logs");
		boolean exists = client.admin().indices().exists(request).actionGet().isExists();
		if(!exists)
			return null;
		//TransportClient client = (TransportClient)node.client();
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
				.setFrom(start).setSize(paginatedBy).setExplain(true)
				.get();
				
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
	
	public JSONObject searchEvents(int[] idList,String filterCol,String filterStr,String sortCol,Boolean isAscending,int start,int stop,Node node) {
		System.out.println("in search method");
		NodeClient client = (NodeClient)node.client();
		//TransportClient client = (TransportClient)node.client();
		String ids = String.valueOf(idList[0]);
		for(int i = 1;i < idList.length;i++)
			ids = ids + " " + idList[i];
		System.out.println(ids);
		try {
			JSONParser parser = new JSONParser();
			if(filterStr != null && !(filterStr.equals("")) && sortCol != null) {
				SearchResponse response = client.prepareSearch("logs")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(new BoolQueryBuilder().must(QueryBuilders.matchQuery("eventID",ids)).must(QueryBuilders.matchPhrasePrefixQuery(filterCol,filterStr)))
				.addSort(sortCol+".keyword",( isAscending ? SortOrder.ASC : SortOrder.DESC))
				.setPostFilter(QueryBuilders.rangeQuery("eventID").from(start).to(stop))
				.setSize(10000)
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
				.setPostFilter(QueryBuilders.rangeQuery("eventID").from(start).to(stop))
				.setSize(10000)
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
				.setPostFilter(QueryBuilders.rangeQuery("eventID").from(start).to(stop))
				.setSize(10000)
				.setExplain(true).get();
				
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
				.setPostFilter(QueryBuilders.rangeQuery("eventID").from(start).to(stop))
				.setSize(10000)
				.get();
				
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
	
	public void delete(int eventID,Node node) {
		NodeClient client = (NodeClient)node.client();
		BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
		.filter(QueryBuilders.matchQuery("eventID", String.valueOf(eventID))) 
		.source("logs")                                  
		.get();                                             
		long deleted = response.getDeleted();
	}
	
	
	public boolean isRepositoryExist(Client client, String repositoryName) {
		boolean result = false;
		try {
			List<RepositoryMetaData> repositories = client.admin().cluster().prepareGetRepositories().get().repositories();
			if(repositories.size() > 0){
				for(RepositoryMetaData repo :repositories)
					result = repositoryName.equals(repo.name())?true:false;
			}
		} catch (Exception ex){
			logger.error("Exception in getRepository method: " + ex.toString());
		} finally {
			return result;
		}
	}
	
	
	public void createRepository(Client client, String repositoryName,String path, boolean compress) {
		try {
			if(!isRepositoryExist(client, repositoryName)) {
			Settings settings = Settings.builder()
				.put("location", path + repositoryName)
				.put("compress", compress).build();
			client.admin().cluster().preparePutRepository(repositoryName)
				.setType("fs").setSettings(settings).get();
			logger.info("Repository was created.");
			} else {
				System.out.println("Repo Already exists");
				logger.info(repositoryName + " repository already exists");
			}
		} catch(Exception ex){
			logger.error("Exception in createRepository method: " + ex.toString());
		}
	}
	
	public void deleteRepository(Client client, String repositoryName){
		try {
			if (isRepositoryExist(client, repositoryName)) {
				client.admin().cluster().prepareDeleteRepository(repositoryName).execute().actionGet();
				logger.info(repositoryName + " repository has been deleted.");
			}
		} catch (Exception ex){
			logger.error("Exception in deleteRepository method: " + ex.toString());
		}
	}
	
	
	public boolean isSnapshotExist(Client client, String repositoryName, String snapshotName){
		boolean result = false;
		try {
			List<SnapshotInfo> snapshotInfo = client.admin().cluster().prepareGetSnapshots(repositoryName).get().getSnapshots();
			if(snapshotInfo.size() > 0){
				for(SnapshotInfo snapshot :snapshotInfo)
					result = snapshotName.equals(snapshot.snapshotId().getName())?true:false;
			}
		} catch (Exception ex) {
			logger.error("Exception in getSnapshot method: " + ex.toString());
		} finally {
			return result;
		}
	}
	
	public void archive(Node node) {
		NodeClient client = (NodeClient)node.client();
		String repositoryName = "mylogs";
		String snapshotName = "logs1";
		String indexName = "logs";
		if(!isRepositoryExist(client, repositoryName))
			createRepository(client,repositoryName,"D:\\ES Backup\\",true);
		if(isRepositoryExist(client, repositoryName) && isSnapshotExist(client,repositoryName,snapshotName))
			client.admin().cluster().deleteSnapshot(new DeleteSnapshotRequest(repositoryName, snapshotName));
			//client.admin().cluster().prepareDeleteSnapshot(repositoryName, snapshotName));
			//client.admin().cluster().snapshot().deleteRepositoryAsync(request, listener); 
		CreateSnapshotResponse createSnapshotResponse = null;
		try {
			Thread.sleep(1000);
			createSnapshotResponse = client.admin().cluster()
				.prepareCreateSnapshot(repositoryName, snapshotName)
				.setWaitForCompletion(true)
				.setIndices(indexName).get();
			System.out.println("Snapshot created");
			client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
			System.out.println("logs archived");
			logger.info("Snapshot was created.");
		} catch (Exception ex){
			logger.error("Exception in createSnapshot method: " + ex.toString());
		}
	}
	
	public void restore(Node node) {
		NodeClient client = (NodeClient)node.client();
		//CloseIndexRequest request = new CloseIndexRequest("logs"); 
		String repositoryName = "mylogs";
		String snapshotName = "logs1";
		RestoreSnapshotResponse restoreSnapshotResponse = null;
		try {
			//if(isRepositoryExist(client, repositoryName) && isSnapshotExist(client, repositoryName, snapshotName)){
			client.admin().indices().close(new CloseIndexRequest("logs"));
			//CloseIndexRequest Close_request = new CloseIndexRequest("logs"); 
			RestoreSnapshotRequest restoreSnapshotRequest = new RestoreSnapshotRequest(repositoryName, snapshotName);
			restoreSnapshotResponse = client.admin().cluster().restoreSnapshot(restoreSnapshotRequest).get();
			logger.info("Snapshot was restored.");
			//}
		} catch (Exception ex) {
		logger.error("Exception in restoreSnapshot method: " + ex.toString());
		}
	}
	
	/*public static void main(String[] args) {
		ElasticClient elasticClient = new ElasticClient();
		//new ElasticClient().insert(4,"mandu","Mandu from java");
		//elasticClient.get(3);
		//elasticClient.delete(1);
		elasticClient.search();
	}*/
}