package org.eclipse.team.core.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.core.TeamException;

/**
 * A <code>ILocalSyncElement</code> describes the relative synchronization of a <b>local</b> 
 * and <b>remote</b> resource using a <b>base</b> resource for comparison.
 * <p>
 * Differences between the base and remote resources are classified as <b>incoming changes</b>; 
 * if there is a difference, the remote resource is considered the <b>incoming resource</b>. </p>
 * 
 * @see ILocalSyncElement
 * @see ISyncProvider
 */
public interface IRemoteSyncElement extends ILocalSyncElement {
	
	/**
	 * Answer the remote sync element of this node. Returns <code>null</code> 
	 * if there is no remote.
	 * 
	 * @return the remote resource in this sync element, or <code>null</code> is there
	 * is none.
	 */
	public IRemoteResource getRemote();

	/**
	 * Answers <code>true</code> if the base of the given resource is different to the 
	 * released state of the given resource. If a base does not exist then this method must
	 * return <code>false</code>.
	 */
	public boolean isOutOfDate();
	
	/**
	 * Answers <code>true</code> if the base tree is not to be considered during sync
	 * comparisons and <code>false</code> if it should. If the base tree is ignored the
	 * sync comparison can be based on isOutOfDate and isDirty methods only.
	 */
	public boolean isThreeWay();
}