/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

import org.eclipse.core.runtime.Platform;

public class Policy {
	// debug constants
	public static boolean DEBUG_SSH_PROTOCOL = false;

	static {
		// init debug options
		if (SSHPlugin.getPlugin().isDebugging()) {
			DEBUG_SSH_PROTOCOL = "true".equalsIgnoreCase(Platform.getDebugOption(SSHPlugin.ID + "/ssh_protocol"));//$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
