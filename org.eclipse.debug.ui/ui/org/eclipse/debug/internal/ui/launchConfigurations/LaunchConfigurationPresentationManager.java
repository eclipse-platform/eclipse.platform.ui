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
package org.eclipse.debug.internal.ui.launchConfigurations;

 
import java.text.MessageFormat;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

/**
 * Manages contributed launch configuration tabs
 */ 
public class LaunchConfigurationPresentationManager {
	
	/**
	 * The singleton launch configuration presentation manager
	 */
	private static LaunchConfigurationPresentationManager fgDefault;
			
	/**
	 * Collection of launch configuration tab group extensions
	 * defined in plug-in xml. Entries are keyed by launch
	 * configuration type identifier (<code>String</code>),
	 * and entires are <code>LaunchConfigurationTabGroupExtension</code>.
	 */
	private Hashtable fTabGroupExtensions;	
		
	/**
	 * Constructs the singleton launch configuration presentation
	 * manager.
	 */
	private LaunchConfigurationPresentationManager() {
		fgDefault = this;
		initializeTabGroupExtensions();
	}

	/**
	 * Returns the launch configuration presentation manager
	 */
	public static LaunchConfigurationPresentationManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new LaunchConfigurationPresentationManager();
		}
		return fgDefault;
	}
		
	/**
	 * Creates launch configuration tab group extensions for each extension
	 * defined in XML, and adds them to the table of tab group extensions.
	 */
	private void initializeTabGroupExtensions() {
		fTabGroupExtensions = new Hashtable();
		IPluginDescriptor descriptor= DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_TAB_GROUPS);
		IConfigurationElement[] groups = extensionPoint.getConfigurationElements();
		for (int i = 0; i < groups.length; i++) {
			LaunchConfigurationTabGroupExtension group = new LaunchConfigurationTabGroupExtension(groups[i]);
			String typeId = group.getTypeIdentifier();
			if (typeId == null) {
				IExtension ext = groups[i].getDeclaringExtension();
				IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.STATUS_INVALID_EXTENSION_DEFINITION,
					 MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationPresentationManager.Launch_configuration_tab_group_extension_{0}_does_not_specify_launch_configuration_type_1"), (new String[] {ext.getUniqueIdentifier()})), null); //$NON-NLS-1$
					DebugUIPlugin.log(status);
			} else {
				// verify it references a valid launch configuration type
				ILaunchConfigurationType lct = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
				if (lct == null) {
					IExtension ext = groups[i].getDeclaringExtension();
					IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.STATUS_INVALID_EXTENSION_DEFINITION,
					 MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationPresentationManager.Launch_configuration_tab_group_extension_{0}_refers_to_non-existant_launch_configuration_type_{1}_2"), (new String[] {ext.getUniqueIdentifier(), typeId})), null); //$NON-NLS-1$
					DebugUIPlugin.log(status);
				}
			}
			if (typeId != null) {
				fTabGroupExtensions.put(typeId, group);
			}
		}
	}	
	
	/**
	 * Returns the tab group for the given type of launch configuration.
	 * 
	 * @return the tab group for the given type of launch configuration
	 * @exception CoreException if an exception occurrs creating the group
	 */
	public ILaunchConfigurationTabGroup getTabGroup(ILaunchConfigurationType type) throws CoreException {
		LaunchConfigurationTabGroupExtension ext = (LaunchConfigurationTabGroupExtension)fTabGroupExtensions.get(type.getIdentifier());
		if (ext == null) {
			IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR,
			 MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationPresentationManager.No_tab_group_defined_for_launch_configuration_type_{0}_3"), (new String[] {type.getIdentifier()})), null);			; //$NON-NLS-1$
			 throw new CoreException(status);
		} else {
			return ext.newTabGroup();
		}		
	}
	
	/**
	 * Returns the identifier of the help context that is associated with the
	 * specified launch configuration type, or <code>null</code> if none.
	 * 
	 * @return the identifier for the help context associated with the given
	 * type of launch configuration, or <code>null</code>
	 * @exception CoreException if an exception occurrs creating the group
	 * @since 2.1
	 */
	public String getHelpContext(ILaunchConfigurationType type) throws CoreException {
		LaunchConfigurationTabGroupExtension ext = (LaunchConfigurationTabGroupExtension)fTabGroupExtensions.get(type.getIdentifier());
		if (ext == null) {
			IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR,
			 MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationPresentationManager.No_tab_group_defined_for_launch_configuration_type_{0}_3"), (new String[] {type.getIdentifier()})), null);			; //$NON-NLS-1$
			 throw new CoreException(status);
		} else {
			return ext.getHelpContextId();
		}		
	}
}

