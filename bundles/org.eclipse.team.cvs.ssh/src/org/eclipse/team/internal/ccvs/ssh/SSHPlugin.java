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
package org.eclipse.team.internal.ccvs.ssh;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
public class SSHPlugin extends Plugin {
	
	public static String ID = "org.eclipse.team.cvs.ssh"; //$NON-NLS-1$

	private static SSHPlugin instance;
	
	/**
	 * Constructor for SSHPlugin
	 */
	public SSHPlugin(IPluginDescriptor d) {
		super(d);	
		instance = this;
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		Policy.localize("org.eclipse.team.internal.ccvs.ssh.messages"); //$NON-NLS-1$
	}
	
	/**
	 * Method getPlugin.
	 */
	public static SSHPlugin getPlugin() {
		return instance;
	}

}
