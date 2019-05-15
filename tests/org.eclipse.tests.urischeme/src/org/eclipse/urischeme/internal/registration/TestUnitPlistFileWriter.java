/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

public class TestUnitPlistFileWriter {

	@Test
	public void addsOneScheme() {
		PlistFileWriter writer = getWriter();

		writer.addScheme("adt", "adtScheme");

		assertSchemesInOrder(writer, "adt");
	}

	@Test
	public void addsTwoSchemes() {
		PlistFileWriter writer = getWriter();

		writer.addScheme("adt", "adtScheme");
		writer.addScheme("other", "otherScheme");

		assertSchemesInOrder(writer, "adt", "other" );
	}

	@Test
	public void addsOneSchemeToEmptyArray() {
		PlistFileWriter writer = getWriter();

		writer.addScheme("adt", "adtScheme");

		assertSchemesInOrder(writer, "adt" );
	}

	@Test
	public void addsSecondToExistingScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.addScheme("other", "otherScheme");

		assertSchemesInOrder(writer, "adt", "other");
	}

	@Test
	public void doesntAddSchemeIfExisting() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.addScheme("adt", "adtScheme");

		assertSchemesInOrder(writer, "adt");
	}

	@Test(expected=IllegalArgumentException.class)
	public void addFailsOnIllegalScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.addScheme("&/%", "thisIsIllegal");
	}

	@Test
	public void doesntRemoveCommentAfterEndArrayTag() {
		String xml = getPlistStartXmlSnippet() +
				"	<key>CFBundleURLTypes</key>\n" +
				"		<array>\n" +
				getSchemeXmlSnippet("other") +
				"		</array>\n" +
				"<!--comment-->\n"+
				getPlistEndXmlSnippet();

		PlistFileWriter writer = new PlistFileWriter(new StringReader(xml));

		writer.addScheme("adt", "adtScheme");

		String expectedXml = getPlistStartXmlSnippet() +
				"	<key>CFBundleURLTypes</key>\n" +
				"		<array>\n" +
				getSchemeXmlSnippet("other") +
				getSchemeXmlSnippet("adt") +
				"		</array>\n" +
				"<!--comment-->\n"+
				getPlistEndXmlSnippet();

		assertXml(expectedXml, writer);
	}

	@Test
	public void doesntRemoveCommentBeforeEndArrayTag() {
		String xml = getPlistStartXmlSnippet() +
				"	<key>CFBundleURLTypes</key>\n" +
				"		<array>\n" +
				getSchemeXmlSnippet("other") +
				"<!--comment-->"+
				"		</array>\n" +
				getPlistEndXmlSnippet();
		PlistFileWriter writer = new PlistFileWriter(new StringReader(xml));

		writer.addScheme("adt", "adtScheme");

		String expectedXml = getPlistStartXmlSnippet() +
				"	<key>CFBundleURLTypes</key>\n" +
				"		<array>\n" +
				getSchemeXmlSnippet("other") +
				"<!--comment-->\n"+
				getSchemeXmlSnippet("adt") +
				"		</array>\n" +
				getPlistEndXmlSnippet();

		assertXml(expectedXml, writer);
	}

	@Test
	public void removesScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.removeScheme("adt");

		String expectedXml = getPlistStartXmlSnippet() +
				"	\n" +
				"		\n" + getPlistEndXmlSnippet();

		assertXml(expectedXml, writer);
	}

	@Test
	public void removesFirstOfTwoSchemes() {
		PlistFileWriter writer = getWriterWithSchemes("adt", "other");

		writer.removeScheme("adt");

		assertSchemesInOrder(writer, "other" );
	}

	@Test
	public void removesLastOfTwoSchemes() {
		PlistFileWriter writer = getWriterWithSchemes("adt", "other");

		writer.removeScheme("other");

		assertSchemesInOrder(writer, "adt" );
	}

	@Test
	public void removesSecondOfThreeSchemes() {
		PlistFileWriter writer = getWriterWithSchemes("adt", "other", "yetAnother");

		writer.removeScheme("other");

		assertSchemesInOrder(writer, "adt", "yetAnother");
	}

	@Test
	public void removesNonExistingScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.removeScheme("other");

		assertSchemesInOrder(writer, "adt" );
	}

	@Test(expected=IllegalArgumentException.class)
	public void removeFailsOnIllegalScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.removeScheme("&/%");
	}

	@Test
	public void doesNothing() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		assertSchemesInOrder(writer, "adt" );
	}

	@Test
	public void removesEmptyCFBundleURLTypesEntry() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.removeScheme("adt");

		// some text nodes stay, we don't care
		String expectedXml = getPlistStartXmlSnippet() +
				"	\n" +
				"		\n" + getPlistEndXmlSnippet();

		assertXml(expectedXml, writer);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnEmptyDocument() {
		new PlistFileWriter(new StringReader(""));
	}

	@Test(expected=IllegalStateException.class)
	public void throwsExceptionOnWrongPlistFile() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
					"<plist version=\"1.0\"/>";
		new PlistFileWriter(new StringReader(xml));
	}

	@Test(expected=IllegalStateException.class)
	public void throwsExceptionOnWrongXmlFile() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
					"<foo/>";
		new PlistFileWriter(new StringReader(xml));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnNonXmlFile() {
		String xml = "foo bar";
		new PlistFileWriter(new StringReader(xml));
	}

	@Test
	public void returnsTrueForRegisteredScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		assertTrue(writer.isRegisteredScheme("adt"));
		assertFalse(writer.isRegisteredScheme("other"));
	}

	@Test
	public void returnsFalseWhenNoSchemeIsRegistered() {
		PlistFileWriter writer = getWriter();

		assertFalse(writer.isRegisteredScheme("adt"));
		assertFalse(writer.isRegisteredScheme("other"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRegisteredFailsOnIllegalScheme() {
		PlistFileWriter writer = getWriterWithSchemes("adt");

		writer.isRegisteredScheme("&/%");
	}

	private void assertSchemesInOrder(PlistFileWriter writer, String... schemes ) {
		assertXml(getXml(schemes), writer);
	}

	private void assertXml(String xml, PlistFileWriter writer) {
		StringWriter stringWriter = new StringWriter();
		writer.writeTo(stringWriter);
		assertEquals(xml, stringWriter.toString());
	}

	private PlistFileWriter getWriter() {
		return new PlistFileWriter(new StringReader(getXml()));
	}

	private PlistFileWriter getWriterWithSchemes(String... schemes) {
		return new PlistFileWriter(new StringReader(getXml(schemes)));
	}

	private String getXml() {
		return getPlistStartXmlSnippet() + getPlistEndXmlSnippet();
	}

	private String getXml(String[] schemes) {
		String snippets = "";
		if (schemes != null) {
			for (String scheme : schemes) {
				snippets += getSchemeXmlSnippet(scheme);
			}
		}
		return getPlistStartXmlSnippet() +
				"	<key>CFBundleURLTypes</key>\n" +
				"		<array>\n" +
				snippets+
				"		</array>\n" +
				getPlistEndXmlSnippet();
	}

	private String getPlistStartXmlSnippet() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
				"<plist version=\"1.0\">\n" +
				"\n" +
				"<dict>\n" +
				"	<key>CFBundleExecutable</key>\n" +
				"		<string>eclipse</string>\n" +
				"	<key>CFBundleGetInfoString</key>\n" +
				"		<string>Eclipse 4.8 for Mac OS X, Copyright IBM Corp. and others 2002, 2017. All rights reserved.</string>\n" +
				"	<key>CFBundleIconFile</key>\n" +
				"		<string>Eclipse.icns</string>\n" +
				"	<key>CFBundleIdentifier</key>\n" +
				"		<string>org.eclipse.sdk.ide</string>\n" +
				"	<key>CFBundleInfoDictionaryVersion</key>\n" +
				"		<string>6.0</string>\n" +
				"	<key>CFBundleName</key>\n" +
				"		<string>Eclipse</string>\n" +
				"	<key>CFBundlePackageType</key>\n" +
				"		<string>APPL</string>\n" +
				"	<key>CFBundleShortVersionString</key>\n" +
				"		<string>4.8.0</string>\n" +
				"	<key>CFBundleSignature</key>\n" +
				"		<string>????</string>\n" +
				"	<key>CFBundleVersion</key>\n" +
				"		<string>4.8.0.I20180516-2000</string>\n" +
				"	<key>NSHighResolutionCapable</key>\n" +
				"		<true/>\n" +
				"	<key>CFBundleDevelopmentRegion</key>\n" +
				"		<string>English</string>		\n" +
				"	<key>Eclipse</key>\n" +
				"		<array>\n" +
				"			<!-- to use a specific Java version (instead of the platform's default) uncomment one of the following options,\n" +
				"					or add a VM found via $/usr/libexec/java_home -V\n" +
				"				<string>-vm</string><string>/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Commands/java</string>\n" +
				"				<string>-vm</string><string>/Library/Java/JavaVirtualMachines/1.8.0.jdk/Contents/Home/bin/java</string>\n" +
				"			-->\n" +
				"			<string>-keyring</string>\n" +
				"			<string>~/.eclipse_keyring</string>\n" +
				"		</array>\n" +
				"	<key>CFBundleDisplayName</key>\n" +
				"		<string>Eclipse</string>\n";
	}

	private String getPlistEndXmlSnippet() {
		return "</dict>\n" +
				"\n" +
				"</plist>";
	}

	private String getSchemeXmlSnippet(String scheme) {
		return	"			<dict>\n" +
				"				<key>CFBundleURLName</key>\n" +
				"					<string>"+scheme+"Scheme</string>\n" +
				"				<key>CFBundleURLSchemes</key>\n" +
				"					<array>\n" +
				"						<string>"+scheme+"</string>\n" +
				"					</array>\n" +
				"			</dict>\n";
	}


}
