/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.NullSubProgressMonitor;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * This abstract class implements the state of a local and corresponding remote resource,
 * and behavior of those resources.
 * <p>
 * Common state for all 'managed' resources includes:
 * <ul>
 * 	<li>the local resource and corresponding remote resource objects</li>
 * 	<li>the timestamp of the local resource as it was last in synch with the provider</li>
 * 	<li>an identifier for the remote resource as it was last in synch with the provider</li>
 */
public abstract class ResourceState {

	/*
	 * Serialization format identifier.
	 * see toBytes() and fromBytes()
	 */
	private static final int BYTES_FORMAT = 0;

	/*
	 * These constants are used to indicate uninitialized values for the local
	 * base timestamp and remote base identifier.
	 */
	protected static final long EMPTY_LOCALBASETS = -1L;
	protected static final String EMPTY_REMOTEBASEID = "Undefined:"; //$NON-NLS-1$

	/*
	 * The base state of the resource.  The 'base' is the state of the resource
	 * state that was fetched from (or put in) the provider.
	 */
	protected long localBaseTimestamp = EMPTY_LOCALBASETS;
	protected String remoteBaseIdentifier = EMPTY_REMOTEBASEID;

	protected boolean checkedOut = true;

	/*
	 * This is the local resource that the receiver represents.  It is initialized by
	 * the constructor.  (The remote resource is maintained by specific subclasses
	 * as it is type-dependent.)
	 */
	protected IResource localResource;
	
	protected QualifiedName stateKey = new QualifiedName("org.eclipse.team.target", "state_info"); //$NON-NLS-1$ //$NON-NLS-2$

	protected URL rootUrl;

	/**
	 * Constructor for a resource state given a local resource.
	 * Remember which local resource this state represents.
	 * 
	 * @param localResource the local part of a synchronized pair of resources.
	 */
	public ResourceState(IResource localResource, URL rootUrl) {
		super();
		this.rootUrl = rootUrl;
		SynchronizedTargetProvider.getSynchronizer().add(stateKey);
		this.localResource = localResource;
	}
	
	/**
	 * Get the timestamp that represents the base state of the local resource, that is
	 * the state that the local resource had when it was initially fetched from the repository.
	 * 
	 * @return the timestamp of the local state of the resource (as reported by
	 * java.io.File.getLastModified()) at the point the resource was downloaded to the
	 * workspace.
	 * @throws BaseIdentifierNotInitializedException if the resource has not yet been
	 * downloaded.
	 */
	public long getLocalBaseTimestamp()
		throws BaseIdentifierNotInitializedException {
		if (localBaseTimestamp == EMPTY_LOCALBASETS)
			throw new BaseIdentifierNotInitializedException();
		return localBaseTimestamp;
	}

	
	/**
	 * Get the identifier that represents the base state of the remote resource, that is
	 * the state of the remote resource when it was fetched as the base state of the
	 * local resource.
	 * <p>
	 * In general, repositories have arbitrary ways to distinguish resource states.
	 * The result should only be used for equality comparison, there should be no
	 * ordering or other information implied from the value returned.  For example,
	 * the value may be a version identifier, timestamp, ETag, etc.  To ensure
	 * schemes do not inadvertantly test equal it is recommended that the identifier
	 * be a URI where the scheme denotes the value type,
	 * e.g., date-rfc1123:Fri, 16 Nov 2001 06:25:24 GMT</p>
	 * 
	 * @return an opaque identifier to the base state of the resource in the provider.
	 * @throws BaseIdentifierNotInitializedException if the resource has not yet been
	 * downloaded.
	 */
	public String getRemoteBaseIdentifier()
		throws BaseIdentifierNotInitializedException {
		if (remoteBaseIdentifier.equals(EMPTY_REMOTEBASEID))
			throw new BaseIdentifierNotInitializedException();
		return remoteBaseIdentifier;
	}

	
	/**
	 * Get the identifier that represents the released state of the resource,
	 * that is the state that it currently has in the repository.
	 * <p>
	 * In general, repositories have arbitrary ways to distinguish resource states.
	 * The result should only be used for equality comparison, there should be no
	 * ordering or other information implied from the value returned.  For example,
	 * the value may be a version identifier, timestamp, ETag, etc.  To ensure
	 * schemes do not inadvertantly test equal it is recommended that the identifier
	 * be a URI where the scheme denotes the value type,
	 * e.g., date-rfc1123:Fri, 16 Nov 2001 06:25:24 GMT</p>
	 * 
		 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @return an opaque identifier to the current released state of the resource in
	 * the provider.
	 * @throws TeamException if there is a problem getting the released state
	 * identifier from the provider.  Valid exception status codes include:
	 * <ul>
	 * 		<li>IO_FAILED</li>
	 * 		<li>NO_REMOTE_RESOURCE</li>
	 * </ul></p>
	 */
	public abstract String getReleasedIdentifier(IProgressMonitor monitor) throws TeamException;

