package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class ReservationTable {
	
	protected Map table = new HashMap();
	
	public ReservationTable() {
	}
	
	public Reservation get(int pageNumber) {
		return (Reservation)table.get(new Integer(pageNumber));
	}
	
	public void put(int pageNumber, Reservation r) {
		table.put(new Integer(pageNumber), r);
	}
	
	public boolean contains(int pageNumber) {
		return table.containsKey(new Integer(pageNumber));
	}
	
	public boolean contains(ObjectAddress address) {
		int pageNumber = address.getPageNumber();
		int objectNumber = address.getObjectNumber();
		if (contains(pageNumber)) {
			if (get(pageNumber).contains(objectNumber)) return true;
		}
		return false;
	}
	
	public void remove(ObjectAddress address) {
		int pageNumber = address.getPageNumber();
		int objectNumber = address.getObjectNumber();
		Reservation r = (Reservation)table.get(new Integer(pageNumber));
		if (r == null) return;
		r.remove(objectNumber);
	}
	
	public void clear() {
		table.clear();
	}
	
}
