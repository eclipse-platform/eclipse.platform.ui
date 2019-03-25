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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestUnitRegistrationWindows {

	@Rule
	public TestName name = new TestName();

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

	@BeforeClass
	public static void classSetup() throws MalformedURLException {
		originalEclipseLauncher = System.getProperty("eclipse.launcher", null);
		originalEclipseHome = System.getProperty("eclipse.home.location", null);
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty("eclipse.launcher", PATH_TO_ECLIPSE_EXE);
		registryWriter = new RegistryWriterMock();
		fileProvider = new FileProviderMock();
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_EXE, true);
		fileProvider.isDirectoryAnswers.put(PATH_TO_ECLIPSE_EXE, false);
		fileProvider.urlTosFilePaths.put(new URL(URL_TO_ECLIPSE_HOME), PATH_TO_ECLIPSE_HOME);
		registrationWindows = new RegistrationWindows(registryWriter, fileProvider);
	}

	@AfterClass
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

		assertEquals("Too many schemes added", 2, registryWriter.addedSchemes.size());
		assertTrue("Scheme not added", registryWriter.addedSchemes.contains(OTHER_SCHEME_INFO.getName()));
		assertTrue("Scheme not added", registryWriter.addedSchemes.contains(ADT_SCHEME_INFO.getName()));

		assertEquals("Too many schemes removed", 0, registryWriter.removedSchemes.size());
	}

	@Test
	public void handlesAddAndRemoveOfSameScheme() throws Exception {
		registrationWindows.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO), Arrays.asList(OTHER_SCHEME_INFO));

		assertEquals("Too many schemes added", 1, registryWriter.addedSchemes.size());
		assertTrue("Scheme not added", registryWriter.addedSchemes.contains(OTHER_SCHEME_INFO.getName()));

		assertEquals("Too many schemes removed", 1, registryWriter.removedSchemes.size());
		assertTrue("Scheme not removed", registryWriter.removedSchemes.contains(OTHER_SCHEME_INFO.getName()));
	}

	@Test
	public void handlesAddAndRemoveOfSameSchemes() throws Exception {
		registrationWindows.handleSchemes(Arrays.asList(OTHER_SCHEME_INFO, ADT_SCHEME_INFO),
				Arrays.asList(ADT_SCHEME_INFO, OTHER_SCHEME_INFO));

		assertEquals("Too many schemes added", 2, registryWriter.addedSchemes.size());
		assertTrue("Scheme not added", registryWriter.addedSchemes.contains(OTHER_SCHEME_INFO.getName()));
		assertTrue("Scheme not added", registryWriter.addedSchemes.contains(ADT_SCHEME_INFO.getName()));

		assertEquals("Too many schemes removed", 2, registryWriter.removedSchemes.size());
		assertTrue("Scheme not removed", registryWriter.removedSchemes.contains(OTHER_SCHEME_INFO.getName()));
		assertTrue("Scheme not removed", registryWriter.removedSchemes.contains(ADT_SCHEME_INFO.getName()));
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
		System.out.println("registrationWindows1: " + registrationWindows);
		System.clearProperty("eclipse.launcher");
		System.setProperty("eclipse.home.location", URL_TO_ECLIPSE_HOME);
		fileProvider.fileExistsAnswers.put(PATH_TO_ECLIPSE_HOME, true);
		fileProvider.isDirectoryAnswers.put(PATH_TO_ECLIPSE_HOME, true);
		fileProvider.newDirectoryStreamAnswers.computeIfAbsent(PATH_TO_ECLIPSE_HOME, path -> new HashMap<>())
				.put("*.exe", Arrays.asList(PATH_TO_ECLIPSE_HOME + "\\Eclipse.exe"));

		System.out.println("registrationWindows2: " + registrationWindows);
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
		assertEquals("Scheme not set correctly", scheme.getName(), schemeInformation.getName());
		assertEquals("Scheme description not set correctly", scheme.getDescription(),
				schemeInformation.getDescription());
		assertEquals("Handler location not set correctly", handlerlocation,
				schemeInformation.getHandlerInstanceLocation());
		assertEquals("isHandled not set correctly", isHandled, schemeInformation.isHandled());
	}
}
