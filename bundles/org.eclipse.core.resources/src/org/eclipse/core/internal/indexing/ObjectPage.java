package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
An ObjectPage is a page in a page file that contains objects.  Objects are byte arrays.
An object page contains metainformation about the objects located on that page as well
as information about the state of the page.  This information is updated as the page
has objects placed on it or removed from it.  Objects within a page are identified by
their object number, which is in the range of 1-255 inclusive.

All pages are numbered.  Space map pages are located at page numbers i*(Page.Size) in 
the page file.  Object pages are located at all page numbers between space map pages.  
Thus object pages will never have page numbers that are multiples of the page size.
*/

import java.util.*;

class ObjectPage extends ObjectStorePage {

	protected static final int MaxEntries = 256;
	protected static final int ObjectDirectoryOffset = 64;
	protected static final int ObjectSpaceOffset = ObjectDirectoryOffset + 512;
	protected static final int FlagOffset = 0;
	protected static final int UsedSpaceOffset = 2;
	protected static final int UsedEntriesOffset = 4;
	protected static final int InitialEntryOffset = 6;
	protected static final int FreeSpaceOffset = 8;

	public static final int ObjectSpaceSize = SIZE - ObjectSpaceOffset;

	protected int usedSpace;
	protected int usedEntries;
	protected int freeSpaceOffset;
	protected int initialEntry;

	/**
	 * Creates a new page from a buffer.
	 */
	public ObjectPage(int pageNumber, byte[] buffer, PageStore pageStore) {
		super(pageNumber, buffer, pageStore);
	}
	
	/**
	 * Writes the contents of the page to a buffer.
	 */
	public void toBuffer(byte[] buffer) {
		dematerialize();
		pageBuffer.copyTo(buffer);
	}
	
	/**
	 * Updates the page fields from its bytes.  This is used when the page has just been mutated from
	 * a its superclass.
	 */
	 
	protected void materialize() {
		int initialized = pageBuffer.getUInt(FlagOffset, 2);
		if (initialized == 0xEEEE) {
			usedSpace = pageBuffer.getUInt(UsedSpaceOffset, 2);
			usedEntries = pageBuffer.getUInt(UsedEntriesOffset, 2);
			initialEntry = pageBuffer.getUInt(InitialEntryOffset, 2);
			freeSpaceOffset = pageBuffer.getUInt(FreeSpaceOffset, 2);
		}
		else {
			usedSpace = 0;
			usedEntries = 1;
			initialEntry = 1;
			freeSpaceOffset = ObjectSpaceOffset;
		}
	}

	/**
	 * Writes the object page header and from the header instance variables.  This is used just before
	 * the page is to be written to the page store.
	 */
	protected void dematerialize() {
		pageBuffer.put(FlagOffset, 2, 0xEEEE);
		pageBuffer.put(UsedSpaceOffset, 2, usedSpace);
		pageBuffer.put(UsedEntriesOffset, 2, usedEntries);
		pageBuffer.put(InitialEntryOffset, 2, initialEntry);
		pageBuffer.put(FreeSpaceOffset, 2, freeSpaceOffset);
	}

