/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * RoleManager is the type that defines and filters based on
 * role.
 */
public class RoleManager {

	private static RoleManager singleton;
	private boolean filterRoles = true;

	private Role[] roles = new Role[0];
	private Hashtable activities = new Hashtable();
	private Hashtable patternBindings = new Hashtable();

	// Prefix for all role preferences
	private static String PREFIX = "UIRoles."; //$NON-NLS-1$
	private static String ROLES_FILE = "roles.xml"; //$NON-NLS-1$
	private static String FILTERING_ENABLED = "filterRoles"; //$NON-NLS-1$

	public static RoleManager getInstance() {
		if (singleton == null)
			singleton = new RoleManager();

		return singleton;

	}

	/**
	 * Read the roles from the primary feature. If there is no
	 * roles file then disable filter roles and leave. Otherwise
	 * read the contents of the file and define the roles 
	 * for the workbench.
	 * @return boolean true if successful
	 */
	private boolean readRoles() {
		IPlatformConfiguration config = BootLoader.getCurrentPlatformConfiguration();
		String id = config.getPrimaryFeatureIdentifier();
		IPlatformConfiguration.IFeatureEntry entry = config.findConfiguredFeatureEntry(id);
		String plugInId = entry.getFeaturePluginIdentifier();
		IPluginDescriptor desc = Platform.getPluginRegistry().getPluginDescriptor(plugInId);
		URL location = desc.getInstallURL();
		try {
			location = new URL(location, ROLES_FILE);
		} catch (MalformedURLException e) {
			reportError(e);
			return false;
		}
		try {
			location = Platform.asLocalURL(location);

			File xmlDefinition = new File(location.getFile());
			if (!xmlDefinition.exists()) {
				return false;
			}
			FileReader reader = new FileReader(xmlDefinition);
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			Activity [] activityArray = RoleParser.readActivityDefinitions(memento);
			for(int i = 0; i < activityArray.length; i++){
				activities.put(activityArray[i].getId(),activityArray[i]);
			}
			
			patternBindings = RoleParser.readPatternBindings(memento);
			
			roles = RoleParser.readRoleDefinitions(memento,activities);

		} catch (IOException e) {
			reportError(e);
			return false;
		} catch (WorkbenchException e) {
			reportError(e);
			return false;
		}
		return true;
	}

	/**
	 * Report the Exception to the log and turn off the filtering.
	 * @param e
	 */
	private void reportError(Exception e) {
		IStatus error = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, e.getLocalizedMessage(), e);
		WorkbenchPlugin.getDefault().getLog().log(error);
		filterRoles = false;
	}

	private RoleManager() {
		if (readRoles())
			loadEnabledStates();
	}

	/**
	 * Loads the enabled states from the preference store.
	 */
	void loadEnabledStates() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

		//Do not set it if the store is not set so as to
		//allow for switching off and on of roles
		if (!store.isDefault(PREFIX + FILTERING_ENABLED))
			setFiltering(store.getBoolean(PREFIX + FILTERING_ENABLED));

		Iterator values = activities.values().iterator();
		while (values.hasNext()) {
			Activity next = (Activity) values.next();
			next.setEnabled(store.getBoolean(createPreferenceKey(next)));
		}
	}

	/**
	 * Save the enabled states in he preference store.
	 */
	void saveEnabledStates() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(PREFIX + FILTERING_ENABLED, isFiltering());

		Iterator values = activities.values().iterator();
		while (values.hasNext()) {
			Activity next = (Activity) values.next();
			store.setValue(createPreferenceKey(next), next.enabled);
		}
	}

	/**
	 * Create the preference key for the activity.
	 * @param activity
	 * @return String
	 */
	private String createPreferenceKey(Activity activity) {
		return PREFIX + activity.getId();
	}

	/**
	 * Return whether or not the id is enabled. If there is a role
	 * whose pattern matches the id return whether or not the role is
	 * enabled. If there is no match return true;
	 * @param id
	 * @return boolean. 
	 */
	public boolean isEnabledId(String id) {

		if (!isFiltering())
			return true;

		Enumeration bindingsPatterns = patternBindings.keys();
		while (bindingsPatterns.hasMoreElements()) {
			String next = (String) bindingsPatterns.nextElement();
			if (id.matches(next)) {
				Activity activity = getActivity((String) patternBindings.get(next));
				if (activity != null)
					return activity.isEnabled();
			}
		}

		return true;
	}

	/**
	 * Return the roles currently defined.
	 * @return
	 */
	public Role[] getRoles() {
		return roles;
	}

	/**
	 * Return whether or not the filtering is currently
	 * enabled.
	 * @return boolean
	 */
	public boolean isFiltering() {
		return filterRoles;
	}

	/**
	 * Set whether or not the filtering is currently
	 * enabled.
	 * @param boolean
	 */
	public void setFiltering(boolean value) {
		filterRoles = value;
	}

	/**
	 * Return the activity with id equal to activityId.
	 * Return <code>null</code> if not found.
	 * @param activityId
	 * @return Activity or <code>null</code>
	 */
	Activity getActivity(String activityId) {
		if (activities.containsKey(activityId))
			return (Activity) activities.get(activityId);
		else
			return null;
	}
	
	/** 
	 * Enable any activity for which there is a pattern
	 * binding that matches id.
	 * @param id
	 */
	public void enableActivities(String id){
		
		Iterator patternIterator = patternBindings.keySet().iterator();
		
		while(patternIterator.hasNext()){
			String next = (String) patternIterator.next();
			if(id.matches(next)){
				String mappingId = (String) patternBindings.get(next);
				Activity activity = getActivity(mappingId);
				if(activity != null)
					activity.setEnabled(true);
			}
		}
		
	}
}
