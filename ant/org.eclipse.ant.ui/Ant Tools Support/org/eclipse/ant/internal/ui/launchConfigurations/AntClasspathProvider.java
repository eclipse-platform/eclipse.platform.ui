/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

public class AntClasspathProvider extends StandardClasspathProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#computeUnresolvedClasspath(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			List rtes = new ArrayList(10);
			IVMInstall vm= null;
			try {
				vm= JavaRuntime.computeVMInstall(configuration);
			} catch (CoreException ce) {
				//likely in a non-Java project
				vm= JavaRuntime.getDefaultVMInstall();
			}
			IPath containerPath = new Path(JavaRuntime.JRE_CONTAINER);
			containerPath = containerPath.append(new Path(vm.getVMInstallType().getId()));
			containerPath = containerPath.append(new Path(vm.getName()));
			
			rtes.add(JavaRuntime.newRuntimeContainerClasspathEntry(containerPath, IRuntimeClasspathEntry.STANDARD_CLASSES));
			rtes.add(new AntHomeClasspathEntry());
			rtes.add(new ContributedClasspathEntriesEntry());
			return (IRuntimeClasspathEntry[]) rtes.toArray(new IRuntimeClasspathEntry[rtes.size()]);
		}
		return super.computeUnresolvedClasspath(configuration);
	}
}
