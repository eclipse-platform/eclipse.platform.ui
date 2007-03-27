/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.LaunchConfigurationTabExtension;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

/**
 * This class is used to wrap a contributed <code>ILaunchConfigurationTabGroup</code> with any contributed tabs
 * for that group (from a <code>launchConfigurationTabs</code> extension point).
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 * @since 3.3
 */
public class LaunchConfigurationTabGroupWrapper implements ILaunchConfigurationTabGroup {
	
	private ILaunchConfigurationTabGroup fGroup = null;
	private String fGroupId = null;
	/**
	 * listing of tab extensions that we have to create
	 */
	private List fTabs = null;
	private String fMode = null;
	private ILaunchConfiguration fConfig = null;
	
	/**
	 * Constructor
	 * @param group the existing group to wrapper
	 * @param groupId the string id of the associated tab group
	 * @param config the launch configuration this tab group is opened on
	 */
	public LaunchConfigurationTabGroupWrapper(ILaunchConfigurationTabGroup group, String groupId, ILaunchConfiguration config) {
		fGroup = group;
		fGroupId = groupId;
		fConfig = config;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		if(fGroup != null) {
			fGroup.createTabs(dialog, mode);
			fMode = mode;
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#dispose()
	 */
	public void dispose() {
		if(fTabs != null) {
			for(int i = 0; i < fTabs.size(); i++) {
				((ILaunchConfigurationTab)fTabs.get(i)).dispose();
			}
			fTabs = null;
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#getTabs()
	 */
	public ILaunchConfigurationTab[] getTabs() {
		if(fTabs == null) {
			try {
				fTabs = new ArrayList();
			//add the tab groups' tabs first (defaults)
				fTabs.addAll(Arrays.asList(fGroup.getTabs()));
			//last, add the extensions (if any)
				LaunchConfigurationTabExtension[] ext = LaunchConfigurationPresentationManager.getDefault().getTabExtensions(fGroupId, fConfig, fMode);
				//copy contributed into correct position or end if no id or id is not found
				String id = null;
				for(int i = 0; i < ext.length; i++) {
					id = ext[i].getRelativeTabId();
					if(id != null) {
						int idx = indexofTab(id);
						if(idx  > -1) {
							fTabs.add(idx+1, ext[i].getTab());
						}
						else {
							fTabs.add(ext[i].getTab());
						}
					}
					else {
						fTabs.add(ext[i].getTab());
					}
				}
			}
			catch (CoreException ce) {DebugUIPlugin.log(ce);}
		}
		return (ILaunchConfigurationTab[]) fTabs.toArray(new ILaunchConfigurationTab[fTabs.size()]);
	}
	
	/**
	 * Returns the index of the tab matching the specified id
	 * @param id the id of the tab to find the index for
	 * @return the index of the tab specified by the id or -1 if not found
	 */
	private int indexofTab(String id) {
		if(id != null) { 
			Object o = null;
			for(int i = 0; i < fTabs.size(); i++) {
				o = fTabs.get(i);
				if(o instanceof AbstractLaunchConfigurationTab) {
					if(id.equals(((AbstractLaunchConfigurationTab)o).getId())) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		if(fTabs != null) {
			for(int i = 0; i < fTabs.size(); i++) {
				((ILaunchConfigurationTab)fTabs.get(i)).initializeFrom(configuration);
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#launched(org.eclipse.debug.core.ILaunch)
	 */
	public void launched(ILaunch launch) {
		if(fGroup != null) {
			fGroup.launched(launch);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(fTabs != null) {
			for(int i = 0; i < fTabs.size(); i++) {
				((ILaunchConfigurationTab)fTabs.get(i)).performApply(configuration);
			}
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if(fTabs != null) {
			for(int i = 0; i < fTabs.size(); i++) {
				((ILaunchConfigurationTab)fTabs.get(i)).setDefaults(configuration);
			}
		}
	}

}
