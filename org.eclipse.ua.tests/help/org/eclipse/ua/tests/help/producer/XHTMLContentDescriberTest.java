/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.producer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;
import org.eclipse.ua.tests.util.ResourceFinder;

public class XHTMLContentDescriberTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(XHTMLContentDescriberTest.class);
	}
	
	public void testValidXHTML() throws IOException {
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), "data/help/producer/xhtml/valid", "html", true);
		for (int i=0;i<urls.length;++i) {
			InputStream in = urls[i].openStream();
			String url = urls[i].toExternalForm();
			String fileName = url.substring(url.lastIndexOf('/') + 1);
			IContentType type = Platform.getContentTypeManager().findContentTypeFor(in, fileName);
			Assert.assertEquals("The supplied valid XHTML was mistakenly recognized as invalid XHTML by the type describer: file=" + fileName, "org.eclipse.help.xhtml", type.getId());
			in.close();
		}
	}

	public void testInvalidXHTML() throws IOException {
		URL[] urls = ResourceFinder.findFiles(UserAssistanceTestPlugin.getDefault(), "data/help/producer/xhtml/invalid", "html", true);
		for (int i=0;i<urls.length;++i) {
			InputStream in = urls[i].openStream();
			String url = urls[i].toExternalForm();
			String fileName = url.substring(url.lastIndexOf('/') + 1);
			IContentType type = Platform.getContentTypeManager().findContentTypeFor(in, fileName);
			Assert.assertFalse("The supplied invalid XHTML was not properly recognized by the XHTML content type describer: file= " + fileName + " type=" + type.getId(), "org.eclipse.help.xhtml".equals(type.getId()));
			in.close();
		}
	}
}
