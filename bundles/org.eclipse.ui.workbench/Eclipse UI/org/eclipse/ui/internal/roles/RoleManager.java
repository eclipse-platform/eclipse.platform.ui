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

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

	private Role[] roles;

	// Prefix for all role preferences
	private static String PREFIX = "UIRoles."; //$NON-NLS-1$
	private static String ROLES_FILE = "roles.xml"; //$NON-NLS-1$

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
			FileReader reader = new FileReader(location.getFile());
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			roles = RoleParser.readRoleDefinitions(memento);

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
		IStatus error =
			new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				IStatus.ERROR,
				e.getLocalizedMessage(),
				e);
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
		for (int i = 0; i < roles.length; i++) {
			roles[i].enabled = store.getBoolean(createPreferenceKey(i));
		}
	}

	/**
	 * Save the enabled states in he preference store.
	 */
	void saveEnabledStates() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		for (int i = 0; i < roles.length; i++) {
			store.setValue(createPreferenceKey(i), roles[i].enabled);
		}
	}

	/**
	 * Create the preference key for the role at index i.
	 * @param i index of the role
	 * @return
	 */
	private String createPreferenceKey(int i) {
		return PREFIX + roles[i].id;
	}

	/**
	 * Return whether or not the id is enabled. If there is a role
	 * whose pattern matches the id return whether or not the role is
	 * enabled. If there is no match return true;
	 * @param id
	 * @return
	 */
	public boolean isEnabledId(String id) {

		if (!filterRoles)
			return true;
		for (int i = 0; i < roles.length; i++) {
			if (roles[i].patternMatches(id))
				return roles[i].enabled;
		}
		return true;
	}

	/**
	 * Enable the roles that satisfy pattern.
	 * @param pattern
	 */
	public void enableRoles(String pattern) {
		if (!filterRoles)
			return;
		if (pattern == null)
			return;
		for (int i = 0; i < roles.length; i++) {
			if (roles[i].patternMatches(pattern))
				roles[i].setEnabled(true);
		}
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
	 * @return
	 */
	public boolean isFiltering() {
		return filterRoles;
	}

}
