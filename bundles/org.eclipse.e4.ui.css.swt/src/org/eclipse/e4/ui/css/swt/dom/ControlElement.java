/*******************************************************************************
 * Copyright (c) 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
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

	protected boolean hasFocus = false;

	protected boolean hasMouseHover = false;

	private FocusListener focusListener = new FocusAdapter() {
		public void focusGained(FocusEvent e) {
			ControlElement.this.hasFocus = true;
			doApplyStyles();
		}

		public void focusLost(FocusEvent e) {
			ControlElement.this.hasFocus = false;
			doApplyStyles();
		}
	};

	// Create SWT MouseTrack listener
	private MouseTrackListener mouseHoverListener = new MouseTrackAdapter() {
		public void mouseEnter(MouseEvent e) {
			// mouse hover, apply styles
			// into the SWT control
			ControlElement.this.hasMouseHover = true;
			doApplyStyles();
		}

		public void mouseExit(MouseEvent e) {
			// mouse hover, apply styles
			ControlElement.this.hasMouseHover = false;
			doApplyStyles();

		}
	};

	public ControlElement(Control control, CSSEngine engine) {
		super(control, engine);
	}

	public void initialize() {
		super.initialize();

		if (!dynamicEnabled) return; 
		
		Control control = getControl();

		// Add focus listener
		control.addFocusListener(focusListener);
		// Add mouse track listener
		control.addMouseTrackListener(mouseHoverListener);

	}

	public void dispose() {
		super.dispose();
		
		if (!dynamicEnabled) return; 
		
		Control control = getControl();
		if (!control.isDisposed()) {
			control.removeFocusListener(focusListener);
			control.removeMouseTrackListener(mouseHoverListener);
		}
	}

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

}
