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
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class XMLContentDescriberTest extends RuntimeTest {
	private final static String XML_WITH_ENCODED_DATA = "<?xml version=\"1.0\" encoding=\"Shift_JIS\"?><root attribute=\"build???SJIS.xml\">";

	public XMLContentDescriberTest() {
		super();
	}

	public XMLContentDescriberTest(String name) {
		super(name);
	}

	public void testEncodedContents() throws Exception {
		IContentDescription description = InternalPlatform.getDefault().getContentTypeManager().getDescriptionFor(new ByteArrayInputStream(XML_WITH_ENCODED_DATA.getBytes("Shift_JIS")), "fake.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNotNull("1.0", description);
		assertEquals("1.1", Platform.PI_RUNTIME + ".xml", description.getContentType().getId());
		assertEquals("1.2", "Shift_JIS", description.getProperty(IContentDescription.CHARSET));
	}

	public static Test suite() {
		return new TestSuite(XMLContentDescriberTest.class);
	}
}