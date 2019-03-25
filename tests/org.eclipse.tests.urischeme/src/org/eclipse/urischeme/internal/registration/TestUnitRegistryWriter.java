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

import org.eclipse.urischeme.internal.registration.WinRegistryMock.Entry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestUnitRegistryWriter {
	private final static String QUOTE = "\"";
	private static String originalEclipseHomeLocation;
	private static String originalEclipseLauncher;

	public static String launcher;
	public static String launcherWithArgs;
	public static String homeLocation;
	private RegistryWriter writer;
	private WinRegistryMock registryMock;
	private FileProviderMock fileProviderMock;

	@Before
	public void setUp() throws Exception {
		registryMock = new WinRegistryMock();
		launcher = "/path/to/eclipse/Eclips.exe";
		launcherWithArgs = QUOTE + launcher + QUOTE + " " + QUOTE + "%1" + QUOTE;
		homeLocation = "/path/to/eclipse/";

		System.setProperty("eclipse.launcher", launcher);
		System.setProperty("eclipse.home.location", homeLocation);

		fileProviderMock = new FileProviderMock();
		fileProviderMock.fileExistsAnswers.put(launcher, true);
		fileProviderMock.isDirectoryAnswers.put(launcher, false);
		writer = new RegistryWriter(registryMock, fileProviderMock);
	}

	@BeforeClass
	public static void classSetup() {
		originalEclipseLauncher = System.getProperty("eclipse.launcher", "");
		originalEclipseHomeLocation = System.getProperty("eclipse.home.location", "");
	}

	@AfterClass
	public static void classTearDown() {
		System.setProperty("eclipse.launcher", originalEclipseLauncher);
		System.setProperty("eclipse.home.location", originalEclipseHomeLocation);
	}

	@Test
	public void addsOneScheme() throws Exception {
		writer.addScheme("adt", launcher);

		assertEntry(registryMock.setValues.get(0), "Software\\Classes\\adt", "URL Protocol", "");
		assertEntry(registryMock.setValues.get(1), "Software\\Classes\\adt", null, "URL:adt");
		assertEntry(registryMock.setValues.get(2), "Software\\Classes\\adt\\shell\\open\\command", "Executable",
				launcher);
		assertEntry(registryMock.setValues.get(3), "Software\\Classes\\adt\\shell\\open\\command", null,
				launcherWithArgs);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnAddingInvalidScheme() throws Exception {
		writer.addScheme("%&$", launcher);
	}

	@Test(expected = WinRegistryException.class)
	public void throwsIllegalStateExceptionOnAddScheme() throws Exception {
		registryMock.setValueForKeyException = new WinRegistryException("failed");
		writer.addScheme("adt", launcher);
	}

	@Test
	public void removesOneScheme() throws Exception {
		registryMock.valuesForKeys.put("Software\\Classes\\adt-URL Protocol", "");
		registryMock.valuesForKeys.put("Software\\Classes\\adt-null", "URL:adt");
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-Executable", launcher);
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-null", launcherWithArgs);

		fileProviderMock.fileExistsAnswers.put(launcher, true);

		writer.removeScheme("adt");

		assertEquals("Software\\Classes\\adt\\shell\\open\\command", registryMock.deletedKeys.get(0));
		assertEquals("Software\\Classes\\adt\\shell\\open", registryMock.deletedKeys.get(1));
		assertEquals("Software\\Classes\\adt\\shell", registryMock.deletedKeys.get(2));
		assertEquals("Software\\Classes\\adt", registryMock.deletedKeys.get(3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnRemovingInvalidScheme() throws Exception {
		writer.removeScheme("%&$");
	}

	@Test(expected = WinRegistryException.class)
	public void throwsWinRegistryExceptionOnRemoveScheme() throws Exception {
		registryMock.valuesForKeys.put("Software\\Classes\\adt-URL Protocol", "");
		registryMock.valuesForKeys.put("Software\\Classes\\adt-null", "URL:adt");
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-Executable", launcher);
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-null", launcherWithArgs);

		fileProviderMock.fileExistsAnswers.put(launcher, true);

		registryMock.deleteKeyException = new WinRegistryException("failed");
		writer.removeScheme("adt");
	}

	private void assertEntry(Entry entry, String key, String attribute, String value) {
		assertEquals(key, entry.key);
		assertEquals(attribute, entry.attribute);
		assertEquals(value, entry.value);
	}

	@Test
	public void addsTwoSchemes() throws WinRegistryException {
		writer.addScheme("adt", launcher);
		writer.addScheme("other", launcher);

		assertEntry(registryMock.setValues.get(0), "Software\\Classes\\adt", "URL Protocol", "");
		assertEntry(registryMock.setValues.get(1), "Software\\Classes\\adt", null, "URL:adt");
		assertEntry(registryMock.setValues.get(2), "Software\\Classes\\adt\\shell\\open\\command", "Executable",
				launcher);
		assertEntry(registryMock.setValues.get(3), "Software\\Classes\\adt\\shell\\open\\command", null,
				launcherWithArgs);

		assertEntry(registryMock.setValues.get(4), "Software\\Classes\\other", "URL Protocol", "");
		assertEntry(registryMock.setValues.get(5), "Software\\Classes\\other", null, "URL:other");
		assertEntry(registryMock.setValues.get(6), "Software\\Classes\\other\\shell\\open\\command", "Executable",
				launcher);
		assertEntry(registryMock.setValues.get(7), "Software\\Classes\\other\\shell\\open\\command", null,
				launcherWithArgs);
	}

	@Test
	public void removesTwoSchemes() throws Exception {
		registryMock.valuesForKeys.put("Software\\Classes\\adt-URL Protocol", "");
		registryMock.valuesForKeys.put("Software\\Classes\\adt-null", "URL:adt");
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-Executable", launcher);
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-null", launcherWithArgs);

		registryMock.valuesForKeys.put("Software\\Classes\\other-URL Protocol", "");
		registryMock.valuesForKeys.put("Software\\Classes\\other-null", "URL:other");
		registryMock.valuesForKeys.put("Software\\Classes\\other\\shell\\open\\command-Executable", launcher);
		registryMock.valuesForKeys.put("Software\\Classes\\other\\shell\\open\\command-null", launcherWithArgs);

		fileProviderMock.fileExistsAnswers.put(launcher, true);

		writer.removeScheme("adt");
		writer.removeScheme("other");

		assertEquals("Software\\Classes\\adt\\shell\\open\\command", registryMock.deletedKeys.get(0));
		assertEquals("Software\\Classes\\adt\\shell\\open", registryMock.deletedKeys.get(1));
		assertEquals("Software\\Classes\\adt\\shell", registryMock.deletedKeys.get(2));
		assertEquals("Software\\Classes\\adt", registryMock.deletedKeys.get(3));

		assertEquals("Software\\Classes\\other\\shell\\open\\command", registryMock.deletedKeys.get(4));
		assertEquals("Software\\Classes\\other\\shell\\open", registryMock.deletedKeys.get(5));
		assertEquals("Software\\Classes\\other\\shell", registryMock.deletedKeys.get(6));
		assertEquals("Software\\Classes\\other", registryMock.deletedKeys.get(7));
	}

	@Test
	public void returnsRegisteredHandlerPath() throws WinRegistryException {
		assertNull(writer.getRegisteredHandlerPath("adt"));

		registryMock.valuesForKeys.put("Software\\Classes\\adt-URL Protocol", "");
		assertNull(writer.getRegisteredHandlerPath("adt"));

		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-null", "URL:adt");
		assertNull(writer.getRegisteredHandlerPath("adt"));

		fileProviderMock.fileExistsAnswers.put(launcher, true);
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-Executable", launcher);
		assertEquals(launcher, writer.getRegisteredHandlerPath("adt"));
	}

	@Test
	public void returnsNullOnNotExitingRegisteredHandlerPath() throws WinRegistryException {
		assertNull(writer.getRegisteredHandlerPath("adt"));

		registryMock.valuesForKeys.put("Software\\Classes\\adt-URL Protocol", "");
		assertNull(writer.getRegisteredHandlerPath("adt"));

		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-null", "URL:adt");
		assertNull(writer.getRegisteredHandlerPath("adt"));

		fileProviderMock.fileExistsAnswers.put(launcher, false);
		registryMock.valuesForKeys.put("Software\\Classes\\adt\\shell\\open\\command-Executable", launcher);

		assertEquals(null, writer.getRegisteredHandlerPath("adt"));
	}

	@Test(expected = WinRegistryException.class)
	public void throwsWinRegistryExceptionOnGetRegisteredHandlerPath() throws Exception {
		registryMock.getValueForKeyException = new WinRegistryException("failed");
		writer.getRegisteredHandlerPath("adt");
	}

	@Test
	public void ignoresUnregisteredSchemeOnRemove() throws WinRegistryException {
		writer.removeScheme("adt");
		assertTrue(registryMock.deletedKeys.isEmpty());
	}

}
