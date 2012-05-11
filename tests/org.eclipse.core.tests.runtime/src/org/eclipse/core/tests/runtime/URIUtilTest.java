/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import java.io.*;
import java.net.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;

/**
 * Tests for the {@link URIUtil} class.
 */
public class URIUtilTest extends RuntimeTest {
	/** Constant value indicating if the current platform is Windows */
	private static final boolean WINDOWS = java.io.File.separatorChar == '\\';

	private static final String[] testPaths = new String[] {"abc", "with spaces", "with%percent"};

	public static Test suite() {
		return new TestSuite(URIUtilTest.class);
	}

	// re-enable once bug 331314 is fixed
	public void testBug331314() {
		doTestBug331314("File with spaces");
		doTestBug331314("FileWithBrackets[]");
		doTestBug331314("normal");
	}

	private void doTestBug331314(String name) {
		File f = new File(new File(System.getProperty("java.io.tmpdir")), name);
		URI original = f.toURI();
		URI base = f.getParentFile().toURI();
		URI relative = URIUtil.makeRelative(original, base);
		assertFalse(name, relative.isAbsolute());
		assertEquals("Wrong ssp", name, relative.getSchemeSpecificPart());
	}

	/**
	 * Tests for {@link URIUtil#toJarURI(URI, IPath)}.
	 */
	public void testToJARURI() {
		URL locationURL = FileLocator.find(Platform.getBundle(RuntimeTestsPlugin.PI_RUNTIME_TESTS), new Path("Plugin_Testing/uriutil/test.jar"), null);
		try {
			locationURL = FileLocator.resolve(locationURL);
			URI location = URIUtil.toURI(locationURL);
			final String suffix = "test/1029/test.txt";
			URI jar = URIUtil.toJarURI(location, new Path(suffix));
			InputStream is = jar.toURL().openStream();
			is.close();

			//null entry path
			URI jar2 = URIUtil.toJarURI(location, null);
			assertEquals("2.0", jar.toString(), jar2.toString() + suffix);

		} catch (MalformedURLException e) {
			fail("1.0", e);
		} catch (IOException e) {
			fail("1.1", e);
		} catch (URISyntaxException e) {
			fail("1.2", e);
		}
	}

	/**
	 * Tests for {@link URIUtil#toFile(URI)}.
	 * @throws URISyntaxException 
	 */
	public void testToFile() throws URISyntaxException {
		File base = new File(System.getProperty("java.io.tmpdir"));
		for (int i = 0; i < testPaths.length; i++) {
			File original = new File(base, testPaths[i]);
			URI uri = original.toURI();
			File result = URIUtil.toFile(uri);
			assertEquals("1." + i, original, result);
		}
	}

	/**
	 * Tests for {@link URIUtil#toFile(URI)} involving UNC paths.
	 * @throws URISyntaxException 
	 */
	public void testToFileUNC() throws URISyntaxException {
		if (!WINDOWS)
			return;
		//UNC paths
		URI path = new URI("file://HOST/some/path");
		File result = URIUtil.toFile(path);
		if (File.pathSeparatorChar == '/')
			assertTrue("2.0", result.getAbsolutePath().startsWith("//"));
		else
			assertTrue("2.1", result.getAbsolutePath().startsWith("\\\\"));
		assertTrue("2.2", new Path(result.toString()).isUNC());
	}

	/**
	 * Tests for {@link URIUtil#toUnencodedString(URI)}.
	 */
	public void testToUnencodedString() throws URISyntaxException {
		assertEquals("1.0", "http://foo.bar", URIUtil.toUnencodedString(new URI("http://foo.bar")));
		assertEquals("1.1", "http://foo.bar#fragment", URIUtil.toUnencodedString(new URI("http://foo.bar#fragment")));
		assertEquals("1.2", "foo.bar#fragment", URIUtil.toUnencodedString(new URI("foo.bar#fragment")));
		assertEquals("1.3", "#fragment", URIUtil.toUnencodedString(new URI("#fragment")));

		//spaces
		assertEquals("2.1", "http://foo.bar/a b", URIUtil.toUnencodedString(new URI("http://foo.bar/a%20b")));
		assertEquals("2.2", "http://foo.bar/a#b c", URIUtil.toUnencodedString(new URI("http://foo.bar/a#b%20c")));
		assertEquals("2.3", "foo.bar/a b", URIUtil.toUnencodedString(new URI("foo.bar/a%20b")));
		assertEquals("2.4", "#a b", URIUtil.toUnencodedString(new URI("#a%20b")));
	}

