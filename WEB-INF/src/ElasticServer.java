import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;	
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.client.Client;

public class ElasticServer {
	static Node node;

	public static Node getNode() throws NodeValidationException {
		/*Arrays.asList(Netty4Plugin.class));*/
		node = new Node(
			Settings.builder()
			.put("transport.type", "netty4")
			.put("http.type", "netty4")
			//.put("http.enabled", "false")
			.put("cluster.name", "elasticsearch")
			//.put("path.home", "elasticsearch-data")
			.put("path.home", "C:\\Users\\ganesh-pt1936\\Downloads\\elasticsearch-6.2.3\\")
			.build());
		node.start();
		return node;
	}
	
	
}