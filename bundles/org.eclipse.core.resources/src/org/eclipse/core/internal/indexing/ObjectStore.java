package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class ObjectStore {

	public static final int MaximumObjectSize = ObjectPage.ObjectSpaceSize - ObjectHeader.Size; 
	private static final int CurrentObjectStoreVersion = 1;
	private static final int ObjectStoreMetadataAreaID = 1;
	private static HashMap FactoryRegistry = new HashMap();

	private PageStore pageStore; /* initialized in open */
	private HashMap acquiredObjects; /* initialized in open */
	private HashMap modifiedObjects; /* initialized in open */
	private String name; /* initialized in open */
	private ObjectStorePSPolicy policy;

	/**
	 * Creates an object store.  This store is unusable until opened.
	 */
	public ObjectStore() {
		policy = new ObjectStorePSPolicy();
	}
	/**
	 * Returns the StoredObject at a given address.
	 */
	public StoredObject acquireObject(ObjectAddress address) throws ObjectStoreException {
		StoredObject object = (StoredObject) acquiredObjects.get(address);
		if (object == null) {
			int pageNumber = address.getPageNumber();
			ObjectPage page = acquireObjectPage(pageNumber);
			object = page.getObject(address);
			object.setStore(this);
			object.setAddress(address);
			acquiredObjects.put(address, object);
			object.acquired();
			page.release();
		}
		object.addReference();
		return object;
	}

	/** 
	 * Acquires an object page.  This is a convenience method to translate exceptions.
	 */
	protected ObjectPage acquireObjectPage(int pageNumber) throws ObjectStoreException {
		ObjectPage page;
		try {
			page = (ObjectPage) pageStore.acquire(pageNumber);
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}
		return page;
	}
	/**
	 * Checks to see if the metadata stored in the object store matches that expected by this
	 * code.  If not, a conversion is necessary.
	 */
	protected void checkMetadata() throws ObjectStoreException {
		Buffer metadata = getMetadataArea(ObjectStoreMetadataAreaID);
		Field versionField = metadata.getField(0, 4);
		int objectStoreVersion = versionField.getInt();
		if (objectStoreVersion == 0) {
			// 0 indicates that the store is new and opened for read/write
			versionField.put(CurrentObjectStoreVersion);
			putMetadataArea(ObjectStoreMetadataAreaID, metadata);
			return;
		}
		if (objectStoreVersion == CurrentObjectStoreVersion)
			return;
		convert(objectStoreVersion);
	}
	/**
	 * Closes the object store.
	 */
	public void close() throws ObjectStoreException {
		commit();
		try {
			pageStore.close();
		} catch (Exception e) {
		}
		acquiredObjects.clear();
	}
	/**
	 * Commits the modified object collection to the underlying page store.
	 */
	public void commit() throws ObjectStoreException {
		Iterator stream = modifiedObjects.values().iterator();
		while (stream.hasNext()) {
			StoredObject object = (StoredObject) stream.next();
			commitObject(object);
			releaseObject(object);
		}
		modifiedObjects.clear();
		try {
			pageStore.commit();
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageWriteFailure);
		}
	}
	/**
	 * Commits an object to its page.
	 */
	protected void commitObject(StoredObject object)
		throws ObjectStoreException {
		ObjectAddress address = object.getAddress();
		int pageNumber = address.getPageNumber();
		ObjectPage op = acquireObjectPage(pageNumber);
		op.updateObject(object);
		op.release();
	}
	/**
	 * Converts the object store from a previous to the current version.  
	 * No conversions are yet defined.
	 */
	protected void convert(int fromVersion) throws ObjectStoreException {
		throw new ObjectStoreException(ObjectStoreException.StoreConversionFailure);
	}
	/**
	 * Creates a repository for the pathname.  
	 */
	public static void create(String path)
		throws ObjectStoreException {
		try {
			PageStore.create(path);
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.StoreCreateFailure);
		}
	}
	/**
	 * Deletes the underlying page store.
	 */
	public static void delete(String path) {
		PageStore.delete(path);
	}
	/**
	 * Checks for existence of an object store file.
	 */
	public static boolean exists(String path) {
		return PageStore.exists(path);
	}
	/**
	 * Returns a factory given its type.
	 */
	public static IObjectFactory getFactory(int type) {
		Integer key = new Integer(type);
		return (IObjectFactory) FactoryRegistry.get(key);
	}
	public Buffer getMetadataArea(int i) throws ObjectStoreException {
		try {
			return new Buffer(pageStore.readMetadataArea(i));
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.MetadataRequestFailure);
		}
	}
	/**
	 * Returns the name of the store.
	 */
	public synchronized String getName() {
		return pageStore.getName();
	}
	/**
	 * Places an object into the store.  This assigns it an address.  The address
	 * is returned.
	 */
	public ObjectAddress insertObject(StoredObject object) throws ObjectStoreException {
		object.setStore(this);
		ObjectPage page = acquirePageForObject(pageStore, object);
		page.insertObject(object);
		page.release();
		ObjectAddress address = object.getAddress();
		return address;
	}
	/**
	 * Opens an object store.
	 */
	public void open(String name) throws ObjectStoreException {
		try {
			pageStore = new PageStore(policy);
			pageStore.open(name);
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.StoreOpenFailure);
		}
		checkMetadata();
		acquiredObjects = new HashMap();
		modifiedObjects = new HashMap();
	}
	public void putMetadataArea(int i, Buffer buffer) throws ObjectStoreException {
		try {
			pageStore.writeMetadataArea(i, buffer.getByteArray());
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.MetadataRequestFailure);
		}
	}
	/**
	 * Registers a type factory.  Each type should register itself once as its class is loaded.
	 */
	public static void registerFactory(int type, IObjectFactory factory) {
		Integer key = new Integer(type);
		FactoryRegistry.put(key, factory);
	}
