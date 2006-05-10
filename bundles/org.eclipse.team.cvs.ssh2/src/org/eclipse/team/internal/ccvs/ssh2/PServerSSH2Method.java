/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;

import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IServerConnection;

public class PServerSSH2Method implements IConnectionMethod {
	public String getName() {
		return "pserverssh2"; //$NON-NLS-1$
	}
	public IServerConnection createConnection(ICVSRepositoryLocation root, String password) {
		return new PServerSSH2ServerConnection(root, password);
	}
	public void disconnect(ICVSRepositoryLocation location) {
	}
}
