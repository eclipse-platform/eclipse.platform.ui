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

import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Test cases for the Path class.
 */
public class PathTest extends RuntimeTest {
	/** Constant value indicating if the current platform is Windows */
	private static final boolean WINDOWS = java.io.File.separatorChar == '\\';

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public PathTest() {
		super(null);
	}

	public PathTest(String name) {
		super(name);
	}

	public static Test suite() {

		return new TestSuite(PathTest.class);

		//TestSuite suite= new TestSuite();
		//suite.addTest(new PathTest("testConstructors"));
		//return suite;
	}

	public void testAddTrailingSeparator() {

		IPath with = new Path("/first/second/third/");
		IPath without = new Path("/first/second/third");

		assertSame("1.0", with, with.addTrailingSeparator());
		assertEquals("1.1", with, without.addTrailingSeparator());
		assertTrue("1.2", without.equals(without.addTrailingSeparator()));

		assertSame("2.0", Path.ROOT, Path.ROOT.addTrailingSeparator());
		assertEquals("2.1", Path.ROOT, Path.EMPTY.addTrailingSeparator());

		with = new Path("//first/second/third/");
		without = new Path("//first/second/third");

		assertSame("3.0", with, with.addTrailingSeparator());
		assertEquals("3.1", with, without.addTrailingSeparator());
		assertTrue("3.2", without.equals(without.addTrailingSeparator()));

		assertSame("4.0", Path.ROOT, Path.ROOT.addTrailingSeparator());
		assertEquals("4.1", Path.ROOT, Path.EMPTY.addTrailingSeparator());

		with = new Path("c:/first/second/third/");
		without = new Path("c:/first/second/third");

		assertSame("5.0", with, with.addTrailingSeparator());
		assertEquals("5.1", with, without.addTrailingSeparator());
		assertTrue("5.2", without.equals(without.addTrailingSeparator()));

		assertSame("6.0", Path.ROOT, Path.ROOT.addTrailingSeparator());
		assertEquals("6.1", Path.ROOT, Path.EMPTY.addTrailingSeparator());
	}

