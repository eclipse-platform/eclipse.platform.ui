package org.eclipse.team.internal.ccvs.ssh;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IServerConnection;

public class SSHMethod implements IConnectionMethod {
	/**
	 * @see IConnectionMethod#getName
	 */
	public String getName() {
		return "extssh";//$NON-NLS-1$
	}
	
	/**
	 * @see IConnectionMethod#createConnection
	 */
	public IServerConnection createConnection(ICVSRepositoryLocation repositoryRoot, String password) {
		return new SSHServerConnection(repositoryRoot, password);
	}

}