	/**
	 * Tests for {@link URIUtil#fromString(String)}.
	 */
	public void testFromString() throws URISyntaxException {
		//spaces
		assertEquals("1.1", new URI("http://foo.bar/a%20b"), URIUtil.fromString("http://foo.bar/a b"));
		assertEquals("1.2", new URI("http://foo.bar/a#b%20c"), URIUtil.fromString("http://foo.bar/a#b c"));
		assertEquals("1.3", new URI("foo.bar/a%20b"), URIUtil.fromString("foo.bar/a b"));
		assertEquals("1.4", new URI("#a%20b"), URIUtil.fromString("#a b"));
		assertEquals("1.5", new URI("file:/C:/foo.bar/a%20b"), URIUtil.fromString("file:/C:/foo.bar/a b"));

		//percent character
		assertEquals("2.1", new URI("http://foo.bar/a%2520b"), URIUtil.fromString("http://foo.bar/a%20b"));
		assertEquals("2.2", new URI("http://foo.bar/a#b%2520c"), URIUtil.fromString("http://foo.bar/a#b%20c"));
		assertEquals("2.3", new URI("foo.bar/a%2520b"), URIUtil.fromString("foo.bar/a%20b"));
		assertEquals("2.4", new URI("#a%2520b"), URIUtil.fromString("#a%20b"));
		assertEquals("2.5", new URI("file:/C:/foo.bar/a%2520b"), URIUtil.fromString("file:/C:/foo.bar/a%20b"));

		//relative URI
		assertEquals("3.1", new URI("a/b"), URIUtil.fromString("file:a/b"));
		assertEquals("3.2", new URI("a/b"), URIUtil.fromString("a/b"));
		if (WINDOWS) {
			assertEquals("3.3", new URI("file:/c:/a/b"), URIUtil.fromString("file:c:/a/b"));
			assertEquals("3.4", new URI("file:/c:/a/b"), URIUtil.fromString("file:c:\\a\\b"));
			assertEquals("3.5", new URI("file:/c:/a/b"), URIUtil.fromString("file:/c:\\a\\b"));
			assertEquals("3.6", new URI("file:/a/b/c"), URIUtil.fromString("file:/a/b/c"));//bug 264101
			//backslash
			assertEquals("3.7", new URI("file:/a/b/c"), URIUtil.fromString("file:\\a\\b\\c"));//bug 264101
		}

		//encoded legal character
		assertEquals("4.1", new URI("http://foo.bar/a%2Cb").getSchemeSpecificPart(), URIUtil.fromString("http://foo.bar/a,b").getSchemeSpecificPart());
		assertEquals("4.2", new URI("file:/foo.bar/a%2Cb").getSchemeSpecificPart(), URIUtil.fromString("file:/foo.bar/a,b").getSchemeSpecificPart());

		//backslash
		URI uri = URIUtil.fromString("a\\b");
		System.out.println(uri);
	}

	/**
	 * Tests for {@link URIUtil#toURI(java.net.URL)}.
	 */
	public void testURLtoURI() throws MalformedURLException, URISyntaxException {
		//spaces
		assertEquals("1.1", new URI("http://foo.bar/a%20b"), URIUtil.toURI(new URL("http://foo.bar/a b")));
		assertEquals("1.2", new URI("http://foo.bar/a#b%20c"), URIUtil.toURI(new URL("http://foo.bar/a#b c")));

		//% characters
		assertEquals("2.1", new URI("http://foo.bar/a%25b"), URIUtil.toURI(new URL("http://foo.bar/a%b")));

		//UNC paths
		assertEquals("3.1", new URI("file:////SERVER/some/path"), URIUtil.toURI(new URL("file://SERVER/some/path")));
		assertEquals("3.2", new URI("file:////SERVER/some/path"), URIUtil.toURI(new URL("file:////SERVER/some/path")));
	}

