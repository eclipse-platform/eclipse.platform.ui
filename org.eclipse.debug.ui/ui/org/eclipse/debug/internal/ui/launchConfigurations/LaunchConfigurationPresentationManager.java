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
package org.eclipse.debug.internal.ui.launchConfigurations;

 
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
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
	 * and entires are tables of launch modes (<code>String</code>)
	 * to <code>LaunchConfigurationTabGroupExtension</code>. "*" is
	 * used to represent the default tab group (i.e. unspecified mode).
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
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_TAB_GROUPS);
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
				// get the map for the config type
				Map map = (Map)fTabGroupExtensions.get(typeId);
				if (map == null) {
					map = new Hashtable();
					fTabGroupExtensions.put(typeId, map);
				}
				Set modes = group.getModes();
				if (modes == null) {
					// default tabs - store with "*"
					map.put("*", group); //$NON-NLS-1$
				} else {
					// store per mode
					Iterator iterator = modes.iterator();
					while (iterator.hasNext()) {
						map.put(iterator.next(), group);
					}
				}
			}
		}
	}	
	
	/**
	 * Returns the tab group for the given launch configuration type and mode.
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode
	 * @return the tab group for the given type of launch configuration
	 * @exception CoreException if an exception occurs creating the group
	 */
	public ILaunchConfigurationTabGroup getTabGroup(ILaunchConfigurationType type, String mode) throws CoreException {
		LaunchConfigurationTabGroupExtension ext = getExtension(type.getIdentifier(), mode);
		if (ext == null) {
			IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR,
			 MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationPresentationManager.No_tab_group_defined_for_launch_configuration_type_{0}_3"), (new String[] {type.getIdentifier()})), null);  //$NON-NLS-1$
			 throw new CoreException(status);
		} else {
			return ext.newTabGroup();
		}		
	}
	
	/**
	 * Returns the launch tab group extension for the given type and mode, or
	 * <code>null</code> if none
	 * 
	 * @param type launch configuration type identifier
	 * @param mode launch mode identifier
	 * @return launch tab group extension or <code>null</code>
	 */
	protected LaunchConfigurationTabGroupExtension getExtension(String type, String mode) {
		// get the map for the config type
		Map map = (Map)fTabGroupExtensions.get(type);
		if (map != null) {
			// try the specific mode
			Object extension = map.get(mode);
			if (extension == null) {
				// get the default tabs
				extension = map.get("*"); //$NON-NLS-1$
			}
			return (LaunchConfigurationTabGroupExtension)extension;
		}
		return null;
	}
	
	/**
	 * Returns the identifier of the help context that is associated with the
	 * specified launch configuration type and mode, or <code>null</code> if none.
	 * 
	 * @param type launch config type
	 * @param mode launch mode
	 * @return the identifier for the help context associated with the given
	 * type of launch configuration, or <code>null</code>
	 * @exception CoreException if an exception occurs creating the group
	 * @since 2.1
	 */
	public String getHelpContext(ILaunchConfigurationType type, String mode) throws CoreException {
		LaunchConfigurationTabGroupExtension ext = getExtension(type.getIdentifier(), mode);
		if (ext == null) {
			IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR,
			 MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationPresentationManager.No_tab_group_defined_for_launch_configuration_type_{0}_3"), (new String[] {type.getIdentifier()})), null); //$NON-NLS-1$
			 throw new CoreException(status);
		} else {
			return ext.getHelpContextId();
		}		
	}
}

