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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CVSSSH2Plugin extends AbstractUIPlugin {

	public static String ID = "org.eclipse.team.cvs.ssh2"; //$NON-NLS-1$
	private static CVSSSH2Plugin plugin;

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
	    CVSSSH2PreferencePage.initDefaults(store);
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);	
		Policy.localize("org.eclipse.team.internal.ccvs.ssh2.messages"); //$NON-NLS-1$
		initializeDefaultPreferences();
	}
}