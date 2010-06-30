/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.dom.html.SWTHTMLElement;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

/**
 * SWT Helper to link w3c Element with SWT widget.
 */
public class SWTElementHelpers {

	public static final String SWT_ELEMENT_KEY = "org.eclipse.e4.ui.core.css.swt.dom.SWTElement.ELEMENT";
	public static final String SWT_NODELIST_KEY = "org.eclipse.e4.ui.core.css.swt.dom.SWTElement.NODELIST";

	private static final Class[] ELEMENT_CONSTRUCTOR_PARAM = { Widget.class,
			CSSEngine.class };

	/**
	 * Return the w3c Element linked to the SWT widget.
	 * 
	 * @param widget
	 * @return
	 */
	public static Element getElement(Widget widget, CSSEngine engine,
			Class classElement) throws NoSuchMethodException,
			InvocationTargetException, InstantiationException,
			IllegalAccessException {
		Constructor constructor = classElement
				.getConstructor(ELEMENT_CONSTRUCTOR_PARAM);
		Object[] o = { widget, engine };
		Element newElement = (Element) constructor.newInstance(o);
		return newElement;
	}

	/**
	 * Return the w3c Element linked to the SWT widget.
	 * 
	 * @param widget
	 * @return
	 */
	public static Element getElement(Widget widget, CSSEngine engine) {
		try {
			return getElement(widget, engine, WidgetElement.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the w3c Element linked to the SWT widget.
	 * 
	 * @param widget
	 * @return
	 */
	public static Element getHTMLElement(Widget widget, CSSEngine engine) {
		try {
			return getElement(widget, engine, SWTHTMLElement.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the SWT Control which is wrapped to the object
	 * <code>element</code>.
	 * 
	 * @param element
	 * @return
	 */
	public static Control getControl(Object element) {
		if (element instanceof Control) {
			return (Control) element;
		} else {
			if (element instanceof CSSStylableElement) {
				CSSStylableElement elt = (CSSStylableElement) element;
				Object widget = elt.getNativeWidget();
				if (widget instanceof Control)
					return (Control) widget;
			}
		}
		return null;
	}

	/**
	 * Return the SWT Widget which is wrapped to the object <code>element</code>.
	 * 
	 * @param element
	 * @return
	 */
	public static Widget getWidget(Object element) {
		if (element instanceof Widget) {
			return (Widget) element;
		} else {
			if (element instanceof CSSStylableElement) {
				CSSStylableElement elt = (CSSStylableElement) element;
				Object widget = elt.getNativeWidget();
				if (widget instanceof Widget)
					return (Widget) widget;
			}
		}
		return null;
	}
}
