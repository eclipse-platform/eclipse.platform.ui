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
import org.eclipse.core.internal.content.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.tests.runtime.DynamicPluginTest;
import org.eclipse.core.tests.runtime.RuntimeTest;
import org.osgi.framework.Bundle;

public class IContentTypeManagerTest extends DynamicPluginTest {
	private final static String MINIMAL_XML = "<?xml version=\"1.0\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_UTF_8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_ISO_8859_1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_ROOT_ELEMENT_ISO_8859_1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><org.eclipse.core.runtime.tests.root-element/>";
	private final static String XML_DTD_US_ASCII = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String BOM_UTF_32_BE = "\u0000\u0000\u00FE\u00FF";
	private final static String BOM_UTF_32_LE = "\u00FF\u00FE\u0000\u0000";
	private final static String BOM_UTF_16_BE = "\u00FE\u00FF";
	private final static String BOM_UTF_16_LE = "\u00FF\u00FE";
	private final static String BOM_UTF_8 = "\u00EF\u00BB\u00BF";
	private final static String SAMPLE_BIN1_SIGNATURE = "\u0010\u0011\u0012\u0013";
	private final static String SAMPLE_BIN1_OFFSET = "12345";
	private final static String SAMPLE_BIN2_SIGNATURE = "\u0010\u0011\u0012\u0014";
	private final static String SAMPLE_BIN2_OFFSET = "";

	public IContentTypeManagerTest(String name) {
		super(name);
	}

