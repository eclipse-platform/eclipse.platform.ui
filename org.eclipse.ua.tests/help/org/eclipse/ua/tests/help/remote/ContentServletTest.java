/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;
import org.osgi.framework.Bundle;

public class ContentServletTest extends TestCase {
	
	private static final String UA_TESTS = "org.eclipse.ua.tests";
	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}

	public void testSimpleContent() throws Exception {
		final String path = "/data/help/index/topic1.html";
		String remoteContent = getRemoteContent(UA_TESTS, path, "en");
		String localContent = getLocalContent(UA_TESTS, path);	   
	    assertEquals(remoteContent, localContent);
	}

	public void testFilteredContent() throws Exception {
		final String path = "/data/help/manual/filter.xhtml";
		String remoteContent = getRemoteContent(UA_TESTS, path, "en");
		String localContent = getLocalContent(UA_TESTS, path);	   
	    assertEquals(remoteContent, localContent);
	}

	public void testContentInEnLocale() throws Exception {
		final String path = "/data/help/search/testnl1.xhtml";
		String remoteContent = getRemoteContent(UA_TESTS, path, "en");
		String localContent = getLocalContent(UA_TESTS, path);	   
	    assertEquals(remoteContent, localContent);
	}
	
	public void testContentInDeLocale() throws Exception {
		final String path = "/data/help/search/testnl1.xhtml";
		String remoteContent = getRemoteContent(UA_TESTS, path, "de");
		String enLocalContent = getLocalContent(UA_TESTS, path);	   
		String deLocalContent = getLocalContent(UA_TESTS, "/nl/de" + path);	   
	    assertEquals(remoteContent, deLocalContent);
	    assertFalse(remoteContent.equals(enLocalContent));
	}
	
	public void testRemoteContentNotFound() throws Exception {
		try {
			getRemoteContent(UA_TESTS, "/no/such/path.html", "en");
			fail("No exception thrown");
		} catch (IOException e) {
			// Exception caught as expected
		}	
	}

	private String getRemoteContent(String plugin, String path, String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/rtopic/" + plugin + path + "?lang=" + locale);
		return readFromURL(url);
	}
	
	private String getLocalContent(String plugin, String path) throws Exception {
        Bundle bundle = Platform.getBundle(plugin);
        URL url;
		if (bundle != null) {
			 url=FileLocator.toFileURL(new URL(bundle.getEntry("/"), path)); //$NON-NLS-1$
		} else {
			return null;
		}
		return readFromURL(url);
	}

	protected String readFromURL(URL url) throws IOException,
			UnsupportedEncodingException {
		InputStream is = url.openStream();
		InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
		StringBuffer buffer = new StringBuffer();
		char[] cbuf = new char[256];
		int len;
		do {
			len = inputStreamReader.read(cbuf);
			if (len > 0) {
				buffer.append(cbuf, 0, len);
			}
		} while (len >= 0);
		return buffer.toString();
	}
	
}
