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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CVSSSH2Plugin extends AbstractUIPlugin {

	public static String ID = "org.eclipse.team.cvs.ssh2"; //$NON-NLS-1$
	private static CVSSSH2Plugin plugin;

	static String SSH_HOME_DEFAULT = null;
	static {
		String ssh_dir_name = ".ssh"; //$NON-NLS-1$
		
		// Windows doesn't like files or directories starting with a dot.
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			ssh_dir_name = "ssh"; //$NON-NLS-1$
		}
		SSH_HOME_DEFAULT = System.getProperty("user.home"); //$NON-NLS-1$
		if (SSH_HOME_DEFAULT != null) {
		    SSH_HOME_DEFAULT = SSH_HOME_DEFAULT + java.io.File.separator + ssh_dir_name;
		} else {
			
		}
	}
	
	public CVSSSH2Plugin() {
		super();
		plugin = this;
	}

	public static CVSSSH2Plugin getPlugin() {
		return plugin;
	}

	public void stop(BundleContext context) throws Exception {
		try {
			JSchSession.shutdown();
		} finally {
			super.stop(context);
		}
	}

	public static CVSSSH2Plugin getDefault() {
		return plugin;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private void initializeDefaultPreferences() {
	    IPreferenceStore store = getPreferenceStore();
	    store.setDefault(ISSHContants.KEY_SSH2HOME, SSH_HOME_DEFAULT);
	    store.setDefault(ISSHContants.KEY_PRIVATEKEY, ISSHContants.PRIVATE_KEYS_DEFAULT);
	    store.setDefault(ISSHContants.KEY_PROXY_TYPE, ISSHContants.HTTP);
	    store.setDefault(ISSHContants.KEY_PROXY_PORT, ISSHContants.HTTP_DEFAULT_PORT);
	    store.setDefault(ISSHContants.KEY_PROXY_AUTH, "false"); //$NON-NLS-1$
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);	
		Policy.localize("org.eclipse.team.internal.ccvs.ssh2.messages"); //$NON-NLS-1$
		initializeDefaultPreferences();
	}
}