	public void testAppend() {

		IPath fore = new Path("/first/second/third/");
		String aftString = "/fourth/fifth";
		IPath aft = new Path(aftString);
		IPath combo = new Path("/first/second/third/fourth/fifth");

		assertEquals("1.0", combo, fore.append(aft));
		assertEquals("1.1", combo, fore.removeTrailingSeparator().append(aft));
		assertEquals("1.2", combo, Path.ROOT.append(fore).append(aft));
		assertTrue("1.3", !fore.append(aft).hasTrailingSeparator());
		assertTrue("1.4", !Path.ROOT.append(fore).append(aft).hasTrailingSeparator());
		assertTrue("1.5", !fore.removeTrailingSeparator().append(aft).hasTrailingSeparator());

		assertEquals("2.0", combo, fore.append(aftString));
		assertEquals("2.1", combo, fore.removeTrailingSeparator().append(aftString));
		assertEquals("2.2", combo, Path.ROOT.append(fore).append(aftString));
		assertTrue("2.3", !fore.append(aftString).hasTrailingSeparator());
		assertTrue("2.4", !Path.ROOT.append(fore).append(aftString).hasTrailingSeparator());
		assertTrue("2.5", !fore.removeTrailingSeparator().append(aftString).hasTrailingSeparator());

		//ensure append preserves correct trailing separator
		assertTrue("3.0", !fore.append("aft").hasTrailingSeparator());
		assertTrue("3.1", fore.append("aft/").hasTrailingSeparator());
		assertTrue("3.2", !fore.append("/aft").hasTrailingSeparator());
		assertTrue("3.3", fore.append("/aft/").hasTrailingSeparator());
		assertTrue("3.4", !fore.append("\\aft").hasTrailingSeparator());
		//backslash is a trailing separator on windows only
		assertTrue("3.5", fore.append("aft\\").hasTrailingSeparator() == WINDOWS);
		assertTrue("3.6", !fore.append("fourth/fifth").hasTrailingSeparator());
		assertTrue("3.7", fore.append("fourth/fifth/").hasTrailingSeparator());
		assertTrue("3.8", !fore.append(new Path("aft")).hasTrailingSeparator());
		assertTrue("3.9", fore.append(new Path("aft/")).hasTrailingSeparator());
		assertTrue("3.10", !fore.append(new Path("fourth/fifth")).hasTrailingSeparator());
		assertTrue("3.11", fore.append(new Path("fourth/fifth/")).hasTrailingSeparator());

		//make sure append converts slashes appropriately
		if (WINDOWS) {
			aftString = "fourth\\fifth";
			assertEquals("4.0", combo, fore.append(aftString));
			assertEquals("4.1", combo, fore.removeTrailingSeparator().append(aftString));
			assertEquals("4.2", combo, Path.ROOT.append(fore).append(aftString));
		}

		assertEquals("5.0", new Path("/foo"), Path.ROOT.append("../foo"));
		assertEquals("5.1", new Path("/foo"), Path.ROOT.append("./foo"));
		assertEquals("5.2", new Path("c:/foo/xyz"), new Path("c:/foo/bar").append("../xyz"));
		assertEquals("5.3", new Path("c:/foo/bar/xyz"), new Path("c:/foo/bar").append("./xyz"));

		//append preserves device and leading separator of receiver
		if (WINDOWS) {
			assertEquals("6.1", new Path("c:foo/bar"), new Path("c:").append("/foo/bar"));
			assertEquals("6.2", new Path("c:foo/bar"), new Path("c:").append("foo/bar"));
			assertEquals("6.3", new Path("c:/foo/bar"), new Path("c:/").append("/foo/bar"));
			assertEquals("6.4", new Path("c:/foo/bar"), new Path("c:/").append("foo/bar"));
			assertEquals("6.5", new Path("c:foo/bar"), new Path("c:").append("z:/foo/bar"));
			assertEquals("6.6", new Path("c:foo/bar"), new Path("c:").append("z:foo/bar"));
			assertEquals("6.7", new Path("c:/foo/bar"), new Path("c:/").append("z:/foo/bar"));
			assertEquals("6.8", new Path("c:/foo/bar"), new Path("c:/").append("z:foo/bar"));
			assertEquals("6.9", new Path("c:/foo"), new Path("c:/").append("z:foo"));
		} else {
			assertEquals("6.1", new Path("c:/foo/bar"), new Path("c:").append("/foo/bar"));
			assertEquals("6.2", new Path("c:/foo/bar/"), new Path("c:").append("foo/bar/"));
			assertEquals("6.3", new Path("/c:/foo/bar"), new Path("/c:").append("/foo/bar"));
			assertEquals("6.4", new Path("/c:/foo/bar"), new Path("/c:").append("foo/bar"));
		}


		assertEquals("6.10", new Path("foo/bar"), new Path("foo").append(new Path("/bar")));
		assertEquals("6.11", new Path("foo/bar"), new Path("foo").append(new Path("bar")));
		assertEquals("6.12", new Path("/foo/bar"), new Path("/foo/").append(new Path("/bar")));
		assertEquals("6.13", new Path("/foo/bar"), new Path("/foo/").append(new Path("bar")));

		assertEquals("6.14", new Path("foo/bar/"), new Path("foo").append(new Path("/bar/")));
		assertEquals("6.15", new Path("foo/bar/"), new Path("foo").append(new Path("bar/")));
		assertEquals("6.16", new Path("/foo/bar/"), new Path("/foo/").append(new Path("/bar/")));
		assertEquals("6.17", new Path("/foo/bar/"), new Path("/foo/").append(new Path("bar/")));

		//append preserves isUNC of receiver
		assertEquals("7.0", new Path("/foo/bar"), new Path("/foo").append("//bar"));
		assertEquals("7.1", new Path("/foo/bar/test"), new Path("/foo").append("bar//test"));
		assertEquals("7.2", new Path("//foo/bar"), new Path("//foo").append("bar"));

		//append empty path does nothing
		assertEquals("8.0", fore, fore.append(Path.ROOT));
		assertEquals("8.1", fore, fore.append(Path.EMPTY));
		assertEquals("8.2", fore, fore.append(new Path("//")));
		assertEquals("8.3", fore, fore.append(new Path("/")));
		assertEquals("8.4", fore, fore.append(new Path("")));
		assertEquals("8.5", fore, fore.append("//"));
		assertEquals("8.6", fore, fore.append("/"));
		assertEquals("8.7", fore, fore.append(""));
		if (WINDOWS) {
			assertEquals("8.8", fore, fore.append("c://"));
			assertEquals("8.9", fore, fore.append("c:/"));
			assertEquals("8.10", fore, fore.append("c:"));
		}
	}

	public void testSegmentCount() {

		assertEquals("1.0", 0, Path.ROOT.segmentCount());
		assertEquals("1.1", 0, Path.EMPTY.segmentCount());

		assertEquals("2.0", 1, new Path("/first").segmentCount());
		assertEquals("2.1", 1, new Path("/first/").segmentCount());
		assertEquals("2.2", 3, new Path("/first/second/third/").segmentCount());
		assertEquals("2.3", 3, new Path("/first/second/third").segmentCount());
		assertEquals("2.4", 5, new Path("/first/second/third/fourth/fifth").segmentCount());

		assertEquals("3.0", 0, new Path("//").segmentCount());
		assertEquals("3.1", 1, new Path("//first").segmentCount());
		assertEquals("3.2", 1, new Path("//first/").segmentCount());
		assertEquals("3.3", 2, new Path("//first/second").segmentCount());
		assertEquals("3.4", 2, new Path("//first/second/").segmentCount());
	}

