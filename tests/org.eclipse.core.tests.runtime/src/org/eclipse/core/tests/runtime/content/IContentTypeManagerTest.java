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
import org.osgi.framework.BundleException;

public class IContentTypeManagerTest extends DynamicPluginTest {
	private final static String MINIMAL_XML = "<?xml version=\"1.0\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_UTF_8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_UTF_16 = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_UTF_16BE = "<?xml version=\"1.0\" encoding=\"UTF-16BE\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_UTF_16LE = "<?xml version=\"1.0\" encoding=\"UTF-16LE\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_ISO_8859_1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_ROOT_ELEMENT_ISO_8859_1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><org.eclipse.core.runtime.tests.root-element/>";
	private final static String XML_ROOT_ELEMENT_EXTERNAL_ENTITY = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE project   [<!ENTITY someentity SYSTEM \"someentity.xml\">]><org.eclipse.core.runtime.tests.root-element/>";
	private final static String XML_DTD_US_ASCII = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?><!DOCTYPE sometype SYSTEM \"org.eclipse.core.runtime.tests.some.dtd\"><org.eclipse.core.runtime.tests.root/>";
	private final static String XML_DTD_EXTERNAL_ENTITY = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?><!DOCTYPE project  SYSTEM \"org.eclipse.core.runtime.tests.some.dtd\"  [<!ENTITY someentity SYSTEM \"someentity.xml\">]><org.eclipse.core.runtime.tests.root/>";
	private final static String SAMPLE_BIN1_SIGNATURE = "\u0010\u0011\u0012\u0013";
	private final static String SAMPLE_BIN1_OFFSET = "12345";
	private final static String SAMPLE_BIN2_SIGNATURE = "\u0010\u0011\u0012\u0014";
	private final static String SAMPLE_BIN2_OFFSET = "";

	public IContentTypeManagerTest(String name) {
		super(name);
	}

	public void testRegistry() {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + '.' + "text");
		assertNotNull("1.0", textContentType);
		assertTrue("1.1", isText(contentTypeManager, textContentType));
		assertNotNull("1.2", ((ContentType) textContentType).getDescriber());
		IContentType xmlContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + ".xml");
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
		IContentType[] xmlTypes = contentTypeManager.findContentTypesFor(changeCase("foo.xml"));
		assertTrue("5.1", contains(xmlTypes, xmlContentType));
		IContentType binaryContentType = contentTypeManager.getContentType(RuntimeTest.PI_RUNTIME_TESTS + '.' + "sample-binary1");
		assertNotNull("6.0", binaryContentType);
		assertTrue("6.1", !isText(contentTypeManager, binaryContentType));
		assertNull("6.2", binaryContentType.getBaseType());
		IContentType[] binaryTypes = contentTypeManager.findContentTypesFor(changeCase("foo.samplebin1"));
		assertEquals("7.0", 1, binaryTypes.length);
		assertEquals("7.1", binaryContentType, binaryTypes[0]);
		IContentType myText = contentTypeManager.getContentType(PI_RUNTIME_TESTS + ".mytext");
		assertNotNull("8.0", myText);
		assertEquals("8.1", "BAR", myText.getDefaultCharset());
		IContentType[] fooBarTypes = contentTypeManager.findContentTypesFor(changeCase("foo.bar"));
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
		IContentType appropriate = contentTypeManager.getContentType(Platform.PI_RUNTIME + ".xml");
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

	private IContentDescription getDescriptionFor(IContentTypeManager manager, String contents, String encoding, String fileName, QualifiedName[] options, boolean text) throws UnsupportedEncodingException, IOException {
		return text ? manager.getDescriptionFor(getReader(contents), fileName, options) : manager.getDescriptionFor(getInputStream(contents, encoding), fileName, options);
	}

