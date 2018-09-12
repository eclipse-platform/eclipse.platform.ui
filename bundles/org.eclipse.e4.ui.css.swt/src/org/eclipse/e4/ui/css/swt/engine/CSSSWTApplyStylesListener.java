/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;


/**
 * Add SWT filter to the {@link Display} to apply styles when SWT widget is
 * resized or showed.
 */
public class CSSSWTApplyStylesListener {
	CSSEngine engine;
	public CSSSWTApplyStylesListener(Display display, final CSSEngine engine) {
		this.engine = engine;
		display.addListener(SWT.Skin, event -> {
			if (engine != null) {
				engine.applyStyles(event.widget, false);
			}
		});
	}

}
