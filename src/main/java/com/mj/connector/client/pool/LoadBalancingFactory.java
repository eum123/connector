package com.tsis.connector.client.pool;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.loadbalancer.FailOver;
import com.tsis.connector.client.loadbalancer.LoadBalancer;

public class LoadBalancingFactory {
	private static final Logger log = LoggerFactory.getLogger(LoadBalancingFactory.class);
	
	private String[] uris = null;
	private ConnectorPoolFactory[] factories = null;
	
	private int maxActive = 1;
	private int minIdle = 10000;
	private int maxIdle = 10000;
	private int minEvictableIdle = 10000;
	private byte whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
	
	private boolean isLazy = true;
	
	private LoadBalancer loadbalancer = null;
	
	public void start() throws Exception {
		
		if(uris == null || uris.length == 0) {
			throw new Exception("uri가 입력되어 있지 않습니다. 형식 : " + DriverManager.URI_FORMAT);
		}
		
		factories = new ConnectorPoolFactory[uris.length];
		for(int i=0 ;i<uris.length ;i++) {
			factories[i] = new ConnectorPoolFactory();
			factories[i].setLazy(isLazy);
			factories[i].setMaxActive(maxActive);
			factories[i].setMaxIdle(maxIdle);
			factories[i].setMinEvictableIdle(minEvictableIdle);
			factories[i].setMinIdle(minIdle);
			factories[i].setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
			factories[i].setUri(uris[i]);
			
			factories[i].start();
		}
		
		if(loadbalancer == null) {
			loadbalancer = new FailOver();
		}
		
	}
	
	
	public void stop() throws Exception {
		for(int i=0 ;i<factories.length ;i++) {
			factories[i].stop();
		}
	}
	
	public Client getConnector() throws Exception {
		return loadbalancer.extract(factories);
	}

	public void release(Client client) {
		try {
			log.debug("release : {}", client);
						
			for(int i=0 ;i<factories.length ;i++) {
				if(factories[i].contains(client)) {
					factories[i].release(client);
				}
				
				log.debug("[" + factories[i].hashCode() + "] active number : {}", factories[i].getNumActive());
			}
		} catch (Exception e) {
			log.error("release fail", e);
		}
	}
	
	public void setUri(String ...uris) {
		this.uris = uris;
	}
	
	public int getMaxActive() {
		return maxActive;
	}


	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}


	public int getMinIdle() {
		return minIdle;
	}


	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}


	public int getMaxIdle() {
		return maxIdle;
	}


	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}


	public int getMinEvictableIdle() {
		return minEvictableIdle;
	}


	public void setMinEvictableIdle(int minEvictableIdle) {
		this.minEvictableIdle = minEvictableIdle;
	}
	
	public boolean isLazy() {
		return isLazy;
	}


	public void setLazy(boolean isLazy) {
		this.isLazy = isLazy;
	}


	public LoadBalancer getLoadbalancer() {
		return loadbalancer;
	}


	public void setLoadbalancer(LoadBalancer loadbalancer) {
		this.loadbalancer = loadbalancer;
	}
}
