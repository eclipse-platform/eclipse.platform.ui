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

	private static IPageFactory pageFactory = new IPageFactory() {
		public Page create(PageStore store, int pageNumber) {
			ObjectPage p = new ObjectPage();
			p.initialize(store, pageNumber);
			return p;
		}
	};
	
	static int ObjectSpaceSize = Page.Size - ObjectSpaceOffset;
	protected int usedSpace;
	protected int usedEntries;
	protected int freeSpaceOffset;
	protected int initialEntry;

	/**
	 * Default constructor -- used only by a page factory.
	 */
	protected ObjectPage() {
	}
	/**
	 * Acquires a new object page from a page store.  Space map pages must be skipped.
	 */
	public static Page acquire(PageStore store) throws PageStoreException {
		ObjectPage page = (ObjectPage) store.acquire(pageFactory);
		if (page.isSpaceMapPage()) {
			page.release();
			page = (ObjectPage) store.acquire(pageFactory);
		}
		return page;
	}
	/**
	 * Acquires an existing page from a page store.
	 */
	public static Page acquire(PageStore store, int pageNumber) throws PageStoreException {
		return store.acquire(pageFactory, pageNumber);
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
	static ObjectPage acquireForObject(PageStore pageStore, StoredObject object)
		throws ObjectStoreException {
		int bytesNeeded = object.length() + ObjectHeader.Size;
		int oPageNumber = 0;
		int numberOfSpans = ((pageStore.numberOfPages() - 1) / Page.Size) + 1;
		for (int i = 0; i <= numberOfSpans; i++) {
			try {
				int sPageNumber = i * Page.Size;
				SpaceMapPage sPage = (SpaceMapPage) SpaceMapPage.acquire(pageStore, sPageNumber);
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
			ObjectPage oPage = (ObjectPage) ObjectPage.acquire(pageStore, oPageNumber);
			return oPage;
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.PageReadFailure);
		}
	}
	SpaceMapPage acquireSpaceMapPage() throws ObjectStoreException {
		try {
			SpaceMapPage p = (SpaceMapPage) SpaceMapPage.acquire(store, spaceMapPageNumber());
			return p;
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.InternalFailure);
		}
	}
/**
 * Compresses the space in the page, putting all the free space at the end of the page.
 * This will adjust the free space offset and the offsets of the individual objects.  All
 * of the other parameters of the page remain the same.
 */
private void compress() throws ObjectStoreException {
	Buffer temp = new Buffer(Page.Size);
	int newBlockOffset = ObjectSpaceOffset;
	int entryOffset = ObjectDirectoryOffset;
	for (int i = 0; i < 256; i++) {
		int oldBlockOffset = getUInt(entryOffset, 2);
		if (oldBlockOffset > 0) {
			ObjectHeader h = new ObjectHeader(get(oldBlockOffset, ObjectHeader.Size));
			int blockLength = h.getObjectLength() + ObjectHeader.Size;
			temp.put(newBlockOffset, get(oldBlockOffset, blockLength));
			put(entryOffset, 2, newBlockOffset);
			newBlockOffset += blockLength;
		}
		entryOffset += 2;
	}
	put(ObjectSpaceOffset, temp.get(ObjectSpaceOffset, Page.Size - ObjectSpaceOffset));
	freeSpaceOffset = newBlockOffset;
}
	/**
	 * Writes the object page header and from the header instance variables.  This is used just before
	 * the page is to be written to the page store.
	 */
	protected void dematerialize() {
		put(FlagOffset, 2, 0xEEEE);
		put(UsedSpaceOffset, 2, usedSpace);
		put(UsedEntriesOffset, 2, usedEntries);
		put(InitialEntryOffset, 2, initialEntry);
		put(FreeSpaceOffset, 2, freeSpaceOffset);
		}
	/**
	 * Returns the amount of free space on this page.
	 */
	private int freeSpace() {
		if (usedEntries >= MaxEntries) return 0;
		return Page.Size - (ObjectSpaceOffset + usedSpace);
		}
/**
 * This method returns the StoredObject for a given object number.
 */
