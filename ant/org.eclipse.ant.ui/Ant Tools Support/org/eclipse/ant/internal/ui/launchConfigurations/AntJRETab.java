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

package org.eclipse.ant.internal.ui.launchConfigurations;

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
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.ant.internal.ui.preferences.ClasspathModel;
import org.eclipse.ant.internal.ui.preferences.MessageDialogWithToggle;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILibrary;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.jres.DefaultJREDescriptor;
import org.eclipse.jdt.internal.debug.ui.launcher.VMArgumentsBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntJRETab extends JavaJRETab {

	private static final String XERCES_IMPL= "xercesImpl.jar"; //$NON-NLS-1$
	private static final String XERCES_API= "xml-apis.jar"; //$NON-NLS-1$
	private static final String XERCES_PARSER_API= "xmlParserAPIs.jar"; //$NON-NLS-1$
	private static final String MAIN_TYPE_NAME= "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner"; //$NON-NLS-1$
	
	private Button updateClasspathButton;
	private IVMInstall previousJRE;
	protected VMArgumentsBlock fVMArgumentsBlock=  new VMArgumentsBlock();
	protected AntWorkingDirectoryBlock fWorkingDirectoryBlock= new AntWorkingDirectoryBlock();
	private boolean warningShown= false;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		Font font= parent.getFont();
		WorkbenchHelp.setHelp(getControl(), IAntUIHelpContextIds.ANT_JRE_TAB);
		Composite comp= (Composite)fJREBlock.getControl();
		
		createVerticalSpacer(comp, 3);
		
		Composite lowerComp = new Composite(comp, SWT.NONE);
				
		GridLayout updateLayout = new GridLayout();
		updateLayout.numColumns = 2;
		updateLayout.marginHeight=0;
		updateLayout.marginWidth=0;
		lowerComp.setLayout(updateLayout);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		lowerComp.setLayoutData(gd);
		lowerComp.setFont(font);
		
		Label label= new Label(lowerComp, SWT.NULL);
		label.setText(AntLaunchConfigurationMessages.getString("AntJRETab.9")); //$NON-NLS-1$
		label.setFont(font);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		label.setLayoutData(gd);
		
		updateClasspathButton= createPushButton(lowerComp, AntLaunchConfigurationMessages.getString("AntJRETab.10"), null); //$NON-NLS-1$
		updateClasspathButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				updateClasspath(getLaunchConfigurationWorkingCopy());
			}
		});
		gd= (GridData)updateClasspathButton.getLayoutData();
		gd.horizontalAlignment= GridData.HORIZONTAL_ALIGN_BEGINNING;
		
		createVerticalSpacer(lowerComp, 2);
		
		fVMArgumentsBlock.createControl(lowerComp);
		((GridData)fVMArgumentsBlock.getControl().getLayoutData()).horizontalSpan= 2;
		createVerticalSpacer(lowerComp, 2);
						
		fWorkingDirectoryBlock.createControl(lowerComp);		
		((GridData)fWorkingDirectoryBlock.getControl().getLayoutData()).horizontalSpan= 2;
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
		fWorkingDirectoryBlock.setEnabled(!fJREBlock.isDefaultJRE());
		fVMArgumentsBlock.setEnabled(!fJREBlock.isDefaultJRE());
		if (fJREBlock.isDefaultJRE()) {
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, (String)null);
			configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);			
		} else {
			super.performApply(configuration);
			applySeparateVMAttributes(configuration);
			fVMArgumentsBlock.performApply(configuration);
			fWorkingDirectoryBlock.performApply(configuration);
		}
		setLaunchConfigurationWorkingCopy(configuration);
	}
	
	private void applySeparateVMAttributes(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, MAIN_TYPE_NAME);
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider"); //$NON-NLS-1$
		configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, IAntUIConstants.REMOTE_ANT_PROCESS_FACTORY_ID);
	}

	/**
	 * Updates the classpath for this Ant build based on the selected JRE.
	 * If running in the same VM as Eclipse, the appropriate tools.jar is added if not already present.
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
		IAntClasspathEntry oldToolsEntry= prefs.getToolsJarEntry(oldJavaPath);
			
		Path newJavaPath= null;
		if (vm == null) {
			newJavaPath = new Path(System.getProperty("java.home")); //$NON-NLS-1$
		} else {
			newJavaPath= new Path(vm.getInstallLocation().getAbsolutePath());
		}
		
		IAntClasspathEntry newToolsEntry= prefs.getToolsJarEntry(newJavaPath);
		
		List antEntries= new ArrayList();
		List userEntries= new ArrayList();	
		getEntries(prefs, configuration, antEntries, userEntries);
	
		StringBuffer classpath= new StringBuffer();
		boolean found= false;
		boolean xercesImplFound= false;
		boolean xercesAPIFound= false;
		boolean[] xercesFlags= new boolean[]{xercesImplFound, xercesAPIFound};
		
		found= lookForToolsAndXerces(antEntries, oldToolsEntry, newToolsEntry, xercesFlags);
					
		//look for the tools.jar and xerces in the additional classpath entries
		boolean foundInAdditional= lookForToolsAndXerces(userEntries, oldToolsEntry, newToolsEntry, xercesFlags);
		if (newToolsEntry != null && !found && !foundInAdditional) {
			classpath.append(newToolsEntry.getLabel());
			classpath.append(AntUtil.ATTRIBUTE_SEPARATOR);
		}
		
		//add the xerces JARs if required and not previously found
		if (!fJREBlock.isDefaultJRE() && (!xercesFlags[0] || !xercesFlags[1])) {
			IPluginDescriptor descriptor = Platform.getPlugin("org.apache.xerces").getDescriptor(); //$NON-NLS-1$
			addLibraries(descriptor, classpath, !xercesFlags[1], !xercesFlags[0]);
		}
		
		ClasspathModel model= getClasspathModel();
		classpath.append(model.serializeClasspath(true));
		configuration.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, classpath.toString());
		previousJRE= vm;
		updateOtherTabs();
		updateLaunchConfigurationDialog();
	}
	
	private void updateOtherTabs() {
		//the classpath has changed...set the targets and classpath tabs to 
		//need to be recomputed
		ILaunchConfigurationTab[] tabs=  getLaunchConfigurationDialog().getTabs();
		for (int i = 0; i < tabs.length; i++) {
			ILaunchConfigurationTab tab = tabs[i];
			if (tab instanceof AntTargetsTab) {
				((AntTargetsTab)tab).setDirty(true);
				continue;
			} else if (tab instanceof AntClasspathTab) {
				((AntClasspathTab)tab).setDirty(true);
				continue;
			}
		}
	}
	
	private ClasspathModel getClasspathModel() {
		//the classpath has changed...set the targets tab to 
		//need to be recomputed
		ILaunchConfigurationTab[] tabs=  getLaunchConfigurationDialog().getTabs();
		for (int i = 0; i < tabs.length; i++) {
			ILaunchConfigurationTab tab = tabs[i];
			if (tab instanceof AntClasspathTab) {
				return ((AntClasspathTab)tab).getClasspathModel();
			}
		}
		return null;
	}
	
	private void getEntries(AntCorePreferences prefs, ILaunchConfigurationWorkingCopy configuration, List antHomeEntries, List additionalEntries) {
		String entryStrings= null;
		try {
			entryStrings = configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_CUSTOM_CLASSPATH, (String) null);
		} catch (CoreException e) {
			AntUIPlugin.log(e);
		}
		if (entryStrings == null) {
			//the global settings
			antHomeEntries.addAll(Arrays.asList(prefs.getAntHomeClasspathEntries()));
			additionalEntries.addAll(Arrays.asList(prefs.getAdditionalClasspathEntries()));
		} else {
			AntUtil.getCustomClasspaths(configuration, antHomeEntries, additionalEntries);
		}
	}

	/**
	 * Returns <code>true</code> a tools.jar was found and was replaced or was found to be compatible
	 * with the specified JRE.
	 * The xerces flags are set based on the Xerces JARs that are found.
	 */
	private boolean lookForToolsAndXerces(List entries, IAntClasspathEntry oldToolsEntry, IAntClasspathEntry newToolsEntry, boolean[] xercesFlags){
		boolean found= false;
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			IAntClasspathEntry entry = (IAntClasspathEntry) iter.next();
			if (sameURL(oldToolsEntry, entry)) {
				entry= newToolsEntry;
				found= newToolsEntry != null;
			} else if (sameURL(newToolsEntry, entry)) {
				found= true;
			} else if (entry.getLabel().endsWith(XERCES_API)) {
				xercesFlags[1]= true;
			} else if (entry.getLabel().endsWith(XERCES_IMPL)) {
				xercesFlags[0]= true;
			} else if (entry.getLabel().endsWith(XERCES_PARSER_API)) {
				xercesFlags[1]= true;
			}
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
	
	private boolean sameURL(IAntClasspathEntry first, IAntClasspathEntry second) {
		if (first == null || second == null) {
			return false;
		}
		File newFile= new File(first.getEntryURL().getFile());
		File existingFile= new File(second.getEntryURL().getFile());
		if (existingFile.equals(newFile)) {
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		fVMArgumentsBlock.initializeFrom(configuration);
		fWorkingDirectoryBlock.initializeFrom(configuration);
		fWorkingDirectoryBlock.setEnabled(!fJREBlock.isDefaultJRE());
		fVMArgumentsBlock.setEnabled(!fJREBlock.isDefaultJRE());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return fWorkingDirectoryBlock.isValid(config);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setLaunchConfigurationDialog(org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
		fVMArgumentsBlock.setLaunchConfigurationDialog(dialog);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getErrorMessage();
		}
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		String m = super.getMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getMessage();
		}
		return m;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab#handleSelectedJREChanged()
	 */
	protected void handleSelectedJREChanged() {
		if (fIsInitializing) {
			return;
		}
		boolean check= AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.ANT_CLASSPATH_WARNING);
		if (check && !warningShown) {
			warningShown= true;
			MessageDialogWithToggle.openWarning(AntUIPlugin.getActiveWorkbenchWindow().getShell(),
				AntLaunchConfigurationMessages.getString("AntJRETab.11"), //$NON-NLS-1$
				AntLaunchConfigurationMessages.getString("AntJRETab.12"), //$NON-NLS-1$
				IAntUIPreferenceConstants.ANT_CLASSPATH_WARNING,
				AntLaunchConfigurationMessages.getString("AntJRETab.13"), //$NON-NLS-1$
				AntUIPlugin.getDefault().getPreferenceStore());	
		}
		super.handleSelectedJREChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		setLaunchConfigurationWorkingCopy(workingCopy);
		warningShown= false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		//by default set an Ant build to occur in a separate VM
		IVMInstall defaultInstall= null;
		try {
			defaultInstall = JavaRuntime.computeVMInstall(config);
		} catch (CoreException e) {
			//core exception thrown for non-Java project
			defaultInstall= JavaRuntime.getDefaultVMInstall();
		}
		if (defaultInstall != null) {
			String vmName = defaultInstall.getName();
			String vmTypeID = defaultInstall.getVMInstallType().getId();					
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmName);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vmTypeID);
			applySeparateVMAttributes(config);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
	}
}