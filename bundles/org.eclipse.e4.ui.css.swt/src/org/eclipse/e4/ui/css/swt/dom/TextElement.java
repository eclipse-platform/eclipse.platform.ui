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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;


/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Text}.
 *
 */
public class TextElement extends ControlElement {

	ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			doApplyStyles();
		}
	};

	public TextElement(Text text, CSSEngine engine) {
		super(text, engine);
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!dynamicEnabled) {
			return;
		}

		Text text = getText();
		text.addModifyListener(modifyListener);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (!dynamicEnabled) {
			return;
		}

		Text text = getText();
		if (!text.isDisposed()) {
			text.removeModifyListener(modifyListener);
		}
	}

	protected Text getText() {
		return (Text) getNativeWidget();
	}
}
