/*******************************************************************************
 * Copyright (c) 2009, 2015 Angelo Zerr and others.
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
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Scale;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Scale}.
 */
public class ScaleElement extends ControlElement {

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			doApplyStyles();
		}
	};

	public ScaleElement(Scale scale, CSSEngine engine) {
		super(scale, engine);
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!dynamicEnabled) {
			return;
		}

		Scale scale = getScale();
		scale.addSelectionListener(selectionListener);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (!dynamicEnabled) {
			return;
		}

		Scale scale = getScale();
		if (!scale.isDisposed()) {
			scale.removeSelectionListener(selectionListener);
		}
	}


	protected Scale getScale() {
		return (Scale) getNativeWidget();
	}
}
