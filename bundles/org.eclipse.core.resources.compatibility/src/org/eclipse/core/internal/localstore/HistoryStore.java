/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.internal.indexing.*;
import org.eclipse.core.internal.properties.IndexedStoreWrapper;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.utils.Convert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

public class HistoryStore implements IHistoryStore {
	protected Workspace workspace;
	protected BlobStore blobStore;
	IndexedStoreWrapper store;
	Set blobsToRemove = new HashSet();

	/* package */final static String INDEX_FILE = ".index"; //$NON-NLS-1$

	public HistoryStore(Workspace workspace, IPath location, int limit) {
		this.workspace = workspace;
		this.blobStore = new BlobStore(EFS.getLocalFileSystem().getStore(location), limit);
		this.store = new IndexedStoreWrapper(location.append(INDEX_FILE));
	}

	/**
	 * Searches indexed store for key, and invokes visitor's defined behaviour on key matches.
	 *
	 * @param key key prefix on which to perform search.  This is assumed to be
	 *      a path only unless the flag includeLastModTime is true.
	 * @param visitOnPartialMatch indicates whether visitor's defined behavior is to be invoked
	 *		on partial or full key matches.  Partial key matches are not supported on keys which
	 *      contain a last modified time.
	 * @param includeLastModTime indicates if the key includes a last modified
	 *      time.  If set to false, the key is assumed to have only a path.
	 */
	protected void accept(byte[] key, IHistoryStoreVisitor visitor, boolean visitOnPartialMatch, boolean includeLastModTime) {
		try {
			IndexCursor cursor = store.getCursor();
			cursor.find(key);
			// Check for a prefix match.
			while (cursor.keyMatches(key)) {
				byte[] storedKey = cursor.getKey();

				int bytesToOmit = includeLastModTime ? ILocalStoreConstants.SIZE_COUNTER : ILocalStoreConstants.SIZE_KEY_SUFFIX;
				// visit if we have an exact match
				if (storedKey.length - bytesToOmit == key.length) {
					HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
					if (!visitor.visit(storedEntry))
						break;
					cursor.next();
					continue;
				}

				// return if we aren't checking partial matches
				if (!visitOnPartialMatch) {
					cursor.next();
					continue;
				}

				// if the last character of the key is a path
				// separator or if the next character in the match
				// is a path separator then visit since it is a child
				// based on path segment matching.
				byte b = storedKey[key.length];
				if (key[key.length - 1] == 47 || b == 47) {
					HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
					if (!visitor.visit(storedEntry))
						break;
				}
				cursor.next();
			}
			cursor.close();
		} catch (Exception e) {
			String message = CompatibilityMessages.history_problemsAccessing;
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_READ_LOCAL, null, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	protected void accept(IPath path, IHistoryStoreVisitor visitor, boolean visitOnPartialMatch) {
		accept(Convert.toUTF8(path.toString()), visitor, visitOnPartialMatch, false);
	}

	/**
	 * Adds state into history log.
	 *
	 * @param path Full workspace path to the resource being logged.
	 * @param uuid UUID for stored file contents.
	 * @param lastModified Timestamp for resource being logged.
	 */
	protected void addState(IPath path, UniversalUniqueIdentifier uuid, long lastModified) {
		// Determine how many states already exist for this path and timestamp.
		class BitVisitor implements IHistoryStoreVisitor {
			BitSet bits = new BitSet();

			public boolean visit(HistoryStoreEntry entry) {
				bits.set(entry.getCount());
				return true;
			}

			public byte useNextClearBit(byte[] key) {
				// Don't use an empty slot as this will put this state
				// out of order relative to the other states with the same 
				// path and last modified time.  So find the first clear bit
				// after the last set bit.
				int nextBit = bits.length();
				// This value must fit in a byte.  If we are running off the
				// end of the byte, check to see if there are any empty bits
				// in the middle.  If so, reorganize the counters so we maintain
				// the ordering of the states but use up the least number
				// of bits (i.e., de-fragment the bit set).
				if (nextBit > Byte.MAX_VALUE) {
					if (bits.cardinality() < Byte.MAX_VALUE) {
						// We know we have some clear bits.
						try {
							IndexCursor cursor = store.getCursor();
							// destCount will always be the count value of the 
							// next key we want to assign a state to
							byte destCount = (byte) bits.nextClearBit(0);
							if (destCount < 0)
								// There are no clear bits
								return (byte) -1;
							// sourceCount will always be the count value of the
							// next key we want to move to destCount.  When
							// sourceCount is -1, there are no more source states
							// to move so we are done.
							byte sourceCount = (byte) bits.nextSetBit(destCount);
							if (sourceCount < 0)
								// There are no more states to move
								return destCount;
							byte[] completeKey = new byte[key.length + 1];
							System.arraycopy(key, 0, completeKey, 0, key.length);
							for (; sourceCount >= 0 && destCount >= 0; destCount++) {
								completeKey[completeKey.length - 1] = sourceCount;
								cursor.find(completeKey);
								if (cursor.keyMatches(completeKey)) {
									HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
									HistoryStoreEntry entryToInsert = new HistoryStoreEntry(storedEntry.getPath(), storedEntry.getUUID(), storedEntry.getLastModified(), destCount);
									remove(storedEntry);
									ObjectID valueID = store.createObject(entryToInsert.valueToBytes());
									store.getIndex().insert(entryToInsert.getKey(), valueID);
									sourceCount = (byte) bits.nextSetBit(sourceCount + 1);
								}
							}
							cursor.close();
							return destCount;
						} catch (Exception e) {
							String message = CompatibilityMessages.history_problemsAccessing;
							ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_READ_LOCAL, null, message, e);
							ResourcesPlugin.getPlugin().getLog().log(status);
						}
					} else {
						// Every count is being used.  Too many states.
						return (byte) -1;
					}
				}
				return (byte) nextBit;
			}
		}

		// Build partial key for which matches will be found.
		byte[] keyPrefix = HistoryStoreEntry.keyPrefixToBytes(path, lastModified);
		BitVisitor visitor = new BitVisitor();
		accept(keyPrefix, visitor, false, true);
		byte index = visitor.useNextClearBit(keyPrefix);
		try {
			if (index < 0) {
				String message = NLS.bind(CompatibilityMessages.history_tooManySimUpdates, path, new Date(lastModified));
				ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, path, message, null);
				ResourcesPlugin.getPlugin().getLog().log(status);
				return;
			}
			HistoryStoreEntry entryToInsert = new HistoryStoreEntry(path, uuid, lastModified, index);
			// valueToBytes just converts the uuid to byte form
			ObjectID valueID = store.createObject(entryToInsert.valueToBytes());
			store.getIndex().insert(entryToInsert.getKey(), valueID);
		} catch (Exception e) {
			resetIndexedStore();
			String message = NLS.bind(CompatibilityMessages.history_couldNotAdd, path);
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, path, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * @see IHistoryStore#addState(IPath, IFileStore, long, boolean)
	 */
	public IFileState addState(IPath key, IFileStore localFile, IFileInfo info, boolean moveContents) {
		long lastModified = info.getLastModified();
		if (Policy.DEBUG_HISTORY)
			System.out.println("History: Adding state for key: " + key + ", file: " + localFile + ", timestamp: " + lastModified + ", size: " + localFile.fetchInfo().getLength()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (!isValid(localFile))
			return null;
		UniversalUniqueIdentifier uuid = null;
		try {
			uuid = blobStore.addBlob(localFile, moveContents);
			addState(key, uuid, lastModified);
			store.commit();
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
		return new FileState(this, key, lastModified, uuid);
	}

	/**
	 * @see IHistoryStore#clean(IProgressMonitor)
	 */
	public void clean(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		int entryCount = 0;
		IWorkspaceDescription description = workspace.internalGetDescription();
		long minimumTimestamp = System.currentTimeMillis() - description.getFileStateLongevity();
		int max = description.getMaxFileStates();
		IPath current = null;
		List result = new ArrayList(Math.min(max, 1000));
		try {
			IndexCursor cursor = store.getCursor();
			cursor.findFirstEntry();
			while (cursor.isSet()) {
				entryCount++;
				HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
				// is it old?
				if (entry.getLastModified() < minimumTimestamp) {
					remove(entry);
					continue;
				}
				if (!entry.getPath().equals(current)) {
					removeOldestEntries(result, max);
					result.clear();
					current = entry.getPath();
				}
				result.add(entry);
				cursor.next();
			}
			removeOldestEntries(result, max);
			cursor.close();
			store.commit();
			if (Policy.DEBUG_HISTORY) {
				Policy.debug("Time to apply history store policies: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$
				Policy.debug("Total number of history store entries: " + entryCount); //$NON-NLS-1$
			}
			start = System.currentTimeMillis();
			// remove unreferenced blobs
			blobStore.deleteBlobs(blobsToRemove);
			if (Policy.DEBUG_HISTORY)
				Policy.debug("Time to remove " + blobsToRemove.size() + " unreferenced blobs: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			blobsToRemove = new HashSet();
		} catch (Exception e) {
			String message = CompatibilityMessages.history_problemsCleaning;
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	boolean stateAlreadyExists(IPath path, final UniversalUniqueIdentifier uuid) {
		final boolean[] rc = new boolean[] {false};
		IHistoryStoreVisitor visitor = new IHistoryStoreVisitor() {
			public boolean visit(HistoryStoreEntry entry) {
				if (rc[0] || uuid.equals(entry.getUUID())) {
					rc[0] = true;
					return false;
				}
				return true;
			}
		};
		accept(path, visitor, false);
		return rc[0];
	}

	/**
	 * @see IHistoryStore#copyHistory(IResource, IResource, boolean)
	 * @since  2.1
	 */
	public void copyHistory(final IResource sourceResource, final IResource destinationResource, boolean moving) {
		// Note that if any states in the local history for destination
		// have the same timestamp as a state for the local history
		// for source, the local history for destination will appear 
		// as an older state than the one for source.

		// return early if either of the paths are null or if the source and
		// destination are the same.
		if (sourceResource == null || destinationResource == null) {
			String message = CompatibilityMessages.history_copyToNull;
			ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message, null);
			ResourcesPlugin.getPlugin().getLog().log(status);
			return;
		}
		if (sourceResource.equals(destinationResource)) {
			String message = CompatibilityMessages.history_copyToSelf;
			ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, sourceResource.getFullPath(), message, null);
			ResourcesPlugin.getPlugin().getLog().log(status);
			return;
		}

		final IPath source = sourceResource.getFullPath();
		final IPath destination = destinationResource.getFullPath();
		// Note that if any states in the local history for destination
		// have the same timestamp as a state for the local history
		// for source, the local history for destination will appear 
		// as an older state than the one for source.

		// matches will be a list of all the places we add local history (without
		// any duplicates).
		final Set matches = new HashSet();

		IHistoryStoreVisitor visitor = new IHistoryStoreVisitor() {
			public boolean visit(HistoryStoreEntry entry) {
				IPath path = entry.getPath();
				int prefixSegments = source.matchingFirstSegments(path);
				// if there are no matching segments then we have an internal error...something
				// is wrong with the visitor
				if (prefixSegments == 0) {
					String message = NLS.bind(CompatibilityMessages.history_interalPathErrors, source, path);
					ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, source, message, null);
					ResourcesPlugin.getPlugin().getLog().log(status);
					return false;
				}
				path = destination.append(path.removeFirstSegments(prefixSegments));
				if (!stateAlreadyExists(path, entry.getUUID())) {
					matches.add(path);
					addState(path, entry.getUUID(), entry.getLastModified());
				}
				return true;
			}
		};

		// Visit all the entries. Visit partial matches too since this is a depth infinity operation
		// and we want to copy history for children.
		accept(source, visitor, true);

		// For each match, make sure we haven't exceeded the maximum number of
		// states allowed.
		IWorkspaceDescription description = workspace.internalGetDescription();
		int maxFileStates = description.getMaxFileStates();
		try {
			for (Iterator i = matches.iterator(); i.hasNext();) {
				List removeEntries = new LinkedList();
				IndexCursor cursor = store.getCursor();
				IPath path = (IPath) i.next();
				byte key[] = Convert.toUTF8(path.toString());
				cursor.find(key);
				// If this key is a match, grab the history store entry for it.
				// Don't need to worry about whether or not this is a full path
				// match as we know we used this path to add new state information
				// to the local history.
				while (cursor.keyMatches(key)) {
					removeEntries.add(HistoryStoreEntry.create(store, cursor));
					cursor.next();
				}
				cursor.close();
				removeOldestEntries(removeEntries, maxFileStates);
			}
		} catch (IndexedStoreException e) {
			String message = NLS.bind(CompatibilityMessages.history_problemsPurging, source, destination);
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_METADATA, source, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		} catch (CoreException e) {
			String message = NLS.bind(CompatibilityMessages.history_problemsPurging, source, destination);
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_METADATA, source, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}

		// We need to do a commit here.  The addState method we are
		// using won't commit store.  The public ones will.
		try {
			store.commit();
		} catch (CoreException e) {
			String message = NLS.bind(CompatibilityMessages.history_problemCopying, source, destination);
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_METADATA, source, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * @see IHistoryStore#exists(IFileState)
	 */
	public boolean exists(IFileState target) {
		return blobStore.fileFor(((FileState) target).getUUID()).fetchInfo().exists();
	}

	/**
	 * @see IHistoryStore#getContents(IFileState)
	 */
	public InputStream getContents(IFileState target) throws CoreException {
		if (!target.exists()) {
			String message = CompatibilityMessages.history_notValid;
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		return blobStore.getBlob(((FileState) target).getUUID());
	}

	/**
	 * @see IHistoryStore#getStates(IPath, IProgressMonitor)
	 */
	public IFileState[] getStates(final IPath key, IProgressMonitor monitor) {
		final int max = workspace.internalGetDescription().getMaxFileStates();
		final List result = new ArrayList(max);
		IHistoryStoreVisitor visitor = new IHistoryStoreVisitor() {
			public boolean visit(HistoryStoreEntry entry) {
				result.add(new FileState(HistoryStore.this, key, entry.getLastModified(), entry.getUUID()));
				return true;
			}
		};
		accept(key, visitor, false);
		if (result.isEmpty())
			return ICoreConstants.EMPTY_FILE_STATES;
		// put in the order of newer first
		IFileState[] states = new IFileState[result.size()];
		for (int i = 0; i < states.length; i++)
			states[i] = (IFileState) result.get(result.size() - (i + 1));
		return states;
	}

	/**
	 * Return a boolean value indicating whether or not the given file
	 * should be added to the history store based on the current history
	 * store policies.
	 * 
	 * @param localFile the file to check
	 * @return <code>true</code> if this file should be added to the history
	 * 	store and <code>false</code> otherwise
	 */
	private boolean isValid(IFileStore localFile) {
		WorkspaceDescription description = workspace.internalGetDescription();
		long length = localFile.fetchInfo().getLength();
		boolean result = length <= description.getMaxFileStateSize();
		if (Policy.DEBUG_HISTORY && !result)
			System.out.println("History: Ignoring file (too large). File: " + localFile.toString() + //$NON-NLS-1$
					", size: " + length + //$NON-NLS-1$
					", max: " + description.getMaxFileStateSize()); //$NON-NLS-1$
		return result;
	}

	protected void remove(HistoryStoreEntry entry) throws IndexedStoreException {
		try {
			Vector objectIds = store.getIndex().getObjectIdentifiersMatching(entry.getKey());
			if (objectIds.size() == 1) {
				store.removeObject((ObjectID) objectIds.get(0));
			} else if (objectIds.size() > 1) {
				// There is a problem with more than one entry having the same
				// key.
				String message = NLS.bind(CompatibilityMessages.history_tooManySimUpdates, entry.getPath(), new Date(entry.getLastModified()));
				ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, entry.getPath(), message, null);
				ResourcesPlugin.getPlugin().getLog().log(status);
			}
		} catch (Exception e) {
			String[] messageArgs = {entry.getPath().toString(), new Date(entry.getLastModified()).toString(), entry.getUUID().toString()};
			String message = NLS.bind(CompatibilityMessages.history_specificProblemsCleaning, messageArgs);
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
		// Do not remove the blob yet.  It may be referenced by another
		// history store entry.
		blobsToRemove.add(entry.getUUID());
		entry.remove();
	}

	/**
	 * Remove all the entries in the store.
	 */
	private void removeAll() {
		// TODO: should implement a method with a better performance
		try {
			IndexCursor cursor = store.getCursor();
			cursor.findFirstEntry();
			while (cursor.isSet()) {
				HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
				remove(entry);
			}
			cursor.close();
			store.commit();
		} catch (Exception e) {
			String message = NLS.bind(CompatibilityMessages.history_problemsRemoving, workspace.getRoot().getFullPath());
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, workspace.getRoot().getFullPath(), message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * @see IHistoryStore#remove(IPath, IProgressMonitor)
	 */
	public void remove(IPath path, IProgressMonitor monitor) {
		if (Path.ROOT.equals(path)) {
			removeAll();
			return;
		}
		try {
			IndexCursor cursor = store.getCursor();
			byte[] key = Convert.toUTF8(path.toString());
			cursor.find(key);
			while (cursor.keyMatches(key)) {
				HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
				remove(entry);
			}
			cursor.close();
			store.commit();
		} catch (Exception e) {
			String message = NLS.bind(CompatibilityMessages.history_problemsRemoving, path);
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, path, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * Go through the history store and remove all of the unreferenced blobs.
	 * Check the instance variable which holds onto a set of UUIDs of potential 
	 * candidates to be removed.
	 * 
	 * @see IHistoryStore#removeGarbage() 
	 */
	public void removeGarbage() {
		try {
			IndexCursor cursor = store.getCursor();
			cursor.findFirstEntry();
			while (!blobsToRemove.isEmpty() && cursor.isSet()) {
				HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
				blobsToRemove.remove(entry.getUUID());
				cursor.next();
			}
			cursor.close();
			blobStore.deleteBlobs(blobsToRemove);
			blobsToRemove = new HashSet();
		} catch (Exception e) {
			String message = CompatibilityMessages.history_problemsCleaning;
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	protected void removeOldestEntries(List entries, int maxEntries) throws IndexedStoreException {
		// do we have more states than necessary?
		if (entries.size() <= maxEntries)
			return;
		int limit = entries.size() - maxEntries;
		for (int i = 0; i < limit; i++)
			remove((HistoryStoreEntry) entries.get(i));
	}

	/**
	 * @see IManager#shutdown(IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) {
		if (store == null)
			return;
		store.close();
	}

	/**
	 * @see IManager#startup(IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) {
		// do nothing
	}

	protected void resetIndexedStore() {
		store.reset();
		java.io.File target = workspace.getMetaArea().getHistoryStoreLocation().toFile();
		Workspace.clear(target);
		target.mkdirs();
		String message = CompatibilityMessages.history_corrupt;
		ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message, null);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	/**
	 * @see IHistoryStore#allFiles(IPath, int, IProgressMonitor)
	 */
	public Set allFiles(IPath path, final int depth, IProgressMonitor monitor) {
		final Set allFiles = new HashSet();
		final int pathLength = path.segmentCount();
		class PathCollector implements IHistoryStoreVisitor {
			public boolean visit(HistoryStoreEntry state) {
				IPath memberPath = state.getPath();
				boolean withinDepthRange = false;
				switch (depth) {
					case IResource.DEPTH_ZERO :
						withinDepthRange = memberPath.segmentCount() == pathLength;
						break;
					case IResource.DEPTH_ONE :
						withinDepthRange = memberPath.segmentCount() <= pathLength + 1;
						break;
					case IResource.DEPTH_INFINITE :
						withinDepthRange = true;
						break;
				}
				if (withinDepthRange) {
					allFiles.add(memberPath);
				}
				// traverse children as long as we're still within depth range
				return withinDepthRange;
			}
		}
		accept(path, new PathCollector(), true);
		return allFiles;
	}

	/**
	 * @see IHistoryStore#getFileFor(IFileState)
	 */
	public File getFileFor(IFileState state) {
		if (!(state instanceof FileState))
			return null;
		return new java.io.File(blobStore.fileFor(((FileState) state).getUUID()).toString());
	}
}
