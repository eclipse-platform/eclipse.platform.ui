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
package org.eclipse.ant.tests.ui.testplugin;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Test to close the workbench, since debug tests do not run in the UI
 * thread.
 */
public class ProjectCreationDecorator extends AbstractAntUITest {
	
	public ProjectCreationDecorator(String name) {
		super(name);
	}
	
	public void testProjectCreation() throws Exception {
		// delete any pre-existing project
		IProject pro = ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectHelper.PROJECT_NAME);
		if (pro.exists()) {
			pro.delete(true, true, null);
		}
		// create project and import buildfiles and support files
		IProject project = ProjectHelper.createProject(ProjectHelper.PROJECT_NAME);
		IFolder folder = ProjectHelper.addFolder(project, "buildfiles");
		ProjectHelper.addFolder(project, "launchConfigurations");
		File root = AntUITestPlugin.getDefault().getFileInPlugin(ProjectHelper.TEST_BUILDFILES_DIR);
		ProjectHelper.importFilesFromDirectory(root, folder.getFullPath(), null);
		
		createLaunchConfiguration("echoing");
		createLaunchConfiguration("bad");
		createLaunchConfigurationForSeparateVM("echoingSepVM");
		
		//do not show the Ant build failed error dialog
		AntUIPlugin.getDefault().getPreferenceStore().setValue(IAntUIPreferenceConstants.ANT_ERROR_DIALOG, false);
	}
	
	/**
	 * Creates a shared launch configuration for launching Ant in a separate VM with the given
	 * name.
	 */
	protected void createLaunchConfigurationForSeparateVM(String launchConfigName) throws Exception {
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		ILaunchConfigurationWorkingCopy config = type.newInstance(getJavaProject().getProject().getFolder("launchConfigurations"), launchConfigName);
		
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner"); //$NON-NLS-1$
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider"); //$NON-NLS-1$
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		config.setAttribute(IExternalToolConstants.ATTR_LOCATION, "${workspace_loc:/AntUITests/buildfiles/" + launchConfigName + ".xml}");
		config.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
		 
		setVM(config);
		setClasspath(config);		
		
//				String workingDirectory= workDirectoryField.getText().trim();
//				if (workingDirectory.length() == 0) {
//					configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String)null);
//				} else {
//					configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
//				}
		
		

//			String arguments= argumentField.getText().trim();
//			if (arguments.length() == 0) {
//				config.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String)null);
//			} else {
//				config.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
//			}
				
		config.doSave();
	}
	
	/**
	 * Creates a shared launch configuration for launching Ant in a separate VM with the given
	 * name.
	 */
	protected void createLaunchConfiguration(String launchConfigName) throws Exception {
		ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
		ILaunchConfigurationWorkingCopy config = type.newInstance(getJavaProject().getProject().getFolder("launchConfigurations"), launchConfigName);
	
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, getJavaProject().getElementName());
		config.setAttribute(IExternalToolConstants.ATTR_LOCATION, "${workspace_loc:/AntUITests/buildfiles/" + launchConfigName + ".xml}");
		config.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
	 
		//setClasspath(config);		
	
//					String workingDirectory= workDirectoryField.getText().trim();
//					if (workingDirectory.length() == 0) {
//						configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String)null);
//					} else {
//						configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
//					}
	
	

//				String arguments= argumentField.getText().trim();
//				if (arguments.length() == 0) {
//					config.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String)null);
//				} else {
//					config.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
//				}
			
		config.doSave();
	}

	private void setVM(ILaunchConfigurationWorkingCopy config) {
		IVMInstall vm = JavaRuntime.getDefaultVMInstall();
		String vmName= vm.getName();
		String vmTypeID= vm.getVMInstallType().getId();			
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmName);
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
	}

	private void setClasspath(ILaunchConfigurationWorkingCopy config) {
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
			
		//Path newJavaPath= new Path(vm.getInstallLocation().getAbsolutePath());
		//URL newToolsURL= prefs.getToolsJarURL(newJavaPath);

		StringBuffer urlString= new StringBuffer();
		//mark as additional classpath entries
		urlString.append(AntUtil.ANT_CLASSPATH_DELIMITER);

		URL[] urls= prefs.getDefaultAntURLs();
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			try {
				urlString.append(Platform.asLocalURL(url).getFile());
				urlString.append(AntUtil.ATTRIBUTE_SEPARATOR);
			} catch (IOException e) {	
			}
		}
		
		IPluginDescriptor descriptor = Platform.getPlugin("org.apache.xerces").getDescriptor(); //$NON-NLS-1$
		addXercesLibraries(descriptor, urlString);
		
		config.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, urlString.substring(0, urlString.length() - 1));
	}
	
	private void addXercesLibraries(IPluginDescriptor xercesPlugin, StringBuffer urlString) {
		URL root = xercesPlugin.getInstallURL();
		ILibrary[] libraries = xercesPlugin.getRuntimeLibraries();
	
		for (int i = 0; i < libraries.length; i++) {
			try {
				IPath path= libraries[i].getPath(); 
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
}
