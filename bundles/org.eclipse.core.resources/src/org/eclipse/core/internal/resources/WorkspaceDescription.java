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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IWorkspaceDescription;

/**
 * @see IWorkspaceDescription
 */
public class WorkspaceDescription extends ModelObject implements IWorkspaceDescription {
	protected boolean autoBuilding = Policy.defaultAutoBuild;
	protected String[] buildOrder = null;
	// thread safety: (Concurrency004)
	protected volatile long fileStateLongevity = Policy.defaultFileStateLongevity;
	protected int maxBuildIterations = Policy.defaultMaxBuildIterations;
	protected int maxFileStates = Policy.defaultMaxFileStates;
	// thread safety: (Concurrency004)
	protected volatile long maxFileStateSize = Policy.defaultMaxFileStateSize;
	protected long maxNotifyDelay = Policy.defaultMaxNotifyDelay;
	// thread safety: (Concurrency004)
	private volatile long snapshotInterval = Policy.defaultSnapshotInterval;
	protected Workspace workspace;

	public WorkspaceDescription(String name) {
		super(name);
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public String[] getBuildOrder() {
		return getBuildOrder(true);
	}
	public String[] getBuildOrder(boolean makeCopy) {
		if (buildOrder == null)
			return null;
		return makeCopy ? (String[]) buildOrder.clone() : buildOrder;
	}
	public long getDeltaExpiration() {
		return Policy.defaultDeltaExpiration;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public long getFileStateLongevity() {
		return fileStateLongevity;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public int getMaxBuildIterations() {
		return maxBuildIterations;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public int getMaxFileStates() {
		return maxFileStates;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public long getMaxFileStateSize() {
		return maxFileStateSize;
	}
	public long getMaxNotifyDelay() {
		return maxNotifyDelay;
	}
	public int getOperationsPerSnapshot() {
		return Policy.defaultOperationsPerSnapshot;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public long getSnapshotInterval() {
		return snapshotInterval;
	}
	public void internalSetBuildOrder(String[] value) {
		buildOrder = value;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public boolean isAutoBuilding() {
		return autoBuilding;
	}
	public boolean isSnapshotEnabled() {
		return Policy.defaultSnapshots;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setAutoBuilding(boolean value) {
		autoBuilding = value;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setBuildOrder(String[] value) {
		buildOrder = (value == null) ? null : (String[]) value.clone();
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setFileStateLongevity(long time) {
		fileStateLongevity = time;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setMaxBuildIterations(int number) {
		maxBuildIterations = number;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setMaxFileStates(int number) {
		maxFileStates = number;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setMaxFileStateSize(long size) {
		maxFileStateSize = size;
	}
	public void setMaxNotifyDelay(long maxNotifyDelay) {
		this.maxNotifyDelay = maxNotifyDelay;
	}
	/**
	 * @see IWorkspaceDescription
	 */
	public void setSnapshotInterval(long snapshotInterval) {
		this.snapshotInterval = snapshotInterval;
	}
}
