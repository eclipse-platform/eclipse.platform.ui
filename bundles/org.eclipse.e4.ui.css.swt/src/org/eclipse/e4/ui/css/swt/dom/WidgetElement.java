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
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.css.swt.helpers.SWTStyleHelpers;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Widget}.
 * 
 */
public class WidgetElement extends ElementAdapter implements NodeList {
	
	boolean dynamicEnabled = Boolean
			.getBoolean("org.eclipse.e4.ui.css.dynamic");
	
	/**
	 * Convenience method for getting the CSS class of a widget.
	 * 
	 * @param widget
	 *            SWT widget with associated CSS class name
	 * @return CSS class name
	 */
	public static String getCSSClass(Widget widget) {
		return (String) widget.getData(CSSSWTConstants.CSS_CLASS_NAME_KEY);
	}

	/**
	 * Convenience method for getting the CSS ID of a widget.
	 * 
	 * @param widget
	 *            SWT widget with associated CSS id
	 * @return CSS ID
	 */
	public static String getID(Widget widget) {
		return (String) widget.getData(CSSSWTConstants.CSS_ID_KEY);
	}

	/**
	 * Convenience method for setting the CSS class of a widget.
	 * 
	 * @param widget
	 *            SWT widget with associated CSS class name
	 * @param className
	 *            class name to set
	 */
	public static void setCSSClass(Widget widget, String className) {
		widget.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, className);
	}

	/**
	 * Convenience method for setting the CSS ID of a widget.
	 * 
	 * @param widget
	 *            SWT widget with associated CSS id
	 * @param id
	 *            CSS id to set
	 */
	public static void setID(Widget widget, String id) {
		widget.setData(CSSSWTConstants.CSS_ID_KEY, id);
	}
	
	/**
	 * Convenience method for getting the CSS engine responsible for a widget.
	 * @param widget SWT widget which is styled by an engine
	 */
	public static CSSEngine getEngine(Widget widget) {
		return getEngine(widget.getDisplay());
	}

	/**
	 * Convenience method for getting the CSS engine responsible for a widget.
	 * 
	 * @param display
	 *            SWT display which is styled by an engine
	 */
	public static CSSEngine getEngine(Display display) {
		return (CSSEngine) display.getData(CSSSWTConstants.CSS_ENGINE_KEY);
	}

	/**
	 * Convenience method for setting the CSS engine responsible for a display.
	 * @param widget SWT display which is styled by an engine
	 * @param engine Engine to be associated with the display
	 */
	public static void setEngine(Display display, CSSEngine engine) {
		display.setData(CSSSWTConstants.CSS_ENGINE_KEY, engine);
	}

	protected String localName;

	protected String namespaceURI;

	protected String swtStyles;

	public WidgetElement(Widget widget, CSSEngine engine) {
		super(widget, engine);
		this.localName = computeLocalName();
		this.namespaceURI = computeNamespaceURI();
		this.computeStaticPseudoInstances();
		this.swtStyles = this.computeAttributeSWTStyle();
	}

	/**
	 * Compute local name.
	 * 
	 * @return
	 */
	protected String computeLocalName() {
		// The localName is simple class name
		// of the SWT widget. For instance
		// for the org.eclipse.swt.widgets.Label
		// localName is Label
		// CSS selector will use this localName
		// ex : Label {background-color:red;}
		Widget widget = getWidget();
		Class clazz = widget.getClass();
		return ClassUtils.getSimpleName(clazz);
	}

	/**
	 * Compute namespaceURI.
	 * 
	 * @return
	 */
	protected String computeNamespaceURI() {
		// The NamespaceURI is package name
		// of the SWT widget. For instance
		// for the org.eclipse.swt.widgets.Label
		// namespaceURI is org.eclipse.swt.widgets.Label
		// CSS selector will use this localName
		// @namespace eclipse org.eclipse.swt.widgets.Label
		// ex : eclipse|Label {background-color:red;}
		Widget widget = getWidget();
		Class clazz = widget.getClass();
		return ClassUtils.getPackageName(clazz);
	}

	/**
	 * Compute static pseudo instances.
	 * 
	 */
	protected void computeStaticPseudoInstances() {
		
	}

	/**
	 * Compute attribute SWT style.
	 * 
	 * @return
	 */
	protected String computeAttributeSWTStyle() {
		return SWTStyleHelpers.getSWTWidgetStyleAsString(getWidget());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.css.core.dom.ElementAdapter#getAttribute(java.lang.
	 * String)
	 */
	public String getAttribute(String attr) {
		Widget widget = getWidget();
		if (attr.equals("style")) {
			return swtStyles;
		} else if (attr.equals("class")) {
			String result = getCSSClass(widget);
			return result != null ? result : "";
		} else if ("swt-data-class".equals(attr)) {
			Object data = widget.getData();
			if (data == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			for (Class<?> clazz = data.getClass(); clazz != Object.class; sb
					.append(' ')) {
				sb.append(clazz.getName());
				clazz = clazz.getSuperclass();
			}
			return sb.toString();
		}
		Object o = widget.getData(attr.toLowerCase());
		if (o != null)
			return o.toString();
		try {
			//o = PropertyUtils.getProperty(widget, attr);
			if (o != null)
				return o.toString();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return "";
	}

	public String getLocalName() {
		return localName;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public Node getParentNode() {		
		return null;
	}

	public NodeList getChildNodes() {
		return this;
	}

	public int getLength() {	
		return 0;
	}

	public Node item(int index) {
		return null;
	}

	protected Widget getWidget() {
		return (Widget) getNativeWidget();
	}

	public String getCSSId() {
		Widget widget = getWidget();
		Object id = getID(widget);
		if (id != null)
			return id.toString();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.CSSStylableElement#getCSSClass()
	 */
	public String getCSSClass() {
		Widget widget = getWidget();
		Object id = getCSSClass(widget);
		if (id != null)
			return id.toString();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.CSSStylableElement#getCSSStyle()
	 */
	public String getCSSStyle() {
		Widget widget = getWidget();
		// TODO should have key in CSSSWT
		Object id = widget.getData("style");
		if (id != null)
			return id.toString();
		return null;
	}


}
