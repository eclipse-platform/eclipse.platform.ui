package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;

public class IndexedStore {
	
	private static final int CurrentVersion = 1;
	private static final int MetadataID = 2;
/*
 * Provides the storage for the registry of stores. Key is the name the store
 * was opened under.  The value is the store itself.  This is used to facilitate
 * recovery in the event of a thread being killed or dying.
 */
private static final Map registry = new HashMap();

	private static final ObjectAddress ContextAddress = new ObjectAddress(1, 1);

	private ObjectAddress objectDirectoryAddress; /* initialized at open */	
	private Index objectDirectory; /* initialized at open */	
	private IndexCursor objectDirectoryCursor; /* initialized at open */

	private ObjectAddress indexDirectoryAddress; /* initialized at open */	
	private Index indexDirectory; /* initialized at open */	
	private IndexCursor indexDirectoryCursor; /* initialized at open */

	private ObjectStore objectStore; /* initialized at open */
	private String name; /* initialized at open */
	private boolean modified; /* initialized at open */	
	private int openNumber; /* initialized at open */	
	private int serialNumber; /* initialized at open */	

	/**
	 * Acquires an anchor.
	 */
	IndexAnchor acquireAnchor(ObjectAddress address) throws IndexedStoreException {
		return (IndexAnchor) acquireObject(address);
	}
	/**
	 * Acquires a context.
	 */
	IndexedStoreContext acquireContext(ObjectAddress address) throws IndexedStoreException {
		return (IndexedStoreContext) acquireObject(address);
	}
	/**
	 * Acquire an index node.
	 */
	IndexNode acquireNode(ObjectAddress address) throws IndexedStoreException {
		return (IndexNode) acquireObject(address);
	}
	/**
	 * Acquires an object.
	 */
	private StoredObject acquireObject(ObjectAddress address) throws IndexedStoreException {
		StoredObject object;
		try {
			object = objectStore.acquireObject(address);
		} catch(ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotAcquired);
		}
		return object;
	}
	/**
	 * Acquires a user object.
	 */
	UserDefinedObject acquireUserObject(ObjectAddress address) throws IndexedStoreException {
		return (UserDefinedObject) acquireObject(address);
	}
	/**
	 * Checks to see if the metadata stored in the object store matches that expected by this
	 * code.  If not, a conversion is necessary.
	 */
	private void checkMetadata() throws IndexedStoreException {
		Buffer metadata = getMetadataArea(MetadataID);
		Field versionField = metadata.getField(0, 4);
		int version = versionField.getInt();
		if (version == 0) {
			// 0 indicates that the store is new
			versionField.put(CurrentVersion);
			putMetadataArea(MetadataID, metadata);
			return;
		}
		if (version == CurrentVersion)
			return;
		convert(version);
	}
	/**
	 * Closes the store.  This is required to free the underlying file.
	 */
	public synchronized void close() throws IndexedStoreException {
		commit();
		objectDirectoryCursor.close();
		indexDirectoryCursor.close();
		try {
			objectStore.close();
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.StoreNotClosed);
		}
		registry.remove(name);
		name = null;
		objectDirectory = null;
		objectDirectoryAddress = null;
		objectDirectoryCursor = null;
		indexDirectory = null;
		indexDirectoryAddress = null;
		indexDirectoryCursor = null;
		openNumber = 0;
		serialNumber = 0;
		modified = false;
	}
	public synchronized void commit() throws IndexedStoreException {
		try {
			objectStore.commit();
		} catch (Exception e) {
			throw new IndexedStoreException(IndexedStoreException.StoreNotCommitted);
		}
	}
	/**
	 * Converts the store from a previous to the current version.  
	 * No conversions are yet defined.
	 */
	private void convert(int fromVersion) throws IndexedStoreException {
		throw new IndexedStoreException(IndexedStoreException.StoreNotConverted);
	}
	/**
	 * Creates and initializes an IndexedStore.
	 */
	public static synchronized void create(String name) throws IndexedStoreException {
		ObjectStore store = new ObjectStore();
		try {
			ObjectStore.create(name);
			store.open(name);
			ObjectAddress contextAddress = store.insertObject(new IndexedStoreContext());
			IndexedStoreContext context = (IndexedStoreContext) store.acquireObject(contextAddress);
			IndexAnchor anchor = new IndexAnchor();
			ObjectAddress address = store.insertObject(anchor);
			context.setIndexDirectoryAddress(address);
			anchor = new IndexAnchor();
			address = store.insertObject(anchor);
			context.setObjectDirectoryAddress(address);
			context.release();
			store.commit();
			store.close();
		} catch (Exception e1) {
			try {
				store.close();
			} catch (ObjectStoreException e2) {
			}
			ObjectStore.delete(name);
			throw new IndexedStoreException(IndexedStoreException.StoreNotCreated);
		}
	}
	/**
	 * Creates an Index with the given name.
	 */
	public synchronized Index createIndex(String indexName) throws IndexedStoreException {
		Index index = null;
		indexDirectoryCursor.find(indexName);
		if (indexDirectoryCursor.keyMatches(indexName)) {
			throw new IndexedStoreException(IndexedStoreException.IndexExists);
		}
		ObjectAddress address = insertObject(new IndexAnchor());
		indexDirectory.insert(indexName, address.toByteArray());
		index = new Index(this, address);
		return index;
	}
	/**
	 * Places a byte array into the store, return a new object identifier.
	 */
	public synchronized ObjectID createObject(byte[] b) throws IndexedStoreException {
		ObjectAddress address = insertObject(new UserDefinedObject(b));
		ObjectID id = getNextObjectID();
		objectDirectory.insert(id.toByteArray(), address.toByteArray());
		return id;
	}
	/**
	 * Places a String into the store.
	 */
	public synchronized ObjectID createObject(String s) throws IndexedStoreException {
		return createObject(Convert.toUTF8(s));
	}
	/**
	 * Places an Insertable into the store.
	 */
	public synchronized ObjectID createObject(Insertable anObject) throws IndexedStoreException {
		return createObject(anObject.toByteArray());
	}
	/**
	 * Deletes the store if it exists.  Does nothing if it does not exist.
	 */
	public static synchronized void delete(String filename) {
		ObjectStore.delete(filename);
	}
	/**
	 * Tests to see if the file acting as the store exists.
	 */
	public static synchronized boolean exists(String filename) {
		return ObjectStore.exists(filename);
	}
	/**
	 * If a store disappears unexpectedly, make sure it gets closed.
	 */
	protected void finalize() {
		try {
			close();
		} catch (Exception e) {
		}
	}
	/**
	 * Finds the handle of an open store for a given its name.  The store may continue with the current transaction,
	 * or may abort the current transaction.  Used to initiate recovery if the reference to the store should be
	 * lost for some reason.  Will return null if the store has not been opened.  The name of the store to be found
	 * must compare equal to the name the store was opened under.
	 */
	public synchronized static IndexedStore find(String name) {
		return (IndexedStore)registry.get(name);
	}
	/**
	 * @deprecated -- use commit()
	 */
	public synchronized void flush() throws IndexedStoreException {
		try {
			objectStore.commit();
		} catch (Exception e) {
			throw new IndexedStoreException(IndexedStoreException.StoreNotFlushed);
		}
	}
	/**
	 * Returns an index given its name.
	 */
	public synchronized Index getIndex(String indexName) throws IndexedStoreException {
		Index index;
		byte[] key = Convert.toUTF8(indexName);
		indexDirectoryCursor.find(key);
		if (!indexDirectoryCursor.keyMatches(key))
			throw new IndexedStoreException(IndexedStoreException.IndexNotFound);
		ObjectAddress address = indexDirectoryCursor.getValueAsObjectAddress();
		index = new Index(this, address);
		return index;
	}
