/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

public interface INavigatable {
	
	static final String NAVIGATOR_PROPERTY= "org.eclipse.compare.internal.Navigator"; //$NON-NLS-1$
	
	/**
	 * Returns true if at end or beginning.
	 */
	boolean gotoDifference(boolean next);
}
