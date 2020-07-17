/*******************************************************************************
 * Copyright (c) 2017 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IDialogSettingsProvider;
import org.eclipse.jface.preference.PreferenceMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.TestPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DialogSettingsCustomizationTest {

	// See DialogSettingsProvider
	private static final String KEY_DEFAULT_DIALOG_SETTINGS_ROOTURL = "default_dialog_settings_rootUrl"; //$NON-NLS-1$
	private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$

	private Path dialogSettingsPath;
	private TestPlugin testPlugin;
	private Path dialogSettingsPathBackup;
	private String rootUrlValue;
	private PreferenceMemento memento;
	private IDialogSettingsProvider dialogSettingsProvider;

	@Before
	public void doSetUp() throws Exception {
		testPlugin = TestPlugin.getDefault();
		rootUrlValue = "platform:/plugin/" + testPlugin.getBundle().getSymbolicName() + "/data/dialog_settings_root";
		dialogSettingsPath = testPlugin.getStateLocation().append(FN_DIALOG_SETTINGS).toFile().toPath();
		dialogSettingsPathBackup = testPlugin.getStateLocation().append(FN_DIALOG_SETTINGS + ".back").toFile().toPath();
		if (Files.exists(dialogSettingsPath)) {
			Files.deleteIfExists(dialogSettingsPathBackup);
			Files.move(dialogSettingsPath, dialogSettingsPathBackup);
		}
		dialogSettingsProvider = PlatformUI.getDialogSettingsProvider(testPlugin.getBundle());
		dialogSettingsProvider.loadDialogSettings();
		memento = new PreferenceMemento();
	}

	@After
	public void doTearDown() throws Exception {
		Files.deleteIfExists(dialogSettingsPath);
		if (Files.exists(dialogSettingsPathBackup)) {
			Files.move(dialogSettingsPathBackup, dialogSettingsPath);
		}
		dialogSettingsProvider.loadDialogSettings();
		memento.resetPreferences();
	}

	@Test
	public void testDialogSettingsContributedByBundle() throws Exception {
		assertDefaultBundleValueIsSet();
		memento.setValue(PlatformUI.getPreferenceStore(), KEY_DEFAULT_DIALOG_SETTINGS_ROOTURL, rootUrlValue);
		assertCustomValueIsSet();
	}

	@Test
	public void testDialogSettingsContributedByFileUrl() throws Exception {
		String rootUrl = FileLocator.toFileURL(new URL(rootUrlValue)).toString();
		assertDefaultBundleValueIsSet();
		memento.setValue(PlatformUI.getPreferenceStore(), KEY_DEFAULT_DIALOG_SETTINGS_ROOTURL, rootUrl);
		assertCustomValueIsSet();
	}

	private void assertDefaultBundleValueIsSet() {
		IDialogSettings section = dialogSettingsProvider.getDialogSettings().getSection("DialogSettingsCustomizationTest");
		assertNotNull(section);
		assertEquals("defaultBundleValue", section.get("testKey"));
	}

	private void assertCustomValueIsSet() throws Exception {
		dialogSettingsProvider.loadDialogSettings();
		IDialogSettings section = dialogSettingsProvider.getDialogSettings().getSection("DialogSettingsCustomizationTest");
		assertNotNull(section);
		assertEquals("testValue", section.get("testKey"));
	}
}