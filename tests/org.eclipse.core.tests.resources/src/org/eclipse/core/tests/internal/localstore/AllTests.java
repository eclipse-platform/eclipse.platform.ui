/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [105554] added PrefixPoolTest
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
		suite.addTestSuite(BlobStoreTest.class);
		suite.addTestSuite(BucketTreeTests.class);
		suite.addTestSuite(CaseSensitivityTest.class);
		suite.addTestSuite(CopyTest.class);
		suite.addTestSuite(DeleteTest.class);
		suite.addTestSuite(FileSystemResourceManagerTest.class);
		suite.addTestSuite(HistoryBucketTest.class);
		suite.addTestSuite(HistoryStoreTest.class);
		suite.addTestSuite(LocalSyncTest.class);
		suite.addTestSuite(MoveTest.class);
		suite.addTestSuite(PrefixPoolTest.class);
		suite.addTestSuite(RefreshLocalTest.class);
		suite.addTestSuite(SafeChunkyInputOutputStreamTest.class);
		suite.addTestSuite(SafeFileInputOutputStreamTest.class);
		suite.addTestSuite(SymlinkResourceTest.class);
		suite.addTestSuite(UnifiedTreeTest.class);
		return suite;
	}
}
