package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Generic object filter mechanism.
 */
public interface ICriteria {
	/**
	 * Returns true if the candidate object satisfies the specified
	 * criteria value according to a particular algorithm.
	 */
	public boolean test(Object candidate, Object value);
}
