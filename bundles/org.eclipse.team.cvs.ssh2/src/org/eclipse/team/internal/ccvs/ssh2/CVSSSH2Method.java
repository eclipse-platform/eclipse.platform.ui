/* -*-mode:java; c-basic-offset:2; -*- */
/*******************************************************************************
 * Copyright (c) 2003, Atsuhiko Yamanaka, JCraft,Inc. and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Atsuhiko Yamanaka, JCraft,Inc. - initial API and
 * implementation.
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.IConnectionMethod;
import org.eclipse.team.internal.ccvs.core.IServerConnection;
import org.eclipse.team.internal.ccvs.ssh.SSHServerConnection;

public class CVSSSH2Method implements IConnectionMethod {
	public String getName() {
		return "extssh"; //$NON-NLS-1$
	}
	
	public IServerConnection createConnection(ICVSRepositoryLocation root, String password) {
		IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();		
		// Support for user defined switching between SSH1 and SSH2 for now. Will
		// improve this to provide automatic server version detection.
		if(store.getBoolean(CVSSSH2PreferencePage.KEY_USE_SSH2)) {
			return new CVSSSH2ServerConnection(root, password);
		} else {
			return new SSHServerConnection(root, password);
		}
	}
	
	public void disconnect(ICVSRepositoryLocation location) {
	}
}