package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class ObjectStore implements Observer {

	public static final int MAXIMUM_OBJECT_SIZE = ObjectPage.ObjectSpaceSize - ObjectHeader.SIZE; 
	protected static final int CurrentObjectStoreVersion = 1;
	protected static final int ObjectStoreMetadataAreaID = 1;

	protected PageStore pageStore; /* initialized in open */
	protected String name; /* initialized in open */
	protected Map acquiredObjects; // objects which are currently active
	protected Map modifiedObjects; // objects that have been modified since the last sync point.
	protected LinkedList cachedObjects; // objects that are just hanging around waiting to be acquired.  Kept for performance.
	protected Set phantoms; // the set of objects that needs to be deleted since the last sync point.
	protected ReservationTable reservations;

	protected ObjectStorePagePolicy pagePolicy;
	protected AbstractObjectPolicy objectPolicy;

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
	 * Creates an object store.  This store is unusable until opened.
	 */
	public ObjectStore(AbstractObjectPolicy objectPolicy) {
		this.pagePolicy = new ObjectStorePagePolicy();
		this.objectPolicy = objectPolicy;
	}

	/**
	 * Opens an object store.
	 */
	public void open(String name) throws ObjectStoreException {
		try {
			pageStore = new PageStore(pagePolicy);
			pageStore.open(name);
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.StoreOpenFailure);
		}
		checkMetadata();
		acquiredObjects = new HashMap();
		modifiedObjects = new HashMap();
		cachedObjects = new LinkedList();
		phantoms = new HashSet();
		reservations = new ReservationTable();
	}
	
	/**
	 * Closes the object store.
	 */
	public void close() throws ObjectStoreException {
		try {
			commit();
		} catch (ObjectStoreException e) {
			//make sure the page store file gets closed no matter what
			pageStore.close(false);
			throw e;
		}
		try {
			pageStore.close();
		} catch (Exception e) {
			//ignore failure to close
		}
		acquiredObjects = null;
		modifiedObjects = null;
		cachedObjects = null;
		phantoms = null;
		reservations = null;
	}
	
	public Buffer getMetadataArea(int i) throws ObjectStoreException {
		try {
			return new Buffer(pageStore.readMetadataArea(i));
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.MetadataRequestFailure);
		}
	}

	public void putMetadataArea(int i, Buffer buffer) throws ObjectStoreException {
		try {
			pageStore.writeMetadataArea(i, buffer.getByteArray());
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.MetadataRequestFailure);
		}
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
	 * Converts the object store from a previous to the current version.  
	 * No conversions are yet defined.
	 */
	protected void convert(int fromVersion) throws ObjectStoreException {
		throw new ObjectStoreException(ObjectStoreException.StoreConversionFailure);
	}
	
	/**
	 * Commits the modified object collection to the underlying page store.
	 */
	public void commit() throws ObjectStoreException {
		for (Iterator z = acquiredObjects.values().iterator(); z.hasNext();) {
			StoredObject object = (StoredObject) z.next();
			object.notifyObservers();
		}
		for (Iterator z = phantoms.iterator(); z.hasNext();) {
			ObjectAddress address = (ObjectAddress) z.next();
			int pageNumber = address.getPageNumber();
			ObjectPage page = acquireObjectPage(pageNumber);
			page.removeObject(address.getObjectNumber());
			updateSpaceMapPage(page.getPageNumber(), page.getFreeSpace());
			page.release();
		}
		phantoms.clear();
		for (Iterator z = modifiedObjects.values().iterator(); z.hasNext();) {
			StoredObject object = (StoredObject) z.next();
			z.remove();
			addToCache(object);
			ObjectAddress address = object.getAddress();
			int pageNumber = address.getPageNumber();
			ObjectPage page = acquireObjectPage(pageNumber);
			if (reservations.contains(address)) {
				page.insertObject(object);
				updateSpaceMapPage(pageNumber, page.getFreeSpace());
			} else {
				page.updateObject(object);
			}
			page.release();
		}
		reservations.clear();
		try {
			pageStore.commit();
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageWriteFailure);
		}
	}
	
	/**
	 * Rollback the modified objects collection.
	 */
	public void rollback() throws ObjectStoreException {
		modifiedObjects.clear();
		reservations.clear();
		phantoms.clear();
	}
	
	/**
	 * Returns the name of the store.
	 */
	public String getName() {
		return pageStore.getName();
	}
	
	/**
	 * Returns the policy used to create objects.
	 */
	public AbstractObjectPolicy getPolicy() {
		return objectPolicy;
	}
	
	/**
	 * Returns the StoredObject at a given address.  This registers the store as an 
	 * observer of changes to this object.
	 */
	public StoredObject acquireObject(ObjectAddress address) throws ObjectStoreException {
		if (phantoms.contains(address)) {
			throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
		}
		StoredObject object = (StoredObject) acquiredObjects.get(address);
		if (object == null) {
			object = (StoredObject) modifiedObjects.get(address);
			if (object == null) {
				object = removeFromCache(address);
				if (object == null) {
					int pageNumber = address.getPageNumber();
					ObjectPage page = acquireObjectPage(pageNumber);
					try {
						Field f = page.getObjectField(address.getObjectNumber());
						if (f == null) throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
						object = objectPolicy.createObject(f, this, address);
					} catch (ObjectStoreException e) {
						page.release();
						throw e;
					}
					page.release();
				}
			}
			acquiredObjects.put(address, object);
			object.addObserver(this);
		}
		object.addReference();
		return object;
	}
	
	/**
	 * Releases an object.  If there are no more references and the object is not
	 * in the modified cache, return the object to 
	 * the standard cache.  Objects in the standard cache always maintain a 
	 * reference count of 0.
	 */
	public void releaseObject(StoredObject object) throws ObjectStoreException {
		object.removeReference();
		if (object.hasReferences()) return;
		object.notifyObservers(); // one last chance to collect changed objects
		object.deleteObserver(this);
		acquiredObjects.remove(object.getAddress());
		addToCache(object);
	}
	
	/**
	 * Updates the store when receiving an object change notification.  Required by
	 * Observer.  This places the object in the modified objects cache.  This will
	 * be cleared during commit/rollback processing.  An object may reside simultaneously
	 * in the modified and acquired object caches.
	 */
	public void update(Observable object, Object arg) {
		StoredObject storedObject = (StoredObject)object;
		modifiedObjects.put(storedObject.getAddress(), storedObject);
	}
	
	/**
	 * Adds an object to the backing cache.  Objects in this cache are neither in 
	 * the modified set or the acquired set.
	 */
	protected void addToCache(StoredObject object) {
		if (acquiredObjects.containsKey(object.getAddress())) return;
		if (modifiedObjects.containsKey(object.getAddress())) return;
		cachedObjects.addFirst(object);
		if (cachedObjects.size() <= 50) return;
		cachedObjects.removeLast();
	}
	
	/**
	 * Removes an object from the backing cache given its address.
	 */
	protected StoredObject removeFromCache(ObjectAddress address) {
		StoredObject object = null;
		for (Iterator z = cachedObjects.iterator(); z.hasNext();) {
			StoredObject o = (StoredObject) z.next();
			if (o.getAddress().equals(address)) {
				z.remove();
				object = o;
				break;
			}
		}
		return object;
	}
	
	/**
	 * Places an object into the store.  This assigns it an address.  The address
	 * is returned. The object is not observed until it is acquired.
	 */
