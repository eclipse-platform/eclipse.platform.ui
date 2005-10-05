/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import junit.framework.*;

public class AllTests extends TestCase {

	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(BlobStoreTest.suite());
		suite.addTest(BucketTreeTests.suite());
		suite.addTest(CaseSensitivityTest.suite());
		suite.addTest(CopyTest.suite());
		suite.addTest(DeleteTest.suite());
		suite.addTest(FileSystemResourceManagerTest.suite());
		suite.addTest(FileStoreTest.suite());
		suite.addTest(HistoryBucketTest.suite());
		suite.addTest(HistoryStoreTest.suite());
		suite.addTest(HistoryStoreConversionTest.suite());
		suite.addTest(LocalSyncTest.suite());
		suite.addTest(MoveTest.suite());
		suite.addTest(RefreshLocalTest.suite());
		suite.addTest(SafeChunkyInputOutputStreamTest.suite());
		suite.addTest(SafeFileInputOutputStreamTest.suite());
		suite.addTest(UnifiedTreeTest.suite());
		return suite;
	}
}
