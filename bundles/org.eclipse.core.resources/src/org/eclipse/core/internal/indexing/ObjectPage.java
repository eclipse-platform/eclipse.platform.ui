/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.indexing;

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
		} else {
			usedSpace = 0;
			usedEntries = 0;
			initialEntry = 0;
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
	 * This method returns the Field mapped over the object for a given object number.
	 */
	public Field getObjectField(int objectNumber) throws ObjectStoreException {
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset == 0) return null;
		ObjectHeader header = new ObjectHeader(pageBuffer.get(blockOffset, ObjectHeader.SIZE));
		Field f = pageBuffer.getField(blockOffset + ObjectHeader.SIZE, header.getObjectLength());
		return f;
	}

	/**
	 * Places an object into a page.  The object must have a reservation.
	 */
	public void insertObject(StoredObject object) throws ObjectStoreException {

		// ensure that there is space for this object
		int blockLength = object.length() + ObjectHeader.SIZE;
		if (getFreeSpace() < blockLength) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
	
		// make sure the slot is still empty
		int objectNumber = object.getAddress().getObjectNumber();
		int entryOffset = ObjectDirectoryOffset + (objectNumber * 2);
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset != 0) {
			throw new ObjectStoreException(ObjectStoreException.PageVacancyFailure);
		}
	
		// place the object into the object space portion of the page
		if (blockLength > (SIZE - freeSpaceOffset)) compress();	// compress the space if necessary
		blockOffset = freeSpaceOffset;						// place the object at the beginning of the free space
		ObjectHeader header = new ObjectHeader(object.length());
		pageBuffer.put(blockOffset, header);
		pageBuffer.put(blockOffset + ObjectHeader.SIZE, object.toByteArray());
		pageBuffer.put(entryOffset, 2, blockOffset);
		freeSpaceOffset += blockLength;						// update where the new free space is
		usedSpace += blockLength;							// indicate that space is used up
		usedEntries++;									// indicate that an entry is used up
		initialEntry = (objectNumber + 1) % MaxEntries;	// set where to begin the next search
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Reserves space for an object on the page.  Records the reservation in the
	 * reservation table.
	 */
	public int reserveObject(StoredObject object, ReservationTable reservations) throws ObjectStoreException {

		// ensure that there is space for this object, there should be since we check beforehand
		int blockLength = object.length() + ObjectHeader.SIZE;
		if (getFreeSpace() < blockLength) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
		
		// get the reservation for this page from the table, create a new one if necessary
		Reservation r = reservations.get(pageNumber);
		if (r == null) {
			r = new Reservation(getFreeSpace(), MaxEntries - usedEntries, initialEntry);
			reservations.put(pageNumber, r);
		}		

		// find an empty slot that is not already reserved
		int objectNumber = r.getInitialEntry();
		int blockOffset = 0;
		int entryOffset = 0;
		for (int i = 0; i < MaxEntries; i++) {
			if (!r.contains(objectNumber)) {
				entryOffset = ObjectDirectoryOffset + (objectNumber * 2);
				blockOffset = pageBuffer.getUInt(entryOffset, 2);
				if (blockOffset == 0) break;
			}
			objectNumber = (objectNumber + 1) % MaxEntries;
		}
		if (blockOffset != 0) {
			throw new ObjectStoreException(ObjectStoreException.PageVacancyFailure);
		}
		
		// begin the next search just after where we left off
		r.setInitialEntry((objectNumber + 1) % MaxEntries);
		
		// update the reservation for this page
		r.add(objectNumber, blockLength);
		return objectNumber;
	}

	public void removeObject(int objectNumber) throws ObjectStoreException {
		
		/* check for existence of the object to be removed */
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset == 0) throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
	
		/* remove the object */
		pageBuffer.put(entryOffset, 2, 0);			// remove its offset from the object table
		ObjectHeader h = new ObjectHeader(pageBuffer.get(blockOffset,ObjectHeader.SIZE));
		int objectLength = h.getObjectLength();
		int blockLength = objectLength + ObjectHeader.SIZE;	// find the length of it in the object space
		pageBuffer.clear(blockOffset,blockLength);	// clear its spot in the object space
		usedSpace -= blockLength;					// space has been freed
		usedEntries--;								// an entry has been freed;
		setChanged();
		notifyObservers();
	}

	/**
	 * Updates an object value on the page.  An object may not change its size.  
	 */
	public void updateObject(StoredObject object) throws ObjectStoreException {
	
		int objectNumber = object.getAddress().getObjectNumber();
	
		/* check for existence of the object to be updated */
		int entryOffset = ObjectDirectoryOffset + 2 * objectNumber;
		int blockOffset = pageBuffer.getUInt(entryOffset, 2);
		if (blockOffset == 0) {
			throw new ObjectStoreException(ObjectStoreException.ObjectExistenceFailure);
		}
	
		/* retrieve the header and check the size */
		ObjectHeader header = new ObjectHeader(pageBuffer.get(blockOffset, ObjectHeader.SIZE));
		if (header.getObjectLength() != object.length()) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
	
		/* update in place */
		int objectOffset = blockOffset + ObjectHeader.SIZE;
		pageBuffer.put(objectOffset, object.toByteArray());
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Compresses the space in the page, putting all the free space at the end of the page.
	 * This will adjust the free space offset and the offsets of the individual objects.  All
	 * of the other parameters of the page remain the same.  Resets the number of 
	 * used entries to fix an old bug.
	 */
	private void compress() throws ObjectStoreException {
		Buffer temp = new Buffer(SIZE);
		int newBlockOffset = ObjectSpaceOffset;
		int entryOffset = ObjectDirectoryOffset;
		usedEntries = 0;
		for (int i = 0; i < MaxEntries; i++) {
			int oldBlockOffset = pageBuffer.getUInt(entryOffset, 2);
			if (oldBlockOffset > 0) {
				ObjectHeader h = new ObjectHeader(pageBuffer.get(oldBlockOffset, ObjectHeader.SIZE));
				int blockLength = h.getObjectLength() + ObjectHeader.SIZE;
				temp.put(newBlockOffset, pageBuffer.get(oldBlockOffset, blockLength));
				pageBuffer.put(entryOffset, 2, newBlockOffset);
				newBlockOffset += blockLength;
				usedEntries++;
			}
			entryOffset += 2;
		}
		pageBuffer.put(ObjectSpaceOffset, temp.get(ObjectSpaceOffset, SIZE - ObjectSpaceOffset));
		freeSpaceOffset = newBlockOffset;
	}
	
	/**
	 * Returns the amount of free space on this page.
	 */
	public int getFreeSpace() {
		if (usedEntries >= MaxEntries) return 0;
		return SIZE - (ObjectSpaceOffset + usedSpace);
	}

	public boolean isObjectPage() {
		return true;
	}
}
