/*******************************************************************************
 * Copyright (c) 2009, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTCursorHelper;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.properties.GradientBackgroundListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Control}.
 *
 */
public class ControlElement extends WidgetElement {
	private static final String WEBSITE_CLASS = "org.eclipse.swt.browser.WebSite";

	protected boolean hasFocus = false;

	protected boolean hasMouseHover = false;

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusGained(FocusEvent e) {
			ControlElement.this.hasFocus = true;
			doApplyStyles();
		}

		@Override
		public void focusLost(FocusEvent e) {
			ControlElement.this.hasFocus = false;
			doApplyStyles();
		}
	};

	// Create SWT MouseTrack listener
	private MouseTrackListener mouseHoverListener = new MouseTrackAdapter() {
		@Override
		public void mouseEnter(MouseEvent e) {
			// mouse hover, apply styles
			// into the SWT control
			ControlElement.this.hasMouseHover = true;
			doApplyStyles();
		}

		@Override
		public void mouseExit(MouseEvent e) {
			// mouse hover, apply styles
			ControlElement.this.hasMouseHover = false;
			doApplyStyles();

		}
	};

	public ControlElement(Control control, CSSEngine engine) {
		super(control, engine);
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!dynamicEnabled) {
			return;
		}

		Control control = getControl();

		// Add focus listener
		control.addFocusListener(focusListener);
		// Add mouse track listener
		control.addMouseTrackListener(mouseHoverListener);

	}

	@Override
	public void dispose() {
		super.dispose();

		if (!dynamicEnabled) {
			return;
		}

		Control control = getControl();
		if (!control.isDisposed()) {
			control.removeFocusListener(focusListener);
			control.removeMouseTrackListener(mouseHoverListener);
		}
	}

	@Override
	public boolean isPseudoInstanceOf(String s) {
		if ("focus".equals(s)) {
			return this.hasFocus;
		}
		if ("hover".equals(s)) {
			return this.hasMouseHover;
		}
		if ("enabled".equals(s)) {
			return getControl().getEnabled();
		}
		if ("disabled".equals(s)) {
			return getControl().getEnabled();
		}
		if ("visible".equals(s)) {
			return getControl().getVisible();
		}
		return super.isPseudoInstanceOf(s);
	}

	@Override
	public Node getParentNode() {
		Control control = getControl();
		Composite parent = control.getParent();
		if (parent != null) {
			Element element = getElement(parent);
			return element;
		}
		return null;
	}

	protected Control getControl() {
		return (Control) getNativeWidget();
	}

	@Override
	public void reset() {
		Control control = getControl();
		CSSSWTFontHelper.restoreDefaultFont(control);
		CSSSWTCursorHelper.restoreDefaultCursor(control);
		GradientBackgroundListener.remove(control);
		if (control.getBackgroundImage() != null) {
			control.setBackgroundImage(null);
		}

		if (WEBSITE_CLASS.equals(control.getClass().getName())) {
			control.setBackground(control.getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND));
			control.setForeground(control.getDisplay().getSystemColor(
					SWT.COLOR_LINK_FOREGROUND));
		} else {
			control.setBackground(null);
			control.setForeground(null);
		}
		super.reset();
	}

}