	public void testCanonicalize() {
		// Test collapsing multiple separators
		// double slashes at the beginning of a path
		// are left and assumed to be a UNC path
		assertEquals("//", new Path("///////").toString());
		assertEquals("/a/b/c", new Path("/a/b//c").toString());
		assertEquals("//a/b/c", new Path("//a/b//c").toString());
		assertEquals("a/b/c/", new Path("a/b//c//").toString());

		// Test collapsing single dots
		assertEquals("2.0", "/", new Path("/./././.").toString());
		assertEquals("2.1", "/a/b/c", new Path("/a/./././b/c").toString());
		assertEquals("2.2", "/a/b/c", new Path("/a/./b/c/.").toString());
		assertEquals("2.3", "a/b/c", new Path("a/./b/./c").toString());

		// Test collapsing double dots
		assertEquals("3.0", "/a/b", new Path("/a/b/c/..").toString());
		assertEquals("3.1", "/", new Path("/a/./b/../..").toString());
		assertEquals("3.2", "../", new Path("../").toString());
		// test bug 46043 - IPath collapseParentReferences
		//	assertEquals("3.3", "../", new Path("./../").toString());
		//	assertEquals("3.4", "../", new Path(".././").toString());
		//	assertEquals("3.5", "..", new Path("./..").toString());

	}

	public void testClone() {

		IPath anyPath = new Path("/a/b/c");
		assertEquals("1.0", anyPath, anyPath.clone());
		anyPath = new Path("//a/b/c");
		assertEquals("1.1", anyPath, anyPath.clone());
		anyPath = new Path("c:/a/b/c");
		assertEquals("1.2", anyPath, anyPath.clone());

		assertEquals("1.3", Path.ROOT, Path.ROOT.clone());
	}

	public void testConstructors() {

		assertEquals("1.0", "", new Path("").toString());
		assertEquals("1.1", "/", new Path("/").toString());
		assertEquals("1.2", "a", new Path("a").toString());
		assertEquals("1.3", "/a", new Path("/a").toString());
		assertEquals("1.4", "//", new Path("//").toString());
		assertEquals("1.5", "/a/", new Path("/a/").toString());
		assertEquals("1.6", "/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z", new Path("/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z").toString());
		assertEquals("1.7", "...", new Path("...").toString());
		assertEquals("1.8", "/a/b/.../c", new Path("/a/b/.../c").toString());

		IPath anyPath = new Path("/first/second/third");

		assertEquals("2.0", Path.EMPTY, new Path(""));
		assertEquals("2.1", Path.ROOT, new Path("/"));
		assertEquals("2.2", anyPath, anyPath);
	}

	public void testFirstSegment() {

		assertNull("1.0", Path.ROOT.segment(0));
		assertNull("1.1", Path.EMPTY.segment(0));

		assertEquals("2.0", "a", new Path("/a/b/c").segment(0));
		assertEquals("2.1", "a", new Path("a").segment(0));
		assertEquals("2.2", "a", new Path("/a").segment(0));
		assertEquals("2.3", "a", new Path("a/b").segment(0));
		assertEquals("2.4", "a", new Path("//a/b").segment(0));
		if (WINDOWS) {
			assertEquals("2.5", "a", new Path("c:a/b").segment(0));
			assertEquals("2.6", "a", new Path("c:/a/b").segment(0));
		} else {
			assertEquals("2.5", "c:", new Path("c:/a/b").segment(0));
			assertEquals("2.6", "c:", new Path("c:/a\\b").segment(0));
			assertEquals("2.5", "a", new Path("a/c:/b").segment(0));
			assertEquals("2.6", "a\\b", new Path("a\\b/b").segment(0));
		}

	}
	
	public void testFromPortableString() {
		assertEquals("1.0", "", Path.fromPortableString("").toString());
		assertEquals("1.1", "/", Path.fromPortableString("/").toString());
		assertEquals("1.2", "a", Path.fromPortableString("a").toString());
		assertEquals("1.3", "/a", Path.fromPortableString("/a").toString());
		assertEquals("1.4", "//", Path.fromPortableString("//").toString());
		assertEquals("1.5", "/a/", Path.fromPortableString("/a/").toString());

		assertEquals("2.1", "a:", Path.fromPortableString("a:").toString());
		assertEquals("2.2", "a:", Path.fromPortableString("a::").toString());
		assertEquals("2.3", "a:b:", Path.fromPortableString("a:b::").toString());
		assertEquals("2.4", "a/b:c", Path.fromPortableString("a/b::c").toString());
		assertEquals("2.5", "a/b:c", Path.fromPortableString("a/b:c").toString());
		assertEquals("2.6", "a:b", Path.fromPortableString("a::b").toString());

	}

