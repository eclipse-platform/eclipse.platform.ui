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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.e4.ui.css.swt.internal.theme.Theme;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ThemeTest extends CSSSWTTestCase {
	private BundleContext context;
	private ServiceRegistration<EventHandler> themeListenerRegistration;
	private ServiceReference<IThemeManager> themeManagerReference;

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();
		Bundle b = FrameworkUtil.getBundle(this.getClass());
		assertNotNull(b, "Not running in an OSGi environment");
		context = b.getBundleContext();
		assertNotNull(b, "Not running in an OSGi environment");
		themeManagerReference = context
				.getServiceReference(IThemeManager.class);
	}

	@Override
	@AfterEach
	public void tearDown() {
		if (themeListenerRegistration != null) {
			themeListenerRegistration.unregister();
		}
		super.tearDown();
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

	private IThemeEngine getThemeEngine(Display display) {
		IThemeManager manager = context.getService(themeManagerReference);
		assertNotNull(manager, "Theme manager service not available");
		return manager.getEngineForDisplay(display);
	}

	@Test
	void settingIsDarkToTrueShoulReportThemeIsDark() {
		Theme theme = new Theme("IdDoesntCare", "DescriptionDoesntCare");
		theme.setIsDark("true");
		assertTrue(theme.isDark(), "Theme should report to be dark");
	}

	@Test
	void settingIsDarkToFalseShoulReportThemeIsNotDark() {
		Theme theme = new Theme("IdDoesntCare", "DescriptionDoesntCare");
		theme.setIsDark("false");
		assertFalse(theme.isDark(), "Theme should report to be NOT dark");
	}

	@Test
	void settingIsDarkToNullShoulReportThemeIsNotDark() {
		Theme theme = new Theme("IdDoesntCare", "DescriptionDoesntCare");
		theme.setIsDark(null);
		assertFalse(theme.isDark(), "Theme should report to be NOT dark");
	}

	@Test
	void settingIsDarkToInvalidValueShoulReportThemeIsNotDark() {
		Theme theme = new Theme("IdDoesntCare", "DescriptionDoesntCare");
		theme.setIsDark("invalid");
		assertFalse(theme.isDark(), "Theme should report to be NOT dark");
	}

}
