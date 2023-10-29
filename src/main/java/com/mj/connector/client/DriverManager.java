package com.tsis.connector.client;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.async.AsyncClient;
import com.tsis.connector.client.async.NettyAsyncClient;
import com.tsis.connector.client.http.HttpClient;
import com.tsis.connector.client.http.NettyHttpClient;
import com.tsis.connector.client.sync.NettySyncClient;
import com.tsis.connector.client.sync.SyncClient;
import com.tsis.connector.common.ConnectorConfiguration;

/**
 * Client Connector를 생성. <br>
 * 설정 정보에 따라 synchronous client 또는 asynchronous client를 생성 할 수 있다.<br>
 * <br>
 * 설정정보
 *  - sync client 설정 : client:<b>sync</b>://ip:port?option <br>
 *  - async client 설정 : client:<b>async</b>://ip:port?option <br>
 * <br>
 * 
 * 
 * <pre>
 * 사용방법 : 
 *  SyncClient client = null;
	try {
		//client 설정
		client = (SyncClient)DriverManager.getClient("client:sync://127.0.0.1:30001?readSize=10&disconnect=true");
		
		//데이터 송수신
		byte[] response = client.write("0123456789".getBytes());
		
	} finally {
		if(client != null) {
			client.stop();
		}
	}
 * </pre>
 * @author jin
 *
 */
public class DriverManager {

	protected static Logger log = LoggerFactory.getLogger(DriverManager.class);

	public static String URI_FORMAT = "client:[sync|async]://host:port?options";

	public static Client getClient(String uri) throws Exception {

		try {
			ConnectorConfiguration config = new ConnectorConfiguration(uri);

			log.debug("Create Client isSync({})", config.isSync());

			if (config.isClient()) {
				if (config.isSync()) {
					if(config.isHttp()) {
						// HTTP 방식은 연결방식의 표준에 따라 disconnect 는 true임
						config.setDisconnect(true); 
						HttpClient client = new NettyHttpClient(config);
						client.start();
	
						return client;
					} else {
						SyncClient client = new NettySyncClient(config);
						client.start();
	
						return client;
					}
				} else {
					AsyncClient client = new NettyAsyncClient(config);
					client.start();

					return client;
				}
			} else {
				throw new URISyntaxException(uri, "잘못된 URI 형식. server로 설정 할수 없습니다. URI foramt["
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
