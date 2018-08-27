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
		suite.addTest(BlobStoreTest.suite());
		suite.addTest(BucketTreeTests.suite());
		suite.addTest(CaseSensitivityTest.suite());
		suite.addTest(CopyTest.suite());
		suite.addTest(DeleteTest.suite());
		suite.addTest(FileSystemResourceManagerTest.suite());
		suite.addTest(HistoryBucketTest.suite());
		suite.addTest(HistoryStoreTest.suite());
		suite.addTest(LocalSyncTest.suite());
		suite.addTest(MoveTest.suite());
		suite.addTest(PrefixPoolTest.suite());
		suite.addTest(RefreshLocalTest.suite());
		suite.addTest(SafeChunkyInputOutputStreamTest.suite());
		suite.addTest(SafeFileInputOutputStreamTest.suite());
		suite.addTest(SymlinkResourceTest.suite());
		suite.addTest(UnifiedTreeTest.suite());
		return suite;
	}
}