	/*
	 * Tests both text and byte stream-based getDescriptionFor methods.
	 */
	public void testContentDescription() throws IOException, CoreException {
		IContentTypeManager contentTypeManager = (LocalContentTypeManager) LocalContentTypeManager.getLocalContentTypeManager();
		IContentType xmlType = contentTypeManager.getContentType(Platform.PI_RUNTIME + ".xml");
		IContentType mytext = contentTypeManager.getContentType(PI_RUNTIME_TESTS + '.' + "mytext");
		IContentType mytext1 = contentTypeManager.getContentType(PI_RUNTIME_TESTS + '.' + "mytext1");
		IContentType mytext2 = contentTypeManager.getContentType(PI_RUNTIME_TESTS + '.' + "mytext2");

		boolean text = false;

		for (int i = 0; i < 2; i++, text = !text) {
			String sufix = text ? "-text" : "-binary";
			IContentDescription description;
			description = getDescriptionFor(contentTypeManager, MINIMAL_XML, "UTF-8", "foo.xml", IContentDescription.ALL, text);
			assertNotNull("1.0" + sufix, description);
			assertEquals("1.1" + sufix, xmlType, description.getContentType());
			description = getDescriptionFor(contentTypeManager, MINIMAL_XML, "UTF-8", "foo.xml", new QualifiedName[] {IContentDescription.CHARSET}, text);
			assertNotNull("2.0" + sufix, description);
			assertEquals("2.1" + sufix, xmlType, description.getContentType());
			// the default charset should have been filled by the content type manager
			assertEquals("2.2" + sufix, "UTF-8", description.getProperty(IContentDescription.CHARSET));
			description = getDescriptionFor(contentTypeManager, XML_ISO_8859_1, "ISO-8859-1", "foo.xml", new QualifiedName[] {IContentDescription.CHARSET}, text);
			assertNotNull("2.3a" + sufix, description);
			assertEquals("2.3b" + sufix, xmlType, description.getContentType());
			assertEquals("2.3c" + sufix, "ISO-8859-1", description.getProperty(IContentDescription.CHARSET));
			description = getDescriptionFor(contentTypeManager, XML_UTF_16, "UTF-8", "foo.xml", new QualifiedName[] {IContentDescription.CHARSET}, text);
			assertNotNull("2.4a" + sufix, description);
			assertEquals("2.4b" + sufix, xmlType, description.getContentType());
			assertEquals("2.4c" + sufix, "UTF-16", description.getProperty(IContentDescription.CHARSET));
			description = getDescriptionFor(contentTypeManager, XML_UTF_16BE, "UTF-8", "foo.xml", new QualifiedName[] {IContentDescription.CHARSET}, text);
			assertNotNull("2.5a" + sufix, description);
			assertEquals("2.5b" + sufix, xmlType, description.getContentType());
			assertEquals("2.5c" + sufix, "UTF-16BE", description.getProperty(IContentDescription.CHARSET));
			description = getDescriptionFor(contentTypeManager, XML_UTF_16LE, "UTF-8", "foo.xml", new QualifiedName[] {IContentDescription.CHARSET}, text);
			assertNotNull("2.6a" + sufix, description);
			assertEquals("2.6b" + sufix, xmlType, description.getContentType());
			// the default charset should have been filled by the content type manager
			assertEquals("2.6c" + sufix, "UTF-16LE", description.getProperty(IContentDescription.CHARSET));
			description = getDescriptionFor(contentTypeManager, MINIMAL_XML, "UTF-8", "foo.xml", IContentDescription.ALL, text);
			assertNotNull("4.0" + sufix, description);
			assertEquals("4.1" + sufix, xmlType, description.getContentType());
			assertEquals("4.2" + sufix, "UTF-8", description.getProperty(IContentDescription.CHARSET));
			assertNotNull("5.0" + sufix, mytext);
			assertEquals("5.0b" + sufix, "BAR", mytext.getDefaultCharset());
			description = getDescriptionFor(contentTypeManager, "some contents", null, "abc.tzt", IContentDescription.ALL, text);
			assertNotNull("5.1" + sufix, description);
			assertEquals("5.2" + sufix, mytext, description.getContentType());
			assertEquals("5.3" + sufix, "BAR", description.getProperty(IContentDescription.CHARSET));
			// now plays with setting a non-default default charset
			mytext.setDefaultCharset("FOO");
			description = getDescriptionFor(contentTypeManager, "some contents", null, "abc.tzt", IContentDescription.ALL, text);
			assertNotNull("5.5" + sufix, description);
			assertEquals("5.6" + sufix, mytext, description.getContentType());
			assertEquals("5.7" + sufix, "FOO", description.getProperty(IContentDescription.CHARSET));
			mytext.setDefaultCharset(null);
			description = getDescriptionFor(contentTypeManager, "some contents", null, "abc.tzt", IContentDescription.ALL, text);
			assertNotNull("5.10" + sufix, description);
			assertEquals("5.11" + sufix, mytext, description.getContentType());
			assertEquals("5.12" + sufix, "BAR", description.getProperty(IContentDescription.CHARSET));
		}
		assertNotNull("6.0", mytext1);
		assertEquals("6.1", "BAR", mytext1.getDefaultCharset());
		assertNotNull("6.2", mytext2);
		assertEquals("6.3", null, mytext2.getDefaultCharset());

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
		return new ByteArrayInputStream(encoding == null ? contents.getBytes() : contents.getBytes(encoding));
	}