//	public ObjectAddress insertObject(StoredObject object) throws ObjectStoreException {
//		int bytesNeeded = object.length() + ObjectHeader.SIZE;
//		ObjectPage page = acquireObjectPageForSize(bytesNeeded);
//		int objectNumber = page.insertObject(object);
//		int pageNumber = page.getPageNumber();
//		updateSpaceMapPage(page.getPageNumber(), page.getFreeSpace());
//		page.release();
//		object.setAddress(new ObjectAddress(pageNumber, objectNumber));
//		object.setStore(this);
//		return object.getAddress();
//	}

	/**
	 * "Inserts" an object into the store by reserving a place for the 
	 * object.  This assigns it an address and
	 * places it in the modified objects map.  A reservation is created that 
	 * records the address and the amount of space used.  The object is not
	 * actually added to the underlying store until a commit operation is executed.
	 */
	public ObjectAddress insertObject(StoredObject object) throws ObjectStoreException {
		int bytesNeeded = object.length() + ObjectHeader.SIZE;
		ObjectPage page = acquireObjectPageForSize(bytesNeeded);
		int pageNumber = page.getPageNumber();
		int objectNumber = page.reserveObject(object, reservations);
		page.release();
		ObjectAddress address = new ObjectAddress(pageNumber, objectNumber);
		object.setAddress(address);
		object.setStore(this);
		modifiedObjects.put(address, object);
		return address;
	}

	/**
	 * Removes an object from the object store.  In doing so, it must remove it from the cache as well.
	 */
	public void removeObject(ObjectAddress address) throws ObjectStoreException {
		if (phantoms.contains(address)) {
			throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
		}
		if (acquiredObjects.containsKey(address)) {
			throw new ObjectStoreException(ObjectStoreException.ObjectIsLocked);
		}
		StoredObject object = (StoredObject)modifiedObjects.get(address);
		boolean inStore = !reservations.contains(address);
		if (object != null) {
			reservations.remove(address);
			modifiedObjects.remove(address);
		}
		removeFromCache(address);
		if (inStore) phantoms.add(address);
	}

	/**
	 * Places the object in the modified objects cache and marks it as in use.  The modified objects
	 * cache is flushed at commit or rollback time.  At that point, the reference to the object
	 * is dropped.
	 */