	/**
	 * This method returns the StoredObject for a given object number.
	 */
	public StoredObject getObject(ObjectAddress address) throws ObjectStoreException {
		if (address.getPageNumber() != pageNumber)
			return null;
		int objectNumber = address.getObjectNumber();
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset == 0)
			return null;
		ObjectHeader header = new ObjectHeader(pageBuffer.get(blockOffset, ObjectHeader.Size));
		Field f = pageBuffer.getField(blockOffset + ObjectHeader.Size, header.getObjectLength());
		int type = f.pointTo(0).getInt(2);
		IObjectFactory factory = ObjectStore.getFactory(type);
		StoredObject object = factory.create(f);
		return object;
	}

	/**
	 * Places an object into a page at an unused object number.
	 */
	public void insertObject(StoredObject object) throws ObjectStoreException {
		
		SpaceMapPage p;
		try {
			p = (SpaceMapPage)pageStore.acquire(spaceMapPageNumber());
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}
		
		// ensure that there is space for this object
		int blockLength = object.length() + ObjectHeader.Size;
		if (freeSpace() < blockLength) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
	
		// find an empty slot
		int objectNumber = initialEntry;
		int blockOffset = 0;
		int entryOffset = 0;
		for (int i = 1; i < MaxEntries; i++) {
			if (objectNumber == MaxEntries) objectNumber = 1;
			entryOffset = ObjectDirectoryOffset + (objectNumber * 2);
			blockOffset = pageBuffer.getUInt(entryOffset, 2);
			if (blockOffset == 0) break;
			objectNumber++;
		}
		if (blockOffset != 0) {
			throw new ObjectStoreException(ObjectStoreException.PageVacancyFailure);
		}
		initialEntry = objectNumber + 1;
		object.setAddress(new ObjectAddress(pageNumber, objectNumber));
	
		// place the object into the object space portion of the page
		if (blockLength > (SIZE - freeSpaceOffset)) compress();	// compress the space if necessary
		blockOffset = freeSpaceOffset;						// place the object at the beginning of the free space
		ObjectHeader header = new ObjectHeader(object.length());
		pageBuffer.put(blockOffset, header);
		pageBuffer.put(blockOffset + ObjectHeader.Size, object.toByteArray());
		pageBuffer.put(entryOffset, 2, blockOffset);
		freeSpaceOffset += blockLength;						// update where the new free space is
		usedSpace += blockLength;							// indicate that space is used up
		usedEntries++;									// indicate that an entry is used up
		p.updateForObjectPage(pageNumber, freeSpace());
		p.release();
		setChanged();
		notifyObservers();
	}

	public void removeObject(ObjectAddress address) throws ObjectStoreException {
		
		SpaceMapPage p = null;
		try {
			p = (SpaceMapPage)pageStore.acquire(spaceMapPageNumber());
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}

		int objectNumber = address.getObjectNumber();
	
		/* check for existence of the object to be removed */
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset == 0) throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
	
		/* remove the object */
		pageBuffer.put(entryOffset, 2, 0);						// remove its offset from the object table
		ObjectHeader h = new ObjectHeader(pageBuffer.get(blockOffset,ObjectHeader.Size));
		int objectLength = h.getObjectLength();
		int blockLength = objectLength + ObjectHeader.Size;	// find the length of it in the object space
		pageBuffer.clear(blockOffset,blockLength);	// clear its spot in the object space
		usedSpace -= blockLength;					// space has been freed
		usedEntries--;								// an entry has been freed;
		p.updateForObjectPage(pageNumber, freeSpace());
		p.release();
		setChanged();
		notifyObservers();
	}

	/**
	 * Updates an object value on the page.  An object may not change its size.  
	 */
	public void updateObject(StoredObject object) throws ObjectStoreException {
	
		if (object.getAddress().getPageNumber() != pageNumber) {
			throw new ObjectStoreException(ObjectStoreException.InternalFailure);
		}
	
		int objectNumber = object.getAddress().getObjectNumber();
	
		/* check for existence of the object to be updated */
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset == 0) {
			throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
		}
	
		/* retrieve the header and check the size */
		ObjectHeader header = new ObjectHeader(pageBuffer.get(blockOffset, ObjectHeader.Size));
		if (header.getObjectLength() != object.length()) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
	
		/* update in place */
		if (object.isPageMapped()) {
			object.dematerialize();
		} else {
			int objectOffset = blockOffset + ObjectHeader.Size;
			pageBuffer.put(objectOffset, object.toByteArray());
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Compresses the space in the page, putting all the free space at the end of the page.
	 * This will adjust the free space offset and the offsets of the individual objects.  All
	 * of the other parameters of the page remain the same.
	 */
	private void compress() throws ObjectStoreException {
		Buffer temp = new Buffer(SIZE);
		int newBlockOffset = ObjectSpaceOffset;
		int entryOffset = ObjectDirectoryOffset;
		for (int i = 0; i < 256; i++) {
			int oldBlockOffset = pageBuffer.getUInt(entryOffset, 2);
			if (oldBlockOffset > 0) {
				ObjectHeader h = new ObjectHeader(pageBuffer.get(oldBlockOffset, ObjectHeader.Size));
				int blockLength = h.getObjectLength() + ObjectHeader.Size;
				temp.put(newBlockOffset, pageBuffer.get(oldBlockOffset, blockLength));
				pageBuffer.put(entryOffset, 2, newBlockOffset);
				newBlockOffset += blockLength;
			}
			entryOffset += 2;
		}
		pageBuffer.put(ObjectSpaceOffset, temp.get(ObjectSpaceOffset, SIZE - ObjectSpaceOffset));
		freeSpaceOffset = newBlockOffset;
	}
	
	/**
	 * Returns the amount of free space on this page.
	 */
	private int freeSpace() {
		if (usedEntries >= MaxEntries) return 0;
		return SIZE - (ObjectSpaceOffset + usedSpace);
	}

	public boolean isObjectPage() {
		return true;
	}
	protected int spaceMapPageNumber() {
		return (pageNumber / SIZE) * SIZE;
	}

}