	public void testRegistry() {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + '.' + "text");
		assertNotNull("1.0", textContentType);
		assertTrue("1.1", isText(contentTypeManager, textContentType));
		assertNull("1.2", ((ContentType) textContentType).getDescriber());
		IContentType xmlContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		assertNotNull("2.0", xmlContentType);
		assertTrue("2.1", isText(contentTypeManager, xmlContentType));
		assertEquals("2.2", textContentType, xmlContentType.getBaseType());
		IContentDescriber xmlDescriber = ((ContentType) xmlContentType).getDescriber();
		assertNotNull("2.3", xmlDescriber);
		assertTrue("2.4", xmlDescriber instanceof XMLContentDescriber);
		IContentType xmlBasedDifferentExtensionContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "xml-based-different-extension");
		assertNotNull("3.0", xmlBasedDifferentExtensionContentType);
		assertTrue("3.1", isText(contentTypeManager, xmlBasedDifferentExtensionContentType));
		assertEquals("3.2", xmlContentType, xmlBasedDifferentExtensionContentType.getBaseType());
		IContentType xmlBasedSpecificNameContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "xml-based-specific-name");
		assertNotNull("4.0", xmlBasedSpecificNameContentType);
		assertTrue("4.1", isText(contentTypeManager, xmlBasedSpecificNameContentType));
		assertEquals("4.2", xmlContentType, xmlBasedSpecificNameContentType.getBaseType());
		IContentType[] xmlTypes = contentTypeManager.findContentTypesFor("foo.xml");
		assertTrue("5.1", contains(xmlTypes, xmlContentType));
		IContentType binaryContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "sample-binary1");
		assertNotNull("6.0", binaryContentType);
		assertTrue("6.1", !isText(contentTypeManager, binaryContentType));
		assertNull("6.2", binaryContentType.getBaseType());
		IContentType[] binaryTypes = contentTypeManager.findContentTypesFor("foo.samplebin1");
		assertEquals("7.0", 1, binaryTypes.length);
		assertEquals("7.1", binaryContentType, binaryTypes[0]);
		IContentType myText = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".mytext");
		assertNotNull("8.0", myText);
		assertEquals("8.1", "BAR", myText.getDefaultCharset());
		IContentType[] fooBarTypes = contentTypeManager.findContentTypesFor("foo.bar");
		assertEquals("9.0", 2, fooBarTypes.length);
		IContentType fooBar = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".fooBar");
		assertNotNull("9.1", fooBar);
		IContentType subFooBar = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".subFooBar");
		assertNotNull("9.2", subFooBar);
		assertTrue("9.3", contains(fooBarTypes, fooBar));
		assertTrue("9.4", contains(fooBarTypes, subFooBar));
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
		LocalContentTypeManager contentTypeManager = (LocalContentTypeManager) LocalContentTypeManager.getLocalContentTypeManager();
		IContentType inappropriate = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + ".sample-binary1");
		IContentType appropriate = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		IContentType appropriateSpecific1 = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + ".xml-based-different-extension");
		IContentType appropriateSpecific2 = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + ".xml-based-specific-name");
		// if only inappropriate is provided, none will be selected
		assertNull("1.0", contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[] {inappropriate}));
		// if inappropriate and appropriate are provided, appropriate will be selected		
		assertEquals("2.0", appropriate, contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[] {inappropriate, appropriate}));
		// if inappropriate, appropriate and a more specific appropriate type are provided, the specific type will be selected		
		assertEquals("3.0", appropriateSpecific1, contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[] {inappropriate, appropriate, appropriateSpecific1}));
		assertEquals("3.1", appropriateSpecific2, contentTypeManager.findContentTypeFor(getInputStream(MINIMAL_XML), new IContentType[] {inappropriate, appropriate, appropriateSpecific2}));
		// if all are provided, the more specific types will appear before the more generic types
		IContentType[] selected = contentTypeManager.findContentTypesFor(getInputStream(MINIMAL_XML), new IContentType[] {inappropriate, appropriate, appropriateSpecific1, appropriateSpecific2});
		assertEquals("4.0", 3, selected.length); 
		assertTrue("4.1", appropriateSpecific1 == selected[0] || appropriateSpecific1 == selected[1]);
		assertTrue("4.2", appropriateSpecific2 == selected[0] || appropriateSpecific2 == selected[1]);
		assertTrue("4.3", appropriate == selected[2]);
	}

	public void testContentDescription() throws IOException {
		IContentTypeManager contentTypeManager = (LocalContentTypeManager) LocalContentTypeManager.getLocalContentTypeManager();
		IContentType xmlType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		IContentDescription description;
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML, "UTF-8"), "foo.xml", IContentDescription.ALL);
		assertNotNull("1.0", description);
		assertEquals("1.1", xmlType, description.getContentType());
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML, "UTF-8"), "foo.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNotNull("2.0", description);
		assertEquals("2.1", xmlType, description.getContentType());
		// the default charset should have been filled by the content type manager
		assertEquals("2.2", "UTF-8", description.getProperty(IContentDescription.CHARSET));
		description = contentTypeManager.getDescriptionFor(getInputStream(XML_ISO_8859_1, "UTF-8"), "foo.xml", new QualifiedName[] {IContentDescription.CHARSET});
		assertNotNull("3.0", description);
		assertEquals("3.1", xmlType, description.getContentType());
		assertEquals("3.2", "ISO-8859-1", description.getProperty(IContentDescription.CHARSET));
		description = contentTypeManager.getDescriptionFor(getInputStream(MINIMAL_XML, "UTF-8"), "foo.xml", IContentDescription.ALL);
		assertNotNull("4.0", description);
		assertEquals("4.1", xmlType, description.getContentType());
		assertEquals("4.2", "UTF-8", description.getProperty(IContentDescription.CHARSET));
		description = contentTypeManager.getDescriptionFor(getInputStream("some contents"), "abc.tzt", IContentDescription.ALL);
		assertNotNull("5.0", description);
		assertEquals("5.1", "BAR", description.getProperty(IContentDescription.CHARSET));
	}

	public void testBinaryTypes() throws IOException {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType sampleBinary1 = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".sample-binary1");
		IContentType sampleBinary2 = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".sample-binary2");
		InputStream contents;
		contents = getInputStream(SAMPLE_BIN1_OFFSET + SAMPLE_BIN1_SIGNATURE + " extra contents", "US-ASCII");
		IContentDescription description = contentTypeManager.getDescriptionFor(contents, null, IContentDescription.ALL);
		assertNotNull("6.0", description);
		assertEquals("6.1", sampleBinary1, description.getContentType());
		contents = getInputStream(SAMPLE_BIN2_OFFSET + SAMPLE_BIN2_SIGNATURE + " extra contents", "US-ASCII");
		description = contentTypeManager.getDescriptionFor(contents, null, IContentDescription.ALL);
		assertNotNull("7.0", description);
		assertEquals("7.1", sampleBinary2, description.getContentType());
	}

	public InputStream getInputStream(String contents, String encoding) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(contents.getBytes(encoding));
	}

	public InputStream getInputStream(String contents) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * The fooBar content type is associated with the "foo.bar" file name and 
	 * the "bar" file extension (what is bogus, anyway). This test ensures it 
	 * does not appear twice in the list of content types associated with the 
	 * "foo.bar" file name.
	 */
	public void testDoubleAssociation() {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType fooBarType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "fooBar");
		assertNotNull("1.0", fooBarType);
		IContentType subFooBarType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "subFooBar");
		assertNotNull("1.1", subFooBarType);
		// ensure we don't get fooBar twice 
		IContentType[] fooBarAssociated = contentTypeManager.findContentTypesFor("foo.bar");
		assertEquals("2.1", 2, fooBarAssociated.length);
		assertTrue("2.2", contains(fooBarAssociated, fooBarType));
		assertTrue("2.3", contains(fooBarAssociated, subFooBarType));
	}

	public void testIsKindOf() {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + '.' + "text");
		IContentType xmlContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		IContentType xmlBasedDifferentExtensionContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "xml-based-different-extension");
		IContentType xmlBasedSpecificNameContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "xml-based-specific-name");
		IContentType binaryContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "sample-binary1");
		assertTrue("1.0", textContentType.isKindOf(textContentType));
		assertTrue("2.0", xmlContentType.isKindOf(textContentType));
		assertTrue("2.1", !textContentType.isKindOf(xmlContentType));
		assertTrue("2.2", xmlContentType.isKindOf(xmlContentType));
		assertTrue("3.0", xmlBasedDifferentExtensionContentType.isKindOf(textContentType));
		assertTrue("3.1", xmlBasedDifferentExtensionContentType.isKindOf(xmlContentType));
		assertTrue("4.0", !xmlBasedDifferentExtensionContentType.isKindOf(xmlBasedSpecificNameContentType));
		assertTrue("5.0", !binaryContentType.isKindOf(textContentType));
	}

	public void testFindContentType() throws Exception {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + '.' + "text");
		IContentType single = contentTypeManager.findContentTypeFor(getInputStream("Just a test"), "file.txt");
		assertNotNull("1.0", single);
		assertEquals("1.1", textContentType, single);
		IContentType xmlContentType = contentTypeManager.getContentType(IPlatform.PI_RUNTIME + ".xml");
		single = contentTypeManager.findContentTypeFor(getInputStream(XML_UTF_8, "UTF-8"), "foo.xml");
		assertNotNull("2.0", single);
		assertEquals("2.1", xmlContentType, single);
		IContentType[] multiple = contentTypeManager.findContentTypesFor(getInputStream(XML_UTF_8, "UTF-8"), null);
		assertTrue("3.0", contains(multiple, xmlContentType));
	}

	public void testAssociations() {
		IContentType text = Platform.getContentTypeManager().getContentType((IPlatform.PI_RUNTIME + ".text"));
		// associate a user-defined file spec
		text.addFileSpec("ini", IContentType.FILE_EXTENSION_SPEC);
		// test associations
		assertTrue("0.1", text.isAssociatedWith("text.txt"));
		assertTrue("0.2", text.isAssociatedWith("text.ini"));
		assertTrue("0.3", text.isAssociatedWith("text.tkst"));
		// check provider defined settings
		String[] providerDefinedExtensions = text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_USER_DEFINED);
		assertTrue("1.0", contains(providerDefinedExtensions, "txt"));
		assertTrue("1.1", !contains(providerDefinedExtensions, "ini"));
		assertTrue("1.2", contains(providerDefinedExtensions, "tkst"));
		// check user defined settings
		String[] textUserDefinedExtensions = text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_PRE_DEFINED);
		assertTrue("2.0", !contains(textUserDefinedExtensions, "txt"));
		assertTrue("2.1", contains(textUserDefinedExtensions, "ini"));
		assertTrue("2.2", !contains(textUserDefinedExtensions, "tkst"));
		// removing pre-defined file specs should not do anything
		text.removeFileSpec("txt", IContentType.FILE_EXTENSION_SPEC);
		assertTrue("3.0", contains(text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_USER_DEFINED), "txt"));
		assertTrue("3.1", text.isAssociatedWith("text.txt"));
		assertTrue("3.2", text.isAssociatedWith("text.ini"));
		assertTrue("3.3", text.isAssociatedWith("text.tkst"));
		// removing user file specs is the normal case and has to work as expected
		text.removeFileSpec("ini", IContentType.FILE_EXTENSION_SPEC);
		assertTrue("4.0", !contains(text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_PRE_DEFINED), "ini"));
		assertTrue("4.1", text.isAssociatedWith("text.txt"));
		assertTrue("4.2", !text.isAssociatedWith("text.ini"));
		assertTrue("4.3", text.isAssociatedWith("text.tkst"));
	}

	/**
	 * This test shows how we deal with orphan file associations (associations
	 * whose content types are missing).
	 */
	public void testOrphanContentType() throws Exception {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType orphan = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".orphan");
		assertNull("0.8", orphan);
		IContentType missing = contentTypeManager.getContentType("org.eclipse.bundle01.missing");
		assertNull("0.9", missing);
		assertEquals("1.1", 0, contentTypeManager.findContentTypesFor("foo.orphan").length);
		assertEquals("1.2", 0, contentTypeManager.findContentTypesFor("orphan.orphan").length);
		assertEquals("1.3", 0, contentTypeManager.findContentTypesFor("foo.orphan2").length);

		//test late addition of content type - orphan2 should become visible
		TestRegistryChangeListener listener = new TestRegistryChangeListener(Platform.PI_RUNTIME, ContentTypeBuilder.PT_CONTENTTYPES, null, null);
		registerListener(listener, Platform.PI_RUNTIME);
		Bundle installed = installBundle("content/bundle01");
		refreshPackages(new Bundle[] {installed});		
		try {
			IRegistryChangeEvent event = listener.getEvent(10000);
			assertNotNull("1.5", event);
			assertNotNull("2.0", Platform.getBundle("org.eclipse.bundle01"));
			orphan = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".orphan");
			assertNotNull("2.1", orphan);
			missing = contentTypeManager.getContentType("org.eclipse.bundle01.missing");
			assertNotNull("2.2", missing);
			// checks orphan's associations
			assertEquals("3.0", 1, contentTypeManager.findContentTypesFor("foo.orphan").length);
			assertEquals("3.1", orphan, contentTypeManager.findContentTypesFor("foo.orphan")[0]);
			assertEquals("4.0", 1, contentTypeManager.findContentTypesFor("orphan.orphan").length);
			assertEquals("4.1", orphan, contentTypeManager.findContentTypesFor("foo.orphan")[0]);
			// check whether an orphan association was added to the dynamically added bundle
			assertEquals("5.0", 1, contentTypeManager.findContentTypesFor("foo.orphan2").length);
			assertEquals("5.1", missing, contentTypeManager.findContentTypesFor("foo.orphan2")[0]);
		} finally {
			//remove installed bundle
			installed.uninstall();
			refreshPackages(new Bundle[] {installed});			
		}
	}

	public void testRootElementAndDTDDescriber() throws IOException {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType rootElement = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".root-element");
		IContentType dtdElement = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".dtd");
		IContentType[] contentTypes = contentTypeManager.findContentTypesFor(getInputStream(XML_ROOT_ELEMENT_ISO_8859_1, "ISO-8859-1"), "fake.xml");
		assertTrue("1.0", contentTypes.length > 0);
		assertEquals("1.1", rootElement, contentTypes[0]);
	}

	private boolean isText(IContentTypeManager manager, IContentType candidate) {
		IContentType text = manager.getContentType(IContentTypeManager.CT_TEXT);
		return candidate.isKindOf(text);
	}

	public static Test suite() {
		return new TestSuite(IContentTypeManagerTest.class);
	}
}