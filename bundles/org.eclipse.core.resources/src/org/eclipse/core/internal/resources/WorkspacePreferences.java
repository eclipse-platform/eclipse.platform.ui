/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;

/**
 * This class provides the same interface as <code>WorkspaceDescription</code>
 * but instead of changing/obtaining values from its internal state, it
 * changes/obtains properties in/from the workspace plug-in's preferences.
 */

public class WorkspacePreferences extends WorkspaceDescription {

	public final static String DESCRIPTION_PREFIX = "description."; //$NON-NLS-1$
	public static final String MAX_FILE_STATES = DESCRIPTION_PREFIX + "maxfilestates"; //$NON-NLS-1$
	public static final String AUTO_BUILDING = DESCRIPTION_PREFIX + "autobuilding"; //$NON-NLS-1$
	public static final String BUILD_ORDER = DESCRIPTION_PREFIX + "buildorder"; //$NON-NLS-1$
	public static final String DEFAULT_BUILD_ORDER = DESCRIPTION_PREFIX + "defaultbuildorder"; //$NON-NLS-1$
	public static final String FILE_STATE_LONGEVITY = DESCRIPTION_PREFIX + "filestatelongevity"; //$NON-NLS-1$
	public static final String MAX_FILE_STATE_SIZE = DESCRIPTION_PREFIX + "maxfilestatesize"; //$NON-NLS-1$
	public static final String SNAPSHOT_INTERVAL = DESCRIPTION_PREFIX + "snapshotinterval"; //$NON-NLS-1$

	private Preferences preferences;

	public WorkspacePreferences() {
		super("Workspace"); //$NON-NLS-1$
		this.preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getBuildOrder()
	 */
	public String[] getBuildOrder() {
		boolean defaultBuildOrder = preferences.getBoolean(DEFAULT_BUILD_ORDER);
		if (defaultBuildOrder)
			return null;
		return convertStringToStringArray(preferences.getString(BUILD_ORDER));
	}
	/**
	 * @see org.eclipse.core.internal.resources.
	 * WorkspaceDescription#getBuildOrder(boolean)
	 */
	public String[] getBuildOrder(boolean makeCopy) {
		String[] buildOrder = getBuildOrder();
		if (buildOrder == null)
			return null;
		return makeCopy ? (String[]) buildOrder.clone() : buildOrder;
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getFileStateLongevity()
	 */
	public long getFileStateLongevity() {
		return preferences.getLong(FILE_STATE_LONGEVITY);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getMaxFileStates()
	 */
	public int getMaxFileStates() {
		return preferences.getInt(MAX_FILE_STATES);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getMaxFileStateSize()
	 */
	public long getMaxFileStateSize() {
		return preferences.getLong(MAX_FILE_STATE_SIZE);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#getSnapshotInterval()
	 */
	public long getSnapshotInterval() {
		return preferences.getInt(SNAPSHOT_INTERVAL);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#isAutoBuilding()
	 */
	public boolean isAutoBuilding() {
		return preferences.getBoolean(AUTO_BUILDING);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setAutoBuilding(boolean)
	 */
	public void setAutoBuilding(boolean value) {
		preferences.setValue(AUTO_BUILDING, value);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setBuildOrder(java.lang.String)
	 */
	public void setBuildOrder(String[] value) {
		preferences.setValue(DEFAULT_BUILD_ORDER, value == null);
		preferences.setValue(BUILD_ORDER, convertStringArraytoString(value));
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setFileStateLongevity(long)
	 */
	public void setFileStateLongevity(long time) {
		preferences.setValue(FILE_STATE_LONGEVITY, time);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setMaxFileStates(int)
	 */
	public void setMaxFileStates(int number) {
		preferences.setValue(MAX_FILE_STATES, number);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setMaxFileStateSize(long)
	 */
	public void setMaxFileStateSize(long size) {
		preferences.setValue(MAX_FILE_STATE_SIZE, size);
	}
	/**
	 * @see org.eclipse.core.resources.IWorkspaceDescription#setSnapshotInterval(long)
	 */
	public void setSnapshotInterval(long delay) {
		preferences.setValue(SNAPSHOT_INTERVAL, delay);
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
			sb.append(',');
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
		for (StringTokenizer tokenizer = new StringTokenizer(string, ","); tokenizer.hasMoreTokens();) //$NON-NLS-1$
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
		target.setMaxFileStates(source.getMaxFileStates());
		target.setMaxFileStateSize(source.getMaxFileStateSize());
		target.setSnapshotInterval(source.getSnapshotInterval());
		//TODO: these attributes are not set by anybody - we should get rid of their mutators		
		target.setDeltaExpiration(source.getDeltaExpiration());
		target.setOperationsPerSnapshot(source.getOperationsPerSnapshot());
		target.setSnapshotEnabled(source.isSnapshotEnabled());
	}
	public void copyFrom(WorkspaceDescription source) {
		copyFromTo(source, this);
	}
	public void copyTo(WorkspaceDescription target) {
		copyFromTo(this, target);
	}
	public void setDefaults(IWorkspaceDescription defaults) {
		preferences.setDefault(AUTO_BUILDING, defaults.isAutoBuilding());
		preferences.setDefault(BUILD_ORDER, convertStringArraytoString(defaults.getBuildOrder()));
		preferences.setDefault(DEFAULT_BUILD_ORDER, defaults.getBuildOrder() == null);
		preferences.setDefault(FILE_STATE_LONGEVITY, defaults.getFileStateLongevity());
		preferences.setDefault(MAX_FILE_STATE_SIZE, defaults.getMaxFileStateSize());
		preferences.setDefault(MAX_FILE_STATES, defaults.getMaxFileStates());
		preferences.setDefault(SNAPSHOT_INTERVAL, defaults.getSnapshotInterval());
	}
	public Object clone() {
		// should never be called - to avoid using a WorkspacePreferences when
		// using WorkspaceDescription was the real intention (this class offers
		// a different protocol for copying state.
		throw new UnsupportedOperationException("clone() is not supported in " + getClass().getName()); //$NON-NLS-1$ 
	}

}