	/**
	 * Check out the receiver. Return a status if the receiver is in the wrong state for the operation to be performed.
	 * 
	 * @throws TeamException if there is a error communicating with the resource from the server.
	 */
	public void checkout(IProgressMonitor progress) throws TeamException {
		progress.beginTask(null, 100);
		try {
			// Not going to allow branching.
			if (isOutOfDate(Policy.subMonitorFor(progress, 50)))
				throw new TeamException(ITeamStatusConstants.CONFLICT_STATUS);
			
			// Sanity check.
			if (!hasRemote(Policy.subMonitorFor(progress, 50)))
				throw new TeamException(ITeamStatusConstants.NO_REMOTE_RESOURCE_STATUS);
			
			// Legally, the resource must be checked in before it can be checked out.
			if (isCheckedOut())
				throw new TeamException(ITeamStatusConstants.NOT_CHECKED_IN_STATUS);
			
			// Do the provider specific action for check-out.
			basicCheckout(progress);
		} finally {
			progress.done();
		}
	}
	
	/**
	 * A basic checkout is provider specific.
	 * Unless overridden, work in an optimistic mode.
	 */
	protected void basicCheckout(IProgressMonitor progress) throws TeamException {
		checkedOut = true;
	}
	
	
	/**
	 * Check in the receiver.
	 * 
	 * @throws TeamException if there is a error communicating with the resource from the server.
	 */
	public void checkin(IProgressMonitor progress) throws TeamException {
		progress = Policy.monitorFor(progress);
		progress.beginTask(null, 100);
		try {
			// The resource must be checked out before it can be checked in.
			if (!isCheckedOut())
				throw new TeamException(ITeamStatusConstants.NOT_CHECKED_OUT_STATUS);
				
			if (!hasLocal()) {
				if (hasRemote(Policy.subMonitorFor(progress, 10))) {
					delete(Policy.subMonitorFor(progress, 80));
				}
			} else {
				// Ensure the necessary remote direcotories exist
				mkRemoteDirs(Policy.subMonitorFor(progress, 10));
				// Copy from the local resource to the repository.
				if (getLocal().getType() == IResource.FILE) {
					upload(Policy.subMonitorFor(progress, 80));
				}
			}
			//if we got to here the upload succeeded (didn't throw)
			checkedOut = false;
		} finally {
			progress.done();
		}
	}
	
	/**
	 * Uncheckout the receiver.
	 */
	public void uncheckout(IProgressMonitor progress) throws TeamException {
		// Has to be checked-out before it can be reversed.
		if (!isCheckedOut())
			throw new TeamException(ITeamStatusConstants.NOT_CHECKED_OUT_STATUS);

		// Nothing interesting to do since the API spec. requires that we do not reverse
		// any local changes.
		checkedOut = false;
	}

	/**
	 * Answer whether the receiver is checked out or not.
	 * <p>
	 * Note that this is a quick operation that will be called from the UI, so providers are required
	 * to cache information that is expensive to compute.  Where the cache may get stale users
	 * have the opportunity to force a refresh using ITeamProvider.refreshState().
	 * 
	 * @return <code>true</code> if the receiver is checked in, and <code>false</code>
	 * if it is not.
	 * @see ITeamProvider#isCheckedOut(IResource)
	 * @see ITeamProvider#refreshState(IResource[], int, IProgressMonitor)
	 */
	public boolean isCheckedOut() {
		return checkedOut;
	}

	/**
	 * Answer if the local resource currently has a different timestamp to the
	 * base timestamp for this resource.
	 * 
	 * @return <code>true</code> if the resource has a different modification
	 * timestamp, and <code>false</code> otherwise.
	 * @see ITeamProvider#isDirty(IResource)
	 */
	public boolean isDirty() {
		if (!hasLocal())
			return hasPhantom();
		if (localBaseTimestamp == EMPTY_LOCALBASETS)
			return localResource.getType() == IResource.FILE;
		return localBaseTimestamp != localResource.getModificationStamp();
	}

