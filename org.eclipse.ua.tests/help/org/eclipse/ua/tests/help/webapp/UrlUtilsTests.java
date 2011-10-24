/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import org.eclipse.help.internal.webapp.data.UrlUtil;

import junit.framework.TestCase;

/**
 * Test for methods in UrlUtils
 */

public class UrlUtilsTests extends TestCase {

	public void testHelpNav() {
		assertTrue(UrlUtil.isNavPath("/help/nav/1_2"));
	}

	public void testOtherNav() {
		assertTrue(UrlUtil.isNavPath("/other/nav/1_2"));
	}

	public void testHelpHelpNav() {
		assertFalse(UrlUtil.isNavPath("/help/help/nav/1_2"));
	}

	public void testNoNav() {
		assertFalse(UrlUtil.isNavPath("/helpcontext"));
	}
	
	public void testNoSlash() {
		assertFalse(UrlUtil.isNavPath("help/nav/1_2"));
	}
	
	public void testNavTopicPath() {
		int[] path = UrlUtil.getTopicPath("/nav/23_4_5", "en_us");
		assertEquals(3, path.length);
		assertEquals(23, path[0]);
		assertEquals(4, path[1]);
		assertEquals(5, path[2]);
    }

	public void testRelativePathUnrelated() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/advanced/index.jsp");
		assertEquals("../basic/index.jsp", UrlUtil.getRelativePath(req, "/basic/index.jsp"));
	}

	public void testRelativePathSameStart() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/advanced/index.jsp");
		assertEquals("test.jsp", UrlUtil.getRelativePath(req, "/advanced/test.jsp"));
	}
	
	public void testRelativePathSameFile() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/advanced/index.jsp");
		assertEquals("index.jsp", UrlUtil.getRelativePath(req, "/advanced/index.jsp"));
	}

	public void testRelativeAlmostMatch1() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/advanced/index.jsp");
		assertEquals("../advance/index.jsp", UrlUtil.getRelativePath(req, "/advance/index.jsp"));
	}
	
	public void testRelativeAlmostMatch2() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/advanced/index.jsp");
		assertEquals("../advancedd/index.jsp", UrlUtil.getRelativePath(req, "/advancedd/index.jsp"));
	}
	
	public void testRelativePathMultipleMatchingSegments() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/a/b/c/index.jsp");
		assertEquals("../d/test.jsp", UrlUtil.getRelativePath(req, "/a/b/d/test.jsp"));
	}	

	public void testRelativePathSecondSegmentMatch1() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/a/a/a/index.jsp");
		assertEquals("../../../b/a/c/test.jsp", UrlUtil.getRelativePath(req, "/b/a/c/test.jsp"));
	}
	
	public void testRelativePathSecondSegmentMatch2() {
		MockServletRequest req = new MockServletRequest();
		req.setPathInfo("/b/a/c/index.jsp");
		assertEquals("../../../a/a/a/test.jsp", UrlUtil.getRelativePath(req, "/a/a/a/test.jsp"));
	}

	public void testGetHelpUrlNull() {
		assertEquals("about:blank", UrlUtil.getHelpURL(null));
	}

	public void testGetHelpUrlHttp() {
		assertEquals("http://www.eclipse.org", UrlUtil.getHelpURL("http://www.eclipse.org"));
	}

	public void testGetHelpUrlHttps() {
		assertEquals("https://bugs.eclipse.org/bugs/", UrlUtil.getHelpURL("https://bugs.eclipse.org/bugs/"));
	}
	
	public void testGetHelpUrlFile() {
		assertEquals("../topic/file://etc/about.html", UrlUtil.getHelpURL("file://etc/about.html"));
	}
	
	public void testGetHelpUrlPageInBundle() {
		assertEquals("../topic/bundle/help.html", UrlUtil.getHelpURL("/bundle/help.html"));
	}
	
	public void testGetHelpUrlNullDepthTwo() {
		assertEquals("about:blank", UrlUtil.getHelpURL(null, 2));
	}

	public void testGetHelpUrlHttpDepthTwo() {
		assertEquals("http://www.eclipse.org", UrlUtil.getHelpURL("http://www.eclipse.org", 2));
	}

	public void testGetHelpUrlHttpDepthTwos() {
		assertEquals("https://bugs.eclipse.org/bugs/", UrlUtil.getHelpURL("https://bugs.eclipse.org/bugs/", 2));
	}
	
	public void testGetHelpUrlFileDepthTwo() {
		assertEquals("../../topic/file://etc/about.html", UrlUtil.getHelpURL("file://etc/about.html", 2));
	}
	
	public void testGetHelpUrlPageInBundleDepthTwo() {
		assertEquals("../../topic/bundle/help.html", UrlUtil.getHelpURL("/bundle/help.html", 2));
	}
	
	
}
