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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUnitRegistrationLinux {

	private static final String PRODUCT_NAME = "myProduct";
	private static final String USER_HOME = "user.home";
	private static final String ECLIPSE_LAUNCHER = "eclipse.launcher";
	private static final String ECLIPSE_HOME_LOCATION = "eclipse.home.location";
	private static final String OTHER_APP_EXECUTABLE_PATH = "/home/myuser/otherApp/app";
	private static final String OTHER_APP_DESKTOP_FILE = "otherApp.desktop";
	private static final String PATH_OTHER_APP_DESKTOP_FILE = "~/.local/share/applications/" + OTHER_APP_DESKTOP_FILE;

	private static final String OWN_EXECUTABLE_PATH = "/home/myuser/Eclipse/Eclipse";
	private static final String OWN_DESKTOP_FILE = "_home_myuser_Eclipse_.desktop";
	private static final String PATH_OWN_DESKTOP_FILE = "~/.local/share/applications/" + OWN_DESKTOP_FILE;

	private static final IScheme ADT_SCHEME = new Scheme("adt", "");

	private static final ISchemeInformation OTHER_SCHEME_INFO = new SchemeInformation("other", "");
	private static final ISchemeInformation ADT_SCHEME_INFO = new SchemeInformation("adt", "");

	private static String originalEclipseHomeLocation;
	private static String originalEclipseLauncher;
	private static String originalUserHome;

	private IOperatingSystemRegistration registration;

	private FileProviderMock fileProvider;
	private ProcessSpy processStub;

	@Before
	public void setup() {
		fileProvider = new FileProviderMock();
		processStub = new ProcessSpy();

		System.setProperty(ECLIPSE_HOME_LOCATION, "file:/home/myuser/Eclipse/");
		System.setProperty(ECLIPSE_LAUNCHER, "/home/myuser/Eclipse/Eclipse");
		System.setProperty(USER_HOME, "~");

		registration = new RegistrationLinux(fileProvider, processStub, PRODUCT_NAME);
	}

	@BeforeClass
	public static void classSetup() {
		originalEclipseHomeLocation = System.getProperty(ECLIPSE_HOME_LOCATION, "");
		originalEclipseLauncher = System.getProperty(ECLIPSE_LAUNCHER, "");
		originalUserHome = System.getProperty(USER_HOME);
	}

	@AfterClass
	public static void classTearDown() {
		System.setProperty(ECLIPSE_HOME_LOCATION, originalEclipseHomeLocation);
		System.setProperty(ECLIPSE_LAUNCHER, originalEclipseLauncher);
		System.setProperty(USER_HOME, originalUserHome);
	}

	@Test
	public void handlesAddOnly() throws Exception {
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE, getFileLines(OWN_EXECUTABLE_PATH, ""));

		registration.handleSchemes(Arrays.asList(ADT_SCHEME_INFO), Collections.emptyList());

		assertFilePathIs(PATH_OWN_DESKTOP_FILE);

		assertMimeTypeInFileIs("x-scheme-handler/adt;");

		assertXdgMimeCalledFor("x-scheme-handler/adt");
	}

	@Test
	public void handlesAddAndRemoveAtOnce() throws Exception {
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE,
				getFileLines(OWN_EXECUTABLE_PATH, "MimeType=x-scheme-handler/adt;"));

		registration.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO), Arrays.asList(ADT_SCHEME_INFO));

		assertFilePathIs(PATH_OWN_DESKTOP_FILE);

		assertMimeTypeInFileIs("x-scheme-handler/other;");

		assertXdgMimeCalledFor("x-scheme-handler/other");
	}

	@Test
	public void handlesRemoveOnly() throws Exception {
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE,
				getFileLines(OWN_EXECUTABLE_PATH, "MimeType=x-scheme-handler/adt;"));

		registration.handleSchemes(Collections.emptyList(), Arrays.asList(ADT_SCHEME_INFO));

		assertFilePathIs(PATH_OWN_DESKTOP_FILE);

		assertNoMimeTypeInFile();

		assertTrue(processStub.records.isEmpty());
	}

	@Test
	public void createsInitialDesktopFile() throws Exception {
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE, new IOException("desktop file not existing"));

		registration.handleSchemes(Arrays.asList(ADT_SCHEME_INFO), Collections.emptyList());

		assertEquals(PATH_OWN_DESKTOP_FILE, fileProvider.writePath);

		assertAppNameInFileIs(PRODUCT_NAME);

		assertMimeTypeInFileIs("x-scheme-handler/adt;");

		assertExecInFileIs("/home/myuser/Eclipse/Eclipse %u");

		assertXdgMimeCalledFor("x-scheme-handler/adt");
	}

	@Test
	public void callsXdgMimeOnceForAllSchemes() throws Exception {
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE, getFileLines(OWN_EXECUTABLE_PATH, ""));

		registration.handleSchemes(Arrays.asList(ADT_SCHEME_INFO, OTHER_SCHEME_INFO), Collections.emptyList());

		assertFilePathIs(PATH_OWN_DESKTOP_FILE);

		assertMimeTypeInFileIs("x-scheme-handler/adt;x-scheme-handler/other;");

		assertXdgMimeCalledFor("x-scheme-handler/adt", "x-scheme-handler/other");
	}

	@Test
	public void givesSchemeInfoForHandledScheme() throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OWN_DESKTOP_FILE, true);
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE,
				getFileLines(OWN_EXECUTABLE_PATH, "MimeType=x-scheme-handler/adt;"));

		processStub.result = OWN_DESKTOP_FILE; // this is returned by xdg-mime query default
																// x-scheme-handler/adt

		List<ISchemeInformation> registeredSchemes = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, registeredSchemes.size());
		assertEquals("adt", registeredSchemes.get(0).getName());
		assertTrue("Scheme should be handled", registeredSchemes.get(0).isHandled());
	}

	@Test
	public void givesSchemeInfoForSchemeHandledByOtherApp() throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, true);
		fileProvider.readAnswers.put(PATH_OTHER_APP_DESKTOP_FILE,
				getFileLines(OTHER_APP_EXECUTABLE_PATH, "MimeType=x-scheme-handler/adt;"));

		processStub.result = OTHER_APP_DESKTOP_FILE; // this is returned by xdg-mime query default
																// x-scheme-handler/adt

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals(OTHER_APP_EXECUTABLE_PATH, infos.get(0).getHandlerInstanceLocation());
	}

	@Test
	public void givesSchemeInfoForSchemeHandledByOtherAppAndInOwnDesktopFile() throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OWN_DESKTOP_FILE, true);
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE,
				getFileLines(OWN_EXECUTABLE_PATH, "MimeType=x-scheme-handler/adt;"));

		fileProvider.fileExistsAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, true);
		fileProvider.readAnswers.put(PATH_OTHER_APP_DESKTOP_FILE,
				getFileLines(OTHER_APP_EXECUTABLE_PATH, "MimeType=x-scheme-handler/adt;"));

		processStub.result = OTHER_APP_DESKTOP_FILE; // this is returned by xdg-mime query default
		// x-scheme-handler/adt

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals(OTHER_APP_EXECUTABLE_PATH, infos.get(0).getHandlerInstanceLocation());
	}

	@Test
	public void givesSchemeInfoForSchemeNotHandledAnymoreButInXdgMimeForOtherApp() throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, true);
		fileProvider.fileExistsAnswers.put(PATH_OWN_DESKTOP_FILE, true);
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE, getFileLines(OWN_EXECUTABLE_PATH, ""));
		fileProvider.readAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, getFileLines(OTHER_APP_EXECUTABLE_PATH, ""));

		processStub.result = OTHER_APP_DESKTOP_FILE;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals("", infos.get(0).getHandlerInstanceLocation());
	}

	@Test
	public void givesSchemeInfoForSchemeNotHandledAnymoreDesktopFileNoLongerExistsButInXdgMimeForOtherApp()
			throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, false);
		fileProvider.fileExistsAnswers.put(PATH_OWN_DESKTOP_FILE, false);

		processStub.result = OTHER_APP_DESKTOP_FILE;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals("", infos.get(0).getHandlerInstanceLocation());
	}

	@Test
	public void givesSchemeInfoForSchemeNotHandledAnymoreButInXdgMimeForOwn() throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OWN_DESKTOP_FILE, true);
		fileProvider.readAnswers.put(PATH_OWN_DESKTOP_FILE, getFileLines(OWN_EXECUTABLE_PATH, ""));
		fileProvider.readAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, getFileLines(OTHER_APP_EXECUTABLE_PATH, ""));

		processStub.result = OWN_DESKTOP_FILE;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals("", infos.get(0).getHandlerInstanceLocation());
	}

	@Test
	public void givesSchemeInfoForSchemeNotHandledAnymoreDesktopFileNoLongerExistsButInXdgMimeForOwn()
			throws Exception {
		fileProvider.fileExistsAnswers.put(PATH_OWN_DESKTOP_FILE, false);
		fileProvider.fileExistsAnswers.put(PATH_OTHER_APP_DESKTOP_FILE, false);

		processStub.result = OWN_DESKTOP_FILE;

		List<ISchemeInformation> infos = registration.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, infos.size());
		assertEquals("adt", infos.get(0).getName());
		assertFalse(infos.get(0).isHandled());
		assertEquals("", infos.get(0).getHandlerInstanceLocation());
	}

	private List<String> getFileLines(String exec, String mimeType) {
		return Arrays.asList("[Desktop Entry]", //
				"Exec=" + exec, //
				"NoDisplay=true", //
				mimeType, //
				"Type=Application");
	}

	private void assertFilePathIs(String filePath) {
		assertEquals(filePath, fileProvider.recordedReadPaths.get(0));
		assertEquals(filePath, fileProvider.writePath);
	}

	private void assertAppNameInFileIs(String name) {
		assertThat(new String(fileProvider.writtenBytes), new StringContains("Name=" + name));
	}

	private void assertMimeTypeInFileIs(String mimeType) {
		assertThat(new String(fileProvider.writtenBytes), new StringContains("MimeType=" + mimeType));
	}

	private void assertExecInFileIs(String exec) {
		assertThat(new String(fileProvider.writtenBytes), new StringContains("Exec=" + exec));
	}

	private void assertNoMimeTypeInFile() {
		assertThat(new String(fileProvider.writtenBytes),
				new IsNot<>(new StringContains("MimeType=x-scheme-handler/adt;")));
	}

	private void assertXdgMimeCalledFor(String... schemeHandler) {
		assertEquals(1, processStub.records.size());
		assertEquals("xdg-mime", processStub.records.get(0).process);
		String schemeHandlers = Arrays.stream(schemeHandler).collect(Collectors.joining(" "));
		assertArrayEquals(new String[] { "default", OWN_DESKTOP_FILE, schemeHandlers },
				processStub.records.get(0).args);
	}
}