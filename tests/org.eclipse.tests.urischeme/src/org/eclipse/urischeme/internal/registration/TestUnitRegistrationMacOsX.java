/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUnitRegistrationMacOsX {

	private static final String OWN_APP_PLIST_PATH = "/Users/myuser/Applications/Eclipse.app/Contents/Info.plist";
	private static final String OTHER_APP_PLIST_PATH = "/Users/myuser/Applications/OtherApp.app/Contents/Info.plist";
	private static final String OTHER_APP_BUNDLE_PATH = "/Users/myuser/Applications/OtherApp.app";

	private static final IScheme ADT_SCHEME = new Scheme("adt", "");
	private static final ISchemeInformation OTHER_SCHEME_INFO = new SchemeInformation("other", "");
	private static final ISchemeInformation ADT_SCHEME_INFO = new SchemeInformation("adt", "");

	private IOperatingSystemRegistration registration;
	private FileProviderMock fileProvider;
	private ProcessSpy processStub;
	private static String originalEclipseHomeLocation;
	private static String originalEclipseLauncher;
	private String lsregisterDumpForOtherApp;
	private String lsregisterDumpForOwnApp;

	@Before
	public void setup() {
		fileProvider = new FileProviderMock();
		fileProvider.writer = new StringWriter();

		processStub = new ProcessSpy();

		System.setProperty("eclipse.home.location", "file:/Users/myuser/Applications/Eclipse.app/Contents/Eclipse/");
		System.setProperty("eclipse.launcher", "/Users/myuser/Applications/Eclipse.app/Contents/MacOS/eclipse");

		registration = new RegistrationMacOsX(fileProvider, processStub);

		InputStream inputStream = getClass().getResourceAsStream("lsregisterForOtherApp.txt");
		lsregisterDumpForOtherApp = convert(inputStream);
		inputStream = getClass().getResourceAsStream("lsregisterForOwnApp.txt");
		lsregisterDumpForOwnApp = convert(inputStream);

	}

	@BeforeClass
	public static void classSetup() {
		originalEclipseHomeLocation = System.getProperty("eclipse.home.location", "");
		originalEclipseLauncher = System.getProperty("eclipse.launcher", "");

	}

	@AfterClass
	public static void classTearDown() {
		System.setProperty("eclipse.home.location", originalEclipseHomeLocation);
		System.setProperty("eclipse.launcher", originalEclipseLauncher);
	}

	private String convert(InputStream inputStream) {
		try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\A").next();
		}
	}

	@Test
	public void handlesAddOnly() throws Exception {
		fileProvider.readAnswers.put(OWN_APP_PLIST_PATH, getPlistFileReader());

		registration.handleSchemes(Arrays.asList(ADT_SCHEME_INFO), Collections.emptyList());

		assertFilePathIs(OWN_APP_PLIST_PATH);

		assertSchemeInFile("adt");

		assertLsRegisterCallWithOptionAtIndex("-u", 0);
		assertLsRegisterCallWithOptionAtIndex("-r", 1);
	}

	@Test
	public void handlesAddAndRemoveAtOnce() throws Exception {
		fileProvider.readAnswers.put(OWN_APP_PLIST_PATH, getPlistFileReaderWithAdtScheme());

		registration.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO), Arrays.asList(ADT_SCHEME_INFO));

		assertFilePathIs(OWN_APP_PLIST_PATH);

		assertSchemeInFile("other");
		assertSchemeNotInFile("adt");

		assertLsRegisterCallWithOptionAtIndex("-u", 0);
		assertLsRegisterCallWithOptionAtIndex("-r", 1);
	}

	@Test
	public void handlesRemoveOnly() throws Exception {
		fileProvider.readAnswers.put(OWN_APP_PLIST_PATH, getPlistFileReaderWithAdtScheme());

		registration.handleSchemes(Collections.emptyList(), Arrays.asList(ADT_SCHEME_INFO));

		assertFilePathIs(OWN_APP_PLIST_PATH);

		assertSchemeNotInFile("adt");

		assertLsRegisterCallWithOptionAtIndex("-u", 0);
		assertLsRegisterCallWithOptionAtIndex("-r", 1);
	}

	@Test
	public void returnsRegisteredSchemes() throws Exception {
		fileProvider.readAnswers.put(OWN_APP_PLIST_PATH, getPlistFileReaderWithAdtScheme());

		processStub.result = lsregisterDumpForOwnApp;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertTrue(infos.get(0).isHandled());
	}

	@Test
	public void givesSchemeInfoForSchemeHandledByOtherApp() throws Exception {
		fileProvider.readAnswers.put(OTHER_APP_PLIST_PATH, getPlistFileReaderWithAdtScheme());

		processStub.result = lsregisterDumpForOtherApp;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals(OTHER_APP_BUNDLE_PATH, infos.get(0).getHandlerInstanceLocation());
	}

	@Test
	public void givesSchemeInfoForSchemeHandledByOtherAppAndInOwnPlistFile() throws Exception {
		fileProvider.readAnswers.put(OWN_APP_PLIST_PATH, getPlistFileReaderWithAdtScheme());
		fileProvider.readAnswers.put(OTHER_APP_PLIST_PATH, getPlistFileReaderWithAdtScheme());

		processStub.result = lsregisterDumpForOtherApp;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals(OTHER_APP_BUNDLE_PATH, infos.get(0).getHandlerInstanceLocation());
	}

	private void assertFilePathIs(String filePath) {
		assertEquals(filePath, fileProvider.recordedReadPaths.get(0));
		assertEquals(filePath, fileProvider.writePath);
	}

	private void assertSchemeInFile(String scheme) {
		assertThat(fileProvider.writer.toString(), new StringContains("<string>" + scheme + "</string>"));

	}

	private void assertSchemeNotInFile(String scheme) {
		assertThat(fileProvider.writer.toString(), new IsNot<>(new StringContains("<string>" + scheme + "</string>")));

	}

	private void assertLsRegisterCallWithOptionAtIndex(String option, int index) {
		String expectedProcess = "/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister";
		String[] expectedArguments = new String[] { option, "/Users/myuser/Applications/Eclipse.app" };

		assertEquals(expectedProcess, processStub.records.get(index).process);
		assertArrayEquals(expectedArguments, processStub.records.get(index).args);
	}

	private Reader getPlistFileReader() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + //
				"<plist version=\"1.0\">\n" + //
				"<dict>\n" + //
				"	<key>CFBundleExecutable</key>\n" + //
				"		<string>eclipse</string>\n" + //
				"</dict>\n" + //
				"</plist>\n";

		return new StringReader(xml);
	}

	private Reader getPlistFileReaderWithAdtScheme() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + //
				"<plist version=\"1.0\">\n" + //
				"<dict>\n" + //
				"	<key>CFBundleExecutable</key>\n" + //
				"		<string>eclipse</string>\n" + //
				"	<key>CFBundleURLTypes</key>\n" + //
				"		<array>\n" + //
				"			<dict>\n" + //
				"				<key>CFBundleURLName</key>\n" + //
				"					<string>AdtScheme</string>\n" + //
				"				<key>CFBundleURLSchemes</key>\n" + //
				"					<array>\n" + //
				"						<string>adt</string>\n" + //
				"					</array>\n" + //
				"			</dict>\n" + //
				"		</array>\n" + //
				"</dict>\n" + //
				"</plist>\n";

		return new StringReader(xml);
	}
}