	public void testGetFileExtension() {

		IPath anyPath = new Path("index.html");
		assertEquals("1.0", anyPath.getFileExtension(), "html");

		assertNull("2.0", Path.ROOT.getFileExtension());
		assertNull("2.1", Path.EMPTY.getFileExtension());
		assertNull("2.2", new Path("index").getFileExtension());
		assertNull("2.3", new Path("/a/b/c.txt/").getFileExtension());

		assertEquals("3.0", "txt", new Path("/a/b/c.txt").getFileExtension());
		assertEquals("3.1", "txt", new Path("/a/b/c.foo.txt").getFileExtension());
		assertEquals("3.2", "txt", new Path("//a/b/c.foo.txt").getFileExtension());
		assertEquals("3.3", "txt", new Path("c:/a/b/c.foo.txt").getFileExtension());
		assertEquals("3.4", "txt", new Path("c:a/b/c.foo.txt").getFileExtension());

	}

	public void testHasTrailingSeparator() {

		// positive
		assertTrue("1.0", new Path("/first/second/third/").hasTrailingSeparator());
		assertTrue("1.1", new Path("//first/second/third/").hasTrailingSeparator());
		assertTrue("1.2", new Path("c:/first/second/third/").hasTrailingSeparator());
		assertTrue("1.3", new Path("c:first/second/third/").hasTrailingSeparator());

		// negative
		assertTrue("2.0", !new Path("first/second/third").hasTrailingSeparator());
		assertTrue("2.1", !Path.ROOT.hasTrailingSeparator());
		assertTrue("2.2", !Path.EMPTY.hasTrailingSeparator());
		assertTrue("2.3", !new Path("//first/second/third").hasTrailingSeparator());
		assertTrue("2.4", !new Path("c:/first/second/third").hasTrailingSeparator());
		assertTrue("2.5", !new Path("c:first/second/third").hasTrailingSeparator());

		//paths of length 0 never have a trailing separator
		assertTrue("3.0", !new Path("/first/").removeLastSegments(1).hasTrailingSeparator());
		assertTrue("3.1", !new Path("/first/").removeFirstSegments(1).hasTrailingSeparator());
		assertTrue("3.2", !new Path("/").hasTrailingSeparator());
		assertTrue("3.3", !new Path("/first/").append("..").hasTrailingSeparator());
		assertTrue("3.4", !new Path("/first/").append(new Path("..")).hasTrailingSeparator());
		assertTrue("3.5", !new Path("/first/../").hasTrailingSeparator());
		assertTrue("3.6", !Path.ROOT.addTrailingSeparator().hasTrailingSeparator());
		assertTrue("3.7", !Path.EMPTY.addTrailingSeparator().hasTrailingSeparator());

	}

	public void testIsAbsolute() {

		// positive
		assertTrue("1.0", new Path("/first/second/third").isAbsolute());
		assertTrue("1.1", Path.ROOT.isAbsolute());
		assertTrue("1.2", new Path("//first/second/third").isAbsolute());
		if (WINDOWS) {
			assertTrue("1.3", new Path("c:/first/second/third").isAbsolute());
		} else {
			assertTrue("1.3", new Path("/c:first/second/third").isAbsolute());
		}

		// negative
		assertTrue("2.0", !new Path("first/second/third").isAbsolute());
		assertTrue("2.1", !Path.EMPTY.isAbsolute());
		assertTrue("2.2", !new Path("c:first/second/third").isAbsolute());

		// unc
		if (WINDOWS) {
			assertTrue("3.0", new Path("c://").isAbsolute());
		} else {
			assertTrue("3.0", new Path("//c:/").isAbsolute());
		}
		assertTrue("3.1", new Path("//").isAbsolute());
		assertTrue("3.2", new Path("//a").isAbsolute());
		assertTrue("3.3", new Path("//a/b/").isAbsolute());

	}

	public void testIsEmpty() {

		// positive
		assertTrue("1.0", Path.EMPTY.isEmpty());
		assertTrue("1.1", new Path("//").isEmpty());
		assertTrue("1.2", new Path("").isEmpty());
		assertTrue("1.1", new Path("c:").isEmpty() == WINDOWS);

		// negative
		assertTrue("2.0", !new Path("first/second/third").isEmpty());
		assertTrue("2.1", !Path.ROOT.isEmpty());
		assertTrue("2.2", !new Path("//a").isEmpty());
		assertTrue("2.3", !new Path("c:/").isEmpty());
	}

	public void testIsPrefixOf() {

		IPath prefix = new Path("/first/second");
		IPath path = new Path("/first/second/third/fourth");

		assertTrue("1.0", prefix.isPrefixOf(path));
		// test the case where the arg is longer than the receiver.
		assertTrue("1.1", !path.isPrefixOf(prefix));
		assertTrue("1.2", !new Path("fifth/sixth").isPrefixOf(path));

		assertTrue("2.0", prefix.addTrailingSeparator().isPrefixOf(path));

		assertTrue("3.0", Path.ROOT.isPrefixOf(path));
		assertTrue("3.1", Path.EMPTY.isPrefixOf(path));
		assertTrue("3.2", !path.isPrefixOf(Path.ROOT));
		assertTrue("3.3", !path.isPrefixOf(Path.EMPTY));
	}

