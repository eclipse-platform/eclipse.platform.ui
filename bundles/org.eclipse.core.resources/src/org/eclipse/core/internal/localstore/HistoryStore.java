package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.properties.IndexedStoreWrapper;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.indexing.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class HistoryStore {
	protected Workspace workspace;
	protected IPath location;
	protected BlobStore blobStore;
	private IndexedStoreWrapper store;
	private final static String INDEX_FILE = ".index";

public HistoryStore(Workspace workspace, IPath location, int limit) {
	this.workspace = workspace;
	blobStore = new BlobStore(location, limit);
	store = new IndexedStoreWrapper(location.append(INDEX_FILE));
}
/**
 * Searches indexed store for key, and invokes visitor's defined behaviour on key matches.
 *
 * @param key key prefix on which to perform search.
 * @param visitOnPartialMatch indicates whether visitor's definined behavior is to be invoked
 *		on partial or full key matches.
 */
protected void accept(byte[] key, IHistoryStoreVisitor visitor, boolean visitOnPartialMatch) {
	try {
		IndexCursor cursor = store.getCursor();
		cursor.find(key);
		// Check for a prefix match.
		while (cursor.keyMatches(key)) {
			if (!visitOnPartialMatch) {
				// Ensure key prefix is of correct length.
				byte[] storedKey = cursor.getKey();
				if (storedKey.length - ILocalStoreConstants.SIZE_KEY_SUFFIX != key.length) {
					cursor.next();
					continue;
				}
			}
			HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
			if (!visitor.visit(storedEntry))
				break;
			cursor.next();
		}
		cursor.close();
	} catch (Exception e) {
		String message = Policy.bind("history.problemsAccessing");
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_READ_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
protected void accept(IPath path, IHistoryStoreVisitor visitor, boolean visitOnPartialMatch) {
	accept(Convert.toUTF8(path.toString()), visitor, visitOnPartialMatch);
}
/**
 * Adds state into history log.
 *
 * @param key Full workspace path to the resource being logged.
 * @param uuid UUID for stored file contents.
 * @params lastModified Timestamp for rseource being logged.
 */
protected void addState(IPath path, UniversalUniqueIdentifier uuid, long lastModified) {
	// Determine how many states already exist for this path and timestamp.
	class CountVisitor implements IHistoryStoreVisitor {
		byte count = 0;
		public boolean visit(HistoryStoreEntry entry) throws IndexedStoreException {
			count++;
			return true;
		}
		public byte getCount() {
			return count;
		}
	};

	// Build partial key for which matches will be found.
	byte[] keyPrefix = HistoryStoreEntry.keyPrefixToBytes(path, lastModified);
	CountVisitor visitor = new CountVisitor();
	accept(keyPrefix, visitor, true);
	HistoryStoreEntry entryToInsert = new HistoryStoreEntry(path, uuid, lastModified, visitor.getCount());
	try {
		ObjectID valueID = store.createObject(entryToInsert.valueToBytes());
		store.getIndex().insert(entryToInsert.getKey(), valueID);
	} catch (Exception e) {
		resetIndexedStore();
		String message = Policy.bind("history.couldNotAdd", path.toString());
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, path, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
/**
 * Add an entry to the history store for the specified resource.
 *
 * @param key Full workspace path to resource being logged.
 * @param localFile Local file system file handle
 * @param lastModified Timestamp for resource.
 *
 * @return true if state added to history store and false otherwise.
 */
public UniversalUniqueIdentifier addState(IPath key, java.io.File localFile, long lastModified, boolean moveContents) {
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
	return uuid;
}
/**
 * Add an entry to the history store for the specified resource.
 *
 * @param key Full workspace path to resource being logged.
 * @param localLocation Local file system path to resource.
 * @param lastModified Timestamp for resource.
 *
 * @return true if state added to history store and false otherwise.
 */
public UniversalUniqueIdentifier addState(IPath key, IPath localLocation, long lastModified, boolean moveContents) {
	return addState(key, localLocation.toFile(), lastModified, moveContents);
}
/**
 * Clean this store applying the current policies.
 */
public void clean() {
	IWorkspaceDescription description = workspace.internalGetDescription();
	long minimumTimestamp = System.currentTimeMillis() - description.getFileStateLongevity();
	int max = description.getMaxFileStates();
	IPath current = null;
	List result = new ArrayList(Math.min(max, 1000));
	Set blobs = new HashSet();
	try {
		IndexCursor cursor = store.getCursor();
		cursor.findFirstEntry();
		while (cursor.isSet()) {
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
			blobs.add(entry.getUUID().toString());
			cursor.next();
		}
		removeOldestEntries(result, max);
		cursor.close();
		store.commit();
		removeGarbage(blobs);
	} catch (Exception e) {
		String message = Policy.bind("history.problemsCleaning");
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
/**
 * Verifies existence of specified resource in the history store.
 *
 * @param target File state to be verified.
 * @return True if file state exists.
 *
 */   
public boolean exists(IFileState target) {
	return blobStore.fileFor(((FileState) target).getUUID()).exists();
}
/**
 * Returns an input stream containing the file contents of the specified state.
 * The user is responsible for closing the returned stream.
 *
 * @param target File state for which an input stream is requested.
 * @return Input stream for requested file state.
 */ 
public InputStream getContents(IFileState target) throws CoreException {
	if (!exists(target)) {
		String message = Policy.bind("history.notValid");
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
	}
	return blobStore.getBlob(((FileState) target).getUUID());
}
/**
 * Returns an array of all states available for the specified resource path or
 * an empty array if none.
 */
public IFileState[] getStates(final IPath key) {
	final int max = workspace.internalGetDescription().getMaxFileStates();
	final List result = new ArrayList(max);
	IHistoryStoreVisitor visitor = new IHistoryStoreVisitor() {
		public boolean visit(HistoryStoreEntry entry) throws IndexedStoreException {
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
 * Verifies whether the specified file at the specified file system location
 * meets current size policies.
 */
public boolean isValid(java.io.File localFile) {
	WorkspaceDescription description = workspace.internalGetDescription();
	return localFile.length() <= description.getMaxFileStateSize();
}
protected void remove(HistoryStoreEntry entry) throws IndexedStoreException {
	blobStore.deleteBlob(entry.getUUID());
	entry.remove();
}
/**
 * Removes all file states from this store.
 */
public void removeAll() {
	// XXX: should implement a method with a better performance
	removeAll(workspace.getRoot());
}
public void removeAll(IResource resource) {
	try {
		IndexCursor cursor = store.getCursor();
		byte[] key = Convert.toUTF8(resource.getFullPath().toString());
		cursor.find(key);
		while (cursor.keyMatches(key)) {
			HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
			remove(entry);
		}
		cursor.close();
		store.commit();
	} catch (Exception e) {
		String message = Policy.bind("history.problemsRemoving", resource.getFullPath().toString());
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, resource.getFullPath(), message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
/**
 * Checks if there are any blobs on disk that have no reference in the index store.
 */
public void removeGarbage() {
	Set blobsToPreserv = new HashSet();
	try {
		IndexCursor cursor = store.getCursor();
		cursor.findFirstEntry();
		while (cursor.isSet()) {
			HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
			blobsToPreserv.add(entry.getUUID().toString());
			cursor.next();
		}
		cursor.close();
	} catch (Exception e) {
		String message = Policy.bind("history.problemsCleaning");
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
	removeGarbage(blobsToPreserv);
}
/**
 * Remove all blobs but the ones in the parameter.
 */
protected void removeGarbage(Set blobsToPreserv) {
	Set storedBlobs = blobStore.getBlobNames();
	for (Iterator i = storedBlobs.iterator(); i.hasNext();) {
		String blob = (String) i.next();
		if (!blobsToPreserv.contains(blob))
			blobStore.deleteBlob(blob);
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
public void shutdown(IProgressMonitor monitor) {
	if (store == null)
		return;
	store.close();
}
public void startup(IProgressMonitor monitor) {
}
protected void resetIndexedStore() {
	store.reset();
	IPath location = workspace.getMetaArea().getHistoryStoreLocation();
	java.io.File target = location.toFile();
	workspace.clear(target);
	target.mkdirs();
	String message = Policy.bind("history.corrupt");
	ResourceStatus status = new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message, null);
	ResourcesPlugin.getPlugin().getLog().log(status);
}
public File getFileFor(UniversalUniqueIdentifier uuid) {
	return blobStore.fileFor(uuid);
}

/**
 * Returns the paths of all files with entries in this history store at or below
 * the given workspace resource path to the given depth.
 * 
 * @param key full workspace path to resource
 * @param depth depth limit: one of <code>DEPTH_ZERO</code>, <code>DEPTH_ONE</code>
 *    or <code>DEPTH_INFINITE</code>
 * @return the set of paths for files that have at least one history entry
 *   (element type: <code>IPath</code>)
 */
public Set allFiles(IPath path, final int depth) {
	final Set allFiles = new HashSet();
	final int pathLength = path.segmentCount();
	class PathCollector implements IHistoryStoreVisitor {
		public boolean visit(HistoryStoreEntry state) {
			IPath memberPath = state.getPath();
			boolean withinDepthRange = false;
			switch (depth) {
				case IResource.DEPTH_ZERO:
					withinDepthRange = memberPath.segmentCount() == pathLength;
					break;
				case IResource.DEPTH_ONE:
					withinDepthRange = memberPath.segmentCount() <= pathLength + 1;
					break;
				case IResource.DEPTH_INFINITE:
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

}