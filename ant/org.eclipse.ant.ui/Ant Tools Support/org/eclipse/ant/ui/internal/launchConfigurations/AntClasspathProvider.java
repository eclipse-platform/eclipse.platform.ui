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

package org.eclipse.ant.ui.internal.launchConfigurations;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathProvider;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.ant.ui.internal.model.AntUtil;

public class AntClasspathProvider implements IRuntimeClasspathProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#computeUnresolvedClasspath(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		return new IRuntimeClasspathEntry[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#resolveClasspath(org.eclipse.jdt.launching.IRuntimeClasspathEntry[], org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration) throws CoreException {
		//no need to add Eclipse extension point URLs if going to build
		//in separate VM..except for the remoteAnt.jar
		boolean separateVM= (null != configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null));
		URL[] antURLs= AntUtil.getCustomClasspath(configuration);
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		if (antURLs == null) {
			if (separateVM) {
				antURLs= prefs.getRemoteAntURLs();
			} else {
				antURLs = prefs.getURLs();
			}
		} else {
			if (separateVM) {
				List fullClasspath= new ArrayList(40);
				fullClasspath.addAll(Arrays.asList(antURLs));
				URL remote= prefs.getRemoteAntURL();
				if (remote != null) {
					fullClasspath.add(remote);
				}
				antURLs= (URL[])fullClasspath.toArray(new URL[fullClasspath.size()]);
			} else {
				List fullClasspath= new ArrayList(50);
				fullClasspath.addAll(Arrays.asList(antURLs));
				fullClasspath.addAll(Arrays.asList(prefs.getExtraClasspathURLs()));
				antURLs= (URL[])fullClasspath.toArray(new URL[fullClasspath.size()]);
			}
		}
		
		IVMInstall vm = JavaRuntime.computeVMInstall(configuration);
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(vm);
		IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[libs.length + antURLs.length];
		int i= 0;
		for (; i < libs.length; i++) {
			IRuntimeClasspathEntry r = JavaRuntime.newArchiveRuntimeClasspathEntry(libs[i].getSystemLibraryPath());
			r.setSourceAttachmentPath(libs[i].getSystemLibrarySourcePath());
			r.setSourceAttachmentRootPath(libs[i].getPackageRootPath());
			r.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
			rtes[i] = r;
		}
		
		for (int j = 0; j < antURLs.length; j++) {
			URL url = antURLs[j];
			IPath path= new Path(url.getPath());
			rtes[i] = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
			i++;
		}
		return rtes;		
	}
}
