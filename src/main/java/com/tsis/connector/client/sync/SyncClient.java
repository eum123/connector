package com.tsis.connector.client.sync;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.MessageHandler;
import com.tsis.connector.common.ConnectorConfiguration;

/**
 * 데이터를 송신하고 결과를 받을때까지 대기하는 client<br>
 * <br>
 * url : client:sync://ip:port?options
 * <br> 
 * 
 * <pre>
 * 	options
 * 
 *   requestTimeout : millisecond. 응답 대기 시간. 설정하지 않으면 응답 받을 때 까지 대기
 *   connectTimeout : millisecond. 연결 대기 시간. 기본값은 10000.
 *   disconnect : 응답 수신 후 연결 종료 여부. true - 연결 종료
 *   type : fixed 또는 variable 설정으로 기본값을 fixed이며 전문의 형태를 정의 하는 것.
 *          variable은 전문에 길이 정보가 있는 경우 사용.
 *   readSize : type이 fixed 일때는 전문 전체 길이를 의미함.
 *              type이 variable일때는 길이 필드의 크기.
 *              기본값 0.
 *   readStart : type이 variable일때 사용하고 길이 필드의 시작 위치. 기본값 0.
 *   readHeaderLength : type이 variable일때 사용하며 길이 필드의 값이 body부분의 길이값일때 
 *          전체 전문크기를 알기 위해 header크기를 설정해야 되므로 header크기를 설정하는 부분.
 *          기본값 0.
 *   readType : type이 variable이고 readEncoding이 binary일때 길이 필드의 값이 문자, Integer, Long인지 구분하는 것.
 *          java.lang.String(기본값), java.lang.Integer, java.lang.Long.
 *   readEndian :  type이 variable이고 readEncoding이 binary일때 길이값의 byte order 설정.
 *          BIG_ENDIAN(기본값) 또는 LITTLE_ENDIAN. 
 *   readEncoding : type이 variable일때 길이 필드 값이 문자인지 숫자인지 구분.
 *          character(기본값) 또는 binary
 *   
 * </pre>
 * @author jin
 *
 */
public interface SyncClient extends Client {
	/**
	 * 데이터 전송 후 응답 수신
	 * @param data	전송할 데이터.
	 * @return 수신한 응답
	 * @throws Exception
	 */
	public byte[] write(byte[] data) throws Exception;
}
