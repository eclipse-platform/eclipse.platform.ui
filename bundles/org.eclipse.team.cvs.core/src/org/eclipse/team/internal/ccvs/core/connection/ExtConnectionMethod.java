package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.*;

public class ExtConnectionMethod implements IConnectionMethod {
	/**
	 * @see IConnectionMethod#getName
	 */
	public String getName() {
		return "ext"; //$NON-NLS-1$
	}
	
	/**
	 * @see IConnectionMethod#createConnection
	 */
	public IServerConnection createConnection(ICVSRepositoryLocation repositoryRoot, String password) {
		return new ExtConnection(repositoryRoot, password);
	}
	
	public void disconnect(ICVSRepositoryLocation location) {
	}
}