	public void testIsRoot() {

		// negative
		assertTrue("1.0", !new Path("/first/second").isRoot());
		assertTrue("1.1", !Path.EMPTY.isRoot());
		assertTrue("1.2", !new Path("//").isRoot());

		// positive
		assertTrue("2.0", Path.ROOT.isRoot());
		assertTrue("2.1", new Path("/").isRoot());
	}

	public void testIsUNC() {

		// negative
		assertTrue("1.0", !Path.ROOT.isUNC());
		assertTrue("1.1", !Path.EMPTY.isUNC());

		assertTrue("2.0", !new Path("a").isUNC());
		assertTrue("2.1", !new Path("a/b").isUNC());
		assertTrue("2.2", !new Path("/a").isUNC());
		assertTrue("2.3", !new Path("/a/b").isUNC());

		assertTrue("3.0", !new Path("c:/a/b").isUNC());
		assertTrue("3.1", !new Path("c:a/b").isUNC());
		assertTrue("3.2", !new Path("/F/../").isUNC());

		assertTrue("4.0", !new Path("c://a/").isUNC());
		assertTrue("4.1", !new Path("c:\\/a/b").isUNC());
		assertTrue("4.2", !new Path("c:\\\\").isUNC());

		// positive
		assertTrue("5.0", new Path("//").isUNC());
		assertTrue("5.1", new Path("//a").isUNC());
		assertTrue("5.2", new Path("//a/b").isUNC());
		if (WINDOWS) {
			assertTrue("5.3", new Path("\\\\ThisMachine\\HOME\\foo.jar").isUNC());
			assertTrue("6.0", new Path("c://a/").setDevice(null).isUNC());
			assertTrue("6.1", new Path("c:\\/a/b").setDevice(null).isUNC());
			assertTrue("6.2", new Path("c:\\\\").setDevice(null).isUNC());
		}
	}

	public void testIsValidPath() {
		IPath test = Path.ROOT;
		// positive
		assertTrue("1.0", test.isValidPath("/first/second/third"));
		assertTrue("1.1", test.isValidPath(""));
		assertTrue("1.2", test.isValidPath("a"));
		assertTrue("1.3", test.isValidPath("c:"));
		assertTrue("1.4", test.isValidPath("//"));
		assertTrue("1.5", test.isValidPath("//a"));
		assertTrue("1.6", test.isValidPath("c:/a"));
		assertTrue("1.7", test.isValidPath("c://a//b//c//d//e//f"));
		assertTrue("1.8", test.isValidPath("//a//b//c//d//e//f"));

		// negative
		if (WINDOWS) {
			assertTrue("2.1", !test.isValidPath("c:b:"));
			assertTrue("2.2", !test.isValidPath("a/b:"));
		}
	}

	public void testLastSegment() {

		assertEquals("1.0", "second", new Path("/first/second").lastSegment());

		assertEquals("2.0", "first", new Path("first").lastSegment());
		assertEquals("2.1", "first", new Path("/first/").lastSegment());
		assertEquals("2.2", "second", new Path("first/second").lastSegment());
		assertEquals("2.3", "second", new Path("first/second/").lastSegment());

		assertNull("3.0", Path.EMPTY.lastSegment());
		assertNull("3.1", Path.ROOT.lastSegment());
		assertNull("3.2", new Path("//").lastSegment());

		assertEquals("4.0", "second", new Path("//first/second/").lastSegment());
		assertEquals("4.1", "second", new Path("//first/second").lastSegment());
		assertEquals("4.2", "second", new Path("c:/first/second/").lastSegment());
		assertEquals("4.3", "second", new Path("c:first/second/").lastSegment());

		assertEquals("5.0", "first", new Path("//first").lastSegment());
		assertEquals("5.1", "first", new Path("//first/").lastSegment());
	}

	public void testMakeAbsolute() {
		IPath anyPath = new Path("first/second/third").makeAbsolute();
		assertTrue("1.0", anyPath.isAbsolute());
		assertEquals("1.1", new Path("/first/second/third"), anyPath);

		anyPath = new Path("").makeAbsolute();
		assertTrue("2.0", anyPath.isAbsolute());
		assertEquals("2.1", Path.ROOT, anyPath);
	}

	public void testMakeRelative() {
		IPath anyPath = new Path("/first/second/third").makeRelative();
		assertTrue("1.0", !anyPath.isAbsolute());
		assertEquals("1.1", new Path("first/second/third"), anyPath);

		anyPath = Path.ROOT.makeRelative();
		assertTrue("2.0", !anyPath.isAbsolute());
		assertEquals("2.1", new Path(""), anyPath);
	}

