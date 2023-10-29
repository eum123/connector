package com.tsis.connector.client.http;

import java.util.Map;

public class ConnHttpResponse {
	
	private Map headers;
	private byte[] message;
	private Throwable cause;

	public ConnHttpResponse(byte[] message) {
		this.message = message;
		this.cause = null;
	}
	public ConnHttpResponse(Map headers, byte[] message) {
		this.headers = headers;
		this.message = message;
		this.cause = null;
	}

	public ConnHttpResponse(Throwable cause) {
		this.message = null;
		this.cause = cause;
	}
	
	
	public Map getHeaders() {
		return headers;
	}
	public byte[] getMessage() {
		return message;
	}
	public Throwable getCause() {
		return cause;
	}
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

}