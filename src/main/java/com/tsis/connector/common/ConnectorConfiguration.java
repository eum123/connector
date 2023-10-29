package com.tsis.connector.common;

import java.net.URI;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class ConnectorConfiguration {

	public static final String FIXED_LENGTH_TYPE = "fixed";
	public static final String VARIABLE_LENGTH_TYPE = "variable";
	public static final String CHARACTER = "character";
	public static final String BINARY = "binary";
	public static final String TIMEOUT = "timeout";

	private boolean isClient = true;
	private boolean isSync = true;
	private boolean isHttp = false;
	private long requestTimeout = 120000;
	private boolean disconnect;
	private int reconnectInterval = 10000;

	private int connectTimeout = 5000;

	private String host = null;
	private int port = -1;

	private String type = FIXED_LENGTH_TYPE;
	/** 시작 위치 */
	private int readStart = 0;
	/** 크기 */
	private int readSize = 0;
	/** java.lang.String, java.lang.Integer, java.lang.Long */
	private String readType = "java.lang.String";
	/** BIG_ENDIAN / LITTLE_ENDIAN */
	private ByteOrder readEndian = ByteOrder.BIG_ENDIAN;
	/** header 크기. 전문의 데이터가 body의 크기만 표시할 경우 header의 크기를 알아야 전체 길이를 구할 수 있음 */
	private int readHeaderLength = 0;
	/** character / binary */
	private String readEncoding = CHARACTER;

	private int maxContentLength = 1048576;

	private String rawPath = "/";
	private HttpVersion httpVersion = HttpVersion.HTTP_1_1;

	private HttpMethod httpType = HttpMethod.POST;

	private String uriEncoding = "utf-8";
	
	private int timeout = 5000; //timeout millisecond
	
	private boolean responseSync = false;
	
	// client:sync:tcp//127.0.0.1:10010?disconnect=true
	public ConnectorConfiguration(String config) throws Exception {

		String scheme = config.substring(0, config.indexOf(":"));
		if (scheme.equalsIgnoreCase("client")) {
			isClient = true;
		} else {
			isClient = false;
		}
		String tempSync = config.substring(config.indexOf(":") + 1);
		String sync = tempSync.substring(0, tempSync.indexOf(":"));
		if (sync.equalsIgnoreCase("sync")) {
			isSync = true;
		} else {
			if (sync.equalsIgnoreCase("http")) {
				isHttp = true;
				isSync = true;
			} else {
				isSync = false;
			}
		}

		URI uri = new URI(tempSync.substring(tempSync.indexOf(":") + 1));
		this.host = uri.getHost();
		this.port = uri.getPort();
		// port가 정의 되어 있지 않으면 80으로 설정
		if (port < 0) {
			port = 80;
		}
		this.rawPath = uri.getRawPath();
		if (rawPath.length() == 0) {
			rawPath = "/";
		}

		Map<String, String> map = queryToMap(uri.getQuery());
		if (map.containsKey("requestTimeout")) {
			this.requestTimeout = Long.parseLong(map.get("requestTimeout"));
		}
		if (map.containsKey("disconnect")) {
			this.disconnect = Boolean.parseBoolean(map.get("disconnect"));
		}
		if (map.containsKey("reconnectInterval")) {
			this.reconnectInterval = Integer.parseInt(map
					.get("reconnectInterval"));
		}
		if (map.containsKey("type")) {
			this.type = map.get("type");
		}
		if (map.containsKey("readSize")) {
			this.readSize = Integer.parseInt(map.get("readSize"));
		}
		if (map.containsKey("readStart")) {
			this.readStart = Integer.parseInt(map.get("readStart"));
		}
		if (map.containsKey("readType")) {
			this.readType = map.get("readType");
		}
		if (map.containsKey("readEndian")) {
			String readEndian = map.get("readEndian");
			if (readEndian.equalsIgnoreCase("LITTLE_ENDIAN")) {
				this.readEndian = ByteOrder.LITTLE_ENDIAN;
			} else {
				this.readEndian = ByteOrder.BIG_ENDIAN;
			}
		}
		if (map.containsKey("readHeaderLength")) {
			this.readHeaderLength = Integer.parseInt(map
					.get("readHeaderLength"));
		}
		if (map.containsKey("readEncoding")) {
			this.readEncoding = map.get("readEncoding");
		}

		if (map.containsKey("maxContentLength")) {
			this.maxContentLength = Integer.parseInt(map
					.get("maxContentLength"));
		}

		if (map.containsKey("httpVersion")) {
			if (map.get("httpVersion").equals("HTTP_1_0")) {
				this.httpVersion = HttpVersion.HTTP_1_0;
			}
		}
		if (map.containsKey("httpType")) {
			if (map.get("httpType").equalsIgnoreCase("GET")) {
				this.httpType = HttpMethod.GET;
			}
		}
		if (map.containsKey("uriEncoding")) {
			this.uriEncoding = map.get("uriEncoding");
		}
		
		if (map.containsKey(TIMEOUT)) {
			this.timeout = Integer.parseInt(map.get(TIMEOUT));
		}
		
		if (map.containsKey("responseSync")) {
			responseSync = Boolean.parseBoolean(map.get("responseSync"));
		}

		validate(map);
	}

	private void validate(Map map) throws Exception {

		if (isHttp) {

		} else {
			if (type.equals(FIXED_LENGTH_TYPE)) {
				if (!map.containsKey("readSize")) {
					throw new Exception("readSize 가 없음. " + FIXED_LENGTH_TYPE
							+ " type에는 readSize가 필수임.");
				}
			} else {
				if (!map.containsKey("readSize")) {
					throw new Exception("readSize 가 없음. "
							+ VARIABLE_LENGTH_TYPE + " type에는 readSize가 필수임.");
				}
				if (!map.containsKey("readStart")) {
					throw new Exception("readStart 가 없음. "
							+ VARIABLE_LENGTH_TYPE + " type에는 readStart가 필수임.");
				}
			}
		}
	}

	private Map<String, String> queryToMap(String query) {
		Map<String, String> map = new HashMap<String, String>();

		if (query != null) {
			StringTokenizer st = new StringTokenizer(query, "&");
			while (st.hasMoreElements()) {
				String token = st.nextToken();

				map.put(token.substring(0, token.indexOf("=")),
						token.substring(token.indexOf("=") + 1));
			}
		}

		return map;
	}

	public boolean isClient() {
		return isClient;
	}

	public void setClient(boolean isClient) {
		this.isClient = isClient;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public long getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(long requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public boolean isDisconnect() {
		return disconnect;
	}

	public void setDisconnect(boolean disconnect) {
		this.disconnect = disconnect;
	}

	public int getReconnectInterval() {
		return reconnectInterval;
	}

	public void setReconnectInterval(int reconnectInterval) {
		this.reconnectInterval = reconnectInterval;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getReadStart() {
		return readStart;
	}

	public void setReadStart(int readStart) {
		this.readStart = readStart;
	}

	public int getReadSize() {
		return readSize;
	}

	public void setReadSize(int readSize) {
		this.readSize = readSize;
	}

	public String getReadType() {
		return readType;
	}

	public void setReadType(String readType) {
		this.readType = readType;
	}

	public ByteOrder getReadEndian() {
		return readEndian;
	}

	public void setReadEndian(String readEndian) {
		if (readEndian.equalsIgnoreCase("LITTLE_ENDIAN")) {
			this.readEndian = ByteOrder.LITTLE_ENDIAN;
		} else {
			this.readEndian = ByteOrder.BIG_ENDIAN;
		}
	}

	public int getReadHeaderLength() {
		return readHeaderLength;
	}

	public void setReadHeaderLength(int readHeaderLength) {
		this.readHeaderLength = readHeaderLength;
	}

	public String getReadEncoding() {
		return readEncoding;
	}

	public void setReadEncoding(String readEncoding) {
		this.readEncoding = readEncoding;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

	public String getRawPath() {
		return rawPath;
	}

	public HttpVersion getHttpVersion() {
		return httpVersion;
	}

	public boolean isHttp() {
		return isHttp;
	}

	public HttpMethod getHttpType() {
		return httpType;
	}
	public String getUriEncoding() {
		return uriEncoding;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public boolean isResponseSync() {
		return responseSync;
	}

	public void setResponseSync(boolean responseSync) {
		this.responseSync = responseSync;
	}
	
	
}