	/**
	 * Answers true if the base identifier of the given resource is different to the
	 * current released state of the resource.
	 */
	public boolean isOutOfDate(IProgressMonitor monitor) throws TeamException {
		if (remoteBaseIdentifier.equals(EMPTY_REMOTEBASEID))
			 return (localResource.getType() == IResource.FILE && hasRemote(monitor));

		String releasedIdentifier = null;
		releasedIdentifier = getReleasedIdentifier(monitor);
		return !remoteBaseIdentifier.equals(releasedIdentifier);
	}

	/**
	 * Download the remote resource represented by the receiver state to the location
	 * represented by the local resource (i.e., resource.getLocation().toFile()).
	 * This copies from the provider to the workspace, <em>and</em> sets the local
	 * base timestamp and remote base identifier.
	 * The provider may (and should wherever possible) optimize the case where it
	 * knows the local resource is identical to the remote resource.
	 */
	public abstract void download(IProgressMonitor progress) throws TeamException;

	/**
	 * Upload the resource represented by the local resource to the remote
	 * resource represented by the receiver.  This copies from the workspace to
	 * the provider <em>and</em> sets the local base timestamp and remote base
	 * identifier.
	 */
	public abstract void upload(IProgressMonitor progress) throws TeamException;

	/**
	 * Delete the remote resource.
	 */
	public abstract void delete(IProgressMonitor progress) throws TeamException;

	/**
	 * Answer if the remote resource exists.
	 */
	public abstract boolean hasRemote(IProgressMonitor monitor) throws TeamException;

	/**
	 * Answer the type of the remote resource (if it exists).
	 * The type should correspond to the IResource enumerated types.
	 */
	public abstract int getRemoteType();

	/**
	 * Answer the array of resource states for each member of the receiver.
	 * If the receiver has no members (or is incapable of having members)
	 * answer an empty array.
	 */
	public abstract ResourceState[] getRemoteChildren(IProgressMonitor monitor) throws TeamException;

	/**
	 * Create the necessary remote directories corresponding to the local resource.
	 * That is, if the resource is a folder, create it and its parents if they don't
	 * already exist. If the resource is a file, create its parents if they don't
	 * already exist.
	 */
	protected abstract void mkRemoteDirs(IProgressMonitor monitor) throws TeamException;
	
	/**
	 */
	public IResource getLocal() {
		return localResource;
	}

	/**
	 * Get the file underlying the local resource.
	 */
	protected File getLocalFile() {
		return localResource.getLocation().toFile();
	}

	/**
	 * Answer if the local resource exists.
	 */
	protected boolean hasLocal() {
		return localResource.exists();
	}
	
	/**
	 * Answer if the local resource has a phantom, which indicates that the respource had both a local 
	 * and remote version at one time.
	 */
	protected boolean hasPhantom() {
		try {
			return SynchronizedTargetProvider.getSynchronizer().getSyncInfo(stateKey, localResource) != null;
		} catch (CoreException e) {
			TeamPlugin.log(e.getStatus());
			return false;
		}
	}

