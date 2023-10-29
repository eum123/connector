package com.tsis.connector.client.pool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jboss.netty.channel.socket.nio.BossPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.sync.NettySyncClient;

public class ConnectorPoolFactory {
	private static final Logger log = LoggerFactory.getLogger(ConnectorPoolFactory.class);

	private ObjectPool<Client> pool;
	private int maxActive = 1;
	private int minIdle = 10000;
	private int maxIdle = 10000;
	private int minEvictableIdle = 10000;
	private byte whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;

	private boolean isLazy = true;

	private String uri = null;

	//private Set<Client> extractList = new HashSet();
	private Set<Client> extractList = Collections.synchronizedSet(new HashSet());

	public void start() throws Exception {

		if (uri == null) {
			throw new Exception("[" + this.hashCode() + "] uri가 입력되어 있지 않습니다. 형식 : "
					+ DriverManager.URI_FORMAT);
		}

		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.maxActive = maxActive;
		config.minIdle = minIdle;
		config.maxIdle = maxIdle;
		// we should test on borrow to ensure the channel is still valid
		//config.testOnBorrow = true;
		// only evict channels which are no longer valid
		//config.testWhileIdle = true;
		// run eviction every 30th second
		//config.timeBetweenEvictionRunsMillis = 10 * 1000L;
		//config.minEvictableIdleTimeMillis = minEvictableIdle;
		config.whenExhaustedAction = whenExhaustedAction;
		pool = new GenericObjectPool<Client>(new ConnectorPoolableObjectFactory(), config);

		if (log.isDebugEnabled()) {
			log.debug(
					"["
							+ this.hashCode()
							+ "] Created ConnectorPoolFactory pool[maxActive={}, minIdle={}, maxIdle={}, minEvictableIdleTimeMillis={}] -> {}",
					new Object[] { config.maxActive, config.minIdle, config.maxIdle,
							config.minEvictableIdleTimeMillis, pool });
		}
		/*
		if (!isLazy) {
			// ensure the connection can be established when we start up
			for (int i = 0; i < maxActive; i++) {
				
				System.out.println("*************************** CHECK :maxActive : " + i );
				 
				Client client = pool.borrowObject();
				
				pool.returnObject(client);
			}
			
		}
		*/
		if (!isLazy) {
			// ensure the connection can be established when we start up
			Client client[] = new Client[maxActive];
			for (int i = 0; i < maxActive; i++) {
				//System.out.println("*************************** CHECK :borrowObject : " + i );	
				client[i] = pool.borrowObject();
				extractList.add(client[i]);
			}
			for (int j = 0; j < maxActive; j++) {
				//System.out.println("*************************** CHECK :returnObject : " + j );	
				pool.returnObject(client[j]);
				extractList.remove(client[j]);
			}
		}
		//System.out.println("*************************************** extractList.size() : " + extractList.size());
		
	}

	public void stop() throws Exception {
		if (pool != null) {
			if (log.isDebugEnabled()) {
				log.debug("[" + this.hashCode() + "] Stopping ConnectorPoolFactory with channel pool[active={}, idle={}]",
						pool.getNumActive(), pool.getNumIdle());
			}
			pool.close();
			pool = null;
		}
	}

	public synchronized Client getConnector() throws Exception {
		Client client = pool.borrowObject();
		extractList.add(client);

		//log.debug("[" + this.hashCode() + "] borrow({}) : {}", extractList.size(), client.hashCode());
		//Object obj[] = {extractList.size(),pool.getNumActive(),pool.getNumIdle() , client.hashCode()};
		log.debug("[" + this.hashCode() + "] borrow : E="+extractList.size()+" , A="+pool.getNumActive()+" , I="+pool.getNumIdle()+" , client="+client.hashCode());
		return client;
	}
	
	public boolean contains(Client client) {
		return extractList.contains(client);
	}

	public synchronized void release(Client client) {
		try {
			
			if (client == null) {
				log.debug("Release : client null ");
				return;
			} else {
				//System.out.println("******************************************* client : " + client.hashCode() );
				pool.returnObject(client);
				extractList.remove(client);
				log.debug("[" + this.hashCode() + "] release : E="+extractList.size()+" , A="+pool.getNumActive()+" , I="+pool.getNumIdle()+" , client="+client.hashCode());
			}
		} catch (Exception e) {
			log.error("[" + this.hashCode() + "] release fail", e);
		}
	}

	public int getNumActive() {
		return pool.getNumActive();
	}

	public int getNumIdle() {
		return pool.getNumIdle();
	}

	/**
	 * Object factory to create {@link Object} used by the pool.
	 */
	private final class ConnectorPoolableObjectFactory implements PoolableObjectFactory<Client> {

		public Client makeObject() throws Exception {
			//System.out.println("**************** makeObject() ");
			Client client = DriverManager.getClient(uri);
			log.debug("[" + ConnectorPoolFactory.this.hashCode() + "] Created client[{}]: {}",
					client.hashCode(), client);
			return client;
		}

		public void destroyObject(Client client) throws Exception {
			log.debug("[" + ConnectorPoolFactory.this.hashCode() + "] Destroying client[{}]: {}",
					client.hashCode(), client);

			if (client != null) {
				client.stop();
			}

		}

		public boolean validateObject(Client client) {
			
			boolean answer;
			try {
				//log.debug("************************************** validateObject ");
				answer = client.isAvailable();
				
			} catch (Exception e) {
				log.error("validateObject - ERROR", e);
				return false;
			}
			
			log.debug("[" + ConnectorPoolFactory.this.hashCode() + "] Validating client: {} -> {}",
					client, answer);
			return answer;			
		}

		public void activateObject(Client client) throws Exception {
			// noop
		}

		public void passivateObject(Client client) throws Exception {
			// noop			
		}
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isLazy() {
		return isLazy;
	}

	public void setLazy(boolean isLazy) {
		this.isLazy = isLazy;
	}

	public byte getWhenExhaustedAction() {
		return whenExhaustedAction;
	}

	public void setWhenExhaustedAction(byte whenExhaustedAction) {
		this.whenExhaustedAction = whenExhaustedAction;
	}
}