	public void testMakeUNC() {

		ArrayList inputs = new ArrayList();
		ArrayList expected = new ArrayList();
		ArrayList expectedNon = new ArrayList();

		inputs.add(Path.ROOT);
		expected.add("//");
		expectedNon.add("/");

		inputs.add(Path.EMPTY);
		expected.add("//");
		expectedNon.add("");

		inputs.add(new Path("a"));
		expected.add("//a");
		expectedNon.add("a");

		inputs.add(new Path("a/b"));
		expected.add("//a/b");
		expectedNon.add("a/b");

		inputs.add(new Path("/a/b/"));
		expected.add("//a/b/");
		expectedNon.add("/a/b/");

		inputs.add(new Path("//"));
		expected.add("//");
		expectedNon.add("/");

		inputs.add(new Path("//a"));
		expected.add("//a");
		expectedNon.add("/a");

		inputs.add(new Path("//a/b"));
		expected.add("//a/b");
		expectedNon.add("/a/b");

		inputs.add(new Path("//a/b/"));
		expected.add("//a/b/");
		expectedNon.add("/a/b/");

		inputs.add(new Path("c:", "/"));
		expected.add("//");
		expectedNon.add("c:/");

		inputs.add(new Path("c:", ""));
		expected.add("//");
		expectedNon.add("c:");

		inputs.add(new Path("c:", "a"));
		expected.add("//a");
		expectedNon.add("c:a");

		inputs.add(new Path("c:", "a/b"));
		expected.add("//a/b");
		expectedNon.add("c:a/b");

		inputs.add(new Path("c:", "/a"));
		expected.add("//a");
		expectedNon.add("c:/a");

		inputs.add(new Path("c:", "/a/b"));
		expected.add("//a/b");
		expectedNon.add("c:/a/b");

		assertEquals("0.0", inputs.size(), expected.size());
		assertEquals("0.1", inputs.size(), expectedNon.size());

		for (int i = 0; i < inputs.size(); i++) {
			IPath path = (IPath) inputs.get(i);
			IPath result = path.makeUNC(true);
			assertTrue("1.0." + path + " (" + result + ")", result.isUNC());
			assertEquals("1.1." + path, (String) expected.get(i), result.toString());
			result = path.makeUNC(false);
			assertTrue("1.3." + path, !result.isUNC());
			assertEquals("1.4." + path, (String) expectedNon.get(i), result.toString());
		}
	}

	/**
	 * This test is for bizarre cases that previously caused errors.
	 */
	public void testRegression() {
		try {
			new Path("C:\\/eclipse");
		} catch (Exception e) {
			fail("1.0", e);
		}
		try {
			if (WINDOWS) {
				IPath path = new Path("d:\\\\ive");
				assertTrue("2.0", !path.isUNC());
				assertEquals("2.1", 1, path.segmentCount());
				assertEquals("2.2", "ive", path.segment(0));
			}
		} catch (Exception e) {
			fail("2.99", e);
		}

	}

	public void testRemoveFirstSegments() {
		assertEquals("1.0", new Path("second"), new Path("/first/second").removeFirstSegments(1));
		assertEquals("1.1", new Path("second/third/"), new Path("/first/second/third/").removeFirstSegments(1));
		assertEquals("1.2", Path.EMPTY, new Path("first").removeFirstSegments(1));
		assertEquals("1.3", Path.EMPTY, new Path("/first/").removeFirstSegments(1));
		assertEquals("1.4", new Path("second"), new Path("first/second").removeFirstSegments(1));
		assertEquals("1.5", Path.EMPTY, new Path("").removeFirstSegments(1));
		assertEquals("1.6", Path.EMPTY, Path.ROOT.removeFirstSegments(1));
		assertEquals("1.7", Path.EMPTY, new Path("/first/second/").removeFirstSegments(2));
		assertEquals("1.8", Path.EMPTY, new Path("/first/second/").removeFirstSegments(3));
		assertEquals("1.9", new Path("third/fourth"), new Path("/first/second/third/fourth").removeFirstSegments(2));

		if (WINDOWS) {
			assertEquals("2.0", new Path("c:second"), new Path("c:/first/second").removeFirstSegments(1));
			assertEquals("2.1", new Path("c:second/third/"), new Path("c:/first/second/third/").removeFirstSegments(1));
			assertEquals("2.2", new Path("c:"), new Path("c:first").removeFirstSegments(1));
			assertEquals("2.3", new Path("c:"), new Path("c:/first/").removeFirstSegments(1));
			assertEquals("2.4", new Path("c:second"), new Path("c:first/second").removeFirstSegments(1));
			assertEquals("2.5", new Path("c:"), new Path("c:").removeFirstSegments(1));
			assertEquals("2.6", new Path("c:"), new Path("c:/").removeFirstSegments(1));
			assertEquals("2.7", new Path("c:"), new Path("c:/first/second/").removeFirstSegments(2));
			assertEquals("2.8", new Path("c:"), new Path("c:/first/second/").removeFirstSegments(3));
			assertEquals("2.9", new Path("c:third/fourth"), new Path("c:/first/second/third/fourth").removeFirstSegments(2));
		}

		assertEquals("3.0", new Path("second"), new Path("//first/second").removeFirstSegments(1));
		assertEquals("3.1", new Path("second/third/"), new Path("//first/second/third/").removeFirstSegments(1));
		assertEquals("3.2", Path.EMPTY, new Path("//first/").removeFirstSegments(1));
		assertEquals("3.3", Path.EMPTY, new Path("//").removeFirstSegments(1));
		assertEquals("3.4", Path.EMPTY, new Path("//first/second/").removeFirstSegments(2));
		assertEquals("3.5", Path.EMPTY, new Path("//first/second/").removeFirstSegments(3));
		assertEquals("3.6", new Path("third/fourth"), new Path("//first/second/third/fourth").removeFirstSegments(2));
	}