	/**
	 * Tests for {@link URIUtil#toURL(java.net.URI)}.
	 */
	public void testURItoURL() throws MalformedURLException, URISyntaxException {
		//spaces
		assertEquals("1.1", new URL("http://foo.bar/a%20b"), URIUtil.toURL(new URI("http://foo.bar/a%20b")));
		assertEquals("1.2", new URL("http://foo.bar/a#b%20c"), URIUtil.toURL(new URI("http://foo.bar/a#b%20c")));

		//% characters
		assertEquals("2.1", new URL("http://foo.bar/a%25b"), URIUtil.toURL(new URI("http://foo.bar/a%25b")));

		//UNC paths
		assertEquals("3.1", new URL("file:////SERVER/some/path"), URIUtil.toURL(new URI("file:////SERVER/some/path")));
		assertEquals("3.2", new URL("file://SERVER/some/path"), URIUtil.toURL(new URI("file://SERVER/some/path")));
	}

	/**
	 * Tests handling of Absolute file system paths on Windows incorrectly encoded as
	 * relative URIs (file:c:/tmp).
	 */
	public void testWindowsPathsFromURI() throws MalformedURLException, URISyntaxException {
		if (!WINDOWS)
			return;
		assertEquals("1.1", new URI("file:/c:/foo/bar.txt"), URIUtil.toURI(new URL("file:c:/foo/bar.txt")));
		assertEquals("1.2", new URI("file:/c:/foo/bar.txt"), URIUtil.toURI(new URL("file:/c:/foo/bar.txt")));
	}

	/**
	 * Tests handling of Absolute file system paths on Windows incorrectly encoded as
	 * relative URIs (file:c:/tmp).
	 */
	public void testWindowsPathsFromString() throws URISyntaxException {
		if (!WINDOWS)
			return;
		assertEquals("1.1", new URI("file:/c:/foo/bar.txt"), URIUtil.fromString("file:c:/foo/bar.txt"));
		assertEquals("1.2", new URI("file:/c:/foo/bar.txt"), URIUtil.fromString("file:/c:/foo/bar.txt"));
	}

	/**
	 * Tests handling of conversion from a File with spaces to URL and File to URI and equivalence of the resulting URI
	 */
	public void testFileWithSpaces() throws MalformedURLException, URISyntaxException {
		File fileWithSpaces = new File("/c:/with spaces/goo");
		URI correctURI = fileWithSpaces.toURI();
		URL fileURL = fileWithSpaces.toURL();
		URI fileURI = null;
		try {
			fileURI = fileURL.toURI();
			fail();
		} catch (URISyntaxException e) {
			fileURI = URIUtil.toURI(fileURL);
		}
		assertEquals("1.1", correctURI, fileURI);

		try {
			fileURI = new URI(fileURL.toString());
			fail();
		} catch (URISyntaxException e) {
			fileURI = URIUtil.fromString(fileURL.toString());
		}
		assertEquals("1.2", correctURI, fileURI);
	}

	/**
	 * Tests handling of conversion from a File with spaces to URL and File to URI and equivalence of the resulting URI
	 */
	public void testFileWithBrackets() throws MalformedURLException, URISyntaxException {
		File fileWithSpaces = new File("/c:/with[brackets]/goo");
		URI correctURI = fileWithSpaces.toURI();
		URL fileURL = fileWithSpaces.toURL();
		URI fileURI = null;
		try {
			fileURI = fileURL.toURI();
			fail();
		} catch (URISyntaxException e) {
			fileURI = URIUtil.toURI(fileURL);
		}
		assertEquals("1.1", correctURI, fileURI);

		try {
			fileURI = new URI(fileURL.toString());
			fail();
		} catch (URISyntaxException e) {
			fileURI = URIUtil.fromString(fileURL.toString());
		}
		assertEquals("1.2", correctURI, fileURI);
	}

