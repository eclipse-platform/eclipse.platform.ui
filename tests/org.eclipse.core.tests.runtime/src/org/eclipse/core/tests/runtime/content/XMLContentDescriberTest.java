/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.content;

import java.io.ByteArrayInputStream;
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
	private final static String ENCODING = "UTF16";
	private final static String XML_WITH_ENCODED_DATA = "<?xml version=\"1.0\" encoding=\""+ ENCODING +"\"?><root attribute=\"" + ENCODED_TEXT + "\">";

	public XMLContentDescriberTest() {
		super();
	}

	public XMLContentDescriberTest(String name) {
		super(name);
	}

	public void testEncodedContents() throws Exception {
		IContentDescription description = Platform.getContentTypeManager().getDescriptionFor(new ByteArrayInputStream(XML_WITH_ENCODED_DATA.getBytes(ENCODING)), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNotNull("1.0", description);
		assertEquals("1.1", Platform.PI_RUNTIME + ".xml", description.getContentType().getId());
		assertEquals("1.2", ENCODING, description.getProperty(IContentDescription.CHARSET));
	}

	public static Test suite() {
		return new TestSuite(XMLContentDescriberTest.class);
	}
}