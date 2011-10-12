/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.search;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.help.internal.search.SearchIndex;

/**
 * Test locking of search index
 */
public class LockTest extends TestCase {
	
	public static Test suite() {
		return new TestSuite(LockTest.class);
	}

	public void testSingleLock() {
		SearchIndex index1 = new SearchIndex(null, null, null);
		boolean try1 = index1.tryLock();
		assertTrue(try1);
		index1.releaseLock();
	}
	
	public void testCompetingLocks() {
		SearchIndex index1 = new SearchIndex(null, null, null);
		boolean try1 = index1.tryLock();
		assertTrue(try1);
		SearchIndex index2 = new SearchIndex(null, null, null);
		boolean try2 = index2.tryLock();
		assertFalse(try2);
		index1.releaseLock();
		index2.releaseLock();
	}
	

	public void testNonCompetingLocks() {
		SearchIndex index1 = new SearchIndex(null, null, null);
		boolean try1 = index1.tryLock();
		assertTrue(try1);
		index1.releaseLock();
		SearchIndex index2 = new SearchIndex(null, null, null);
		boolean try2 = index2.tryLock();
		assertTrue(try2);
		index2.releaseLock();
	}
			
}