	/**
	 * Tests for {@link URIUtil#append(URI, String)}.
	 * @throws URISyntaxException 
	 */
	public void testAppend() throws URISyntaxException {
		URI base = new URI("http://a.b.c/a%20b/");
		URI result = URIUtil.append(base, "file.txt");
		assertEquals("1.0", "http://a.b.c/a%20b/file.txt", result.toString());
		assertEquals("1.1", "//a.b.c/a b/file.txt", result.getSchemeSpecificPart());

		base = new URI("http://a.b.c/a%20b/");
		result = URIUtil.append(base, "a b.txt");
		assertEquals("2.0", "http://a.b.c/a%20b/a%20b.txt", result.toString());
		assertEquals("2.1", "//a.b.c/a b/a b.txt", result.getSchemeSpecificPart());

	}

	/**
	 * Tests for {@link URIUtil#append(URI, String)} when dealing with UNC paths.
	 */
	public void testAppendUNC() throws URISyntaxException {
		//UNC paths
		URI base = new URI("file:////SERVER/some/path/");
		URI relative = new URI("plugins/javax.servlet_2.4.0.v200806031604.jar");
		URI expectedResolved = new URI("file:////SERVER/some/path/plugins/javax.servlet_2.4.0.v200806031604.jar");
		URI resolved = URIUtil.append(base, relative.toString());
		assertEquals("1.0", expectedResolved, resolved);
	}

	/**
	 * Tests for {@link URIUtil#append(URI, String)} when dealing with paths containing brackets.
	 * @throws URISyntaxException 
	 */
	public void testAppendWithBrackets() throws URISyntaxException {
		//append a simple string
		URI base = new URI("http://example.com/base/");
		URI result = URIUtil.append(base, "file[with brackets].txt");
		assertEquals("1.0", "http://example.com/base/file%5Bwith%20brackets%5D.txt", result.toString());
		assertEquals("1.1", "/base/file[with brackets].txt", result.getPath());

		//append a relative path
		result = URIUtil.append(base, "some/path/file[with brackets].txt");
		assertEquals("2.0", "http://example.com/base/some/path/file%5Bwith%20brackets%5D.txt", result.toString());
		assertEquals("2.1", "/base/some/path/file[with brackets].txt", result.getPath());

		//simple string where base has no trailing separator
		base = new URI("http://example.com/base");
		result = URIUtil.append(base, "file[with brackets].txt");
		assertEquals("3.0", "http://example.com/base/file%5Bwith%20brackets%5D.txt", result.toString());
		assertEquals("3.1", "/base/file[with brackets].txt", result.getPath());

		//append a path where base has no trailing separator
		result = URIUtil.append(base, "some/path/file[with brackets].txt");
		assertEquals("4.0", "http://example.com/base/some/path/file%5Bwith%20brackets%5D.txt", result.toString());
		assertEquals("4.1", "/base/some/path/file[with brackets].txt", result.getPath());

		//TODO opaque URI
		//		URI opaque = new URI("opaque:something/opaque/");
		//		result = URIUtil.append(opaque, "some/path/file[with brackets].txt");
		//		assertEquals("5.0", "opaque:something/opaque/some/path/file%5Bwith%20brackets%5D.txt", result.toString());
		//		assertEquals("5.1", null, result.getPath());
	}

	public void testBug286339() throws URISyntaxException {

		//single letter server path
		URI base = new URI("file:////S/some/path/");
		URI relative = new URI("plugins/javax.servlet_2.4.0.v200806031604.jar");
		URI expectedResolved = new URI("file:////S/some/path/plugins/javax.servlet_2.4.0.v200806031604.jar");
		URI resolved = URIUtil.append(base, relative.toString());
		assertEquals("1.1", expectedResolved, resolved);

	}

