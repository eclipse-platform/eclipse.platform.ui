package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class Reservation {

	protected int freeSlots = 0;
	protected int freeSpace = 0;
	protected int reservedSpace = 0;
	protected int initialEntry = 0;
	protected Map reservedItems = new HashMap();
	
	public Reservation(int freeSpace, int freeSlots, int initialEntry) {
		this.freeSlots = freeSlots;
		this.freeSpace = freeSpace;
		this.initialEntry = initialEntry;
	}
	
	public void add(int slot, int bytes) {
		reservedSpace += bytes;
		reservedItems.put(new Integer(slot), new Integer(bytes));
	}
	
	public void remove(int slot) {
		Integer bytes = (Integer)reservedItems.remove(new Integer(slot));
		if (bytes == null) return;
		reservedSpace -= bytes.intValue();		
	}
	
	boolean contains(int slot) {
		return reservedItems.containsKey(new Integer(slot));
	}
	
	int getFreeSpace() {
		if (reservedItems.size() >= freeSlots) return 0;
		return Math.max(0, freeSpace - reservedSpace);
	}
	
	public int getInitialEntry() {
		return initialEntry;
	}
	
	public void setInitialEntry(int n) {
		initialEntry = n;
	}

}
