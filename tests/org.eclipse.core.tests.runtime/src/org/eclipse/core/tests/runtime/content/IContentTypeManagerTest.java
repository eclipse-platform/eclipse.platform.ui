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

import java.io.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.internal.content.ContentType;
import org.eclipse.core.internal.content.XMLContentDescriber;
import org.eclipse.core.runtime.IPlatform;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.tests.runtime.RuntimeTest;

public class IContentTypeManagerTest extends RuntimeTest {
	private final static String MINIMAL_XML = "<?xml version=\"1.0\"?><root/>";
	private final static String XML_UTF_8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
	private final static String XML_ISO_8859_1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root/>";
	private final static String BOM_UTF_32_BE = "\u0000\u0000\u00FE\u00FF";
	private final static String BOM_UTF_32_LE = "\u00FF\u00FE\u0000\u0000";
	private final static String BOM_UTF_16_BE = "\u00FE\u00FF";
	private final static String BOM_UTF_16_LE = "\u00FF\u00FE";
	private final static String BOM_UTF_8 = "\u00EF\u00BB\u00BF";
	public IContentTypeManagerTest(String name) {
		super(name);
	}
	public IContentTypeManager getLocalContentTypeManager() {
		LocalContentTypeManager contentTypeManager = new LocalContentTypeManager();
		contentTypeManager.startup();
		return contentTypeManager;
	}
	public void testRegistry() {
		IContentTypeManager contentTypeManager = getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + '.' + "text");
		assertNotNull("1.0", textContentType);
		assertTrue("1.1", isText(textContentType));
		assertNull("1.2", ((ContentType) textContentType).getDescriber());
		IContentType xmlContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		assertNotNull("2.0", xmlContentType);
		assertTrue("2.1", isText(xmlContentType));
		assertEquals("2.2", textContentType, xmlContentType.getBaseType());
		IContentDescriber xmlDescriber = ((ContentType) xmlContentType).getDescriber();
		assertNotNull("2.3", xmlDescriber);
		assertTrue("2.4", xmlDescriber instanceof XMLContentDescriber);
		IContentType xmlBasedDifferentExtensionContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "xml-based-different-extension");
		assertNotNull("3.0", xmlBasedDifferentExtensionContentType);
		assertTrue("3.1", isText(xmlBasedDifferentExtensionContentType));
		assertEquals("3.2", xmlContentType, xmlBasedDifferentExtensionContentType.getBaseType());
		IContentType xmlBasedSpecificNameContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "xml-based-specific-name");
		assertNotNull("4.0", xmlBasedSpecificNameContentType);
		assertTrue("4.1", isText(xmlBasedSpecificNameContentType));
		assertEquals("4.2", xmlContentType, xmlBasedSpecificNameContentType.getBaseType());
		IContentType[] xmlTypes = contentTypeManager.findContentTypesForFileName("foo.xml");
		assertEquals("5.0", 1, xmlTypes.length);
		assertEquals("5.1", xmlContentType, xmlTypes[0]);
		IContentType binaryContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "sample-binary");
		assertNotNull("6.0", binaryContentType);
		assertTrue("6.1", !isText(binaryContentType));
		assertNull("6.2", binaryContentType.getBaseType());
		IContentType[] binaryTypes = contentTypeManager.findContentTypesForFileName("foo.samplebin");
		assertEquals("7.0", 1, binaryTypes.length);
		assertEquals("7.1", binaryContentType, binaryTypes[0]);
	}
	private boolean contains(Object[] array, Object element) {
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(element))
				return true;
		return false;
	}
	/**
	 * @see IContentTypeManager#getContentTypeFor
	 */
	public void testContentDetection() throws IOException {
		IContentTypeManager contentTypeManager = (LocalContentTypeManager) getLocalContentTypeManager();
		IContentType[] candidates;
		IContentType inappropriate = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".text");
		IContentType appropriate = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		IContentType appropriateSpecific1 = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + ".xml-based-different-extension");
		IContentType appropriateSpecific2 = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + ".xml-based-specific-name");
		assertNull("1.0", contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[]{inappropriate}));
		assertEquals("2.0", appropriate, contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[]{inappropriate, appropriate}));
		assertEquals("3.0", appropriateSpecific1, contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[]{inappropriate, appropriate, appropriateSpecific1}));
		assertEquals("3.1", appropriateSpecific2, contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[]{inappropriate, appropriate, appropriateSpecific2}));
		assertNull("4.0", contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[]{inappropriate, appropriate, appropriateSpecific1, appropriateSpecific2}));
	}
	public void testContentDescription() throws IOException {
		IContentTypeManager contentTypeManager = (LocalContentTypeManager) getLocalContentTypeManager();
		IContentType xmlType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		IContentType[] candidates = contentTypeManager.findContentTypesForFileName("foo.xml");
		IContentDescription description;
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML), candidates, 0);
		assertNotNull("1.0", description);
		assertEquals("1.1", xmlType, description.getContentType());
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML), candidates, IContentDescription.CHARSET);
		assertNotNull("2.0", description);
		assertEquals("2.1", xmlType, description.getContentType());
		// the default charset should have been filled by the content type manager
		assertEquals("2.2", "UTF-8", description.getCharset());
		description = contentTypeManager.getDescriptionFor(getInputStream(XML_ISO_8859_1), candidates, IContentDescription.CHARSET);
		assertNotNull("3.0", description);
		assertEquals("3.1", xmlType, description.getContentType());
		assertEquals("3.2", "ISO-8859-1", description.getCharset());
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML), candidates, IContentDescription.ALL);
		assertNotNull("4.0", description);
		assertEquals("4.1", xmlType, description.getContentType());
		assertEquals("4.2", "UTF-8", description.getCharset());
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML), candidates, IContentDescription.ALL + 1);
		assertNotNull("5.0", description);		
		assertNull("5.0", description.getCharset());		
	}	
	public InputStream getInputStream(String contents) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(contents.getBytes("UTF-8"));
	}
	/**
	 * This test shows how we deal with orphan content types (content types
	 * whose base types are missing): orphan types are considered invalid and do 
	 * not appear in the catalog.
	 */
	public void testOrphanContentType() {
		IContentTypeManager contentTypeManager = getLocalContentTypeManager();
		assertNull("1.0", contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "orphan"));
		assertEquals("1.1", 0, contentTypeManager.findContentTypesForFileName("foo.orphan").length);
		assertEquals("1.2", 0, contentTypeManager.findContentTypesForFileName("orphan.orphan").length);
		//test late addition of base type - it should become valid
		LocalContentTypeManager localManager = ((LocalContentTypeManager) contentTypeManager);
		IContentType newType = localManager.createContentType("org.eclipse.foo", "bar", "Foo Bar", null, null, null);
		localManager.addContentType(newType);
		assertNotNull("2.0", contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "orphan"));
		assertEquals("2.1", 1, contentTypeManager.findContentTypesForFileName("foo.orphan").length);
		assertEquals("2.2", 1, contentTypeManager.findContentTypesForFileName("orphan.orphan").length);
	}
	/**
	 * The fooBar content type is associated with the "foo.bar" file name and 
	 * the "bar" file extension (what is bogus, anyway). This test ensures it 
	 * does not appear twice in the list of content types associated with the 
	 * "foo.bar" file name.
	 */
	public void testDoubleAssociation() {
		IContentTypeManager contentTypeManager = getLocalContentTypeManager();
		IContentType fooBarType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "fooBar");
		assertNotNull("1.0", fooBarType);
		// ensure we don't get fooBar twice 
		IContentType[] fooBarAssociated = contentTypeManager.findContentTypesForFileName("foo.bar");
		assertEquals("2.1", 1, fooBarAssociated.length);
		assertEquals("2.2", fooBarType, fooBarAssociated[0]);
	}
	private boolean isText(IContentType candidate) {
		IContentType text = ((ContentType) candidate).getManager().getContentType(IContentTypeManager.CT_TEXT);
		return candidate.isKindOf(text);
	}
	public static Test suite() {
		return new TestSuite(IContentTypeManagerTest.class);		
	}
}