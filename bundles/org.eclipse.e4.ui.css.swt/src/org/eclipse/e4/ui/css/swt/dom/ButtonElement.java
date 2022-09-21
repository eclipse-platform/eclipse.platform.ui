/*******************************************************************************
 * Copyright (c) 2009, 2017 Angelo Zerr and others.
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
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 513300
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTImageHelper;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Button}.
 *
 */
public class ButtonElement extends ControlElement {

	private boolean isSelected = false;

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!e.widget.isDisposed()) {
				ButtonElement.this.isSelected = getButton().getSelection();
				doApplyStyles();
			}
		}
	};

	public ButtonElement(Button button, CSSEngine engine) {
		super(button, engine);
		this.isSelected = button.getSelection();
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!dynamicEnabled) {
			return;
		}


		Button button = getButton();
		button.addSelectionListener(selectionListener);
	}

	@Override
	public void dispose() {

		super.dispose();

		if (!dynamicEnabled) {
			return;
		}

		Button button = getButton();
		if (!button.isDisposed()) {
			button.removeSelectionListener(selectionListener);
		}
	}

	@Override
	public boolean isPseudoInstanceOf(String s) {
		if ("checked".equalsIgnoreCase(s)) {
			return this.isSelected;
		}
		return super.isPseudoInstanceOf(s);
	}

	protected Button getButton() {
		return (Button) getNativeWidget();
	}

	@Override
	public void reset() {
		CSSSWTImageHelper.restoreDefaultImage(getButton());
		super.reset();
	}

}
