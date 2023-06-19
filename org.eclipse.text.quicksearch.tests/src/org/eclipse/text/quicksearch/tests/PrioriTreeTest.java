/*******************************************************************************
 * Copyright (c) 2013-2019 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.tests;

import static org.eclipse.text.quicksearch.internal.core.priority.PriorityFunction.PRIORITY_DEFAULT;
import static org.eclipse.text.quicksearch.internal.core.priority.PriorityFunction.PRIORITY_IGNORE;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IPath;
import org.eclipse.text.quicksearch.internal.core.priority.PrioriTree;
import org.junit.Before;
import org.junit.Test;

public class PrioriTreeTest {

	PrioriTree tree;

	@Before
	public void setUp() throws Exception {
		tree = PrioriTree.create();
	}

	@Test
	public void testWithEmptyTree() {
		//In the empty tree most paths are assigned 'DEFAULT' priority.
		checkPriority(PRIORITY_DEFAULT, "/");
		checkPriority(PRIORITY_DEFAULT, "/foo/bar/zor");

		//Paths with ignored extensions should be 'ignored'.
		checkPriority(PRIORITY_IGNORE, "/foo/muck.zip");
		checkPriority(PRIORITY_IGNORE, "/muck.jar");
		checkPriority(PRIORITY_IGNORE, "/images/muck.jpg");

		//Names starting with ignored prefix should be ignored
		checkPriority(PRIORITY_IGNORE, "/project/.git");

		//Some specific names are also to be ignored
		checkPriority(PRIORITY_IGNORE, "/project/target");
		checkPriority(PRIORITY_IGNORE, "/project/build");

	}
	@Test
	public void testSinglePathSet() {
		setPriority("/foo/bar/zor", 100.0);

		//Path itself should have the set priority
		checkPriority(100.0, "/foo/bar/zor");

		//Also the parent paths should have been set automatically
		checkPriority(100.0, "/foo/bar");
		checkPriority(100.0, "/foo");
		checkPriority(100.0, "/");

		//Things not on the paths should still be 'default'
		checkPriority(PRIORITY_DEFAULT, "/other/bar");
		checkPriority(PRIORITY_DEFAULT, "/other");

		//The things nested underneath the set path also get assigned implicitly ...
		checkPriority(100.0, "/foo/bar/zor/nested");
		checkPriority(100.0, "/foo/bar/zor/nested/deeper");
		// ... unless they are 'ignored'. Ignored paths are never converted to non-ignored.

		checkPriority(PRIORITY_IGNORE, "/foo/bar/zor/nested/big.zip");
	}
	@Test
	public void testSetOverlappingPaths() {
		setPriority("/shared/foo", 50.0);
		setPriority("/shared/bar", 100.0);

		tree.dump();

		checkPriority(50.0,  "/shared/foo");
		checkPriority(100.0, "/shared/bar");

		//Shared section of path should get highest priority of both
		checkPriority(100.0,  "/");
		checkPriority(100.0, "/shared");

		//Disjoint paths remain default
		checkPriority(PRIORITY_DEFAULT, "/other");
	}

	/**
	 * Similar to testSetOverlappingPaths but order of
	 * priority set operations is reversed. The result should
	 * be the same.
	 */
	@Test
	public void testSetOverlappingPaths2() {
		setPriority("/shared/bar", 100.0);
		setPriority("/shared/foo", 50.0);

		checkPriority(50.0,  "/shared/foo");
		checkPriority(100.0, "/shared/bar");

		//Shared section of path should get highest priority of both
		checkPriority(100.0,  "/");
		checkPriority(100.0, "/shared");

		//Disjoint paths remain default
		checkPriority(PRIORITY_DEFAULT, "/other");
	}

	/**
	 * Need support for setting priority of an entire subtree.
	 */
	@Test
	public void testSetTreePriority() {
		setPriority("/promoted", 100.0);

		//Stuff not in the raised subtree should be unchanged
		checkPriority(PRIORITY_DEFAULT, "/unrelated");

		//Stuff in the raised subtree should be affected.
		checkPriority(100.0,            "/promoted");
		checkPriority(100.0,            "/promoted/sub");
		checkPriority(100.0,            "/promoted/sub/sub");

		//But... ignored stuff should never be made searchable even in a raised subtree.
		checkPriority(PRIORITY_IGNORE,  "/promoted/big.zip");
	}

	/**
	 * Check that setting priotity of a tree raises children priority also if those
	 * children already had a priority assigned before.
	 */
	@Test
	public void testSetTreePriority2() {
		setPriority("/promoted/sub/sub", 50.0);
		checkPriority(50.0, 			"/promoted");
		checkPriority(50.0, 			"/promoted/sub");
		checkPriority(50.0, 			"/promoted/sub/sub");
		checkPriority(PRIORITY_DEFAULT,	"/promoted/other");

		setPriority("/promoted", 100.0);

		//Stuff not in the raised subtree should be unchanged
		checkPriority(PRIORITY_DEFAULT, "/unrelated");

		//Stuff in the raised subtree should be affected.
		checkPriority(100.0,            "/promoted");
		checkPriority(100.0,            "/promoted/sub");
		checkPriority(100.0,            "/promoted/sub/sub");
		checkPriority(100.0,            "/promoted/other");

		//But... ignored stuff should never be made searchable even in a raised subtree.
		checkPriority(PRIORITY_IGNORE,  "/promoted/sub/big.zip");
		checkPriority(PRIORITY_IGNORE,  "/promoted/other/big.zip");
	}

	private void setPriority(String pathStr, double pri) {
		tree.setPriority(IPath.fromOSString(pathStr), pri);
	}

	private void checkPriority(double expected, String pathStr) {
		assertEquals(pathStr,
				expected, tree.priority(new MockResource(pathStr)), 0);
	}

}
