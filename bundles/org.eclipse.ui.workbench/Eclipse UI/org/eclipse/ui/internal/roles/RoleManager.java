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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
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
 * RoleManager is the type that defines and filters based on role. */
public class RoleManager implements IActivityListener {

	private static RoleManager singleton;
	private boolean filterRoles = true;

	private Role[] roles = new Role[0];
	private Hashtable activities = new Hashtable();
	private Hashtable patternBindings = new Hashtable();

	private IResourceChangeListener listener;

	// Prefix for all role preferences
	private static String PREFIX = "UIRoles."; //$NON-NLS-1$
	private static String ROLES_FILE = "roles.xml"; //$NON-NLS-1$
	private static String FILTERING_ENABLED = "filterRoles"; //$NON-NLS-1$

	private RecentActivityManager recentActivities;

	/**
	 * Set of IActivityManagerListeners */
	private Set listeners = new HashSet();

	/**
	 * Utility for firing off batched ActivityEvents in an
	 * ActivityManagerEvent. Managed by ThreadLocal in order to make the
	 * calculator threadsafe.
	 */
	private final ThreadLocal deltaCalcs = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return new ActivityDeltaCalculator(RoleManager.this);
		}
	};

	public static RoleManager getInstance() {
		if (singleton == null)
			singleton = new RoleManager();

		return singleton;

	}

	public static void shutdown() {
		if (singleton == null)
			return;

		if (singleton.listener != null) {
			WorkbenchPlugin.getPluginWorkspace().removeResourceChangeListener(
				singleton.listener);
		}

		if (singleton.getRecentActivityManager() != null) {
			singleton.getRecentActivityManager().shutdown();
		}
	}

	/**
	 * @return the recent activities manager */
	public RecentActivityManager getRecentActivityManager() {
		return recentActivities;
	}

	/**
	 * 
	 * @param listener */
	public void addActivityManagerListener(IActivityManagerListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Fire the given event to all listeners.
	 * 
	 * @param event
	 */
	void fireActivityManagerEvent(ActivityManagerEvent event) {
		if (event == null) {
			return;
		}
		Set listenersCopy;
		synchronized (listeners) {
			listenersCopy = new HashSet(listeners);
		}

		for (Iterator i = listenersCopy.iterator(); i.hasNext();) {
			IActivityManagerListener listener =
				(IActivityManagerListener) i.next();
			listener.activityManagerChanged(event);
		}
	}

	/**
	 * 
	 * @param listener */
	public void removeActivityManagerListener(IActivityManagerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Fired whenever one of it's child Activities changes its enablement.
	 * 
	 * @see
	 * org.eclipse.ui.internal.roles.IActivityListener#activityChanged(org.eclipse.ui.internal.roles.ActivityEvent)
	 */
	public void activityChanged(ActivityEvent event) {
		ActivityDeltaCalculator deltaCalculator = getDeltaCalculator();
		boolean newSession = deltaCalculator.startDeltaSession();
		deltaCalculator.addActivity(event.getActivity());
		if (newSession) {
			fireActivityManagerEvent(deltaCalculator.endDeltaSession());
		}
	}

	/**
	 * Apply default pattern bindings to the objects governed by the given
	 * manager.
	 * 
	 * @param manager
	 */
	public void applyPatternBindings(ObjectActivityManager manager) {
		Set keys = manager.getObjectIds();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			ObjectContributionRecord record =
				(ObjectContributionRecord) i.next();
			String objectKey = record.toString();
			for (Iterator j = patternBindings.entrySet().iterator();
				j.hasNext();
				) {
				Map.Entry patternEntry = (Map.Entry) j.next();
				if (objectKey.matches((String) patternEntry.getKey())) {
					Collection activityIds =
						(Collection) patternEntry.getValue();
					for (Iterator k = activityIds.iterator(); k.hasNext();) {
						String activityId = (String) k.next();
						if (getActivity(activityId) != null) {
							manager.addActivityBinding(record, activityId);
						}
					}
				}
			}
		}
	}

	/**
	 * Read the roles from the primary feature. If there is no roles file then
	 * disable filter roles and leave. Otherwise read the contents of the file
	 * and define the roles for the workbench.
	 * 
	 * @return boolean true if successful
	 */
	private boolean readRoles() {
		IPlatformConfiguration config =
			BootLoader.getCurrentPlatformConfiguration();
		String id = config.getPrimaryFeatureIdentifier();
		if (id == null)
			return false;
		IPlatformConfiguration.IFeatureEntry entry =
			config.findConfiguredFeatureEntry(id);
		String plugInId = entry.getFeaturePluginIdentifier();
		if (plugInId == null)
			return false;

		IPluginDescriptor desc =
			Platform.getPluginRegistry().getPluginDescriptor(plugInId);
		if (desc == null)
			return false;

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
			Activity[] activityArray =
				RoleParser.readActivityDefinitions(memento);
			for (int i = 0; i < activityArray.length; i++) {
				activityArray[i].addListener(this);
				activities.put(activityArray[i].getId(), activityArray[i]);
			}

			patternBindings = RoleParser.readPatternBindings(memento);

			roles = RoleParser.readRoleDefinitions(memento, activities);

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
	 * 
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
		if (readRoles()) {
			// recent activities expire after an hour - create this irre
			recentActivities = new RecentActivityManager(this, 3600000L);
			loadEnabledStates();
			listener = getChangeListener();
			WorkbenchPlugin.getPluginWorkspace().addResourceChangeListener(
				listener);
		}
	}

	/**
	 * Loads the enabled states from the preference store. */
	void loadEnabledStates() {
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();

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
	 * Save the enabled states in he preference store. */
	void saveEnabledStates() {
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(PREFIX + FILTERING_ENABLED, isFiltering());

		Iterator values = activities.values().iterator();
		while (values.hasNext()) {
			Activity next = (Activity) values.next();
			store.setValue(createPreferenceKey(next), next.enabled);
		}
	}

	/**
	 * Create the preference key for the activity.
	 * 
	 * @param activity
	 * @return String
	 */
	private String createPreferenceKey(Activity activity) {
		return PREFIX + activity.getId();
	}

	/**
	 * Return whether or not the id is enabled. If there is a role whose
	 * pattern matches the id return whether or not the role is enabled. If
	 * there is no match return true; TODO: replace with usage of
	 * ObjectActivityManager.getActiveObjects()
	 * 
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
				Iterator activityIds =
					((Collection) patternBindings.get(next)).iterator();
				while (activityIds.hasNext()) {
					Activity activity =
						getActivity((String) activityIds.next());
					if (activity != null)
						return activity.isEnabled();
				}

			}
		}

		return true;
	}

	/**
	 * Return the roles currently defined.
	 * 
	 * @return
	 */
	public Role[] getRoles() {
		return roles;
	}

	/**
	 * Return whether or not the filtering is currently enabled.
	 * 
	 * @return boolean
	 */
	public boolean isFiltering() {
		return filterRoles;
	}

	/**
	 * Set whether or not the filtering is currently enabled.
	 * 
	 * @param boolean
	 */
	public void setFiltering(boolean value) {
		filterRoles = value;
	}

	/**
	 * Set the enablement (in a batch) of the given activities. By the time the
	 * method has returned there will have been an ActivityManagerEvent fired
	 * that describes the sum of the changes.
	 * 
	 * @param activities
	 * @param enabled
	 */
	public void setEnabled(Activity[] activities, boolean enabled) {
		ActivityDeltaCalculator deltaCalculator = getDeltaCalculator();
		boolean newSession = deltaCalculator.startDeltaSession();
		for (int i = 0; i < activities.length; i++) {
			activities[i].setEnabled(enabled);
		}

		if (newSession) {
			fireActivityManagerEvent(deltaCalculator.endDeltaSession());
		}
	}

	/**
	 * Return the activity with id equal to activityId. Return <code>null</code>
	 * if not found.
	 * 
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
	 * Enable any activity for which there is a pattern binding that matches
	 * id. TODO: replace with usage of
	 * ObjectActivityManager.setEnablementFor(bool)
	 * 
	 * @param id
	 */
	public void enableActivities(String id) {

		Iterator patternIterator = patternBindings.keySet().iterator();

		while (patternIterator.hasNext()) {
			String next = (String) patternIterator.next();
			if (id.matches(next)) {
				Iterator mappingIds =
					((Collection) patternBindings.get(next)).iterator();
				while (mappingIds.hasNext()) {
					String mappingId = (String) mappingIds.next();
					Activity activity = getActivity(mappingId);
					if (activity != null)
						activity.setEnabled(true);
				}

			}
		}
	}

	/**
	 * @return the set of Activity objects. */
	Collection getActivities() {
		return activities.values();
	}

	/**
	 * @return a delta calculator usable by the calling thread. */
	private ActivityDeltaCalculator getDeltaCalculator() {
		return (ActivityDeltaCalculator) deltaCalcs.get();
	}

	private IResourceChangeListener getChangeListener() {
		return new IResourceChangeListener() {
			/*
			 * (non-Javadoc) @see
			 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
			 */
			public void resourceChanged(IResourceChangeEvent event) {

				IResourceDelta mainDelta = event.getDelta();

				if (mainDelta == null)
					return;
				//Has the root changed?
				if (mainDelta.getKind() == IResourceDelta.CHANGED
					&& mainDelta.getResource().getType() == IResource.ROOT) {

					try {
						IResourceDelta[] children =
							mainDelta.getAffectedChildren();
						for (int i = 0; i < children.length; i++) {
							IResourceDelta delta = children[i];
							if (delta.getResource().getType()
								== IResource.PROJECT
								&& delta.getKind() == IResourceDelta.ADDED) {
								IProject project =
									(IProject) delta.getResource();
								String[] ids =
									project.getDescription().getNatureIds();
								for (int j = 0; j < ids.length; j++) {
									enableActivities(ids[j]);
								}
							}
						}

					} catch (CoreException exception) {
						//Do nothing if there is a CoreException
					}
				}

			}
		};
	}
}
