/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sopot Cela - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.swt.internal.gtk;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.EventHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

@SuppressWarnings("restriction")
public class DarkThemeProcessor {

	@Inject
	IEventBroker eventBroker;

	private EventHandler eventHandler;

	@PostConstruct
	public void intialize() {

		eventHandler = event -> {
			if (event == null) {
				return;
			}
			ITheme theme = (ITheme) event.getProperty("theme");
			final boolean isDark = theme.getId().contains("dark"); //$NON-NLS-1$
			Display display = (Display) event.getProperty(IThemeEngine.Events.DEVICE);

			// not using UISynchronize as this is specific to SWT/GTK
			// scenarios
			display.asyncExec(() -> OS.setDarkThemePreferred(isDark));
		};
		// using the IEventBroker explicitly because the @EventTopic annotation
		// is unpredictable with processors within the debugger
		eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED, eventHandler);
	}

	@PreDestroy
	public void cleanUp() {
		eventBroker.unsubscribe(eventHandler);
	}

}