/**
 * Releases an object.
 */
public void releaseObject(StoredObject object) throws ObjectStoreException {
	object.removeReference();
	if (object.hasReferences())
		return;
	acquiredObjects.remove(object.getAddress());
	object.released();
}
	/**
	 * Removes an object from the object store.  In doing so, it must remove it from the cache as well.
	 */
	public void removeObject(ObjectAddress address) throws ObjectStoreException {
		StoredObject object = (StoredObject)modifiedObjects.get(address);
		if (object != null) {
			modifiedObjects.remove(address);
			releaseObject(object);
		}
		if (acquiredObjects.containsKey(address)) {
			throw new ObjectStoreException(ObjectStoreException.ObjectIsLocked);
		}
		int pageNumber = address.getPageNumber();
		ObjectPage page = acquireObjectPage(pageNumber);
		page.removeObject(address);
		page.release();
	}
/**
 * Rollback the modified objects collection.
 */
public void rollback() throws ObjectStoreException {
	Iterator stream = modifiedObjects.values().iterator();
	while (stream.hasNext()) {
		StoredObject object = (StoredObject) stream.next();
		releaseObject(object);
	}
	modifiedObjects.clear();
	pageStore.rollback();
}
/**
 * Places the object in the modified objects cache and marks it as in use.  The modified objects
 * cache is flushed at commit or rollback time.  At that point, the reference to the object
 * is dropped.
 */
public void updateObject(StoredObject object) {
	ObjectAddress address = object.getAddress();
	if (modifiedObjects.get(address) == null) {
		modifiedObjects.put(address, object);
		try {
			acquireObject(address);
		} catch (ObjectStoreException e) {
		}
	}
}


	/** 
	 * Looks for the first page that guarantees enough space to meet the criteria.
	 * This is the "first fit" algorithm and will get slow as the page file grows since
	 * it is O(n**2).  (Each addition of a new page is preceded by a search of all pages.)
	 * We reduce the overhead by maintaining SpaceMapPages that tell us how full each page
	 * is.  A space map page is the first page of a span of 8K pages (64M total).
	 * Each byte in a space map page indicates the fullness of each page in the span.
	 * Since databases are expected to be quite small (<200Mb) we might be able to live with
	 * this simple algorithm.
	 */
	protected ObjectPage acquirePageForObject(PageStore pageStore, StoredObject object)
		throws ObjectStoreException {
		int bytesNeeded = object.length() + ObjectHeader.Size;
		int oPageNumber = 0;
		int numberOfSpans = ((pageStore.numberOfPages() - 1) / ObjectStorePage.SIZE) + 1;
		for (int i = 0; i <= numberOfSpans; i++) {
			try {
				int sPageNumber = i * ObjectStorePage.SIZE;
				SpaceMapPage sPage = (SpaceMapPage) pageStore.acquire(sPageNumber);
				oPageNumber = sPage.findObjectPageNumberForSize(bytesNeeded); 
				sPage.release();
			} catch (PageStoreException e) {
				throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
			}
			if (oPageNumber != 0) break;
		}
		if (oPageNumber == 0) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}
		try {
			ObjectPage oPage = (ObjectPage) pageStore.acquire(oPageNumber);
			return oPage;
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}
	}

}
