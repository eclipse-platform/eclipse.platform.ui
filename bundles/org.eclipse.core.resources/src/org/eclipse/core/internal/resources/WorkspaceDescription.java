package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.utils.Policy;
/**
 * @see IWorkspaceDescription
 */
public class WorkspaceDescription extends ModelObject implements IWorkspaceDescription {
	protected Workspace workspace;
	protected boolean dirty;
	protected boolean autoBuilding = Policy.defaultAutoBuild;
	protected int operationsPerSnapshot = Policy.defaultOperationsPerSnapshot;
	protected boolean snapshotsEnabled = Policy.defaultSnapshots;
	// thread safety: (Concurrency004)
	protected volatile long deltaExpiration = Policy.defaultDeltaExpiration;
	// thread safety: (Concurrency004)
	protected volatile long fileStateLongevity = Policy.defaultFileStateLongevity;
	// thread safety: (Concurrency004)
	protected volatile long maxFileStateSize = Policy.defaultMaxFileStateSize;
	protected int maxFileStates = Policy.defaultMaxFileStates;
	protected String[] buildOrder = null;
public WorkspaceDescription(String name) {
	super(name);
	dirty = true;
}
/**
 * Clears the dirty bit.  This should be used after saving descriptions to disk
 */
public void clean() {
	dirty = false;
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
	return deltaExpiration;
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
public int getMaxFileStates() {
	return maxFileStates;
}
/**
 * @see IWorkspaceDescription
 */
public long getMaxFileStateSize() {
	return maxFileStateSize;
}
public int getOperationsPerSnapshot() {
	return operationsPerSnapshot;
}
public void internalSetBuildOrder(String[] value) {
	dirty = true;
	buildOrder = value;
}
/**
 * @see IWorkspaceDescription
 */
public boolean isAutoBuilding() {
	return autoBuilding;
}
/**
 * Returns true if this resource has been modified since the last save
 * and false otherwise.
 */
public boolean isDirty() {
	return dirty;
}
public boolean isSnapshotEnabled() {
	return snapshotsEnabled;
}
/**
 * @see IWorkspaceDescription
 */
public void setAutoBuilding(boolean value) {
	dirty = true;
	autoBuilding = value;
}
/**
 * @see IWorkspaceDescription
 */
public void setBuildOrder(String[] value) {
	dirty = true;
	buildOrder = (value == null) ? null : (String[]) value.clone();
}
public void setDeltaExpiration(long deltaExpiration) {
	dirty = true;
	this.deltaExpiration = deltaExpiration;
}
/**
 * @see IWorkspaceDescription
 */
public void setFileStateLongevity(long time) {
	dirty = true;
	fileStateLongevity = time;
}
/**
 * @see IWorkspaceDescription
 */
public void setMaxFileStates(int number) {
	dirty = true;
	maxFileStates = number;
}
/**
 * @see IWorkspaceDescription
 */
public void setMaxFileStateSize(long size) {
	dirty = true;
	maxFileStateSize = size;
}
public void setOperationsPerSnapshot(int operations) {
	dirty = true;
	operationsPerSnapshot = operations;
}
public void setSnapshotEnabled(boolean enable) {
	dirty = true;
	snapshotsEnabled = enable;
}
}
