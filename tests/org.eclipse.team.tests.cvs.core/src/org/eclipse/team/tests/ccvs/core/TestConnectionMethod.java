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
package org.eclipse.team.tests.ccvs.core;

import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IServerConnection;

/**
 * @author Administrator
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestConnectionMethod implements IConnectionMethod {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IConnectionMethod#getName()
	 */
	public String getName() {
		return "test";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IConnectionMethod#createConnection(org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation, java.lang.String)
	 */
	public IServerConnection createConnection(ICVSRepositoryLocation location, String password) {
		return TestConnection.createConnection(location, password);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.IConnectionMethod#disconnect(org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation)
	 */
	public void disconnect(ICVSRepositoryLocation location) {
		// Nothing need to be done
	}

}
