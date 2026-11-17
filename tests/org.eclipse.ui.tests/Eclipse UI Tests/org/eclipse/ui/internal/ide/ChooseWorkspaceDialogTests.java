/*******************************************************************************
 * Copyright (c) 2022, 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.ui.ide.IDE;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChooseWorkspaceDialogTests {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * Validates that {@link ChooseWorkspaceDialog#filterDuplicatedPaths(String[])}
	 * filters out non-unique paths.
	 *
	 * @see <a href=
	 *      "https://github.com/eclipse-platform/eclipse.platform.ui/issues/312">GitHub
	 *      issue 312</a>
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=531611">Eclipse
	 *      bug 531611</a>
	 */
	@Test
	public void testFilterDuplicatedPaths() {
		String[] testPaths = {
				"some/location1/",
				"/some/location1/",
				"/some/location1",
				"/some/location2/",
				"/some///location1",
				"//some//location1//",
		};
		String[] expectedFilteredPaths = { "some/location1/", "/some/location1/", "/some/location2/", };
		adoptToWindows(testPaths);
		adoptToWindows(expectedFilteredPaths);
		List<String> actualFilteredPaths = ChooseWorkspaceDialog.filterDuplicatedPaths(testPaths);
		assertEquals("Non-unique paths were not filtered as expected", Arrays.asList(expectedFilteredPaths),
				actualFilteredPaths);
	}

	@Test
	public void testDecodeStoredWorkspacePaths_Protocol3() {
		String encoded = "/path/to/ws1\n/path/to/ws2\n/path/to/ws3";
		String[] decoded = ChooseWorkspaceData.decodeStoredWorkspacePaths(
				ChooseWorkspaceData.PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS, encoded);
		assertEquals(List.of("/path/to/ws1", "/path/to/ws2", "/path/to/ws3"), Arrays.asList(decoded));
	}

	@Test
	public void testDecodeStoredWorkspacePaths_Protocol2() {
		String encoded = "/path/to/ws1,/path/to/ws2,/path/to/ws3";
		String[] decoded = ChooseWorkspaceData.decodeStoredWorkspacePaths(
				ChooseWorkspaceData.PERS_ENCODING_VERSION_CONFIG_PREFS, encoded);
		assertEquals(List.of("/path/to/ws1", "/path/to/ws2", "/path/to/ws3"), Arrays.asList(decoded));
	}

	@Test
	public void testImportWorkspacesFromEclipseInstallation_Protocol3() throws Exception {
		File installDir = tempFolder.newFolder("eclipse-install");
		File ws1 = tempFolder.newFolder("ws1");
		File ws2 = tempFolder.newFolder("ws2");

		File configSettings = new File(new File(installDir, "configuration"), ".settings");
		configSettings.mkdirs();
		File prefsFile = new File(configSettings, "org.eclipse.ui.ide.prefs");

		Properties props = new Properties();
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL,
				String.valueOf(ChooseWorkspaceData.PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS));
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES,
				ws1.getAbsolutePath() + "\n" + ws2.getAbsolutePath());

		try (FileOutputStream fos = new FileOutputStream(prefsFile)) {
			props.store(fos, null);
		}

		List<String> imported = ChooseWorkspaceDialog.importWorkspacesFromEclipseInstallation(installDir.getAbsolutePath());
		assertEquals(List.of(ws1.getAbsolutePath(), ws2.getAbsolutePath()), imported);
	}

	@Test
	public void testImportWorkspacesFromEclipseInstallation_Protocol2() throws Exception {
		File installDir = tempFolder.newFolder("eclipse-install-p2");
		File ws1 = tempFolder.newFolder("ws1-p2");
		File ws2 = tempFolder.newFolder("ws2-p2");

		File configSettings = new File(new File(installDir, "configuration"), ".settings");
		configSettings.mkdirs();
		File prefsFile = new File(configSettings, "org.eclipse.ui.ide.prefs");

		Properties props = new Properties();
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL,
				String.valueOf(ChooseWorkspaceData.PERS_ENCODING_VERSION_CONFIG_PREFS));
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES,
				ws1.getAbsolutePath() + "," + ws2.getAbsolutePath());

		try (FileOutputStream fos = new FileOutputStream(prefsFile)) {
			props.store(fos, null);
		}

		List<String> imported = ChooseWorkspaceDialog.importWorkspacesFromEclipseInstallation(installDir.getAbsolutePath());
		assertEquals(List.of(ws1.getAbsolutePath(), ws2.getAbsolutePath()), imported);
	}

	@Test
	public void testImportWorkspacesFromEclipseInstallation_FilterFilesAndNonExistent() throws Exception {
		File installDir = tempFolder.newFolder("eclipse-install-filter");
		File validWs = tempFolder.newFolder("valid-ws");
		File regularFile = tempFolder.newFile("regular-file.txt");
		File nonExistentDir = new File(tempFolder.getRoot(), "does-not-exist");

		File configSettings = new File(new File(installDir, "configuration"), ".settings");
		configSettings.mkdirs();
		File prefsFile = new File(configSettings, "org.eclipse.ui.ide.prefs");

		Properties props = new Properties();
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL,
				String.valueOf(ChooseWorkspaceData.PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS));
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES,
				validWs.getAbsolutePath() + "\n" + regularFile.getAbsolutePath() + "\n"
						+ nonExistentDir.getAbsolutePath());

		try (FileOutputStream fos = new FileOutputStream(prefsFile)) {
			props.store(fos, null);
		}

		List<String> imported = ChooseWorkspaceDialog.importWorkspacesFromEclipseInstallation(installDir.getAbsolutePath());
		assertEquals(List.of(validWs.getAbsolutePath()), imported);
	}

	@Test
	public void testImportWorkspacesFromEclipseInstallation_AlternativeLocation() throws Exception {
		File installDir = tempFolder.newFolder("eclipse-install-alt");
		File ws1 = tempFolder.newFolder("ws1-alt");

		File altSettings = new File(installDir, ".metadata/.plugins/org.eclipse.core.runtime/.settings");
		altSettings.mkdirs();
		File prefsFile = new File(altSettings, "org.eclipse.ui.ide.prefs");

		Properties props = new Properties();
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL,
				String.valueOf(ChooseWorkspaceData.PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS));
		props.setProperty(IDE.Preferences.RECENT_WORKSPACES, ws1.getAbsolutePath());

		try (FileOutputStream fos = new FileOutputStream(prefsFile)) {
			props.store(fos, null);
		}

		List<String> imported = ChooseWorkspaceDialog.importWorkspacesFromEclipseInstallation(installDir.getAbsolutePath());
		assertEquals(List.of(ws1.getAbsolutePath()), imported);
	}

	@Test
	public void testImportWorkspacesFromEclipseInstallation_NonExistentInstall() {
		File nonExistentInstall = new File(tempFolder.getRoot(), "non-existent-install");
		List<String> imported = ChooseWorkspaceDialog
				.importWorkspacesFromEclipseInstallation(nonExistentInstall.getAbsolutePath());
		assertTrue("Expected empty list for non-existent installation", imported.isEmpty());
	}

	@Test
	public void testImportWorkspacesFromEclipseInstallation_MalformedUnicodeEscape() throws Exception {
		File installDir = tempFolder.newFolder("eclipse-install-malformed");
		File configSettings = new File(new File(installDir, "configuration"), ".settings");
		configSettings.mkdirs();
		File prefsFile = new File(configSettings, "org.eclipse.ui.ide.prefs");
		// A truncated unicode escape in the value makes Properties.load() throw an
		// IllegalArgumentException, even for a key unrelated to recent workspaces
		Files.writeString(prefsFile.toPath(), "unrelated=\\u12\n");

		List<String> imported = ChooseWorkspaceDialog
				.importWorkspacesFromEclipseInstallation(installDir.getAbsolutePath());
		assertTrue("Expected empty list for a corrupt preferences file", imported.isEmpty());
	}

	@Test
	public void testMergeRecentWorkspaces_KeepsUnavailableExistingAndPadsArray() throws Exception {
		File imported = tempFolder.newFolder("imported-ws");
		File missingExisting = new File(tempFolder.getRoot(), "offline-ws");
		String[] existing = new String[10];
		existing[0] = missingExisting.getAbsolutePath();

		String[] merged = ChooseWorkspaceDialog.mergeRecentWorkspaces(existing, List.of(imported.getAbsolutePath()));

		assertEquals("Capacity of the recent workspaces array must be preserved", 10, merged.length);
		assertEquals(missingExisting.getAbsolutePath(), merged[0]);
		assertEquals(imported.getAbsolutePath(), merged[1]);
		for (int i = 2; i < merged.length; i++) {
			assertNull("Slot " + i + " must stay free", merged[i]);
		}
	}

	@Test
	public void testMergeRecentWorkspaces_FiltersInvalidIncomingAndDuplicates() throws Exception {
		File ws = tempFolder.newFolder("ws");
		File regularFile = tempFolder.newFile("file.txt");
		String[] existing = new String[10];
		existing[0] = ws.getAbsolutePath();

		String[] merged = ChooseWorkspaceDialog.mergeRecentWorkspaces(existing,
				List.of(ws.getAbsolutePath() + File.separator, regularFile.getAbsolutePath()));

		assertEquals(10, merged.length);
		assertEquals(ws.getAbsolutePath(), merged[0]);
		assertNull("Duplicate spelling and regular file must not be added", merged[1]);
	}

	static void adoptToWindowsList(List<String> paths) {
		String[] array = paths.toArray(new String[0]);
		adoptToWindows(array);
		paths.clear();
		paths.addAll(Arrays.asList(array));
	}

	@Test
	public void testFilterNewWorkspaces_EquivalentSpellingOfExistingIsNotOffered() {
		List<String> existing = new ArrayList<>(List.of("/work/ws", "/work/other"));
		List<String> detected = new ArrayList<>(List.of("/work/ws/", "/work//other", "/work/new"));
		adoptToWindowsList(existing);
		adoptToWindowsList(detected);

		List<String> result = ChooseWorkspaceDialog.filterNewWorkspaces(existing, detected);

		List<String> expected = new ArrayList<>(List.of("/work/new"));
		adoptToWindowsList(expected);
		assertEquals(expected, result);
	}

	@Test
	public void testFilterNewWorkspaces_EquivalentDetectedEntriesAreOfferedOnce() {
		List<String> detected = new ArrayList<>(List.of("/work/a", "/work/a/", "/work/b"));
		adoptToWindowsList(detected);

		List<String> result = ChooseWorkspaceDialog.filterNewWorkspaces(List.of(), detected);

		List<String> expected = new ArrayList<>(List.of("/work/a", "/work/b"));
		adoptToWindowsList(expected);
		assertEquals(expected, result);
	}

	@Test
	public void testFilterNewWorkspaces_NullExistingKeepsAllDetected() {
		List<String> detected = new ArrayList<>(List.of("/work/a", "/work/b"));
		adoptToWindowsList(detected);

		assertEquals(detected, ChooseWorkspaceDialog.filterNewWorkspaces(null, detected));
	}

	static void adoptToWindows(String[] paths) {
		if (File.separatorChar == '\\') {
			for (int i = 0; i < paths.length; i++) {
				paths[i] = paths[i].replace('/', '\\');
			}
		}
	}
}
