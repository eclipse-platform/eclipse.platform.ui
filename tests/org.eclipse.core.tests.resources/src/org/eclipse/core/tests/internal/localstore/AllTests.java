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
package org.eclipse.core.tests.internal.localstore;

import junit.framework.*;

public class AllTests extends TestCase {
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests() {
	super(null);
}
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests(String name) {
	super(name);
}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(BlobStoreTest.suite());
		suite.addTest(CaseSensitivityTest.suite());
		suite.addTest(CopyTest.suite());
		suite.addTest(DeleteTest.suite());
		suite.addTest(FileSystemResourceManagerTest.suite());
		suite.addTest(FileSystemStoreTest.suite());
		suite.addTest(HistoryStoreTest.suite());
		suite.addTest(LocalStoreTest.suite());
		suite.addTest(LocalSyncTest.suite());
		suite.addTest(MoveTest.suite());
		suite.addTest(RefreshLocalTest.suite());
		suite.addTest(SafeChunkyInputOutputStreamTest.suite());
		suite.addTest(SafeFileInputOutputStreamTest.suite());
		suite.addTest(UnifiedTreeTest.suite());
		return suite;
	}
}
