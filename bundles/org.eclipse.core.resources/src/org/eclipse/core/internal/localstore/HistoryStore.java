package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.indexing.*;
import java.io.InputStream;
import java.util.*;

public class HistoryStore {
	protected Workspace workspace;
	protected IPath location;
	protected BlobStore blobStore;
	private IndexedStore store;

	/** constants */
	protected final static String INDEX_FILE = ".index";
	protected final static String INDEX_NAME = "index";
	protected final static IFileState[] EMPTY_FILE_STATES = new IFileState[0];
public HistoryStore(Workspace workspace, IPath location, int limit) {
	this.workspace = workspace;
	blobStore = new BlobStore(location, limit);
	this.location = location.append(INDEX_FILE);
}
/**
 * Searches indexed store for key, and invokes visitor's defined behaviour on key matches.
 *
 * @param key key prefix on which to perform search.
 * @param visitOnPartialMatch indicates whether visitor's definined behavior is to be invoked
 *		on partial or full key matches.
 */
protected void accept(byte[] key, IHistoryStoreVisitor visitor, boolean visitOnPartialMatch) {
	IndexedStore store = getIndexedStore();
	if (store == null)
		return;
	Index index = getIndex(store);
	if (index == null)
		return;
	try {
		IndexCursor cursor = index.open();
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
	} catch (IndexedStoreException e) {
		String message = "Problems accessing history store";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_READ_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
protected void accept(IPath path, IHistoryStoreVisitor visitor, boolean visitOnPartialMatch) {
	accept(path.toString().getBytes(), visitor, visitOnPartialMatch);
}
/**
 * Adds state into history log.
 *
 * @param key Full workspace path to the resource being logged.
 * @param uuid UUID for stored file contents.
 * @params lastModified Timestamp for rseource being logged.
 */
protected void addState(IPath path, UniversalUniqueIdentifier uuid, long lastModified) {
	IndexedStore store = getIndexedStore();
	if (store == null)
		return;
	Index index = getIndex(store);
	if (index == null)
		return;

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
		index.insert(entryToInsert.getKey(), valueID);
	} catch (IndexedStoreException e) {
		String message = "Could not add history";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
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
public void addState(IPath key, IPath localLocation, long lastModified, boolean moveContents) {
	if (!isValid(localLocation))
		return;
	IndexedStore store = getIndexedStore();
	if (store == null)
		return;
	try {
		UniversalUniqueIdentifier uuid = blobStore.addBlob(localLocation.toFile(), moveContents);
		addState(key, uuid, lastModified);
		commit(store);
	} catch (CoreException e) {
		ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
	}
}
/**
 * Clean this store applying the current policies.
 */
public void clean() {
	IndexedStore store = getIndexedStore();
	if (store == null)
		return;
	Index index = getIndex(store);
	if (index == null)
		return;
	IWorkspaceDescription description = workspace.internalGetDescription();
	long minimumTimestamp = System.currentTimeMillis() - description.getFileStateLongevity();
	int max = description.getMaxFileStates();
	IPath current = null;
	List result = new ArrayList(max);
	Set blobs = new HashSet();
	try {
		IndexCursor cursor = index.open();
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
	} catch (IndexedStoreException e) {
		String message = "Problems cleaning up history store";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
protected synchronized void close(IndexedStore store) {
	try {
		store.close();
	} catch (IndexedStoreException e) {
		String message = "Could not close history store";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
protected void commit(IndexedStore store) {
	try {
		store.commit();
	} catch (IndexedStoreException e) {
		String message = "History store transactions did not commit properly";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
/**
 * Performs conversion of a byte array to a long representation.
 *
 * @param value byte[]
 * @return long
 * @see convertLongToBytes(long).
 */
public static long convertBytesToLong(byte[] value) {

	long longValue = 0L;

	// See method convertLongToBytes(long) for algorithm details.	
	for (int i = 0; i < value.length; i++) {
		// Left shift has no effect thru first iteration of loop.
		longValue <<= 8;
		longValue ^= value[i] & 0xFF;
	}

	return longValue;
}
/**
 * Performs conversion of a long value to a byte array representation.
 *
 * @param value long
 * @return byte[]
 * @see convertBytesToLong(byte[]).
 */
public static byte[] convertLongToBytes(long value) {
	
	// A long value is 8 bytes in length.
	byte[] bytes = new byte[8];

	// Convert and copy value to byte array:
	//   -- Cast long to a byte to retrieve least significant byte;
	//   -- Left shift long value by 8 bits to isolate next byte to be converted;
	//   -- Repeat until all 8 bytes are converted (long = 64 bits).
	// Note: In the byte array, the least significant byte of the long is held in
	// the highest indexed array bucket.
	
	for (int i = 0; i < bytes.length; i++) {
		bytes[(bytes.length - 1) - i] = (byte) value;
		value >>>= 8;
	}

	return bytes;
}
protected synchronized IndexedStore create(IndexedStore store) {
	store = open(store);
	createIndex(store);
	return store;
}
protected synchronized Index createIndex(IndexedStore store) {
	try {
		return store.createIndex(INDEX_NAME);
	} catch (IndexedStoreException e) {
		String message = "Could not create index for history store";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
		return null;
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
		String message = "State is not valid.";
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, null, message, null);
	}
	return blobStore.getBlob(((FileState) target).getUUID());
}
protected Index getIndex(IndexedStore store) {
	try {
		return store.getIndex(INDEX_NAME);
	} catch (IndexedStoreException e) {
		String message = "Problems accessing history store index";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
		return createIndex(store);
	}
}
protected IndexedStore getIndexedStore() {
	if (store == null) {
		String name = location.toOSString();
		store = IndexedStore.find(name);
		if (store != null) {
			rollback(store);
		} else {
			if (IndexedStore.exists(name))
				store = open(new IndexedStore());
			else
				store = create(new IndexedStore());
		}
	}
	return store;
}
/**
 * Returns an array of all states available for the specified resource path or
 * an empty array if none.
 */
public IFileState[] getStates(final IPath key) {
	final int max = workspace.internalGetDescription().getMaxFileStates();
	final long minimumTimestamp = System.currentTimeMillis() - workspace.internalGetDescription().getFileStateLongevity();
	final List result = new ArrayList(max);
	IHistoryStoreVisitor visitor = new IHistoryStoreVisitor() {
		public boolean visit(HistoryStoreEntry entry) throws IndexedStoreException {
			if (entry.getLastModified() < minimumTimestamp)
				return true;
			result.add(new FileState(HistoryStore.this, key, entry.getLastModified(), entry.getUUID()));
			return result.size() < max;
		}
	};
	accept(key, visitor, false);
	if (result.isEmpty())
		return EMPTY_FILE_STATES;
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
public boolean isValid(IPath location) {
	WorkspaceDescription description = workspace.internalGetDescription();
	return location.toFile().length() <= description.getMaxFileStateSize();
}
protected synchronized IndexedStore open(IndexedStore store) {
	try {
		store.open(location.toOSString());
	} catch (IndexedStoreException e) {
		String message = "Could not open history store";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
		store = recreate(store);
	}
	return store;
}
protected synchronized IndexedStore recreate(IndexedStore store) {
	close(store);
	String name = location.toOSString();
	// Rename the problematic store for future analysis.
	location.toFile().renameTo(location.append(".001").toFile());
	location.toFile().delete();
	if (location.toFile().exists())
		return null; // we would not be able to recreate the store
	blobStore.deleteAll();
	store = create(new IndexedStore());
	return store;
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
	IndexedStore store = getIndexedStore();
	if (store == null)
		return;
	Index index = getIndex(store);
	if (index == null)
		return;
	try {
		IndexCursor cursor = index.open();
		byte[] key = resource.getFullPath().toString().getBytes();
		cursor.find(key);
		while (cursor.keyMatches(key)) {
			HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
			remove(entry);
		}
		cursor.close();
		commit(store);
	} catch (IndexedStoreException e) {
		String message = "Problems cleaning up history store";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
/**
 * Checks if there are any blobs on disk that have no reference in the index store.
 */
public void removeGarbage() {
	IndexedStore store = getIndexedStore();
	if (store == null)
		return;
	Index index = getIndex(store);
	if (index == null)
		return;
	Set blobsToPreserv = new HashSet();
	try {
		IndexCursor cursor = index.open();
		cursor.findFirstEntry();
		while (cursor.isSet()) {
			HistoryStoreEntry entry = HistoryStoreEntry.create(store, cursor);
			blobsToPreserv.add(entry.getUUID().toString());
			cursor.next();
		}
		cursor.close();
	} catch (IndexedStoreException e) {
		String message = "Problems cleaning up history store";
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
protected void rollback(IndexedStore store) {
	try {
		store.rollback();
	} catch (IndexedStoreException e) {
		String message = "History store transactions did not rollback properly";
		ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, null, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
public void shutdown(IProgressMonitor monitor) {
	if (store == null)
		return;
	close(store);
}
public void startup(IProgressMonitor monitor) {
}
}
