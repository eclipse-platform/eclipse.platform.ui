/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.jsch;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jsch.internal.core.IConstants;
import org.eclipse.jsch.internal.core.JSchCorePlugin;
import org.eclipse.jsch.internal.core.PreferenceInitializer;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class PreferenceInitializerTest extends EclipseTest {

	private boolean PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME_value = false;
	private String KEY_SSH2HOME_value = null;

	protected void setUp() throws Exception {
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return;

		super.setUp();
		// remembering preferences changed in test
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(JSchCorePlugin.ID);
		PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME_value = preferences.getBoolean(
				IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME, false);
		KEY_SSH2HOME_value = preferences.get(IConstants.KEY_SSH2HOME, null);
	}

	protected void tearDown() throws Exception {
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return;

		super.tearDown();
		// restoring preferences changed in test
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(JSchCorePlugin.ID);
		preferences.putBoolean(
				IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME,
				PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME_value);
		if (KEY_SSH2HOME_value != null)
			preferences.put(IConstants.KEY_SSH2HOME, KEY_SSH2HOME_value);
		else
			preferences.remove(IConstants.KEY_SSH2HOME);
	}

	public void testChangeDefaultWin32SshHome() {
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return;

		String userHome = System
				.getProperty(IConstants.SYSTEM_PROPERTY_USER_HOME);

		String oldDefaultWin32SshHome = null;
		if (userHome != null) {
			oldDefaultWin32SshHome = userHome + File.separator
					+ IConstants.SSH_OLD_DEFAULT_WIN32_HOME;
			File file = new File(oldDefaultWin32SshHome);
			if (!file.exists()) {
				file.mkdir();
			}
		}

		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(JSchCorePlugin.ID);
		preferences.remove(IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME);
		preferences.remove(IConstants.KEY_SSH2HOME);

		// verify that the preference is not set
		assertFalse(preferences.getBoolean(
				IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME, false));

		PreferenceInitializer preferenceInitializer = new PreferenceInitializer();
		preferenceInitializer.initializeDefaultPreferences();

		// verify that the preference is set now
		assertTrue(preferences.getBoolean(
				IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME, false));
		assertEquals(oldDefaultWin32SshHome,
				preferences.get(IConstants.KEY_SSH2HOME, null));
	}

	public static Test suite() {
		return new TestSuite(PreferenceInitializerTest.class);
	}
}
