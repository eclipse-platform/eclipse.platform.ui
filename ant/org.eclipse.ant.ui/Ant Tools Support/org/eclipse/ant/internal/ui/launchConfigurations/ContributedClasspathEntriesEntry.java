/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.AbstractRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A classpath entry that contains a contributed classpath entries
 * via the <code>extraClasspathEntries</code> extension point.
 * 
 * @since 3.0 
 */
public class ContributedClasspathEntriesEntry extends AbstractRuntimeClasspathEntry {
	
	public static final String TYPE_ID = "org.eclipse.ant.ui.classpathentry.extraClasspathEntries"; //$NON-NLS-1$
		
	/**
	 * Default contructor required to instantiate persisted extensions.
	 */
	public ContributedClasspathEntriesEntry() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.AbstractRuntimeClasspathEntry#buildMemento(org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	protected void buildMemento(Document document, Element memento) throws CoreException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.launching.IRuntimeClasspathEntry2#initializeFrom(org.w3c.dom.Element)
	 */
	public void initializeFrom(Element memento) throws CoreException {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getTypeId()
	 */
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getRuntimeClasspathEntries(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(ILaunchConfiguration configuration) throws CoreException {
		boolean separateVM= (null != configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null));
		boolean setInputHandler= configuration.getAttribute(IAntUIConstants.SET_INPUTHANDLER, true);
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		IAntClasspathEntry[] antClasspathEntries = prefs.getContributedClasspathEntries();
		IAntClasspathEntry[] userEntries = prefs.getAdditionalClasspathEntries();
		List rtes = new ArrayList(antClasspathEntries.length + userEntries.length);
		IAntClasspathEntry entry;
		for (int i = 0; i < antClasspathEntries.length; i++) {
			 entry= antClasspathEntries[i];
			if (!separateVM || (separateVM && !entry.isEclipseRuntimeRequired())) {
				rtes.add(JavaRuntime.newStringVariableClasspathEntry(entry.getLabel()));
			}
		}
		boolean haveToolsEntry= false;
		String path;
		for (int i = 0; i < userEntries.length; i++) {
			entry = userEntries[i];
			path= entry.getLabel();
			if (path.endsWith("tools.jar")) { //$NON-NLS-1$
				haveToolsEntry= true;
				// replace with dynamically resolved tools.jar based on
				// the JRE being used
				addToolsJar(configuration, rtes, path);
			} else {
				rtes.add(JavaRuntime.newStringVariableClasspathEntry(path));
			}
		}
		if (!haveToolsEntry) {
			addToolsJar(configuration, rtes, null);
		}
		
		if (setInputHandler && separateVM) {
			addSWTJar(rtes);
		}
		
		return (IRuntimeClasspathEntry[]) rtes.toArray(new IRuntimeClasspathEntry[rtes.size()]);
	}
	
	private void addToolsJar(ILaunchConfiguration configuration, List rtes, String path) {
		IRuntimeClasspathEntry tools = getToolsJar(configuration);
		if (tools == null) {
			if (path != null) {
				//use the global entry
				rtes.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(path)));
			} else {
				//use the default vm install to try to find a tools.jar
				IVMInstall install= JavaRuntime.getDefaultVMInstall();
				if (install != null) {
					IAntClasspathEntry entry = AntCorePlugin.getPlugin().getPreferences().getToolsJarEntry(new Path(install.getInstallLocation().getAbsolutePath()));
					if (entry != null) {
						rtes.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(entry.getEntryURL().getPath())));
					}
				}
			}
		} else {
			rtes.add(tools);
		}
	}
	
	private void addSWTJar(List rtes) {
		IPath swtPath= AntLaunchDelegate.getSWTPath();
		swtPath= swtPath.append("ws"); //$NON-NLS-1$
		swtPath= swtPath.append(SWT.getPlatform());
		swtPath= swtPath.append("swt.jar"); //$NON-NLS-1$
		rtes.add(JavaRuntime.newArchiveRuntimeClasspathEntry(swtPath));
	}
	
	/**
	 * Returns the tools.jar to use for this launch configuration, or <code>null</code>
	 * if none.
	 * 
	 * @param configuration configuration to resolve a tools.jar for
	 * @return associated tools.jar archive, or <code>null</code>
	 */
	private IRuntimeClasspathEntry getToolsJar(ILaunchConfiguration configuration) {
		try {
			IVMInstall install = JavaRuntime.computeVMInstall(configuration);
			if (install != null) {
				IAntClasspathEntry entry = AntCorePlugin.getPlugin().getPreferences().getToolsJarEntry(new Path(install.getInstallLocation().getAbsolutePath()));
				if (entry != null) {
					return JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(entry.getEntryURL().getPath()));
				}
			}
		} catch (CoreException ce) {
			//likely dealing with a non-Java project
		}
			
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("ContributedClasspathEntriesEntry.1"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getType()
	 */
	public int getType() {
		return IRuntimeClasspathEntry.OTHER;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry2#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof ContributedClasspathEntriesEntry;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getClass().hashCode();
	}
}
