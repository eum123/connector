package com.tsis.connector.client.http;

import java.util.Map;

import com.tsis.connector.client.Client;

/**
 * http protocol을 이용하여 데이터 송/수신<br>
 * <br>
 * url : client:http://ip:port?options
 * <br> 
 * 
 * <pre>
 * 	options
 * 
 *   requestTimeout : millisecond. 응답 대기 시간. 설정하지 않으면 응답 받을 때 까지 대기
 *   connectTimeout : millisecond. 연결 대기 시간. 기본값은 10000.
 *   httpVersion : http Protocol version. HTTP_1_1(기본값), HTTP_1_0
 *	   
 * </pre>
 * @author jin
 *
 */
public interface HttpClient extends Client {
	/**
	 * 데이터 전송 후 응답 수신
	 * @param data	전송할 데이터.
	 * @return 수신한 응답
	 * @throws Exception
	 */
	public byte[] write(byte[] data) throws Exception;
	public byte[] write(Map headers, byte[] data) throws Exception;
}