	public void testAppendWindows() throws URISyntaxException {
		if (!WINDOWS)
			return;
		URI base = new URI("file:/C:/a%20b");
		URI result = URIUtil.append(base, "file.txt");
		assertEquals("1.0", "file:/C:/a%20b/file.txt", result.toString());
		assertEquals("1.1", "/C:/a b/file.txt", result.getSchemeSpecificPart());

		base = new URI("file:/C:/Documents%20and%20Settings/aniefer/junit-workspace/pde.build/265726/buildRepo/");
		result = URIUtil.append(base, "content.jar");
		assertEquals("2.0", "file:/C:/Documents%20and%20Settings/aniefer/junit-workspace/pde.build/265726/buildRepo/content.jar", result.toString());
		assertEquals("2.1", "/C:/Documents and Settings/aniefer/junit-workspace/pde.build/265726/buildRepo/content.jar", result.getSchemeSpecificPart());
	}

	/**
	 * Tests handling of conversion from a File with %20 to URL and File to URI and equivalence of the resulting URI
	 */
	public void testFileWithPercent20() throws MalformedURLException, URISyntaxException {
		File fileWithPercent20 = new File("/c:/with%20spaces/goo");
		URI correctURI = fileWithPercent20.toURI();

		URL fileURL = fileWithPercent20.toURL();
		assertNotSame("1.1", correctURI, fileURL.toURI());
		assertEquals("1.2", correctURI, URIUtil.toURI(fileURL));
		assertNotSame("1.3", correctURI, new URI(fileURL.toString()));
		// we expect these to not be the same because fromString assumes a decoded URL String
		assertNotSame("1.4", correctURI, URIUtil.fromString(fileURL.toString()));
	}

	public void testRemoveExtension() {
		try {
			URI uri1 = new URI("file:/foo/bar/zoo.txt");
			assertEquals(new URI("file:/foo/bar/zoo"), URIUtil.removeFileExtension(uri1));

			URI uri2 = new URI("file:/foo/bar.zoo/foo.txt");
			assertEquals(new URI("file:/foo/bar.zoo/foo"), URIUtil.removeFileExtension(uri2));

			URI uri3 = new URI("file:/foo/bar.zoo/foo");
			assertEquals(new URI("file:/foo/bar.zoo/foo"), URIUtil.removeFileExtension(uri3));

			URI uri4 = new URI("file:/C:/DOCUME~1/ADMINI~1/LOCALS~1/Temp/testRepo/plugins/org.junit_3.8.2.v200706111738.jar");
			assertEquals(new URI("file:/C:/DOCUME~1/ADMINI~1/LOCALS~1/Temp/testRepo/plugins/org.junit_3.8.2.v200706111738"), URIUtil.removeFileExtension(uri4));
		} catch (URISyntaxException e) {
			fail("URI syntax exception", e);
		}
	}

	public void testRemoveFileExtensionFromFile() {
		String fileName = "/c:/some.dir/afile";
		File testFileWithExtension = new File(fileName + ".extension");
		File testFileWithOutExtension = new File(fileName);
		URI correctURI = testFileWithOutExtension.toURI();

		assertEquals(correctURI, URIUtil.removeFileExtension(testFileWithExtension.toURI()));
		assertEquals(correctURI, URIUtil.removeFileExtension(testFileWithOutExtension.toURI()));
	}

	public void testSameURI() throws URISyntaxException {
		assertFalse("1.0", URIUtil.sameURI(new File("a").toURI(), URIUtil.fromString("file:a")));
		assertFalse("1.1", URIUtil.sameURI(new URI("file:/a"), URIUtil.fromString("file:a")));

		//encoded characters
		assertTrue("2.0", URIUtil.sameURI(new URI("foo:/a%2Cb"), new URI("foo:/a,b")));
		assertTrue("2.1", URIUtil.sameURI(new URI("file:/a%2Cb"), new URI("file:/a,b")));
	}

	public void testSameURIWindows() throws URISyntaxException {
		if (!WINDOWS)
			return;
		//device and case variants
		assertTrue("1.0", URIUtil.sameURI(new URI("file:C:/a"), new URI("file:c:/a")));
		assertTrue("1.1", URIUtil.sameURI(new URI("file:/C:/a"), new URI("file:/c:/a")));
		assertTrue("1.2", URIUtil.sameURI(new URI("file:/A"), new URI("file:/a")));
		assertTrue("1.3", URIUtil.sameURI(new URI("file:A"), new URI("file:a")));
		assertTrue("1.4", URIUtil.sameURI(new URI("file:/A/"), new URI("file:/a/")));

		//negative cases
		assertFalse("2.0", URIUtil.sameURI(new URI("file:/a/b"), new URI("file:/c:/a/b")));
	}

