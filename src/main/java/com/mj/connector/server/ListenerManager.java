package com.tsis.connector.server;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.async.AsyncClient;
import com.tsis.connector.client.async.NettyAsyncClient;
import com.tsis.connector.client.sync.NettySyncClient;
import com.tsis.connector.client.sync.SyncClient;
import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.server.async.NettyAsyncServer;

public class ListenerManager {
	protected static Logger log = LoggerFactory.getLogger(ListenerManager.class);

	public static String URI_FORMAT = "server:async://host:port?options";

	public static Server getServer(String uri) throws Exception {

		try {
			ConnectorConfiguration config = new ConnectorConfiguration(uri);

			log.debug("Create Listener isSync({})", config.isSync());

			if (!config.isClient()) {
				NettyAsyncServer server = new NettyAsyncServer(config);
				server.start();
				
				return server;
							} else {
				throw new URISyntaxException(uri, "잘못된 URI 형식. client로 설정 할수 없습니다. URI foramt["
						+ URI_FORMAT + "]");
			}

		} catch (URISyntaxException e) {
			throw new URISyntaxException(uri, "잘못된 URI 형식. URI foramt[" + URI_FORMAT + "]");
		} catch (StringIndexOutOfBoundsException e) {
			throw new StringIndexOutOfBoundsException("URI [ " + uri + "]은 잘못된 형식("
					+ e.getMessage() + "). URI format[" + URI_FORMAT + "]");
		}
	}
}
