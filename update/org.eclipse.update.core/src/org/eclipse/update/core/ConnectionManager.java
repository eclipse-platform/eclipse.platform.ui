package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2002, 2003
 *  All  Rights Reserved.
 */
import java.net.URL;
import java.util.*;

/**
 * Connection  Manager.
 * A  helper class used for creating ConnectionRequest instance.  
 * Connection manager is a singleton class. It cannot be instantiated; all
 * functionality is provided by static methods.
 * 
 * Users should call shutdown when finished with all connections 
 * This class is not inteneded to be sublcassed.
 * 
 * @see org.eclipse.update.core.ConnectionRequest
 * @since 2.1
 */
public class ConnectionManager {
	private static ConnectionManager shared;
	private Vector requests;
	private boolean noMoreRequests = false;
	
	
	private ConnectionManager() {
		startup();
	}
	
	private static ConnectionManager getManager() {
		if (shared==null) 
			shared = new ConnectionManager();
		return shared;
	}
	
	/**
	 * Closes all open ConnectionRequest
	 */
	public static void shutdown() {
		if (shared!=null)
			shared.doShutdown();
	}
	
	private void startup() {
		requests = new Vector();
	}
	
	private void doShutdown() {
		noMoreRequests = true;
		for (int i=0; i<requests.size(); i++) {
			ConnectionRequest request = (ConnectionRequest)requests.get(i);
			request.close();
		}
		requests.clear();
	}

	/**
	 * Creates a new connection to the specified <code>java.net.URL</code>
	 * 
	 * @param url the URl to connect to
	 * @param listener the listener to notify when the connection is cancelled
	 * or sucessfull
	 * @return ConnectionRequest the connection request
	 */
	public static ConnectionRequest openConnection(URL url, IConnectionRequestListener listener) {
		return getManager().doOpenConnection(url,listener);
	}
	
	private ConnectionRequest doOpenConnection(URL url, IConnectionRequestListener listener) {
		if (noMoreRequests) return null;
		ConnectionRequest request = new ConnectionRequest(url);
		request.setManager(getManager());
		requests.add(request);
		request.open(listener);
		return request;
	}
		
	void cancelConnection(ConnectionRequest request) {
		if (requests.contains(request)) {
			request.cancel();
			requests.remove(request);
		}
	}
	
	synchronized void purge(ConnectionRequest request) {
		requests.remove(request);
	}
}
