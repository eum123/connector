package com.tsis.connector.session;

import java.net.SocketAddress;

public interface Session {
	public String getIp();
	public int getPort();
	public SocketAddress getAddress();
}
