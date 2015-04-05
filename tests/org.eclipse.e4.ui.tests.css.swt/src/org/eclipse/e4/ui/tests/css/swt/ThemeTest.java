/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.e4.ui.css.swt.internal.theme.Theme;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ThemeTest extends CSSSWTTestCase {
	private BundleContext context;
	private ServiceRegistration<EventHandler> themeListenerRegistration;
	private ServiceReference<IThemeManager> themeManagerReference;

	@Override
	@Before
	public void setUp() {
		Bundle b = FrameworkUtil.getBundle(this.getClass());
		assertNotNull("Not running in an OSGi environment", b);
		context = b.getBundleContext();
		assertNotNull("Not running in an OSGi environment", b);
		themeManagerReference = context
				.getServiceReference(IThemeManager.class);
	}

	@Override
	@After
	public void tearDown() {
		themeListenerRegistration.unregister();
		super.tearDown();
	}

	@Test
	public void testThemeChangeNotification() {
		// we don't call createEngine() as ThemeEngine creates its own engine

		final Display display = Display.getDefault();
		final IThemeEngine themer = getThemeEngine(display);

		final boolean success[] = new boolean[] { false };

		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC,
				IThemeEngine.Events.THEME_CHANGED);
		themeListenerRegistration = context.registerService(EventHandler.class, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				ITheme theme = (ITheme)event.getProperty(IThemeEngine.Events.THEME);
				success[0] = IThemeEngine.Events.THEME_CHANGED.equals(event.getTopic())
						&& theme != null
						&& theme.getId().equals("test")
						&& event.getProperty(IThemeEngine.Events.DEVICE) == display
						&& event.getProperty(IThemeEngine.Events.THEME_ENGINE) == themer
						&& event.getProperty(IThemeEngine.Events.RESTORE) == Boolean.TRUE;
			}}, properties);

		assertFalse(success[0]);
		themer.setTheme(new Theme("test", "Test"), true);
		assertTrue(success[0]);
	}

	private IThemeEngine getThemeEngine(Display display) {
		IThemeManager manager = context.getService(themeManagerReference);
		assertNotNull("Theme manager service not available", manager);
		return manager.getEngineForDisplay(display);
	}

}