//	public void updateObject(StoredObject object) {
//		ObjectAddress address = object.getAddress();
//		if (modifiedObjects.get(address) == null) {
//			modifiedObjects.put(address, object);
//			try {
//				acquireObject(address);
//			} catch (ObjectStoreException e) {
//			}
//		}
//	}

	
	protected void updateSpaceMapPage(int objectPageNumber, int freeSpace) throws ObjectStoreException {
		SpaceMapPage p = acquireSpaceMapPage(objectPageNumber);
		p.setFreeSpace(objectPageNumber, freeSpace);
		p.release();
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
	 * Looks for the first page that guarantees enough space to meet the criteria.
	 * This is the "first fit" algorithm and will get slow as the page file grows since
	 * it is O(n**2).  (Each addition of a new page is preceded by a search of all pages.)
	 * We reduce the overhead by maintaining SpaceMapPages that tell us how full each page
	 * is.  A space map page is the first page of a span of 8K pages (64M total).
	 * Each byte in a space map page indicates the fullness of each page in the span.
	 * Since databases are expected to be quite small (<200Mb) we might be able to live with
	 * this simple algorithm.
	 */
	protected ObjectPage acquireObjectPageForSize(int bytesNeeded)
		throws ObjectStoreException {
		int oPageNumber = 0;
		int numberOfSpans = ((pageStore.numberOfPages() - 1) / ObjectStorePage.SIZE) + 1;
		for (int i = 0; i <= numberOfSpans; i++) {
			try {
				int sPageNumber = i * ObjectStorePage.SIZE;
				SpaceMapPage sPage = (SpaceMapPage) pageStore.acquire(sPageNumber);
				for (int j = 1; j < ObjectStorePage.SIZE; j++) {
					int n = sPageNumber + j;
					Reservation r = reservations.get(n);
					int bytesAvailable = (r == null) ? sPage.getFreeSpace(n) : r.getFreeSpace();
					if (bytesNeeded <= bytesAvailable) {
						oPageNumber = n;
						break;
					}
				}
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
	
	/** 
	 * Acquires a space map page.  This is a convenience method to translate exceptions.
	 */
	protected SpaceMapPage acquireSpaceMapPage(int objectPageNumber) throws ObjectStoreException {
		int pageNumber = objectPageNumber & 0xFFFFE000;
		SpaceMapPage p = null;
		try {
			p = (SpaceMapPage)pageStore.acquire(pageNumber);
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}
		return p;
	}
	
}
