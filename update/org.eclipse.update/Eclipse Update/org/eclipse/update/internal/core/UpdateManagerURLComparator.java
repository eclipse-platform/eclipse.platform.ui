package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.boot.update.*;
import java.net.*;
/**
 * 
 */
import java.util.Comparator;
public class UpdateManagerURLComparator implements Comparator{
/**
 * UpdateManagerTreeItemComparator constructor comment.
 */
public UpdateManagerURLComparator() {
	super();
}
/**
 * Compares two strings independent of case.
 * 
 * @return a negative integer, zero, or a positive integer as the
 *         first argument is less than, equal to, or greater than the
 *         second. 
 * @throws ClassCastException if the arguments' types prevent them from
 *         being compared by this Comparator.
 */
public int compare(java.lang.Object o1, java.lang.Object o2) {

	if (o1 instanceof URLNamePair && o2 instanceof URLNamePair) {
		return ((URLNamePair) o1).getURL().toExternalForm().toLowerCase().compareTo(((URLNamePair) o2).getURL().toExternalForm().toLowerCase());
	}

	return 0;
}
}