	public void testMakeAbsolute() throws URISyntaxException {
		URI[][] data = new URI[][] {
				// simple path
				new URI[] {new URI("b"), new URI("file:/a/"), new URI("file:/a/b")}, //
				new URI[] {new URI("b"), new URI("file:/a"), new URI("file:/a/b")},
				// common root
				new URI[] {new URI("plugins/foo.jar"), new URI("file:/eclipse/"), new URI("file:/eclipse/plugins/foo.jar")},
				// non-local
				new URI[] {new URI("http:/foo.com/a/b"), new URI("file:/a/x"), new URI("http:/foo.com/a/b")}, //
				new URI[] {new URI("file:/a/b"), new URI("http:/foo.com/a/x"), new URI("file:/a/b")}, //
				//
				new URI[] {new URI("../plugins/foo.jar"), new URI("file:/eclipse/configuration"), new URI("file:/eclipse/plugins/foo.jar")}, //
				//cases that can't be made absolute
				//different scheme
				new URI[] {new URI("file:../plugins/foo.jar"), new URI("http:/eclipse/configuration"), new URI("file:../plugins/foo.jar")}, //
				//already absolute
				new URI[] {new URI("file:../plugins/foo.jar"), new URI("file:/eclipse/configuration"), new URI("file:../plugins/foo.jar")}, //
				new URI[] {new URI("file:/foo.jar"), new URI("file:/eclipse/configuration"), new URI("file:/foo.jar")}, //
				//encoded characters
				new URI[] {new URI("plugins%5Cfoo.jar"), new URI("file:/eclipse/"), new URI("file:/eclipse/plugins%5Cfoo.jar")},//
				new URI[] {new URI("a%20b"), new URI("file:/eclipse/"), new URI("file:/eclipse/a%20b")},//
		};

		for (int i = 0; i < data.length; i++) {
			URI location = data[i][0];
			URI root = data[i][1];
			URI expected = data[i][2];
			URI actual = URIUtil.makeAbsolute(location, root);
			assertEquals("1." + Integer.toString(i), expected, actual);
		}

		// run some Windows-specific tests with drive letters
		if (!WINDOWS)
			return;
		data = new URI[][] {
				// simple path
				new URI[] {new URI("b"), new URI("file:/c:/a/"), new URI("file:/c:/a/b")}, //
				new URI[] {new URI("b"), new URI("file:/c:/a"), new URI("file:/c:/a/b")},
				// common root
				new URI[] {new URI("plugins/foo.jar"), new URI("file:/c:/eclipse/"), new URI("file:/c:/eclipse/plugins/foo.jar")},
				// different drives
				new URI[] {new URI("file:/c:/a/b"), new URI("file:/d:/a/x"), new URI("file:/c:/a/b")}, //
				new URI[] {new URI("file:/c:/eclipse/plugins/foo.jar"), new URI("file:/d:/eclipse/"), new URI("file:/c:/eclipse/plugins/foo.jar")},
				// non-local
				new URI[] {new URI("http:/c:/a/b"), new URI("file:/c:/a/x"), new URI("http:/c:/a/b")}, //
				new URI[] {new URI("file:/c:/a/b"), new URI("http:/c:/a/x"), new URI("file:/c:/a/b")}, //
				//
				new URI[] {new URI("b"), new URI("file:/C:/a/"), new URI("file:/C:/a/b")}, //
				new URI[] {new URI("b"), new URI("file:/C:/a"), new URI("file:/C:/a/b")}, //
				new URI[] {new URI("file:/c:/"), new URI("file:/d:/"), new URI("file:/c:/")}, //
				new URI[] {new URI("/c:/a/b/c"), new URI("file:/d:/a/b/"), new URI("file:/c:/a/b/c")}, //
				new URI[] {new URI(""), new URI("file:/c:/"), new URI("file:/c:/")}, //
				//
				new URI[] {new URI("../plugins/foo.jar"), new URI("file:/c:/eclipse/configuration"), new URI("file:/c:/eclipse/plugins/foo.jar")}, //
				//already absolute
				new URI[] {new URI("file:../plugins/foo.jar"), new URI("file:/c:/eclipse/configuration"), new URI("file:../plugins/foo.jar")}, //
		};
		for (int i = 0; i < data.length; i++) {
			URI location = data[i][0];
			URI root = data[i][1];
			URI expected = data[i][2];
			URI actual = URIUtil.makeAbsolute(location, root);
			assertEquals("2." + Integer.toString(i), expected, actual);
		}
	}