	/**
	 * Initializes the resource state instance from the given serialized state.
	 * The format of the serialized state is that produced by <code>toBytes()</code>.
	 * 
	 * @param bytes the serialized resource state.
	 */
	public final void loadState() throws TeamException {
		try {
			byte[] storedState =
				SynchronizedTargetProvider.getSynchronizer().getSyncInfo(stateKey, localResource);
			if (storedState != null)
				fromBytes(storedState);
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
	}

	/**
	 * Initializes the resource state instance from the given serialized state.
	 * The format of the serialized state is that produced by <code>toBytes()</code>.
	 * 
	 * @param bytes the serialized resource state.
	 */
	protected void fromBytes(byte[] bytes) throws TeamException{
		try {
			DataInputStream dataStream =
				new DataInputStream(new ByteArrayInputStream(bytes));
			if (BYTES_FORMAT != dataStream.readByte())
				return;

			// Restore common resource state values.
			remoteBaseIdentifier = dataStream.readUTF();
			localBaseTimestamp = dataStream.readLong();

		} catch (IOException e) {
			throw TeamPlugin.wrapException(e);
		}
	};

	public final void storeState() throws TeamException {
		try {
			SynchronizedTargetProvider.getSynchronizer().setSyncInfo(
				stateKey,
				localResource,
				toBytes());
			// Ensure that the parent has base info recorded (otherwise deleting the parent will cause the lose of sync info)
			if (localResource.getType() == IResource.PROJECT) return;
			IContainer parent = localResource.getParent();
			if (parent != null && parent.getType() != IResource.PROJECT &&
				SynchronizedTargetProvider.getSynchronizer().getSyncInfo(stateKey, parent) == null) {
				getParent().storeState();
			} else {
				ResourcesPlugin.getWorkspace().save(false, null);
			}
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
	}

	/**
	 * Answer the resource state as a sequence of bytes, in a format that can be used to 
	 * reconstruct an equivalent resource state using the <code>fromBytes(byte[])</code>
	 * method.
	 * <p>
	 * Subclasses should implement <code>storeState(DataOutputStream)</code> to
	 * store provider specific state information.</p>
	 * 
	 * @return the resource state as a byte array.
	 * @see #storeState(DataOutputStream)
	 * @see fromBytes(byte[])
	 */
	protected byte[] toBytes() throws TeamException {
		try {
			// Create a stream to store the byte representation of the receiver's state.
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(32);
			// Guess ~32 bytes
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			dataStream.writeByte(BYTES_FORMAT);

			// Store data common to all resource states.
			dataStream.writeUTF(remoteBaseIdentifier);
			dataStream.writeLong(localBaseTimestamp);

			dataStream.close();
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw TeamPlugin.wrapException(e);
		}
	}

	final public void removeState() throws TeamException {
		try {
			if (localResource.exists() || localResource.isPhantom()) {
				SynchronizedTargetProvider.getSynchronizer().flushSyncInfo(
					stateKey,
					localResource,
					IResource.DEPTH_INFINITE);
			}
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * Method getRoot.
	 * @return URL of this resource's parent
	 */
	public URL getRoot() {
		return rootUrl;
	}
	
	private ResourceState getParent() throws TeamException {
		return getResourceStateFor(localResource.getParent());
	}
	
	private ResourceState getResourceStateFor(IResource resource) throws TeamException {
		TargetProvider provider = TargetManager.getProvider(resource.getProject());
		return ((SynchronizedTargetProvider)provider).getState(resource);
	}
	
	/**
	 * Get the resource corresponding to the receiver to the specified depth.
	 */
	protected final void get(int depth, IProgressMonitor progress) throws TeamException {

		progress = Policy.monitorFor(progress);
		
		// the no progress monitor is used to control the progress given to
		// other methods and ensures that the progress monitor is not overloaded
		// with subtask messages and work. The null monitor does propagate 
		// cancellation.
		IProgressMonitor noProgress = new NullSubProgressMonitor(progress);
		try {
			progress.beginTask(null, 100);
			Policy.checkCanceled(progress);
			
			// If remote does not exist then simply ensure no local resource exists.			
			if (!hasRemote(noProgress)) {
				if (hasLocal()) 
					deleteLocal(noProgress);
				return;
			}
			
			// Ensure that the required local folders exist
			if (!hasLocal()) {
				mkLocalDirs(noProgress);
			}
			
			// If the remote resource is a file, download the remote contents
			if (getRemoteType() == IResource.FILE) {
				download(Policy.subMonitorFor(progress, 100));
				return;
			}
			
			// The remote resource is a container.
			
			// If the local resource is a file, we must remove it first.
			if (getLocal().getType() == IResource.FILE) {
				if (hasLocal()) {
					deleteLocal(noProgress); // May not exist.
				}
				// change the local resource to a folder and create it
				localResource = localResource.getParent().getFolder(new Path(localResource.getName()));
				mkLocalDirs(noProgress);
			}
			
			// Finally, resolve the collection membership based upon the depth parameter.
			switch (depth) {
				case IResource.DEPTH_ZERO :
					// If we are not considering members of the collection then we are done.
					return;
				case IResource.DEPTH_ONE :
					// If we are considering only the immediate members of the collection
					getFolderShallow(Policy.subMonitorFor(progress, 100));
					return;
				case IResource.DEPTH_INFINITE :
					// We are going in deep.
					getFolderDeep(Policy.subMonitorFor(progress, 100));
					return;
				default :
					// We have covered all the legal cases.
					Assert.isLegal(false);
					return; // Never reached.
			} // end switch
		} finally {
			progress.done();
		}
	}
	
	/**
	 * Get the folder resource represented by the receiver deeply.
	 */
	protected final void getFolderDeep(IProgressMonitor progress) throws TeamException {
		progress = Policy.monitorFor(progress);
		try {
			progress.beginTask(null, 10);
			// Could throw if problem getting the folder at this level.
			ResourceState[] childFolders = getFolderShallow(Policy.subMonitorFor(progress, 7));
	
			// If there are no further children then we are done.
			if (childFolders.length == 0)
				return;
	
			IProgressMonitor subProgress = Policy.subMonitorFor(progress, 3);
			// Collect the responses in the multistatus.
			try {
				subProgress.beginTask(null, childFolders.length);
				for (int i = 0; i < childFolders.length; i++) {
					childFolders[i].get(IResource.DEPTH_INFINITE, Policy.subMonitorFor(subProgress, 1));
				}
			} finally {
				subProgress.done();
			}

			return;
		} finally {
			progress.done();
		}
	}
	
	/**
	 * Synchronize from the remote provider to the workspace.
	 * Assume that the 'remote' folder is correct, and change the local
	 * folder to look like the remote folder.
	 * 
	 * returns an array of children of the remote resource that are themselves
	 * collections.
	 */
	protected final ResourceState[] getFolderShallow(IProgressMonitor progress) throws TeamException {
		progress = Policy.monitorFor(progress);
		IProgressMonitor noProgress = new NullProgressMonitor();
		try {
			// We are assuming that the resource is a container.
			Assert.isLegal(getLocal() instanceof IContainer);
			IContainer localContainer = (IContainer)getLocal();
	
			// Get list of all _remote_ children.
			ResourceState[] remoteChildren = getRemoteChildren(noProgress);
	
			// This will be the list of remote children that are themselves containers.
			Set remoteChildFolders = new HashSet();
	
			// Make a list of _local_ children that have not yet been processed,
			IResource[] localChildren = getLocalChildren();
			Set surplusLocalChildren = new HashSet(localChildren.length);
			surplusLocalChildren.addAll(Arrays.asList(localChildren));
	
			progress.beginTask(null, remoteChildren.length * 100);
			// For each remote child that is a file, make the local file content equivalent.
			for (int i = 0; i < remoteChildren.length; i++) {
				Policy.checkCanceled(progress);
				ResourceState remoteChildState = remoteChildren[i];
				// If the remote child is a container add it to the list, and ensure that the local child
				// is a folder if it exists.
				if (remoteChildState.getRemoteType() == IResource.FILE) {
					// The remote resource is a file.  Copy the content of the remote file
					// to the local file, overwriting any existing content that may exist, and
					// creating the file if it doesn't.
					remoteChildState.download(Policy.subMonitorFor(progress, 100));
					// Remember that we have processed this child.
					surplusLocalChildren.remove(remoteChildState.getLocal());
				} else {
					// The remote resource is a container.
					remoteChildFolders.add(remoteChildState);
					// If the local child is not a container then it must be deleted.
					IResource localChild = remoteChildState.getLocal();
					if (localChild.exists() && (!(localChild instanceof IContainer)))
						remoteChildState.deleteLocal(noProgress);
				} // end if
			} // end for
	
			// Remove each local child that does not have a corresponding remote resource.
			TargetProvider provider = TargetManager.getProvider(localContainer.getProject());
			Iterator childrenItr = surplusLocalChildren.iterator();
			while (childrenItr.hasNext()) {
				IResource unseenChild = (IResource) childrenItr.next();
				((SynchronizedTargetProvider)provider).newState(unseenChild).deleteLocal(noProgress);
			} // end-while
	
			// Answer the array of children seen on the remote collection that are
			// themselves collections (to support depth operations).
			return (ResourceState[]) remoteChildFolders.toArray(
				new ResourceState[remoteChildFolders.size()]);
		} finally {
			progress.done();
		}
	}

	/**
	 * Delete the local resource represented by the resource state.  Do not complain if the resource does not exist.
	 */
	protected final void deleteLocal(IProgressMonitor progress) throws TeamException {
		try {
			getLocal().delete(IResource.KEEP_HISTORY, progress);
			removeState();
		} catch (CoreException exception) {
			throw TeamPlugin.wrapException(exception);
		}
	}
	
	/**
	 * Make the local directories matching the description of the local resource state.
	 */
	protected final void mkLocalDirs(IProgressMonitor progress) throws TeamException {	
		try {
			IResource resource = getLocal();
			if (resource.getType() == IResource.FILE) {
				resource = resource.getParent();
			}
			if (resource.getType() == IResource.FOLDER && ! resource.exists()) {
				((IFolder)resource).create(false /* force */, true /* make local */, progress);
				// Mark the folders as having a base
				storeState();
			}

		} catch (CoreException exception) {
			// The creation failed.
			throw TeamPlugin.wrapException(exception);
		}
	}
	
	/**
	 * Get an array of local children of the given container, or an empty array if the
	 * container does not exist or has no children.
	 */
	protected final IResource[] getLocalChildren() throws TeamException {
		// We are assuming that the resource is a container.
		Assert.isLegal(getLocal() instanceof IContainer);
		IContainer container = (IContainer)getLocal();
		if (container.exists())
			try {
				return container.members();
			} catch (CoreException exception) {
				throw TeamPlugin.wrapException(exception);
			}
		return new IResource[0];
	}
	
	/**
	 * Put the resource from the workspace to the remote provider.
	 * Assume that the 'local' resource is correct, and change the remote
	 * resource to look like the local resource. This includes removing any
	 * child resources that exist remotely but do not exist locally.
	 */
	protected final void put(IProgressMonitor progress) throws TeamException {

		progress = Policy.monitorFor(progress);
		
		// the no progress monitor is used to control the progress given to
		// other methods and ensures that the progress monitor is not overloaded
		// with subtask messages and work. The null monitor does propagate 
		// cancellation.
		IProgressMonitor noProgress = new NullSubProgressMonitor(progress);
		try {
			// Check cancellation
			progress.beginTask(null, 100);
			Policy.checkCanceled(progress);
	
			// Ensure that the remote type matches the local type
			boolean hasRemote = hasRemote(noProgress);
			if ((getRemoteType() != localResource.getType() && localResource.getType() != IResource.PROJECT)) {
				if (hasRemote) delete(noProgress);
				hasRemote = false;
			}
					
			// Upload the resource (this is a shallow operation for folders)			
			checkin(Policy.subMonitorFor(progress, 75));
			
			// If we're putting a file, we're done
			if (localResource.getType() == IResource.FILE) return;
			
			// If the local doesn't exist then we just deleted the remote so we're done
			if (!hasLocal()) return;
	
			// Make a list of _remote_ children that have not yet been processed,
			Map surplusRemoteChildren = new HashMap();
			if (hasRemote) {
				ResourceState[] remoteChildren = remoteChildren = getRemoteChildren(progress);
				for (int i = 0; i < remoteChildren.length; i++) {
					ResourceState resourceState = remoteChildren[i];
					surplusRemoteChildren.put(resourceState.getLocal(), resourceState);
				}
			}
	
			// For each local child that is a file, make the remote file content equivalent.
			IResource[] localChildren = getLocalChildren();
			IProgressMonitor subMonitor = Policy.subMonitorFor(progress, 25);
			try {
				subMonitor.beginTask(null, localChildren.length * 100);
				for (int i = 0; i < localChildren.length; i++) {
					IResource localChild = localChildren[i];
					// Get the resource state corresponding to the local resource
					ResourceState state = (ResourceState)surplusRemoteChildren.get(localChild);
					if (state == null) {
						// There is no remote corresponding to the local
						state = getResourceStateFor(localChild);
					} else {
						// There is a remote. Remember that we have processed this child.
						surplusRemoteChildren.remove(localChild);
					}
					// Put the child (this is a deep operation for folders)
					state.put(Policy.subMonitorFor(subMonitor, 100));
				}
			} finally {
				subMonitor.done();
			}
	
			// Remove each remote child that does not have a corresponding local resource.
			Iterator childrenItr = surplusRemoteChildren.values().iterator();
			while (childrenItr.hasNext()) {
				ResourceState unseenChild = (ResourceState) childrenItr.next();
				unseenChild.delete(noProgress);
			}
		} finally {
			progress.done();
			
		}
	}
}