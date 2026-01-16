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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestUnitRegistrationWindows {

	private static final String PATH_TO_OTHER_APPLICATION_EXE = "c:\\path\\to\\otherApplication.exe";
	private static final String PATH_TO_ECLIPSE_EXE = "c:\\path\\with spaces\\to\\eclipse\\Eclipse.exe";
	private static final String PATH_TO_ECLIPSE_HOME = "c:\\path\\with spaces\\to\\eclipse";
	private static final String URL_TO_ECLIPSE_HOME = "file:/c:/path/with spaces/to/eclipse/";

	private static final IScheme OTHER_SCHEME = new Scheme("other", "");
	private static final IScheme ADT_SCHEME = new Scheme("adt", "");
	private static final ISchemeInformation OTHER_SCHEME_INFO = new SchemeInformation("other", "");
	private static final ISchemeInformation ADT_SCHEME_INFO = new SchemeInformation("adt", "");

	RegistryWriterMock registryWriter;
	FileProviderMock fileProvider;
	private static String originalEclipseLauncher;
	private static String originalEclipseHome;
	private RegistrationWindows registrationWindows;

	@BeforeAll
	public static void classSetup() throws MalformedURLException {
		originalEclipseLauncher = System.getProperty("eclipse.launcher", null);
		originalEclipseHome = System.getProperty("eclipse.home.location", null);
	}

	@BeforeEach
	public void setUp() throws Exception {
		System.setProperty("eclipse.launcher", PATH_TO_ECLIPSE_EXE);
		registryWriter = new RegistryWriterMock();
		fileProvider = new FileProviderMock();
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_EXE, true);
		fileProvider.isDirectoryAnswers.put(PATH_TO_ECLIPSE_EXE, false);
		fileProvider.urlTosFilePaths.put(new URL(URL_TO_ECLIPSE_HOME), PATH_TO_ECLIPSE_HOME);
		registrationWindows = new RegistrationWindows(registryWriter, fileProvider);
	}

	@AfterAll
	public static void tearDown() throws Exception {
		if (originalEclipseLauncher != null) {
			System.setProperty("eclipse.launcher", originalEclipseLauncher);
		} else {
			System.clearProperty("eclipse.launcher");
		}
		if (originalEclipseHome != null) {
			System.setProperty("eclipse.home.location", originalEclipseHome);
		} else {
			System.clearProperty("eclipse.home.location");
		}
	}

	@Test
	public void handlesAdd() throws Exception {
		registrationWindows.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO, ADT_SCHEME_INFO), Collections.emptyList());

		assertEquals(2, registryWriter.addedSchemes.size(), "Too many schemes added");
		assertTrue(registryWriter.addedSchemes.contains(OTHER_SCHEME_INFO.getName()), "Scheme not added");
		assertTrue(registryWriter.addedSchemes.contains(ADT_SCHEME_INFO.getName()), "Scheme not added");

		assertEquals(0, registryWriter.removedSchemes.size(), "Too many schemes removed");
	}

	@Test
	public void handlesAddAndRemoveOfSameScheme() throws Exception {
		registrationWindows.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO), Arrays.asList(OTHER_SCHEME_INFO));

		assertEquals(1, registryWriter.addedSchemes.size(), "Too many schemes added");
		assertTrue(registryWriter.addedSchemes.contains(OTHER_SCHEME_INFO.getName()), "Scheme not added");

		assertEquals(1, registryWriter.removedSchemes.size(), "Too many schemes removed");
		assertTrue(registryWriter.removedSchemes.contains(OTHER_SCHEME_INFO.getName()), "Scheme not removed");
	}

	@Test
	public void handlesAddAndRemoveOfSameSchemes() throws Exception {
		registrationWindows.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO, ADT_SCHEME_INFO),
				Arrays.asList(ADT_SCHEME_INFO, OTHER_SCHEME_INFO));

		assertEquals(2, registryWriter.addedSchemes.size(), "Too many schemes added");
		assertTrue(registryWriter.addedSchemes.contains(OTHER_SCHEME_INFO.getName()), "Scheme not added");
		assertTrue(registryWriter.addedSchemes.contains(ADT_SCHEME_INFO.getName()), "Scheme not added");

		assertEquals(2, registryWriter.removedSchemes.size(), "Too many schemes removed");
		assertTrue(registryWriter.removedSchemes.contains(OTHER_SCHEME_INFO.getName()), "Scheme not removed");
		assertTrue(registryWriter.removedSchemes.contains(ADT_SCHEME_INFO.getName()), "Scheme not removed");
	}

	@Test
	public void returnsUnregisteredSchemeInformation() throws Exception {
		List<ISchemeInformation> schemeInformation = registrationWindows
				.getSchemesInformation(Arrays.asList(ADT_SCHEME, OTHER_SCHEME));

		assertEquals(2, schemeInformation.size());
		assertSchemeInformation(schemeInformation.get(0), ADT_SCHEME, "", false);
		assertSchemeInformation(schemeInformation.get(1), OTHER_SCHEME, "", false);
	}

	@Test
	public void returnsRegisteredSchemeInformationForThisEclipse() throws Exception {
		registryWriter.schemeToHandlerPath.put(ADT_SCHEME.getName(), PATH_TO_ECLIPSE_EXE);

		List<ISchemeInformation> schemeInformation = registrationWindows
				.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, schemeInformation.size());
		assertSchemeInformation(schemeInformation.get(0), ADT_SCHEME, PATH_TO_ECLIPSE_EXE, true);
	}

	@Test
	public void returnsRegisteredSchemeInformationForOtherApplication() throws Exception {
		registryWriter.schemeToHandlerPath.put(ADT_SCHEME.getName(), PATH_TO_OTHER_APPLICATION_EXE);

		List<ISchemeInformation> schemeInformation = registrationWindows
				.getSchemesInformation(Arrays.asList(ADT_SCHEME));

		assertEquals(1, schemeInformation.size());
		assertSchemeInformation(schemeInformation.get(0), ADT_SCHEME, PATH_TO_OTHER_APPLICATION_EXE, false);
	}

	@Test
	public void getLauncherPathFromLauncherProperty() throws Exception {
		System.setProperty("eclipse.launcher", PATH_TO_ECLIPSE_EXE);
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_EXE, true);
		fileProvider.isDirectoryAnswers.put(PATH_TO_ECLIPSE_EXE, false);

		assertEquals(PATH_TO_ECLIPSE_EXE, registrationWindows.getEclipseLauncher());
	}

	@Test
	public void getLauncherPathFromEclipseHomeProperty() throws Exception {
		System.clearProperty("eclipse.launcher");
		System.setProperty("eclipse.home.location", URL_TO_ECLIPSE_HOME);
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_HOME, true);
		fileProvider.isDirectoryAnswers.put(PATH_TO_ECLIPSE_HOME, true);
		fileProvider.newDirectoryStreamAnswers.computeIfAbsent(PATH_TO_ECLIPSE_HOME, path -> new HashMap<>())
				.put("*.exe", Arrays.asList(PATH_TO_ECLIPSE_HOME + "\\Eclipse.exe"));

		assertEquals(PATH_TO_ECLIPSE_EXE, registrationWindows.getEclipseLauncher());
	}

	@Test
	public void getLauncherPathFromEclipseHomeProperty_NoExeFileInDirectory() throws Exception {
		System.clearProperty("eclipse.launcher");
		System.setProperty("eclipse.home.location", URL_TO_ECLIPSE_HOME);
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_HOME, true);
		fileProvider.isDirectoryAnswers.put(PATH_TO_ECLIPSE_HOME, true);
		fileProvider.newDirectoryStreamAnswers.computeIfAbsent(PATH_TO_ECLIPSE_HOME, path -> new HashMap<>())
				.put("*.exe", Collections.emptyList());

		assertNull(registrationWindows.getEclipseLauncher());
	}

	@Test
	public void getLauncherPathFromEclipseHomeProperty_DirectoryDoesNotExist() throws Exception {
		System.clearProperty("eclipse.launcher");
		System.setProperty("eclipse.home.location", URL_TO_ECLIPSE_HOME);
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_HOME, false);

		assertNull(registrationWindows.getEclipseLauncher());
	}

	@Test
	public void getLauncherPathFromEclipseHomeProperty_NotAFileUrl() throws Exception {
		System.clearProperty("eclipse.launcher");
		System.setProperty("eclipse.home.location", "http://path/to/eclipse");

		assertNull(registrationWindows.getEclipseLauncher());
	}

	private void assertSchemeInformation(ISchemeInformation schemeInformation, IScheme scheme, String handlerlocation,
			boolean isHandled) {
		assertEquals(scheme.getName(), schemeInformation.getName(), "Scheme not set correctly");
		assertEquals(scheme.getDescription(), schemeInformation.getDescription(), "Scheme description not set correctly");
		assertEquals(handlerlocation, schemeInformation.getHandlerInstanceLocation(), "Handler location not set correctly");
		assertEquals(isHandled, schemeInformation.isHandled(), "isHandled not set correctly");
	}
}
