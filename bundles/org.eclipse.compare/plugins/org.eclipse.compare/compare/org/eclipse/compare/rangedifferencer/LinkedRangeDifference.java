/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.rangedifferencer;

/* package */ class LinkedRangeDifference extends RangeDifference {

	static final int INSERT= 0;
	static final int DELETE= 1;
	static final int CHANGE= 2;
	static final int ERROR= 3;

	LinkedRangeDifference fNext;

	/**
	 * Creates a LinkedRangeDifference an initializes it to the error state
	 */
	LinkedRangeDifference() {
		super(ERROR);
		fNext= null;
	}

	/**
	 * Constructs and links a LinkeRangeDifference to another LinkedRangeDifference
	 */
	LinkedRangeDifference(LinkedRangeDifference next, int operation) {
		super(operation);
		fNext= next;
	}

	/**
	 * Follows the next link
	 */
	LinkedRangeDifference getNext() {
		return fNext;
	}

	boolean isDelete() {
		return kind() == DELETE;
	}

	boolean isInsert() {
		return kind() == INSERT;
	}

	/**
	 * Sets the next link of this LinkedRangeDifference
	 */
	void setNext(LinkedRangeDifference next) {
		fNext= next;
	}
}