private Buffer getMetadataArea(int i) throws IndexedStoreException {
	try {
		return objectStore.getMetadataArea(i);
	} catch (ObjectStoreException e) {
		throw new IndexedStoreException(IndexedStoreException.MetadataRequestError);
	}
}
	/**
	 * Returns the name of the store.
	 */
	public synchronized String getName() {
		return name;
	}
	/**
	 * Returns the next ObjectID
	 */
	private ObjectID getNextObjectID() throws IndexedStoreException {
		if (!modified) {
			IndexedStoreContext context = acquireContext(ContextAddress);
			context.incrementOpenNumber();
			openNumber = context.getOpenNumber();
			context.release();
			modified = true;
		}
		serialNumber++;
		return new ObjectID(openNumber, serialNumber);
	}
	/**
	 * Returns a byte array given its object identifier.
	 */
	public synchronized byte[] getObject(ObjectID id) throws IndexedStoreException {
		objectDirectoryCursor.find(id.toByteArray());
		ObjectAddress address = objectDirectoryCursor.getValueAsObjectAddress();
		UserDefinedObject object = acquireUserObject(address);
		byte[] b = object.getValue();
		object.release();
		return b;
	}
	/**
	 * Returns an object as a string, truncated at the first null.
	 */
	public synchronized String getObjectAsString(ObjectID id) throws IndexedStoreException {
		String s;
		s = Convert.fromUTF8(getObject(id));
		int i = s.indexOf(0);
		if (i == -1)
			return s;
		return s.substring(0, i);
	}
	/**
	 * Returns the object store.
	 */
	public synchronized ObjectStore getObjectStore() {
		return objectStore;
	}
	/** 
	 * Inserts a new object into my store.
	 */
	ObjectAddress insertObject(StoredObject object) throws IndexedStoreException {
		try {
			ObjectAddress address = objectStore.insertObject(object);
			return address;
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotStored);
		}
	}
	/**
	 * Opens the store.
	 */
	public synchronized void open(String name) throws IndexedStoreException {
		if (registry.get(name) != null) {
			throw new IndexedStoreException(IndexedStoreException.StoreIsOpen);
		}
		IndexAnchor.registerFactory();
		UserDefinedObject.registerFactory();
		IndexedStoreContext.registerFactory();
		IndexNode.registerFactory();
		if (!exists(name)) create(name);
		try {
			objectStore = new ObjectStore();
			objectStore.open(name);
			checkMetadata();
			IndexedStoreContext context = acquireContext(ContextAddress);
			indexDirectoryAddress = context.getIndexDirectoryAddress();
			objectDirectoryAddress = context.getObjectDirectoryAddress();
			context.release();
			serialNumber = 0;
			indexDirectory = new Index(this, indexDirectoryAddress);
			indexDirectoryCursor = indexDirectory.open();
			objectDirectory = new Index(this, objectDirectoryAddress);
			objectDirectoryCursor = objectDirectory.open();
			this.name = name;
			registry.put(name, this);
		} catch (Exception e) {
			throw new IndexedStoreException(IndexedStoreException.GenericError);
		}
	}
	private void putMetadataArea(int i, Buffer b) throws IndexedStoreException {
		try {
			objectStore.putMetadataArea(i, b);
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.MetadataRequestError);
		}
	}
	/**
	 * Destroys an Index given its name.
	 */
	public synchronized void removeIndex(String indexName) throws IndexedStoreException {
		byte[] key = Convert.toUTF8(indexName);
		indexDirectoryCursor.find(key);
		if (!indexDirectoryCursor.keyMatches(key)) {
			throw new IndexedStoreException(IndexedStoreException.IndexNotFound);
		}
		ObjectAddress address = indexDirectoryCursor.getValueAsObjectAddress();
		IndexAnchor anchor = acquireAnchor(address);
		anchor.destroyChildren();
		anchor.release();
		removeObject(address);
		indexDirectoryCursor.remove();
	}
	/** 
	 * Removes an object from my store.
	 */
	void removeObject(ObjectAddress address) throws IndexedStoreException {
		try {
			objectStore.removeObject(address);
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotRemoved);
		}
	}
	/**
	 * Removes the object identified by id from the store.
	 */
	public synchronized void removeObject(ObjectID id) throws IndexedStoreException {
		byte[] key = id.toByteArray();
		objectDirectoryCursor.find(key);
		if (!objectDirectoryCursor.keyMatches(key)) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotFound);
		}
		ObjectAddress address = objectDirectoryCursor.getValueAsObjectAddress();
		objectDirectoryCursor.remove();
		removeObject(address);
	}
	public synchronized void rollback() throws IndexedStoreException {
		try {
		objectStore.rollback();
		} catch (ObjectStoreException e) {
			throw new IndexedStoreException(IndexedStoreException.StoreNotRolledBack);
		}
	}
	/**
	 * Replaces the contents of the object identified by "id" with the byte array "b".
	 */
	public synchronized void updateObject(ObjectID id, byte[] b) throws IndexedStoreException {
		byte[] key = id.toByteArray();
		objectDirectoryCursor.find(key);
		if (!objectDirectoryCursor.keyMatches(key)) {
			throw new IndexedStoreException(IndexedStoreException.ObjectNotFound);
		}
		ObjectAddress oldAddress = objectDirectoryCursor.getValueAsObjectAddress();
		ObjectAddress newAddress = insertObject(new UserDefinedObject(b));
		objectDirectoryCursor.updateValue(newAddress.toByteArray());
		removeObject(oldAddress);
	}
	/**
	 * Updates an object with a String.
	 */
	public synchronized void updateObject(ObjectID id, String s) throws IndexedStoreException {
		updateObject(id, Convert.toUTF8(s));
	}
	/**
	 * Updates an object with an Insertable.
	 */
	public synchronized void updateObject(ObjectID id, Insertable anObject)
		throws IndexedStoreException {
		updateObject(id, anObject.toByteArray());
	}
}