	public void testRemoveLastSegments() {

		assertEquals("1.0", new Path("/first"), new Path("/first/second").removeLastSegments(1));
		assertEquals("1.1", new Path("//first"), new Path("//first/second").removeLastSegments(1));
		assertEquals("1.2", new Path("c:/first"), new Path("c:/first/second").removeLastSegments(1));
		assertEquals("1.3", new Path("c:first"), new Path("c:first/second").removeLastSegments(1));

		assertEquals("2.0", new Path("/first/second/"), new Path("/first/second/third/").removeLastSegments(1));
		assertEquals("2.1", new Path("//first/second/"), new Path("//first/second/third/").removeLastSegments(1));
		assertEquals("2.2", new Path("c:/first/second/"), new Path("c:/first/second/third/").removeLastSegments(1));
		assertEquals("2.3", new Path("c:first/second/"), new Path("c:first/second/third/").removeLastSegments(1));

		assertEquals("3.0", Path.EMPTY, new Path("first").removeLastSegments(1));
		assertEquals("3.1", Path.ROOT, new Path("/first/").removeLastSegments(1));
		assertEquals("3.2", new Path("first"), new Path("first/second").removeLastSegments(1));

		assertEquals("4.0", Path.EMPTY, new Path("").removeLastSegments(1));
		assertEquals("4.1", Path.ROOT, Path.ROOT.removeLastSegments(1));
		assertEquals("4.2", new Path("//"), new Path("//").removeLastSegments(1));
	}

	public void testRemoveTrailingSeparator() {

		IPath with = new Path("/first/second/third/");
		IPath without = new Path("/first/second/third");

		assertSame("1.0", without, without.removeTrailingSeparator());
		assertEquals("1.1", without, with.removeTrailingSeparator());
		// trailing separators have no bearing on path equality so check via
		// other means....
		assertTrue("1.2", !with.removeTrailingSeparator().hasTrailingSeparator());
		assertTrue("1.3", !without.hasTrailingSeparator());
		assertEquals("1.4", without.toString(), with.removeTrailingSeparator().toString());

		assertSame("2.0", Path.ROOT, Path.ROOT.removeTrailingSeparator());
		assertEquals("2.1", Path.EMPTY, new Path("").removeTrailingSeparator());

		assertEquals("3.0", new Path("//"), new Path("//").removeTrailingSeparator());
		assertEquals("3.1", new Path("//a"), new Path("//a").removeTrailingSeparator());
		assertEquals("3.2", new Path("//a"), new Path("//a/").removeTrailingSeparator());
		assertEquals("3.3", new Path("//a/b"), new Path("//a/b").removeTrailingSeparator());
		assertEquals("3.4", new Path("//a/b"), new Path("//a/b/").removeTrailingSeparator());

		assertEquals("4.0", new Path("c:"), new Path("c:").removeTrailingSeparator());
		assertEquals("4.1", new Path("c:/"), new Path("c:/").removeTrailingSeparator());
		assertEquals("4.2", new Path("c:/a"), new Path("c:/a/").removeTrailingSeparator());
		assertEquals("4.3", new Path("c:/a/b"), new Path("c:/a/b").removeTrailingSeparator());
		assertEquals("4.4", new Path("c:/a/b"), new Path("c:/a/b/").removeTrailingSeparator());

		assertEquals("5.0", new Path("c:a"), new Path("c:a/").removeTrailingSeparator());
		assertEquals("5.1", new Path("c:a/b"), new Path("c:a/b").removeTrailingSeparator());
		assertEquals("5.2", new Path("c:a/b"), new Path("c:a/b/").removeTrailingSeparator());
	}

