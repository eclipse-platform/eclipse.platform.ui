/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.util.HashMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.CorePerformanceTest;

public class BenchPath extends CorePerformanceTest {
	public BenchPath() {
		super();
	}

	public BenchPath(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(BenchPath.class.getName());

		//	suite.addTest(new BenchPath("benchHash"));
		//	suite.addTest(new BenchPath("benchPathCreation"));
		suite.addTest(new BenchPath("benchToOSString"));
		suite.addTest(new BenchPath("benchToString"));

		return suite;
	}

	/**
	 * Tests performance of equals and hashCode by using paths
	 * as keys in a hash map.
	 */
	public void benchHash() {
		final int REPEAT = 2000000;
		IPath[] paths = generateVariousPaths();
		HashMap map = new HashMap(32);
		for (int i = 0; i < paths.length; i++) {
			map.put(paths[i], "");
		}
		int numPaths = paths.length;
		startBench();
		for (int i = 0; i < REPEAT; i++) {
			for (int p = 0; p < numPaths; p++) {
				map.get(paths[p]);
			}
		}
		stopBench("benchHash", REPEAT * numPaths);
	}

	/**
	 * Tests the performance of path creation
	 */
	public void benchPathCreation() {

		final int repeat = 100000;
		final int count = repeat * 20;

		startBench();

		for (int rep = repeat; --rep >= 0;) {
			//folders (5)
			new Path("/");
			new Path("/Foo");
			new Path("/Foo/bar");
			new Path("/Foo/bar/baz");
			new Path("/Foo/bar/baz/blap");

			//files (15)
			new Path("/Foo/abc.txt");
			new Path("/Foo/bar/abc.txt");
			new Path("/Foo/bar/baz/abc.txt");
			new Path("/Foo/bar/baz/blap/abc.txt");
			new Path("/Foo/bar/abc.txt");
			new Path("/Foo/bar/baz/abc.txt");
			new Path("/Foo/bar/baz/blap/abc.txt");
			new Path("/Foo/bar/baz/abc.txt");
			new Path("/Foo/bar/baz/blap/abc.txt");
			new Path("/Foo/bar/baz/abc.txt");
			new Path("/Foo/bar/baz/blap/abc.txt");
			new Path("/Foo/bar/baz/abc.txt");
			new Path("/Foo/bar/baz/blap/abc.txt");
			new Path("/Foo/bar/baz/blap/blam/abc.txt");
			new Path("/Foo/bar/baz/blap/blam/blip/boop/abc.txt");

		}

		stopBench("benchPathCreation", count);
	}

	/**
	 * Tests the performance of Path.toOSString
	 */
	public void benchToOSString() {
		final int repeat = 500000;

		IPath[] paths = generateVariousPaths();
		final int count = repeat * paths.length;

		startBench();
		for (int rep = repeat; --rep >= 0;) {
			for (int p = paths.length; --p >= 0;) {
				paths[p].toOSString();
			}
		}
		stopBench("benchToOSString", count);
	}

	/**
	 * Tests the performance of Path.toOSString
	 */
	public void benchToString() {
		final int repeat = 500000;

		IPath[] paths = generateVariousPaths();
		final int count = repeat * paths.length;

		startBench();
		for (int rep = repeat; --rep >= 0;) {
			for (int p = paths.length; --p >= 0;) {
				paths[p].toString();
			}
		}
		stopBench("benchToString", count);
	}

	/**
	 * Returns an array containing various paths.
	 */
	private IPath[] generateVariousPaths() {
		IPath[] paths = new IPath[20];
		int i = 0;
		paths[i++] = new Path("/");
		paths[i++] = new Path("/Foo");
		paths[i++] = new Path("/Foo/bar");
		paths[i++] = new Path("/Foo/bar/baz");
		paths[i++] = new Path("/Foo/bar/baz/blap");

		//files (15)
		paths[i++] = new Path("/Foo/abc.txt");
		paths[i++] = new Path("/Foo/bar/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/abc.txt");
		paths[i++] = new Path("/Foo/bar/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/blam/abc.txt");
		paths[i++] = new Path("/Foo/bar/baz/blap/blam/blip/boop/abc.txt");

		return paths;
	}
}

