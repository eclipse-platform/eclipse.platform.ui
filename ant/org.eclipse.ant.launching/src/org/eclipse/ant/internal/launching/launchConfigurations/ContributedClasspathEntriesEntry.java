/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.launching.launchConfigurations;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.launching.AntLaunching;
import org.eclipse.ant.internal.launching.AntLaunchingUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.AbstractRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.Bundle;
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
    
    public static List fgSWTEntries= null;
		
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
		boolean separateVM= AntLaunchingUtil.isSeparateJREAntBuild(configuration);
		boolean setInputHandler= configuration.getAttribute(AntLaunching.SET_INPUTHANDLER, true);
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
            IPath toolsPath= new Path(path);
			if (toolsPath.lastSegment().equals("tools.jar")) { //$NON-NLS-1$
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
			addSWTJars(rtes);
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
	
	private void addSWTJars(List rtes) {
        if (fgSWTEntries == null) {
            fgSWTEntries= new ArrayList();
            Bundle bundle= Platform.getBundle("org.eclipse.swt"); //$NON-NLS-1$
            BundleDescription description= Platform.getPlatformAdmin().getState(false).getBundle(bundle.getBundleId());
            BundleDescription[] fragments= description.getFragments();
            for (int i = 0; i < fragments.length; i++) {
                Bundle fragmentBundle= Platform.getBundle(fragments[i].getName());
                URL bundleURL;
                try {
                    bundleURL = FileLocator.resolve(fragmentBundle.getEntry("/")); //$NON-NLS-1$
                } catch (IOException e) {
                    AntLaunching.log(e);
                   continue;
                }
                String urlFileName= bundleURL.getFile();
                if (urlFileName.startsWith(IAntCoreConstants.FILE_PROTOCOL)) {
                    try {
                        urlFileName= new URL(urlFileName).getFile();
                        if (urlFileName.endsWith("!/")) { //$NON-NLS-1$
                            urlFileName= urlFileName.substring(0, urlFileName.length() - 2);
                        }
                    } catch (MalformedURLException e) {
                    	 AntLaunching.log(e);
                       continue;
                    }
                }
                IPath fragmentPath= new Path(urlFileName);
                if (fragmentPath.getFileExtension() != null) { //JAR file
                    fgSWTEntries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(fragmentPath));
                } else { // folder
                    File bundleFolder= fragmentPath.toFile();
                    if (!bundleFolder.isDirectory()) {
                        continue;
                    }
                    String[] names= bundleFolder.list(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".jar"); //$NON-NLS-1$
                        }
                    });
                    for (int j = 0; j < names.length; j++) {
                        String jarName = names[j];
                        fgSWTEntries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(fragmentPath.append(jarName)));
                    }
                }
            }
        }
        rtes.addAll(fgSWTEntries);
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
		return AntLaunchConfigurationMessages.ContributedClasspathEntriesEntry_1;
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
