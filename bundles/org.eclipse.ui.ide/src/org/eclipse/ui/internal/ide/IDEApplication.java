/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
//@issue org.eclipse.ui.internal.AboutInfo - illegal reference to generic workbench internals
import org.eclipse.ui.internal.AboutInfo;
import org.eclipse.ui.PlatformUI;

/**
 * The "main program" for the Eclipse IDE.
 * 
 * @since 3.0
 */
public final class IDEApplication implements IPlatformRunnable, IExecutableExtension {
	
	/**
	 * Creates a new IDE application.
	 */
	public IDEApplication() {
		// There is nothing to do for IDEApplication
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		
		// create the workbench with this adviser and run it until it exits
		// N.B. createWorkbench remembers the adviser, and also registers the
		// workbench globally so that all UI plug-ins can find it using
		// PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
		int returnCode = PlatformUI.createAndRunWorkbench(new IDEWorkbenchAdviser());
		
		// exit the application with an appropriate return code
		if (returnCode == PlatformUI.RETURN_RESTART) {
			return IPlatformRunnable.EXIT_RESTART;
		} else {
			return IPlatformRunnable.EXIT_OK;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// There is nothing to do for IDEApplication
	}
	
	/**
	 * Returns the about information of the primary feature.
	 * 
	 * @return info about the primary feature, or <code>null</code> if there 
	 * is no primary feature or if this information is unavailable
	 */
	public static AboutInfo getPrimaryInfo() {
		IPlatformConfiguration conf = BootLoader.getCurrentPlatformConfiguration();
		String id = conf.getPrimaryFeatureIdentifier();
		if (id == null) {
			return null;
		}
		return AboutInfo.readFeatureInfo(id);
	}
	
	/**
	 * Returns the about information of all known features, omitting any
	 * features which are missing this infomration.
	 * 
	 * @return a possibly empty list of about infos
	 */
	public static AboutInfo[] getFeatureInfos() {
		IPlatformConfiguration conf = BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.IFeatureEntry[] entries = conf.getConfiguredFeatureEntries();
		List infos = new ArrayList(entries.length);
		for (int i = 0; i < entries.length; i++) {
			AboutInfo info = AboutInfo.readFeatureInfo(entries[i].getFeatureIdentifier());
			if (info != null) {
				infos.add(info);
			}
		}
		AboutInfo[] result = new AboutInfo[infos.size()];
		infos.toArray(result);
		return result;
	}
}
