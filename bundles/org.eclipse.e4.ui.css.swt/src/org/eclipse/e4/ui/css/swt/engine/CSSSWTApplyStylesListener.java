/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
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


/**
 * Add SWT filter to the {@link Display} to apply styles when SWT widget is
 * resized or showed.
 */
public class CSSSWTApplyStylesListener {
	CSSEngine engine;
	public CSSSWTApplyStylesListener(Display display, final CSSEngine engine) {
		this.engine = engine;
		display.addListener(SWT.Skin, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (engine != null) {
					engine.applyStyles(event.widget, false);
				}
			}
		});
	}
	
}
