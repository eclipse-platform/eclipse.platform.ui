package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


public class VersionComparator implements java.util.Comparator {
/**
 * VersionComparator constructor comment.
 */
public VersionComparator() {
	super();
}
/**
 * Compares the version object1 with object2.
 * Returns 0 if versions are equal.
 * Returns -1 if object1 is older than object2.
 * Returns +1 if object1 is newer than object2.
 *
 */
public int compare(Object o1, Object o2) {
	VersionIdentifier v1=null, v2 = null;
	
	v1 = new VersionIdentifier( (String)o1 );
	v2 = new VersionIdentifier( (String)o2 );
		
	if (v1.equals(v2))
		return 0;
	if (v1.getMajorComponent() < v2.getMajorComponent())
		return -1;
	else if (v1.getMajorComponent() > v2.getMajorComponent())
		return 1;
	else if (v1.isCompatibleWith(v2))	// same major, but minor or service newer
		return 1;
	else
		return -1;

		

}
}
