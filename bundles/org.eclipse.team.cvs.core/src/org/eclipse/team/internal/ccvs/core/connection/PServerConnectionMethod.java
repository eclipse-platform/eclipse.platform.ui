/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.connection;
 
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
public class PServerConnectionMethod implements IConnectionMethod {
	/**
	 * @see IConnectionMethod#createConnection(ICVSRepositoryLocation, String)
	 */
	public IServerConnection createConnection(ICVSRepositoryLocation location, String password) {
		return new PServerConnection(location, password);
	}
	/**
	 * @see IConnectionMethod#getName()
	 */
	public String getName() {
		return "pserver";//$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.IConnectionMethod#disconnect(org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation)
	 */
	public void disconnect(ICVSRepositoryLocation location) {
	}
}
