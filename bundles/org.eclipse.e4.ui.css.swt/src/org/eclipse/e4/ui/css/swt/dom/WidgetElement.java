/*******************************************************************************
 * Copyright (c) 2008, 2016 Angelo Zerr and others.
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

import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.core.runtime.Assert;
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

	boolean dynamicEnabled = Boolean.getBoolean("org.eclipse.e4.ui.css.dynamic");

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
	 * Convenience method for requesting the CSS engine to re-apply styles to a
	 * widget.
	 *
	 * @param widget
	 *            widget to be restyled
	 * @param applyStylesToChildNodes
	 *            if true, apply styles to the child nodes
	 */
	public static void applyStyles(Widget widget,
			boolean applyStylesToChildNodes) {
		CSSEngine engine = getEngine(widget);
		if (engine != null) {
			engine.applyStyles(widget, applyStylesToChildNodes);
		}
	}

	/**
	 * Convenience method for setting the CSS engine responsible for a display.
	 *
	 * @param display
	 *            SWT display which is styled by an engine
	 * @param engine
	 *            Engine to be associated with the display
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

		// for inner classes, the hyphen is used, e.g., for Outer$Inner,
		// the selector is Outer-Inner {background-color:red;}
		Widget widget = getWidget();
		Class<?> clazz = widget.getClass();
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
		Class<?> clazz = widget.getClass();
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

	@Override
	public final String getAttribute(String attr) {
		Supplier<String> attribute = internalGetAttribute(attr);
		if (attribute != null) {
			String attributeValue = attribute.get();
			Assert.isNotNull(attributeValue);
			return attributeValue;
		}
		return "";
	}

	@Override
	public final boolean hasAttribute(String attr) {
		return internalGetAttribute(attr) != null;
	}

	/**
	 *
	 * The goal for {@link #internalGetAttribute(String)} is to share the code
	 * of {@link #hasAttribute(String)} and {@link #getAttribute(String)} and to
	 * keep the performance footprint for {@link #hasAttribute(String)} small.
	 * This shall be accomplished by:
	 * <ul>
	 * <li>The method shall only be a lookup for a supplier, no actual
	 * computation shall be made.
	 * <li>The result of the supplier must hold the requirements of the result
	 * of {@link #getAttribute(String)}. Especially it <strong>must not</strong>
	 * return {@code null}.
	 * <li>If the attribute isn't set on the widget, the method must return
	 * {@code null}.
	 * </ul>
	 *
	 *
	 * @param attr
	 *            the name of the attribute to look for.
	 * @return a supplier which will return the actual attribute value or
	 *         {@code null} if the attribute isn't set for the widget.
	 */
	protected Supplier<String> internalGetAttribute(String attr) {
		Widget widget = getWidget();
		switch(attr){
		case "style":
			return () -> swtStyles != null ? swtStyles : "";
		case "class":
			return () -> Objects.toString(getCSSClass(widget), "");
		case "swt-data-class":
			return () -> {
				Object data = widget.getData();
				if (data == null) {
					return "";
				}
				StringBuilder sb = new StringBuilder();
				for (Class<?> clazz = data.getClass(); clazz != Object.class; sb.append(' ')) {
					sb.append(clazz.getName());
					clazz = clazz.getSuperclass();
				}
				return sb.toString();
			};
		default:
			Object o = widget.getData(attr.toLowerCase());
			if (o != null) {
				return o::toString;
			}
		}

		return null;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public NodeList getChildNodes() {
		return this;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public Node item(int index) {
		return null;
	}

	protected Widget getWidget() {
		return (Widget) getNativeWidget();
	}

	@Override
	public String getCSSId() {
		Widget widget = getWidget();
		Object id = getID(widget);
		if (id != null) {
			return id.toString();
		}
		return null;
	}

	@Override
	public String getCSSClass() {
		Widget widget = getWidget();
		Object id = getCSSClass(widget);
		if (id != null) {
			return id.toString();
		}
		return null;
	}

	@Override
	public String getCSSStyle() {
		Widget widget = getWidget();
		// TODO should have key in CSSSWT
		Object id = widget.getData("style");
		if (id != null) {
			return id.toString();
		}
		return null;
	}

	/**
	 * Called by the CSS engine upon a CSS theme switch. Implementations
	 * should restore the default value so that the new theme can be applied to
	 * the application without restart
	 */

	public void reset() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getWidget();
	}
}
