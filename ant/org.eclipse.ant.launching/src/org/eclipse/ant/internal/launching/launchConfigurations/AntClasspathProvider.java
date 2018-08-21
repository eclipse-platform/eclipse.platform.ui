/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.launching.launchConfigurations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

public class AntClasspathProvider extends StandardClasspathProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathProvider#computeUnresolvedClasspath(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			List<IRuntimeClasspathEntry> rtes = new ArrayList<>(10);
			IRuntimeClasspathEntry jreEntry = null;
			try {
				jreEntry = JavaRuntime.computeJREEntry(configuration);
				if (jreEntry != null) {
					jreEntry.setClasspathProperty(IRuntimeClasspathEntry.STANDARD_CLASSES);
				}
			}
			catch (CoreException e) {
				// not a java project
			}
			if (jreEntry == null) {
				jreEntry = JavaRuntime.newRuntimeContainerClasspathEntry(JavaRuntime.newDefaultJREContainerPath(), IRuntimeClasspathEntry.STANDARD_CLASSES);
			}
			rtes.add(jreEntry);
			rtes.add(new AntHomeClasspathEntry());
			rtes.add(new ContributedClasspathEntriesEntry());
			return rtes.toArray(new IRuntimeClasspathEntry[rtes.size()]);
		}
		return super.computeUnresolvedClasspath(configuration);
	}
}
