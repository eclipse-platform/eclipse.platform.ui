package org.eclipse.team.core.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * A <code>ILocalSyncElement</code> describes the relative synchronization of a <b>local</b> 
 * resource using a <b>base</b> resource for comparison.
 * <p>
 * Differences between the base and local resources are classified as <b>outgoing changes</b>; 
 * if there is a difference, the local resource is considered the <b>outgoing resource</b>. </p>
 * 
 * @see IRemoteSyncElement
 */
public interface ILocalSyncElement {
	
	/*====================================================================
	 * Constants defining synchronization types:  
	 *====================================================================*/

	/**
	 * Sync constant (value 0) indicating element is in sync.
	 */
	public static final int IN_SYNC = 0;
	
	/**
	 * Sync constant (value 1) indicating that one side was added.
	 */
	public static final int ADDITION = 1;
	
	/**
	 * Sync constant (value 2) indicating that one side was deleted.
	 */
	public static final int DELETION = 2;
	
	/**
	 * Sync constant (value 3) indicating that one side was changed.
	 */
	public static final int CHANGE = 3;

	/**
	 * Bit mask for extracting the change type.
	 */
	public static final int CHANGE_MASK = CHANGE;
	
	/*====================================================================
	 * Constants defining synchronization direction: 
	 *====================================================================*/
	
	/**
	 * Sync constant (value 4) indicating a change to the local resource.
	 */
	public static final int OUTGOING = 4;
	
	/**
	 * Sync constant (value 8) indicating a change to the remote resource.
	 */
	public static final int INCOMING = 8;
	
	/**
	 * Sync constant (value 12) indicating a change to both the remote and local resources.
	 */
	public static final int CONFLICTING = 12;
	
	/**
	 * Bit mask for extracting the synchronization direction. 
	 */
	public static final int DIRECTION_MASK = CONFLICTING;
	
	/*====================================================================
	 * Constants defining synchronization conflict types:
	 *====================================================================*/
	
	/**
	 * Sync constant (value 16) indication that both the local and remote resources have changed 
	 * relative to the base but their contents are the same. 
	 */
	public static final int PSEUDO_CONFLICT = 16;
	
	/**
	 * Sync constant (value 32) indicating that both the local and remote resources have changed 
	 * relative to the base but their content changes do not conflict (e.g. source file changes on different 
	 * lines). These conflicts could be merged automatically.
	 */
	public static final int AUTOMERGE_CONFLICT = 32;
	
	/**
	 * Sync constant (value 64) indicating that both the local and remote resources have changed relative 
	 * to the base and their content changes conflict (e.g. local and remote resource have changes on 
	 * same lines). These conflicts can only be correctly resolved by the user.
	 */
	public static final int MANUAL_CONFLICT = 64;
	
	/*====================================================================
	 * Constants defining synchronization granularity:
	 *====================================================================*/	
	 
	/**
	 * Constant (value 1) to only consider timestamp comparisons (e.g. isDirty) when 
	 * calculating the synchronization kind. This is the faster sync compare option but it can result in false 
	 * conflicts.
	 */
	public static final int GRANULARITY_TIMESTAMP = 1;

	/**
	 * Constant (value 2) indicating to consider file contents when calculating the synchronization kind. This 
	 * synchronization mode will perform a content comparison only after timestamp operations (isDirty) 
	 * indicate a change. This mode allows conflicts types to be correctly identified.
	 */
	public static final int GRANULARITY_CONTENTS = 2;
	
	/**
 	 * Answer a string that describes the simple name of the sync node,  which is suitable 
 	 * for display to a user.  The name will be used in UI operations, so it is expected that 
 	 * implementations will cache this value.
 	 * 
	 * @return the simple name that identifies the resource within its parent container.
	 */
	public String getName();
	
	/**
	 * Answer if the sync node is a container and may have children.
	 * 
 	 * @return <code>true</code> if the remote resource is a container, and <code>
	 * false</code> if it is not.
	 */
	public boolean isContainer();

	/**
	 * Answers the local sync element of this node. Returns a non-existing local
	 * resource handle if the local resource does not exist in the workspace. 
	 * 
	 * @return the local resource handle in this node. There should always be a local
	 * resource available, however the resource may not exist.
	 */
	public IResource getLocal();

	/**
	 * Answers the base sync element of this node. Returns <code>null</code> 
	 * if there is no base (e.g. conflicting add).
	 * 
	 * @return the base resource in this node, or <code>null</code> is there
	 * is none.
	 */
	public IRemoteResource getBase();
	
	/**
	 * Answers and array of <code>ILocalSyncElement</code> elements that are immediate 
	 * children of this sync element, in no particular order. The returned sync nodes are
	 * a combination of the nodes represented by the sync element (e.g. local, base, remote).
	 * 
 	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @return array of immediate children of this sync node. 
	 */
	public ILocalSyncElement[] members(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Performs a synchronization calculation on the given element based on the local and base 
	 * resources. Returns an integer describing the synchronization state of this element.
	 * 
	 * @param granularity the granularity at which the elements of this sync element
	 * should be compared. On of <code>GRANULARITY_TIMESTAMP</code>, or 
	 * <code>GRANULARITY_CONTENTS</code>.
 	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @return an integer describing the synchronization state of this element.
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress);	
}