	public InputStream getInputStream(String contents) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(contents.getBytes());
	}

	public Reader getReader(String contents) {
		return new CharArrayReader(contents.toCharArray());
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
		IContentType[] fooBarAssociated = contentTypeManager.findContentTypesFor(changeCase("foo.bar"));
		assertEquals("2.1", 2, fooBarAssociated.length);
		assertTrue("2.2", contains(fooBarAssociated, fooBarType));
		assertTrue("2.3", contains(fooBarAssociated, subFooBarType));
	}

	public void testIsKindOf() {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + '.' + "text");
		IContentType xmlContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + ".xml");
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

	public void testFindContentType() throws UnsupportedEncodingException, IOException {
		IContentTypeManager contentTypeManager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType textContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + '.' + "text");
		IContentType single = contentTypeManager.findContentTypeFor(getInputStream("Just a test"), changeCase("file.txt"));
		assertNotNull("1.0", single);
		assertEquals("1.1", textContentType, single);
		IContentType xmlContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + ".xml");
		single = contentTypeManager.findContentTypeFor(getInputStream(XML_UTF_8, "UTF-8"), changeCase("foo.xml"));
		assertNotNull("2.0", single);
		assertEquals("2.1", xmlContentType, single);
		IContentType[] multiple = contentTypeManager.findContentTypesFor(getInputStream(XML_UTF_8, "UTF-8"), null);
		assertTrue("3.0", contains(multiple, xmlContentType));
	}

	public void testAssociations() throws CoreException {
		IContentType text = Platform.getContentTypeManager().getContentType((Platform.PI_RUNTIME + ".text"));
		// associate a user-defined file spec
		text.addFileSpec("ini", IContentType.FILE_EXTENSION_SPEC);
		// test associations
		assertTrue("0.1", text.isAssociatedWith(changeCase("text.txt")));
		assertTrue("0.2", text.isAssociatedWith(changeCase("text.ini")));
		assertTrue("0.3", text.isAssociatedWith(changeCase("text.tkst")));
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
		assertTrue("3.1", text.isAssociatedWith(changeCase("text.txt")));
		assertTrue("3.2", text.isAssociatedWith(changeCase("text.ini")));
		assertTrue("3.3", text.isAssociatedWith(changeCase("text.tkst")));
		// removing user file specs is the normal case and has to work as expected
		text.removeFileSpec("ini", IContentType.FILE_EXTENSION_SPEC);
		assertTrue("4.0", !contains(text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_PRE_DEFINED), "ini"));
		assertTrue("4.1", text.isAssociatedWith(changeCase("text.txt")));
		assertTrue("4.2", !text.isAssociatedWith(changeCase("text.ini")));
		assertTrue("4.3", text.isAssociatedWith(changeCase("text.tkst")));
	}

	/**
	 * This test shows how we deal with orphan file associations (associations
	 * whose content types are missing).
	 */
	public void testOrphanContentType() throws IOException, BundleException {
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
		contentTypes = contentTypeManager.findContentTypesFor(getInputStream(XML_ROOT_ELEMENT_EXTERNAL_ENTITY, "ISO-8859-1"), "fake.xml");
		assertTrue("2.0", contentTypes.length > 0);
		assertEquals("2.1", rootElement, contentTypes[0]);
		contentTypes = contentTypeManager.findContentTypesFor(getInputStream(XML_DTD_US_ASCII, "US-ASCII"), "fake.xml");
		assertTrue("3.0", contentTypes.length > 0);
		assertEquals("3.1", dtdElement, contentTypes[0]);
		contentTypes = contentTypeManager.findContentTypesFor(getInputStream(XML_DTD_EXTERNAL_ENTITY, "US-ASCII"), "fake.xml");
		assertTrue("4.0", contentTypes.length > 0);
		assertEquals("4.1", dtdElement, contentTypes[0]);
	}

	public void testInvalidMarkup() {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		assertEquals("1.0", 0, contentTypeManager.findContentTypesFor("invalid.missing.identifier").length);
		assertEquals("2.0", 0, contentTypeManager.findContentTypesFor("invalid.missing.name").length);
		assertNull("3.0", contentTypeManager.getContentType(PI_RUNTIME_TESTS + '.' + "invalid-missing-name"));
	}

	public void testByteOrderMark() throws UnsupportedEncodingException, IOException {
		IContentType text = Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);
		QualifiedName[] options = new QualifiedName[] {IContentDescription.BYTE_ORDER_MARK};
		IContentDescription description;
		// tests with UTF-8 BOM
		String UTF8_BOM = new String(IContentDescription.BOM_UTF_8, "ISO-8859-1");
		description = text.getDescriptionFor(new ByteArrayInputStream((UTF8_BOM + MINIMAL_XML).getBytes("ISO-8859-1")), options);
		assertNotNull("1.0", description.getProperty(IContentDescription.BYTE_ORDER_MARK));
		assertEquals("1.1", IContentDescription.BOM_UTF_8, description.getProperty(IContentDescription.BYTE_ORDER_MARK));
		// tests with UTF-16 Little Endian BOM			
		String UTF16_LE_BOM = new String(IContentDescription.BOM_UTF_16LE, "ISO-8859-1");
		description = text.getDescriptionFor(new ByteArrayInputStream((UTF16_LE_BOM + MINIMAL_XML).getBytes("ISO-8859-1")), options);
		assertNotNull("2.0", description.getProperty(IContentDescription.BYTE_ORDER_MARK));
		assertEquals("2.1", IContentDescription.BOM_UTF_16LE, description.getProperty(IContentDescription.BYTE_ORDER_MARK));
		// tests with UTF-16 Big Endian BOM			
		String UTF16_BE_BOM = new String(IContentDescription.BOM_UTF_16BE, "ISO-8859-1");
		description = text.getDescriptionFor(new ByteArrayInputStream((UTF16_BE_BOM + MINIMAL_XML).getBytes("ISO-8859-1")), options);
		assertNotNull("3.0", description.getProperty(IContentDescription.BYTE_ORDER_MARK));
		assertEquals("3.1", IContentDescription.BOM_UTF_16BE, description.getProperty(IContentDescription.BYTE_ORDER_MARK));
		// test with no BOM
		description = text.getDescriptionFor(new ByteArrayInputStream(MINIMAL_XML.getBytes("ISO-8859-1")), options);
		assertNull("4.0", description.getProperty(IContentDescription.BYTE_ORDER_MARK));
	}

	public void _testFileSpecConflicts() throws IOException {
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType preferredConflict1 = manager.findContentTypeFor("test.conflict1");
		assertNotNull("1.0", preferredConflict1);
		assertEquals("1.1", PI_RUNTIME_TESTS + ".conflict1", preferredConflict1.getId());
		IContentType preferredConflict2 = manager.findContentTypeFor("test.conflict2");
		assertNotNull("2.0", preferredConflict2);
		assertEquals("2.1", PI_RUNTIME_TESTS + ".conflict2", preferredConflict2.getId());
		IContentType preferredConflict3 = manager.findContentTypeFor("test.conflict3");
		assertNotNull("3.0", preferredConflict3);
		assertEquals("3.1", PI_RUNTIME_TESTS + ".conflict3", preferredConflict3.getId());
	}

	public void testMyContentDescriber() throws UnsupportedEncodingException, IOException {
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType myContent = manager.getContentType(PI_RUNTIME_TESTS + '.' + "myContent");
		assertNotNull("0.5", myContent);
		assertEquals("0.6", myContent, manager.findContentTypeFor("myContent.mc"));
		IContentDescription description = manager.getDescriptionFor(getInputStream(MyContentDescriber.SIGNATURE, "US-ASCII"), "myContent.mc", IContentDescription.ALL);
		assertNotNull("1.0", description);
		assertEquals("1.1", myContent, description.getContentType());
		assertTrue("1.2", !(description instanceof DefaultDescription));
		for (int i = 0; i < MyContentDescriber.MY_OPTIONS.length; i++)
			assertEquals("2." + i, MyContentDescriber.MY_OPTION_VALUES[i], description.getProperty(MyContentDescriber.MY_OPTIONS[i]));
	}
	
	public void testOrderWithEmptyFiles() throws IOException {
		IContentTypeManager manager = LocalContentTypeManager.getLocalContentTypeManager();
		IContentType xml = manager.getContentType(Platform.PI_RUNTIME + ".xml");
		IContentType rootElement = manager.getContentType(PI_RUNTIME_TESTS + ".root-element");
		IContentType dtdElement = manager.getContentType(PI_RUNTIME_TESTS + ".dtd");
		// for an empty file, the most generic content type should be returned
		IContentType selected = manager.findContentTypeFor(getInputStream(""), "foo.xml");
		assertEquals("1.0", xml, selected);
		// it should be equivalent to omitsting the contents
		assertEquals("1.1", xml, manager.findContentTypeFor("foo.xml"));
	}

	private boolean isText(IContentTypeManager manager, IContentType candidate) {
		IContentType text = manager.getContentType(IContentTypeManager.CT_TEXT);
		return candidate.isKindOf(text);
	}

	public static Test suite() {
		return new TestSuite(IContentTypeManagerTest.class);
	}
	/**
	 * Helps to ensure we don't get fooled by case sensitivity in file names/specs.
	 */
	private String changeCase(String original) {
		StringBuffer result = new StringBuffer(original);
		for (int i = result.length()-1; i >= 0; i--) {
			char originalChar = original.charAt(i);
			result.setCharAt(i, i % 2 == 0 ? Character.toLowerCase(originalChar) : Character.toUpperCase(originalChar));			
		}
		return result.toString();
	}	
}