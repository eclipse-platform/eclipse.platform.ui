/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.connection;

 
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
		if(password==null){
			password=""; //$NON-NLS-1$
		}
		return new ExtConnection(repositoryRoot, password);
	}
	
	public void disconnect(ICVSRepositoryLocation location) {
	}
}
