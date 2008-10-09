/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.content;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.tests.runtime.RuntimeTest;

/**
 * Ensures the XMLContentDescriber is able to handle encodings properly.
 */
public class XMLContentDescriberTest extends RuntimeTest {
	private final static String ENCODED_TEXT = "\u1000\u20001\u3000\u4000\u5000\u6000\u7000\u8000\u9000\uA000";
	private final static String ENCODING_UTF16 = "UTF-16";
	private final static String ENCODING_UTF8 = "UTF-8";
	private final static String ENCODING_NOTSUPPORTED = "ENCODING_NOTSUPPORTED";
	private final static String ENCODING_INCORRECT = "<?ENCODING?>";

	public XMLContentDescriberTest() {
		super();
	}

	public XMLContentDescriberTest(String name) {
		super(name);
	}

	public void testEncodedContents1() throws Exception {
		checkEncodedContents(ENCODING_UTF16, ENCODING_UTF16, ENCODING_UTF16);
	}

	public void testEncodedContents2() throws Exception {
		checkEncodedContents(ENCODING_UTF8, ENCODING_UTF8, ENCODING_UTF8);
	}

	public void testEncodedContents3() throws Exception {
		checkEncodedContents(ENCODING_UTF16, ENCODING_UTF16, ENCODING_UTF8);
	}

	public void testEncodedContents4() throws Exception {
		checkEncodedContents(ENCODING_NOTSUPPORTED, ENCODING_NOTSUPPORTED, ENCODING_UTF8);
	}

	public void testEncodedContents5() throws Exception {
		checkEncodedContents(ENCODING_NOTSUPPORTED, ENCODING_NOTSUPPORTED, ENCODING_UTF16);
	}

	public void testEncodedContents6() throws Exception {
		IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(new ByteArrayInputStream(getContent(ENCODING_INCORRECT, ENCODING_UTF8)), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNull("1.0", description);

		description = Platform.getContentTypeManager().getDescriptionFor(new InputStreamReader(new ByteArrayInputStream(getContent(ENCODING_INCORRECT, ENCODING_UTF8))), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNull("2.0", description);
	}

	public void testEncodedContents7() throws Exception {
		IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(new ByteArrayInputStream(getContent(ENCODING_INCORRECT, ENCODING_UTF16)), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNull("1.0", description);

		description = Platform.getContentTypeManager().getDescriptionFor(new InputStreamReader(new ByteArrayInputStream(getContent(ENCODING_INCORRECT, ENCODING_UTF16))), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNull("2.0", description);
	}

	public static Test suite() {
		return new TestSuite(XMLContentDescriberTest.class);
	}

	private void checkEncodedContents(String resultEncoding, String encodingInContent, String encoding) throws Exception {
		IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(new ByteArrayInputStream(getContent(encodingInContent, encoding)), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNotNull("1.0", description);
		assertEquals("1.1", Platform.PI_RUNTIME + ".xml", description.getContentType().getId());
		assertEquals("1.2", resultEncoding, description.getProperty(IContentDescription.CHARSET));

		description = Platform.getContentTypeManager().getDescriptionFor(new InputStreamReader(new ByteArrayInputStream(getContent(encodingInContent, encoding))), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNotNull("2.0", description);
		assertEquals("2.1", Platform.PI_RUNTIME + ".xml", description.getContentType().getId());
		assertEquals("2.2", resultEncoding, description.getProperty(IContentDescription.CHARSET));
	}

	private byte[] getContent(String encodingInContent, String encoding) throws UnsupportedEncodingException {
		String content = "<?xml version=\"1.0\" encoding=\"" + encodingInContent + "\"?><root attribute=\"" + ENCODED_TEXT + "\">";
		return content.getBytes(encoding);
	}
}
