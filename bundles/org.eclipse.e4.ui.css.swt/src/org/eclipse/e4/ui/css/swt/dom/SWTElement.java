/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.eclipse.e4.ui.css.core.utils.NumberUtils;
import org.eclipse.e4.ui.css.swt.CSSSWT;
import org.eclipse.e4.ui.css.swt.helpers.SWTStyleHelpers;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * w3c Element which wrap SWT widget.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class SWTElement extends ElementAdapter implements NodeList {

	protected String localName;

	protected String namespaceURI;

	protected String swtStyles;

	public SWTElement(Widget widget, CSSEngine engine) {
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
		Widget widget = getWidget();
		if (widget instanceof CTabFolder) {
			// it's CTabFolder. Set selected as static pseudo instance.
			// because this widget define methods
			// CTabFolder#setSelectionBackground (Color color)
			// which set background Color when a CTabItem is selected.
			super.addStaticPseudoInstance("selected");
		}
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
	 * @see org.eclipse.e4.ui.css.core.dom.ElementAdapter#getAttribute(java.lang.String)
	 */
	public String getAttribute(String attr) {
		Widget widget = getWidget();
		if (attr.equals("style")) {
			return swtStyles;
		}
		Object o = widget.getData(attr.toLowerCase());
		if (o != null)
			return o.toString();
		try {
			o = PropertyUtils.getProperty(widget, attr);
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
		Widget widget = getWidget();
		if (widget instanceof Control) {
			Control control = (Control) widget;
			Composite parent = control.getParent();
			if (parent != null) {
				Element element = getElement(parent);
				return element;
			}
		}
		return null;
	}

	public NodeList getChildNodes() {
		return this;
	}

	public int getLength() {
		Widget widget = getWidget();
		if (widget instanceof Composite) {
			return ((Composite) widget).getChildren().length;
		}
		return 0;
	}

	public Node item(int index) {
		Widget widget = getWidget();
		if (widget instanceof Composite) {
			Widget w = ((Composite) widget).getChildren()[index];
			return getElement(w);
		}
		return null;
	}

	protected Widget getWidget() {
		return (Widget) getNativeWidget();
	}

	public String getCSSId() {
		Widget widget = getWidget();
		Object id = CSSSWT.getID(widget);
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
		Object id = CSSSWT.getCSSClass(widget);
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
		//TODO should have key in CSSSWT
		Object id = widget.getData("style");
		if (id != null)
			return id.toString();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.ElementAdapter#isPseudoInstanceOf(java.lang.String)
	 */
	public boolean isPseudoInstanceOf(String s) {
		if ("enabled".equals(s)) {
			Control control = (Control) getNativeWidget();
			return control.isEnabled();
		}
		if ("disabled".equals(s)) {
			Control control = (Control) getNativeWidget();
			return !control.isEnabled();
		}
		if ("checked".equals(s)) {
			if (getNativeWidget() instanceof Button) {
				Button button = (Button) getNativeWidget();
				return button.getSelection();
			}
			return false;
		}
		if ("visible".equals(s)) {
			Control control = (Control) getNativeWidget();
			return !control.isVisible();
		}
		if ("focus".equals(s)) {
			Control control = (Control) getNativeWidget();
			if (control.isFocusControl()) {
				return control.getData("focusLost") == null;
			}
		}
		if ("hover".equals(s)) {
			Control control = (Control) getNativeWidget();
			return control.getData("mouseHover") != null;
		}
		if ("odd".equals(s)) {
			Object widget = getNativeWidget();
			if (widget instanceof TableItem) {
				TableItem tableItem = (TableItem) widget;
				int index = tableItem.getParent().indexOf(tableItem);
				return NumberUtils.isOdd(index);
			}
		}
		if ("even".equals(s)) {
			Object widget = getNativeWidget();
			if (widget instanceof TableItem) {
				TableItem tableItem = (TableItem) widget;
				int index = tableItem.getParent().indexOf(tableItem);
				return NumberUtils.isEven(index);
			}
		}
		return super.isPseudoInstanceOf(s);
	}

}