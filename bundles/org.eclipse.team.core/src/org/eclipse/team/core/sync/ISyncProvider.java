package org.eclipse.team.core.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * The <code>ISyncProvider</code> interface exposes a standard mechanism for retreiving
 * local (e.g. checkedout, dirty) and remote (outOfDate) synchronization information from a 
 * provider.
 * <p>
 * Local synchronization information includes only state for local resources without considering the 
 * remote contents. The sync provider API suggests that local sync information should be cached by 
 * a provider whereas remote sync information may not.</p>
 * <p>
 * Remote synchronization information considers the remote contents and allows for determining
 * incoming changes.
 * 
 * @see ILocalSyncElement
 * @see IRemoteSyncElement
 */
public interface ISyncProvider {
	
	/**
	 * Allows the provider to refresh the resource state information for a resource. This is mostly used
	 * to allow external provider tools to interoperate with the workbench provider. If an external tool
	 * modifies the state information for a resource this method can be called to update the provider's
	 * plug-in state information.
	 * <p>
	 * Some state information may be cached by the provider implementation to avoid server round 
	 * trips and allow responsive API calls via the element tree returned from <code>getLocalSyncTree</code>.  
	 * Where a caller is relying on this information being current, they should first explicitly refresh 
	 * the resouce state. Of course, there are no guarantees that  the refreshed information will not 
	 * become stale immediately after the call to this method.</p>
	 * <p>
	 * When resource state changes occur the provider must broadcast state change events 
	 * to allow all UI components the opportunity to update.</p>
	 * 
 	 * @param resources the local resource to be refreshed.
	 * @param depth the depth to traverse the given resources, taken from <code>IResource</code>
	 * constants.
	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws TeamProviderException if there is a problem refreshing one or more of
	 * the resources.  The exception will contain multiple statuses, one for each resource in the
	 * <code>resources</code> array.  Possible status codes include:
	 * <ul>
	 *			<li>IO_FAILED</li>
	 * 		<li>UNABLE</li>
	 * </ul>
	 */
	public void refreshLocalSync(IResource resource, int depth, IProgressMonitor progress) throws TeamException;
	
	/**
	 * Answers the local synchronization tree. This operation should be very responsive such that it can safely
	 * be called for UI decorations.
	 * 
	 * @param resource the local resource for which to get local synchronization information.
 	 * @param depth the depth to traverse the given resource, taken from <code>IResource</code>
	 * constants.
	 * 
	 * @return the local sync element that describes the resources sync state. If a depth > 0 was
	 * provided the returned <code>ILocalSyncElement</code> is a tree of local sync elements
	 * rooted at the specified local resource.
	 */
	public ILocalSyncElement getLocalSyncTree(IResource resource, int depth);
	
	/**
	 * Answers the remote synchronization tree. This operation can be long running.
	 * 
	 * @param resource the local resource for which to get remote synchronization information.
 	 * @param depth the depth to traverse the given resource, taken from <code>IResource</code>
	 * constants.
	 * 
	 * @return the remote sync element that describes the resources sync state. If a depth > 0 was
	 * provided the returned <code>IRemoteSyncElement</code> is a tree of remote sync elements
	 * rooted at the specified local resource.
	 * @throws TeamProviderException if there is a problem retrieving one or more of
	 * the remote resources.  Possible status codes include:
	 * <ul>
	 *			<li>IO_FAILED</li>
	 * 		<li>UNABLE</li>
	 * </ul>
	 */
	public IRemoteSyncElement getRemoteSyncTree(IResource resource, int depth, IProgressMonitor progress) throws TeamException;
}