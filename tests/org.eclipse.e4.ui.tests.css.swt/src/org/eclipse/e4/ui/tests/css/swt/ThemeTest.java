/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.UserScope;
import org.eclipse.e4.ui.css.swt.internal.theme.Theme;
import org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ThemeTest {

	private static final String THEMEID_KEY = "themeid";

	@RegisterExtension
	CssSwtEngine css = new CssSwtEngine();

	private BundleContext context;
	private ServiceRegistration<EventHandler> themeListenerRegistration;
	private ServiceReference<IThemeManager> themeManagerReference;

	@BeforeEach
	public void setUp() {
		Bundle b = FrameworkUtil.getBundle(this.getClass());
		assertNotNull(b, "Not running in an OSGi environment");
		context = b.getBundleContext();
		assertNotNull(b, "Not running in an OSGi environment");
		themeManagerReference = context
				.getServiceReference(IThemeManager.class);
	}

	@AfterEach
	public void tearDown() {
		if (themeListenerRegistration != null) {
			themeListenerRegistration.unregister();
		}
	}

	@Test
	void testThemeChangeNotification() {
		// we don't call createEngine() as ThemeEngine creates its own engine

		final Display display = Display.getDefault();
		final IThemeEngine themer = getThemeEngine(display);

		final boolean success[] = new boolean[] { false };

		Dictionary<String, String> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC,
				IThemeEngine.Events.THEME_CHANGED);
		themeListenerRegistration = context.registerService(EventHandler.class, event -> {
			ITheme theme = (ITheme)event.getProperty(IThemeEngine.Events.THEME);
			success[0] = IThemeEngine.Events.THEME_CHANGED.equals(event.getTopic())
					&& theme != null
					&& theme.getId().equals("test")
					&& event.getProperty(IThemeEngine.Events.DEVICE) == display
					&& event.getProperty(IThemeEngine.Events.THEME_ENGINE) == themer
					&& event.getProperty(IThemeEngine.Events.RESTORE) == Boolean.TRUE;
		}, properties);

		assertFalse(success[0]);
		themer.setTheme(new Theme("test", "Test"), true);
		assertTrue(success[0]);
	}

	@Test
	void testInheritedThemeIsReadableFromPreferenceService() {
		IThemeEngine themer = getThemeEngine(Display.getDefault());
		ITheme previousTheme = themer.getActiveTheme();
		IEclipsePreferences[] explicitNodes = { InstanceScope.INSTANCE.getNode(ThemeEngine.THEME_PLUGIN_ID),
				ConfigurationScope.INSTANCE.getNode(ThemeEngine.THEME_PLUGIN_ID),
				UserScope.INSTANCE.getNode(ThemeEngine.THEME_PLUGIN_ID) };
		String[] previousIds = new String[explicitNodes.length];

		// an inherited theme is applied without any scope recording an explicit choice
		for (int i = 0; i < explicitNodes.length; i++) {
			previousIds[i] = explicitNodes[i].get(THEMEID_KEY, null);
			explicitNodes[i].remove(THEMEID_KEY);
		}
		try {
			themer.setTheme(new Theme("inherited.test", "Inherited"), false);

			assertEquals("inherited.test",
					Platform.getPreferencesService().getString(ThemeEngine.THEME_PLUGIN_ID, THEMEID_KEY, null, null));
			assertNull(explicitNodes[0].get(THEMEID_KEY, null), "an inherited theme must not be persisted");
		} finally {
			if (previousTheme != null) {
				themer.setTheme(previousTheme, false);
			}
			for (int i = 0; i < explicitNodes.length; i++) {
				if (previousIds[i] != null) {
					explicitNodes[i].put(THEMEID_KEY, previousIds[i]);
				}
			}
		}
	}

	private IThemeEngine getThemeEngine(Display display) {
		IThemeManager manager = context.getService(themeManagerReference);
		assertNotNull(manager, "Theme manager service not available");
		return manager.getEngineForDisplay(display);
	}

}
