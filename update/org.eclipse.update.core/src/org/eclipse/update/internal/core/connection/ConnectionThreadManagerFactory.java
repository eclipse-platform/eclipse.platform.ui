package org.eclipse.update.internal.core.connection;

public class ConnectionThreadManagerFactory {

	
	//Connection manager
	private static ConnectionThreadManager connectionManager;
	
	/**
	 * Returns the manager that manages URL connection threads.
	 */
	public static ConnectionThreadManager getConnectionManager() {
		
		if (connectionManager == null)
			connectionManager = new ConnectionThreadManager();
		return connectionManager;
	}
}
