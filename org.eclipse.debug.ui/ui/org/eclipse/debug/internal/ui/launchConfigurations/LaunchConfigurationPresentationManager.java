package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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

/**
 * Manages contributed launch configuration tabs
 */ 
public class LaunchConfigurationPresentationManager {
	
	/**
	 * The singleton launch configuration presentation manager
	 */
	private static LaunchConfigurationPresentationManager fgDefault;
	
	/**
	 * The key associated with launch configuration tab extensions
	 * that are applicable to all launch configurations.
	 */
	protected static String GENERIC_TABS = "*";
	
	/**
	 * Collection of launch configuration tab extensions
	 * defined in plug-in xml. Entries are keyed by launch
	 * configuration type identifier (<code>String</code>),
	 * and entires are a list (<code>List</code>) of launch
	 * configuration tab extensions
	 * (<code>LaunchConfigurationTabExtension</code>). Tabs
	 * applicable to all launch confiugration types are keyed
	 * by the String "*".
	 */
	private Hashtable fTabExtensions;
		
	/**
	 * Constructs the singleton launch configuration presentation
	 * manager.
	 */
	private LaunchConfigurationPresentationManager() {
		fgDefault = this;
		initialize();
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
	 * Returns a collection of tab extensions used to present a
	 * launch configuration of the specified type. The tabs
	 * are returned in the order in which they should be
	 * presented.
	 * 
	 * @param lanuchConfigurationType a launch configuration type
	 * @return the tab extentions to present a launch configuration of the
	 *  specified type 
	 */
	public LaunchConfigurationTabExtension[] getTabs(ILaunchConfigurationType type) {
		List specificTabs = (List)fTabExtensions.get(type.getIdentifier());
		List genericTabs = (List)fTabExtensions.get(GENERIC_TABS);
		int size = 0;
		if (specificTabs != null) {
			size += specificTabs.size();
		}
		if (genericTabs != null) {
			size += genericTabs.size();
		}
		LaunchConfigurationTabExtension[] tabs = new LaunchConfigurationTabExtension[size];
		int pos = 0;
		if (specificTabs != null) {
			Iterator iter = specificTabs.iterator();
			while (iter.hasNext()) {
				tabs[pos] = (LaunchConfigurationTabExtension)iter.next();
				pos++;
			}
		}
		if (genericTabs != null) {
			Iterator iter = genericTabs.iterator();
			while (iter.hasNext()) {
				tabs[pos] = (LaunchConfigurationTabExtension)iter.next();
				pos++;
			}
		}
		return tabs;		
	}
	
	/**
	 * Creates launch configuration tab extensions for each extension
	 * defined in XML, and adds them to the table of tab extensions.
	 */
	private void initialize() {
		fTabExtensions = new Hashtable();
		IPluginDescriptor descriptor= DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugUIConstants.ID_LAUNCH_CONFIGURATION_TABS);
		IConfigurationElement[] tabs = extensionPoint.getConfigurationElements();
		for (int i = 0; i < tabs.length; i++) {
			LaunchConfigurationTabExtension tab = new LaunchConfigurationTabExtension(tabs[i]);
			String typeId = tab.getTypeIdentifier();
			if (typeId == null) {
				typeId = GENERIC_TABS;
			} else {
				// verify it references a valid launch configuration type
				ILaunchConfigurationType lct = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
				if (lct == null) {
					IExtension ext = tabs[i].getDeclaringExtension();
					IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.STATUS_INVALID_EXTENSION_DEFINITION,
					 MessageFormat.format("Launch configuration tab extension {0} refers to non-existant launch configuration type {1}", (new String[] {ext.getUniqueIdentifier(), typeId})), null);
					DebugUIPlugin.logError(new CoreException(status));
				}
			}
			if (typeId != null) {
				List list = (List)fTabExtensions.get(typeId);
				if (list == null) {
					list = new ArrayList();
					fTabExtensions.put(typeId, list);
				}
				list.add(tab);
			}
		}
	}
	
	
}

