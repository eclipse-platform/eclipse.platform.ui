package org.eclipse.core.internal.dtree;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * This class represents the changes in a single node between two data trees.
 */
public final class NodeComparison {
	/**
	 * The data of the old tree
	 */
	private Object oldData;

	/**
	 * The data of the new tree
	 */
	private Object newData;

	/**
	 * Integer describing changes between two data elements
	 */
	private int comparison;

	/**
	 * Extra integer that can be assigned by the client
	 */
	private int userInt;

	/**
	 * Special bits in the comparison flag to indicate the type of change
	 */
	public final static int K_ADDED = 1;
	public final static int K_REMOVED = 2;
	public final static int K_CHANGED = 4;
	NodeComparison(Object oldData, Object newData, int realComparison, int userComparison) {
		this.oldData = oldData;
		this.newData = newData;
		this.comparison = realComparison;
		this.userInt = userComparison;
	}
/**
 * Reverse the nature of the comparison.
 */
NodeComparison asReverseComparison(IComparator comparator) {
	/* switch the data */
	Object tempData = oldData;
	oldData = newData;
	newData = tempData;

	/* re-calculate user comparison */
	userInt = comparator.compare(oldData, newData);

	if (comparison == K_ADDED) {
		comparison = K_REMOVED;
	} else {
		if (comparison == K_REMOVED) {
			comparison = K_ADDED;
		}
	}
	return this;
}
/**
 * Returns an integer describing the changes between the two data objects.
 * The four possible values are K_ADDED, K_REMOVED, K_CHANGED, or 0 representing
 * no change.
 */
public int getComparison() {
	return comparison;
}
/**
 * Returns the data of the new node.
 */
public Object getNewData() {
	return newData;
}
/**
 * Returns the data of the old node.
 */
public Object getOldData() {
	return oldData;
}
/**
 * Returns the client specified integer
 */
public int getUserComparison() {
	return userInt;
}
/**
 * Returns true if this comparison has no change, and false otherwise.
 */
boolean isUnchanged() {
	return userInt == 0;
}
/**
 * For debugging
 */
public String toString() {
	StringBuffer buf = new StringBuffer("NodeComparison(");
	switch (comparison) {
		case K_ADDED:
			buf.append("Added, ");
			break;
		case K_REMOVED:
			buf.append("Removed, ");
			break;
		case K_CHANGED:
			buf.append("Changed, ");
			break;
		case 0:
			buf.append("No change, ");
			break;
		default:
			buf.append("Corrupt(" + comparison + "), ");
	}
	buf.append(userInt);
	buf.append(")");
	return buf.toString();
}
}
