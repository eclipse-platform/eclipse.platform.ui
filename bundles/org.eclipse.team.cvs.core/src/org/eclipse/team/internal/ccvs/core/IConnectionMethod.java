package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Implementators of this class can act as factories for creating connections to a CVS server
 * with the desired custom communication protocol. Providers of CVS connection methods must implement 
 * this interface and register the implementation with the extension point:
 * 
 * 		org.eclipse.team.cvs.core.connectionmethods
 * 
 * The <code>createConnection()</code> method will be invoked by the CVS client when the user 
 * is attempting to make a connection to the server using the connection name which matches
 * the <code>String</code> returned by <code>getName()</code> (e.g. "pserver", "ext", etc.).
 */
public interface IConnectionMethod {
	
	/**
	 * Returns the name of this connection method (e.g."local", "ext").
	 */
	public String getName();
	
	/**
	 * Creates a new server connection using the given repository root
	 * (which includes the user name) and the given password.
	 */
	public IServerConnection createConnection(ICVSRepositoryLocation location, String password);
}
