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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.AntUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class AntJRETab extends JavaJRETab {

	private static final String XERCES_IMPL= new String("xercesImpl.jar"); //$NON-NLS-1$
	private static final String XERCES_API= new String("xml-apis.jar"); //$NON-NLS-1$
	private static final String XERCES_PARSER_API= new String("xmlParserAPIs.jar"); //$NON-NLS-1$
	
	private Button updateClasspathButton;
	private IVMInstall previousJRE;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		Control control= fJREBlock.getControl();
		Label label= new Label((Composite) control, SWT.NULL);
		GridData data = new GridData(GridData.BEGINNING);
		data.horizontalSpan = 3;
		label.setLayoutData(data);
		
		label= new Label((Composite) control, SWT.NULL);
		label.setText(AntLaunchConfigurationMessages.getString("AntJRETab.9")); //$NON-NLS-1$
		
		updateClasspathButton= new Button((Composite) control, SWT.PUSH);
		updateClasspathButton.setText(AntLaunchConfigurationMessages.getString("AntJRETab.10")); //$NON-NLS-1$
		updateClasspathButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				updateClasspath(getLaunchConfigurationWorkingCopy());
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab#getDefaultJREDescriptor()
	 */
	protected DefaultJREDescriptor getDefaultJREDescriptor() {
		return new DefaultJREDescriptor() {
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDefaultJRE()
			 */
			public IVMInstall getDefaultJRE() {
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor#getDescription()
			 */
			public String getDescription() {
				
				return AntLaunchConfigurationMessages.getString("AntJRETab.2"); //$NON-NLS-1$
			}
		};
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fJREBlock.isDefaultJRE()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);			
		} else {
			super.performApply(configuration);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.apache.tools.ant.Main"); //$NON-NLS-1$
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider"); //$NON-NLS-1$
		}
		setLaunchConfigurationWorkingCopy(configuration);
	}
	
	/**
	 * Updates the classpath for this Ant build based on the selected JRE.
	 * If running in the same VM as Eclipse, the appropriate tools.jar is added and 
	 * the Xerces JARs are removed.
	 * If running in the separate VM from Eclipse, the appropriate tools.jar is added and 
	 * the Xerces JARs are added.
	 */
	private void updateClasspath(ILaunchConfigurationWorkingCopy configuration) {
		
		IVMInstall vm= fJREBlock.getJRE();
		if(fJREBlock.isDefaultJRE()) {
			vm= null;
		}
		
		Path oldJavaPath=null;
		if (previousJRE == null) {
			oldJavaPath = new Path(System.getProperty("java.home")); //$NON-NLS-1$
		} else {  
			oldJavaPath= new Path(previousJRE.getInstallLocation().getAbsolutePath());
		}
		
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		URL oldToolsURL= prefs.getToolsJarURL(oldJavaPath);
			
		Path newJavaPath= null;
		if (vm == null) {
			newJavaPath = new Path(System.getProperty("java.home")); //$NON-NLS-1$
		} else {
			newJavaPath= new Path(vm.getInstallLocation().getAbsolutePath());
		}
		
		URL newToolsURL= prefs.getToolsJarURL(newJavaPath);
		
		List antURLs= new ArrayList();
		List userURLs= new ArrayList();
		
		getURLs(prefs, configuration, antURLs, userURLs);

		StringBuffer urlString= new StringBuffer();
		boolean found= false;
		boolean[] xercesFlags;
		{ 
			boolean xercesImplFound= false;
			boolean xercesAPIFound= false;
			xercesFlags= new boolean[]{xercesImplFound, xercesAPIFound};
		}
		found= lookForToolsAndXerces(urlString, antURLs, oldToolsURL, newToolsURL, xercesFlags);
		
		//mark as additional classpath entries
		urlString.append(AntUtil.ANT_CLASSPATH_DELIMITER);
		
		//look for the tools.jar and xerces in the additional classpath entries
		boolean foundInAdditional= lookForToolsAndXerces(urlString, userURLs, oldToolsURL, newToolsURL, xercesFlags);
		if (newToolsURL != null && !found && !foundInAdditional) {
			urlString.append(newToolsURL.getFile());
			urlString.append(AntUtil.ATTRIBUTE_SEPARATOR);
		}
		
		if (!fJREBlock.isDefaultJRE() && (!xercesFlags[0] || !xercesFlags[1])) {
			IPluginDescriptor descriptor = Platform.getPlugin("org.apache.xerces").getDescriptor(); //$NON-NLS-1$
			addLibraries(descriptor, urlString, !xercesFlags[1], !xercesFlags[0]);
		}
		configuration.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, urlString.substring(0, urlString.length() - 1));
		previousJRE= vm;
	}
	
	private void getURLs(AntCorePreferences prefs, ILaunchConfigurationWorkingCopy configuration, List antURLs, List userURLs) {
		String urlStrings= null;
		try {
			urlStrings = configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String) null);
		} catch (CoreException e) {
			AntUIPlugin.log(e);
		}
		if (urlStrings == null) {
			//the global settings
			antURLs.addAll(Arrays.asList(prefs.getAntURLs()));
			userURLs.addAll(Arrays.asList(prefs.getCustomURLs()));
		} else {
			try {
				AntUtil.getCustomClasspaths(configuration, antURLs, userURLs);
			} catch (CoreException e) {
				AntUIPlugin.log(e);
			}
		}
	}

	/**
	 * Returns <code>true</code> a tools.jar was found and was replaced or was found to be compatible
	 * with the specified JRE.
	 * The xerces flags are set based on the Xerces JARs that are found.
	 */
	private boolean lookForToolsAndXerces(StringBuffer urlString, List URLs, URL oldToolsURL, URL newToolsURL, boolean[] xercesFlags){
		boolean include= true;
		boolean found= false;
		for (Iterator iter = URLs.iterator(); iter.hasNext();) {
			URL url = (URL) iter.next();
			if (sameURL(oldToolsURL, url)) {
				url= newToolsURL;
				found= newToolsURL != null;
				include= found;
			} else if (sameURL(newToolsURL, url)) {
				found= true;
			} else if (url.getFile().endsWith(XERCES_API)) {
				xercesFlags[1]= true;
				if (fJREBlock.isDefaultJRE()) {
					include= false;
				}
			} else if (url.getFile().endsWith(XERCES_IMPL)) {
				xercesFlags[0]= true;
				if (fJREBlock.isDefaultJRE()) {
					include= false;
				}
			} else if (url.getFile().endsWith(XERCES_PARSER_API)) {
				xercesFlags[1]= true;
				if (fJREBlock.isDefaultJRE()) {
					include= false;
				}
			}
			if (include) {
				urlString.append(url.getFile());
				urlString.append(AntUtil.ATTRIBUTE_SEPARATOR);
			}
			include= true;
		}
		return found;
	}
	
	private void addLibraries(IPluginDescriptor xercesPlugin, StringBuffer urlString, boolean addAPI, boolean addImpl) {
		URL root = xercesPlugin.getInstallURL();
		ILibrary[] libraries = xercesPlugin.getRuntimeLibraries();
		
		for (int i = 0; i < libraries.length; i++) {
			try {
				IPath path= libraries[i].getPath(); 
				if (path.lastSegment().equals(XERCES_API) && !addAPI) {
					continue;
				} else if (path.lastSegment().equals(XERCES_PARSER_API) && !addAPI) {
					continue;
				} else if (path.lastSegment().equals(XERCES_IMPL) && !addImpl) {
					continue;
				} 
				URL url = new URL(root, path.toString());
				urlString.append(Platform.asLocalURL(url).getFile());
				urlString.append(AntUtil.ATTRIBUTE_SEPARATOR);
			} catch (MalformedURLException e1) {
				continue;
			} catch (IOException e2) {
				continue;
			}
		}
	}
	
	private boolean sameURL(URL first, URL second) {
		if (first == null || second == null) {
			return false;
		}
		File newFile= new File(first.getFile());
		File existingFile= new File(second.getFile());
		if (existingFile.equals(newFile)) {
			return true;
		}
		return false;
	}
}