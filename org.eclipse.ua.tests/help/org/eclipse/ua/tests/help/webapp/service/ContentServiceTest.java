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
package org.eclipse.ua.tests.help.webapp.service;

import java.io.IOException;

import org.eclipse.ua.tests.help.remote.ContentServletTest;

public class ContentServiceTest extends ContentServletTest {

	private static final String UA_TESTS = "org.eclipse.ua.tests";

	public void testSimpleContent() throws Exception {
		final String path = "/data/help/index/topic1.html";
		String remoteContent = ServicesTestUtils.getRemoteContent(UA_TESTS, path, "en");
		String localContent = ServicesTestUtils.getLocalContent(UA_TESTS, path);
	    assertEquals(remoteContent, localContent);
	}

	public void testFilteredContent() throws Exception {
		final String path = "/data/help/manual/filter.xhtml";
		String remoteContent = ServicesTestUtils.getRemoteContent(UA_TESTS, path, "en");
		String localContent = ServicesTestUtils.getLocalContent(UA_TESTS, path);
	    assertEquals(remoteContent, localContent);
	}

	public void testContentInEnLocale() throws Exception {
		final String path = "/data/help/search/testnl1.xhtml";
		String remoteContent = ServicesTestUtils.getRemoteContent(UA_TESTS, path, "en");
		String localContent = ServicesTestUtils.getLocalContent(UA_TESTS, path);
	    assertEquals(remoteContent, localContent);
	}
	
	public void testContentInDeLocale() throws Exception {
		final String path = "/data/help/search/testnl1.xhtml";
		String remoteContent = ServicesTestUtils.getRemoteContent(UA_TESTS, path, "de");
		String enLocalContent = ServicesTestUtils.getLocalContent(UA_TESTS, path);
		String deLocalContent = ServicesTestUtils.getLocalContent(UA_TESTS, "/nl/de" + path);
	    assertEquals(remoteContent, deLocalContent);
	    assertFalse(remoteContent.equals(enLocalContent));
	}
	
	public void testRemoteContentNotFound() throws Exception {
		try {
			ServicesTestUtils.getRemoteContent(UA_TESTS, "/no/such/path.html", "en");
			fail("No exception thrown");
		} catch (IOException e) {
			// Exception caught as expected
		}
	}

}
