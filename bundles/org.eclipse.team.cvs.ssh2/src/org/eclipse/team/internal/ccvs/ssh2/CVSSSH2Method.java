/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - removed preference for enabling SSH2
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;

import org.eclipse.team.internal.ccvs.core.*;

/**
 * SSH2 will hijack the 'extssh' method and use the SSH2 protocol instead
 * of SSH1. If the server doesn't support SSH2, the server connection method
 * will try connecting with SSH1.
 * 
 * @since 3.0
 */
public class CVSSSH2Method implements IConnectionMethod {
	
	public String getName() {
		return "extssh"; //$NON-NLS-1$
	}
	
	public IServerConnection createConnection(ICVSRepositoryLocation root, String password) {
		return new CVSSSH2ServerConnection(root, password);
	}
	
	public void disconnect(ICVSRepositoryLocation location) {
	}
}
