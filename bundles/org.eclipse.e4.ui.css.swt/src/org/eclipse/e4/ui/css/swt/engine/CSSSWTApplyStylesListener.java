/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Add SWT filter to the {@link Display} to apply styles when SWT widget is
 * resized or showed.
 */
public class CSSSWTApplyStylesListener {

	private final static String WIDGET_ALREADY_ADDED = "WIDGET_ALREADY_ADDED";

	private CSSEngine engine;

	public CSSSWTApplyStylesListener(Display display, CSSEngine engine) {
		this.engine = engine;
		display.addFilter(SWT.Resize, new ResizeListener());
	}

	private final boolean isWidgetAlreadyAdded(Widget widget) {
		if (widget == null)
			return true;
		return widget.getData(WIDGET_ALREADY_ADDED) != null;
	}

	private class ResizeListener implements Listener {

		public void handleEvent(Event event) {
			// On resize, apply styles (on the first resize)
			Widget widget = event.widget;
			if (!isWidgetAlreadyAdded(widget)) {
				widget.setData(WIDGET_ALREADY_ADDED, WIDGET_ALREADY_ADDED);
				if (engine != null) {
					engine.applyStyles(widget, false);
				}
			}
		}
	}
}