public StoredObject getObject(ObjectAddress address) throws ObjectStoreException {
	if (address.getPageNumber() != pageNumber)
		return null;
	int objectNumber = address.getObjectNumber();
	int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
	int blockOffset = getUInt(entryOffset, 2);
	if (blockOffset == 0)
		return null;
	ObjectHeader header = new ObjectHeader(get(blockOffset, ObjectHeader.Size));
	Field f = getField(blockOffset + ObjectHeader.Size, header.getObjectLength());
	int type = f.pointTo(0).getInt(2);
	IObjectFactory factory = ObjectStore.getFactory(type);
	StoredObject object = factory.create(f);
	return object;
}
	/**
	 * Initializes an instance of this page.
	 */
	protected void initialize(PageStore store, int pageNumber) {
		super.initialize(store, pageNumber);
		usedSpace = 0;
		usedEntries = 1;
		initialEntry = 1;
		freeSpaceOffset = ObjectSpaceOffset;
	}
	/**
	 * Places an object into a page at an unused object number.
	 */
	public void insertObject(StoredObject object) throws ObjectStoreException {
		
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
			blockOffset = getUInt(entryOffset, 2);
			if (blockOffset == 0) break;
			objectNumber++;
		}
		if (blockOffset != 0) {
			throw new ObjectStoreException(ObjectStoreException.PageVacancyFailure);
		}
		initialEntry = objectNumber + 1;
		object.setAddress(new ObjectAddress(pageNumber, objectNumber));
	
		// place the object into the object space portion of the page
		if (blockLength > (Page.Size - freeSpaceOffset)) compress();	// compress the space if necessary
		blockOffset = freeSpaceOffset;						// place the object at the beginning of the free space
		ObjectHeader header = new ObjectHeader(object.length());
		put(blockOffset, header);
		put(blockOffset + ObjectHeader.Size, object.toByteArray());
		put(entryOffset, 2, blockOffset);
		freeSpaceOffset += blockLength;						// update where the new free space is
		usedSpace += blockLength;							// indicate that space is used up
		usedEntries++;									// indicate that an entry is used up
		SpaceMapPage p = acquireSpaceMapPage();
		p.updateForObjectPage(this);
		releaseSpaceMapPage(p);
	}
	/**
	 * Updates the page fields from its bytes.  This is used when the page has just been mutated from
	 * a its superclass.
	 */
	 
	protected void materialize() {
		super.materialize();
		int initialized = getUInt(FlagOffset, 2);
		if (initialized == 0xEEEE) {
			usedSpace = getUInt(UsedSpaceOffset, 2);
			usedEntries = getUInt(UsedEntriesOffset, 2);
			initialEntry = getUInt(InitialEntryOffset, 2);
			freeSpaceOffset = getUInt(FreeSpaceOffset, 2);
		}
		else {
			usedSpace = 0;
			usedEntries = 1;
			initialEntry = 1;
			freeSpaceOffset = ObjectSpaceOffset;
		}
	}
	void releaseSpaceMapPage(SpaceMapPage p) throws ObjectStoreException {
		try {
			p.release();
		} catch (PageStoreException e) {
			throw new ObjectStoreException(ObjectStoreException.InternalFailure);
		}
	}
	public void removeObject(ObjectAddress address) throws ObjectStoreException {
		
		int objectNumber = address.getObjectNumber();
	
		/* check for existence of the object to be removed */
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = getUInt(entryOffset, 2);
		if (blockOffset == 0) throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
	
		/* remove the object */
		put(entryOffset, 2, 0);						// remove its offset from the object table
		ObjectHeader h = new ObjectHeader(get(blockOffset,ObjectHeader.Size));
		int objectLength = h.getObjectLength();
		int blockLength = objectLength + ObjectHeader.Size;	// find the length of it in the object space
		clear(blockOffset,blockLength);				// clear its spot in the object space
		usedSpace -= blockLength;					// space has been freed
		usedEntries--;							// an entry has been freed;
		SpaceMapPage p = acquireSpaceMapPage();
		p.updateForObjectPage(this);
		releaseSpaceMapPage(p);
	}
	/**
	 * Returns the space class of this page.  The space class indicates an upper bound 
	 * on how much of the object space on a page is used.
	 */
	public byte spaceClass() {
		return SpaceMapPage.spaceClass(freeSpace());
	}
	int spaceMapPageNumber() {
		return (pageNumber / Page.Size) * Page.Size;
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
	int blockOffset = getUInt(entryOffset, 2);
	if (blockOffset == 0) {
		throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
	}

	/* retrieve the header and check the size */
	ObjectHeader header = new ObjectHeader(get(blockOffset, ObjectHeader.Size));
	if (header.getObjectLength() != object.length()) {
		throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
	}

	/* update in place */
	if (object.isPageMapped()) {
		object.dematerialize();
	} else {
		int objectOffset = blockOffset + ObjectHeader.Size;
		put(objectOffset, object.toByteArray());
	}
}
}