	/**
	 * Tests for {@link URIUtil#makeAbsolute(URI, URI)} involving UNC paths.
	 */
	public void testMakeAbsoluteUNC() throws URISyntaxException {
		URI base = new URI("file:////SERVER/some/path/");
		URI relative = new URI("plugins/javax.servlet_2.4.0.v200806031604.jar");
		URI result = URIUtil.makeAbsolute(relative, base);
		assertEquals("1.0", new URI("file:////SERVER/some/path/plugins/javax.servlet_2.4.0.v200806031604.jar"), result);

		//an absolute URI should not be resolved
		URI absolute = new URI("file:////ANOTHERSERVER/another/path");
		URI resolved = URIUtil.makeAbsolute(absolute, base);
		assertEquals("1.1", absolute, resolved);

	}

	public void testMakeRelative() throws URISyntaxException {
		URI[][] data = new URI[][] {
				// simple path
				new URI[] {new URI("file:/a/b"), new URI("file:/a/x"), new URI("../b")},
				// common root
				new URI[] {new URI("file:/eclipse/plugins/foo.jar"), new URI("file:/eclipse/"), new URI("plugins/foo.jar")},
				// non-local
				new URI[] {new URI("http:/foo.com/a/b"), new URI("file:/a/x"), new URI("http:/foo.com/a/b")}, //
				new URI[] {new URI("file:/a/b"), new URI("http:/foo.com/a/x"), new URI("file:/a/b")}, //
				//
				new URI[] {new URI("file:/"), new URI("file:/"), new URI("")}, //
		};

		for (int i = 0; i < data.length; i++) {
			URI location = data[i][0];
			URI root = data[i][1];
			URI expected = data[i][2];
			URI actual = URIUtil.makeRelative(location, root);
			assertEquals("1." + Integer.toString(i), expected, actual);
		}

		// test some Windows-specific paths with drive letters
		if (!WINDOWS)
			return;
		data = new URI[][] {
				// simple path
				new URI[] {new URI("file:/c:/a/b"), new URI("file:/c:/a/x"), new URI("../b")},
				// common root
				new URI[] {new URI("file:/c:/eclipse/plugins/foo.jar"), new URI("file:/c:/eclipse/"), new URI("plugins/foo.jar")},
				// different drives
				new URI[] {new URI("file:/c:/a/b"), new URI("file:/d:/a/x"), new URI("file:/c:/a/b")}, //
				new URI[] {new URI("file:/c:/eclipse/plugins/foo.jar"), new URI("file:/d:/eclipse/"), new URI("file:/c:/eclipse/plugins/foo.jar")},
				// non-local
				new URI[] {new URI("http:/c:/a/b"), new URI("file:/c:/a/x"), new URI("http:/c:/a/b")}, //
				new URI[] {new URI("file:/c:/a/b"), new URI("http:/c:/a/x"), new URI("file:/c:/a/b")}, //
				//
				new URI[] {new URI("file:/c:/a/b"), new URI("file:/C:/a/x"), new URI("../b")}, //
				new URI[] {new URI("file:/c:/"), new URI("file:/d:/"), new URI("file:/c:/")}, //
				new URI[] {new URI("file:/c:/"), new URI("file:/c:/"), new URI("")}, //
		};
		for (int i = 0; i < data.length; i++) {
			URI location = data[i][0];
			URI root = data[i][1];
			URI expected = data[i][2];
			URI actual = URIUtil.makeRelative(location, root);
			assertEquals("2." + Integer.toString(i), expected, actual);
		}
	}
}