	public void testSegments() {

		IPath anyPath = null;
		String[] segs = null;

		// Case One: typical case
		anyPath = new Path("/first/second/third/fourth");
		segs = anyPath.segments();

		assertEquals("1.0", 4, segs.length);
		assertEquals("1.1", "first", segs[0]);
		assertEquals("1.2", "second", segs[1]);
		assertEquals("1.3", "third", segs[2]);
		assertEquals("1.4", "fourth", segs[3]);

		// Case Two: trailing separator
		anyPath = new Path("/first/second/");
		segs = anyPath.segments();

		assertEquals("2.0", 2, segs.length);
		assertEquals("2.1", "first", segs[0]);
		assertEquals("2.2", "second", segs[1]);

		// Case Three: no leading or trailing separators
		anyPath = new Path("first/second");
		segs = anyPath.segments();

		assertEquals("3.0", 2, segs.length);
		assertEquals("3.1", "first", segs[0]);
		assertEquals("3.2", "second", segs[1]);

		// Case Four: single segment
		anyPath = new Path("first");
		segs = anyPath.segments();

		assertEquals("4.0", 1, segs.length);
		assertEquals("4.1", "first", segs[0]);

		// Case Five(a): no segments
		anyPath = Path.EMPTY;
		segs = anyPath.segments();

		assertEquals("5.0", 0, segs.length);

		// Case Five(b): no segments
		anyPath = Path.ROOT;
		segs = anyPath.segments();

		assertEquals("6.0", 0, segs.length);

		// Case Six: UNC path
		anyPath = new Path("//server/volume/a/b/c");
		segs = anyPath.segments();
		assertEquals("7.0", 5, segs.length);
		assertEquals("7.1", "server", segs[0]);
		assertEquals("7.2", "volume", segs[1]);
		assertEquals("7.3", "a", segs[2]);
		assertEquals("7.4", "b", segs[3]);
		assertEquals("7.5", "c", segs[4]);
	}

	public void testToString() {

		IPath anyPath = new Path("/first/second/third");
		assertEquals("1.0", "/first/second/third", anyPath.toString());

		assertEquals("1.1", "/", Path.ROOT.toString());
		assertEquals("1.2", "", Path.EMPTY.toString());
	}

	public void testUptoSegment() {

		//Case 1, absolute path with no trailing separator
		IPath anyPath = new Path("/first/second/third");

		assertEquals("1.0", Path.ROOT, anyPath.uptoSegment(0));
		assertEquals("1.1", new Path("/first"), anyPath.uptoSegment(1));
		assertEquals("1.2", new Path("/first/second"), anyPath.uptoSegment(2));
		assertEquals("1.3", new Path("/first/second/third"), anyPath.uptoSegment(3));
		assertEquals("1.4", new Path("/first/second/third"), anyPath.uptoSegment(4));

		//Case 2, absolute path with trailing separator
		anyPath = new Path("/first/second/third/");

		assertEquals("2.0", Path.ROOT, anyPath.uptoSegment(0));
		assertEquals("2.1", new Path("/first/"), anyPath.uptoSegment(1));
		assertEquals("2.2", new Path("/first/second/"), anyPath.uptoSegment(2));
		assertEquals("2.3", new Path("/first/second/third/"), anyPath.uptoSegment(3));
		assertEquals("2.4", new Path("/first/second/third/"), anyPath.uptoSegment(4));

		//Case 3, relative path with no trailing separator
		anyPath = new Path("first/second/third");

		assertEquals("3.0", Path.EMPTY, anyPath.uptoSegment(0));
		assertEquals("3.1", new Path("first"), anyPath.uptoSegment(1));
		assertEquals("3.2", new Path("first/second"), anyPath.uptoSegment(2));
		assertEquals("3.3", new Path("first/second/third"), anyPath.uptoSegment(3));
		assertEquals("3.4", new Path("first/second/third"), anyPath.uptoSegment(4));

		//Case 4, relative path with trailing separator
		anyPath = new Path("first/second/third/");

		assertEquals("4.0", Path.EMPTY, anyPath.uptoSegment(0));
		assertEquals("4.1", new Path("first/"), anyPath.uptoSegment(1));
		assertEquals("4.2", new Path("first/second/"), anyPath.uptoSegment(2));
		assertEquals("4.3", new Path("first/second/third/"), anyPath.uptoSegment(3));
		assertEquals("4.4", new Path("first/second/third/"), anyPath.uptoSegment(4));

		// bug 58835 - upToSegment(0) needs to preserve device
		if (WINDOWS) {
			anyPath = new Path("c:/first/second/third");
			assertEquals("5.0", new Path("c:/"), anyPath.uptoSegment(0));
			anyPath = new Path("c:/first/second/third/");
			assertEquals("5.1", new Path("c:/"), anyPath.uptoSegment(0));
			anyPath = new Path("c:first/second/third/");
			assertEquals("5.2", new Path("c:"), anyPath.uptoSegment(0));
		}
		anyPath = new Path("//one/two/three");
		assertEquals("5.3", new Path("//"), anyPath.uptoSegment(0));
		anyPath = new Path("//one/two/three/");
		assertEquals("5.4", new Path("//"), anyPath.uptoSegment(0));
	}
}