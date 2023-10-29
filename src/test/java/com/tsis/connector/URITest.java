package com.tsis.connector;

import java.net.URI;

import org.junit.Test;

public class URITest {
	@Test
	public void test() {
		String uriString = "client:sync:tcp://127.0.0.1:10010?disconnect=true";
		
		uriString = "tcp://127.0.0.1:10010/aaa?disconnect=true";

		URI uri = URI.create(uriString);

		System.out.println("host : " + uri.getHost());
		System.out.println("port : " + uri.getPort());
		System.out.println("Authority : " + uri.getAuthority());
		System.out.println("Fragment : " + uri.getFragment());
		System.out.println("getScheme : " + uri.getScheme());
		System.out.println("getQuery : " + uri.getQuery());
		System.out.println("getRawAuthority : " + uri.getRawAuthority());
		System.out.println("getRawFragment : " + uri.getRawFragment());
		System.out.println("getRawPath : " + uri.getRawPath());
		System.out.println("getRawQuery : " + uri.getRawQuery());
		System.out.println("getRawSchemeSpecificPart : " + uri.getRawSchemeSpecificPart());
		System.out.println("getRawUserInfo : " + uri.getRawUserInfo());
		System.out.println("getSchemeSpecificPart : " + uri.getSchemeSpecificPart());
		System.out.println("getUserInfo : " + uri.getUserInfo());
		System.out.println(uri);
	}
}
