/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.html;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * w3c Element which wrap SWT widget to manage HTML/XUL selectors.
 */
public class SWTHTMLElement extends WidgetElement {

	protected String attributeType;

	public SWTHTMLElement(Widget widget, CSSEngine engine) {
		super(widget, engine);
		attributeType = computeAttributeType();
	}

	@Override
	public String getAttribute(String attr) {
		if ("type".equals(attr))
			return attributeType;
		return super.getAttribute(attr);
	}

	@Override
	protected String computeLocalName() {
		Widget widget = getWidget();
		// HTML name
		if (widget instanceof Text) {
			int style = widget.getStyle();
			if ((style | SWT.MULTI) == style)
				return "textarea";
			return "input";
		}
		if (widget instanceof Button)
			return "input";
		if (widget instanceof Combo)
			return "select";
		if (widget instanceof CCombo)
			return "select";
		if (widget instanceof Label)
			return "label";
		if (widget instanceof Shell)
			return "body";
		if (widget instanceof Canvas)
			return "canvas";
		if (widget instanceof Scrollable)
			return "div";
		if (widget instanceof List)
			return "body";
		if (widget instanceof Group)
			return "div";
		if (widget instanceof Link)
			return "a";
		if (widget instanceof Composite)
			return "div";
		// XUL name
		if (widget instanceof Tree)
			return "tree";
		if (widget instanceof Table)
			return "listbox";
		return super.computeLocalName();
	}

	protected String computeAttributeType() {
		Widget widget = getWidget();
		if (widget instanceof Button) {
			Button button = (Button) widget;
			int style = button.getStyle();
			if ((style | SWT.RADIO) == style)
				return "radio";
			if ((style | SWT.CHECK) == style)
				return "checkbox";
			return "button";
		}
		if (widget instanceof Text) {
			Text text = (Text) widget;
			if ((text.getStyle() & SWT.PASSWORD) != 0)
				return "password";
			else if ((text.getStyle() & SWT.MULTI) != 0)
				return "";
			else
				return "text";
		}
		return "";
	}

}
