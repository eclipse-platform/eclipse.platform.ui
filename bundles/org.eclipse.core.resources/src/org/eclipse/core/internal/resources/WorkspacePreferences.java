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
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

/**
 * This class provides the same interface as <code>WorkspaceDescription</code>
 * but instead of changing/obtaining values from its internal state, it
 * changes/obtains properties in/from the workspace plug-in's preferences.
 * 
 * Obs.: for performance reasons, some frequently called acessor methods are
 * reading a cached value from the super class instead of reading the
 * corresponding property preference store. To keep the cache synchronized with
 * the preference store, a property change listener is used.
 */

public class WorkspacePreferences extends WorkspaceDescription {

	private Preferences preferences;

	public final static String PROJECT_SEPARATOR = ":"; //$NON-NLS-1$

	public WorkspacePreferences() {
		super("Workspace"); //$NON-NLS-1$
		this.preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		//initialize cached preferences (for better performance)
		if (preferences.contains(ResourcesPlugin.PREF_AUTO_BUILDING))
			super.setAutoBuilding(preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING));
		if (preferences.contains(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL))
			super.setSnapshotInterval(preferences.getInt(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL));
		if (preferences.contains(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS))
			super.setMaxBuildIterations(preferences.getInt(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS));
		// This property listener ensures we are being updated properly when changes
		// are done directly to the preference store.
		preferences.addPropertyChangeListener(new Preferences.IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				synchronizeWithPreferences(event.getProperty());
			}
		});
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getBuildOrder()
	 */
	public String[] getBuildOrder() {
		boolean defaultBuildOrder = preferences.getBoolean(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER);
		if (defaultBuildOrder)
			return null;
		return convertStringToStringArray(preferences.getString(ResourcesPlugin.PREF_BUILD_ORDER));
	}

	/**
	 * @see org.eclipse.core.internal.resources.
	 * WorkspaceDescription#getBuildOrder(boolean)
	 */
	public String[] getBuildOrder(boolean makeCopy) {
		String[] result = getBuildOrder();
		if (result == null)
			return null;
		return makeCopy ? (String[]) result.clone() : result;
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getFileStateLongevity()
	 */
	public long getFileStateLongevity() {
		return preferences.getLong(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getMaxFileStates()
	 */
	public int getMaxFileStates() {
		return preferences.getInt(ResourcesPlugin.PREF_MAX_FILE_STATES);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getMaxFileStateSize()
	 */
	public long getMaxFileStateSize() {
		return preferences.getLong(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setAutoBuilding(boolean)
	 */
	public void setAutoBuilding(boolean value) {
		preferences.setValue(ResourcesPlugin.PREF_AUTO_BUILDING, value);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setBuildOrder(String[])
	 */
	public void setBuildOrder(String[] value) {
		preferences.setValue(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER, value == null);
		preferences.setValue(ResourcesPlugin.PREF_BUILD_ORDER, convertStringArraytoString(value));
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setFileStateLongevity(long)
	 */
	public void setFileStateLongevity(long time) {
		preferences.setValue(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY, time);
	}

	/**
	 * @see IWorkspaceDescription#setMaxBuildIterations(int)
	 */
	public void setMaxBuildIterations(int number) {
		preferences.setValue(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS, number);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setMaxFileStates(int)
	 */
	public void setMaxFileStates(int number) {
		preferences.setValue(ResourcesPlugin.PREF_MAX_FILE_STATES, number);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setMaxFileStateSize(long)
	 */
	public void setMaxFileStateSize(long size) {
		preferences.setValue(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE, size);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setSnapshotInterval(long)
	 */
	public void setSnapshotInterval(long delay) {
		preferences.setValue(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL, delay);
	}

	/**
	 * Helper method that converts a string string array {"string1","
	 * string2",..."stringN"} to a string in the form "string1,string2,...
	 * stringN".
	 */
	public static String convertStringArraytoString(String[] array) {
		if (array == null || array.length == 0)
			return ""; //$NON-NLS-1$
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			sb.append(PROJECT_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Helper method that converts a string in the form "string1,string2,...
	 * stringN" to a string array {"string1","string2",..."stringN"}.
	 */
	public static String[] convertStringToStringArray(String string) {
		List list = new LinkedList();
		for (StringTokenizer tokenizer = new StringTokenizer(string, PROJECT_SEPARATOR); tokenizer.hasMoreTokens();)
			list.add(tokenizer.nextToken());
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Helper method that copies all attributes from a workspace description
	 * object to another.
	 */
	private static void copyFromTo(WorkspaceDescription source, WorkspaceDescription target) {
		target.setAutoBuilding(source.isAutoBuilding());
		target.setBuildOrder(source.getBuildOrder());
		target.setFileStateLongevity(source.getFileStateLongevity());
		target.setMaxBuildIterations(source.getMaxBuildIterations());
		target.setMaxFileStates(source.getMaxFileStates());
		target.setMaxFileStateSize(source.getMaxFileStateSize());
		target.setSnapshotInterval(source.getSnapshotInterval());
	}

	public void copyFrom(WorkspaceDescription source) {
		copyFromTo(source, this);
	}

	public void copyTo(WorkspaceDescription target) {
		copyFromTo(this, target);
	}

	public void setDefaults(IWorkspaceDescription defaults) {
		preferences.setDefault(ResourcesPlugin.PREF_AUTO_BUILDING, defaults.isAutoBuilding());
		preferences.setDefault(ResourcesPlugin.PREF_BUILD_ORDER, convertStringArraytoString(defaults.getBuildOrder()));
		preferences.setDefault(ResourcesPlugin.PREF_DEFAULT_BUILD_ORDER, defaults.getBuildOrder() == null);
		preferences.setDefault(ResourcesPlugin.PREF_FILE_STATE_LONGEVITY, defaults.getFileStateLongevity());
		preferences.setDefault(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS, defaults.getMaxBuildIterations());
		preferences.setDefault(ResourcesPlugin.PREF_MAX_FILE_STATE_SIZE, defaults.getMaxFileStateSize());
		preferences.setDefault(ResourcesPlugin.PREF_MAX_FILE_STATES, defaults.getMaxFileStates());
		preferences.setDefault(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL, defaults.getSnapshotInterval());
	}

	public Object clone() {
		// should never be called - throws an exception to avoid using a 
		// WorkspacePreferences when using WorkspaceDescription was the real 
		// intention (this class offers a different protocol for copying state).
		throw new UnsupportedOperationException("clone() is not supported in " + getClass().getName()); //$NON-NLS-1$ 
	}

	protected void synchronizeWithPreferences(String property) {
		// do not use the value in the event - may be a string instead 
		// of the expected type. Retrieve it from the preferences store 
		// using the type-specific method 		
		if (property.equals(ResourcesPlugin.PREF_AUTO_BUILDING))
			super.setAutoBuilding(preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING));
		else if (property.equals(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL))
			super.setSnapshotInterval(preferences.getLong(ResourcesPlugin.PREF_SNAPSHOT_INTERVAL));
		else if (property.equals(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS))
			super.setMaxBuildIterations(preferences.getInt(ResourcesPlugin.PREF_MAX_BUILD_ITERATIONS));
	}
}