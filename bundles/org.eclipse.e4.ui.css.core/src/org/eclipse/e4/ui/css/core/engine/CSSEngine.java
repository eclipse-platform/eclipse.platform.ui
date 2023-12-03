/*******************************************************************************
 * Copyright (c) 2008, 2020 Angelo Zerr and others.
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
package org.eclipse.e4.ui.css.core.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.stylesheets.StyleSheet;

/**
 * CSS Engine interface used to parse style sheet and apply styles to something
 * (UI SWT, UI Swing...).
 */
public interface CSSEngine {

	/*--------------- Parse style sheet -----------------*/

	/**
	 * Parse style sheet from Reader reader.
	 */
	StyleSheet parseStyleSheet(Reader reader) throws IOException;

	/**
	 * Parse style sheet from InputStream stream.
	 */
	StyleSheet parseStyleSheet(InputStream stream) throws IOException;

	/**
	 * Parse style sheet from InputSource source.
	 */
	StyleSheet parseStyleSheet(InputSource source) throws IOException;

	/*--------------- Parse style declaration -----------------*/

	/**
	 * Parse style declaration from String style.
	 */
	CSSStyleDeclaration parseStyleDeclaration(String style) throws IOException;

	/**
	 * Parse style declaration from Reader reader.
	 */
	CSSStyleDeclaration parseStyleDeclaration(Reader reader) throws IOException;

	/**
	 * Parse style declaration from InputStream stream.
	 */
	CSSStyleDeclaration parseStyleDeclaration(InputStream stream) throws IOException;

	/**
	 * Parse style declaration from InputSource source.
	 */
	CSSStyleDeclaration parseStyleDeclaration(InputSource source) throws IOException;

	/*--------------- Parse CSS Property Value-----------------*/

	/**
	 * Parse CSSValue from String value.
	 */
	CSSValue parsePropertyValue(String value) throws IOException;

	/**
	 * Parse CSSValue from InputStream stream.
	 */
	CSSValue parsePropertyValue(InputStream stream) throws IOException;

	/**
	 * Parse CSSValue from Reader reader.
	 */
	CSSValue parsePropertyValue(Reader reader) throws IOException;

	/**
	 * Parse CSSValue from InputSource source.
	 */
	CSSValue parsePropertyValue(InputSource source) throws IOException;

	/*--------------- Apply styles -----------------*/

	/**
	 * Parse Selectors from String value.
	 */
	SelectorList parseSelectors(String text) throws IOException;

	/**
	 * Parse Selectors from InputSource value.
	 */
	SelectorList parseSelectors(InputSource source) throws IOException;

	/**
	 * Parse Selectors from InputStream.
	 */
	SelectorList parseSelectors(InputStream stream) throws IOException;

	/**
	 * Parse Selectors from String value.
	 */
	SelectorList parseSelectors(Reader reader) throws IOException;

	/**
	 * Check if the <code>selector</code> matches the object <code>node</code>.
	 */
	boolean matches(Selector selector, Object node, String pseudo);

	/*--------------- Apply styles -----------------*/

	/**
	 * Apply styles to the Object node (SWT Text,...). If
	 * <code>applyStylesToChildNodes</code> is true, apply styles to the child
	 * nodes (ex : if node is SWT Composite, styles are applied to the child
	 * controls too).
	 */
	void applyStyles(Object node, boolean applyStylesToChildNodes);

	/**
	 * Apply styles to the Object node (SWT Text,...). If
	 * <code>applyStylesToChildNodes</code> is true, apply styles to the child
	 * nodes (ex : if node is SWT Composite, styles are applied to the child
	 * controls too). If <code>computeDefaultStyle</code> is true, default
	 * style is computed before apply styles.
	 */
	void applyStyles(Object node, boolean applyStylesToChildNodes, boolean computeDefaultStyle);

	/*--------------- Apply style declaration -----------------*/

	/**
	 * Apply style declaration to the object node.
	 */
	void applyStyleDeclaration(Object node, CSSStyleDeclaration style, String pseudo);

	/**
	 * Parse and apply style declaration from Reader reader.
	 */
	CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node, Reader reader) throws IOException;

	/**
	 * Parse and apply style declaration from InputStream stream.
	 */
	CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node, InputStream stream) throws IOException;

	/**
	 * Parse and apply style declaration from InputSource source.
	 */
	CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node, InputSource sourcee) throws IOException;

	/**
	 * Parse and apply style declaration from String style.
	 */
	CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node, String style) throws IOException;

	/*--------------- Apply inline style -----------------*/

	/**
	 * Apply inline style of the object node. If
	 * <code>applyStylesToChildNodes</code> is true, apply style inline to the
	 * child nodes (ex : if node is SWT Composite, styles are applied to the
	 * child controls too).
	 */
	void applyInlineStyle(Object node, boolean applyStylesToChildNodes) throws IOException;

	/**
	 * Return {@link CSSErrorHandler} used to handles exception error.
	 */
	CSSErrorHandler getErrorHandler();

	/**
	 * Register the {@link CSSErrorHandler} used to handles exception error.
	 */
	void setErrorHandler(CSSErrorHandler errorHandler);

	/*--------------- Resources -----------------*/

	/**
	 * Set the {@link IResourcesLocatorManager} to use manage resources.
	 */
	void setResourcesLocatorManager(IResourcesLocatorManager resourcesLocatorManager);

	/**
	 * Get the {@link IResourcesLocatorManager} to use manage resources.
	 */
	IResourcesLocatorManager getResourcesLocatorManager();

	/*--------------- Document/View CSS -----------------*/

	/**
	 * Return the {@link DocumentCSS} used to store {@link CSSStyleSheet}.
	 */
	DocumentCSS getDocumentCSS();

	/**
	 * Return the {@link ViewCSS} used to compute {@link CSSStyleDeclaration}.
	 */
	ViewCSS getViewCSS();

	/*--------------- w3c Element -----------------*/

	/**
	 * Get {@link IElementProvider} registered used to retrieve w3c
	 * {@link Element} which wrap native widget.
	 */
	IElementProvider getElementProvider();

	/**
	 * Set {@link IElementProvider} registered used to retrieve w3c
	 * {@link Element} which wrap native widget.
	 */
	void setElementProvider(IElementProvider elementProvider);

	/*--------------- CSS Property Handler -----------------*/

	/**
	 * Apply CSS property <code>property</code> to the <code>node</code>
	 * with <code>value</code>.
	 */
	ICSSPropertyHandler applyCSSProperty(Object node, String property, CSSValue value, String pseudo)
			throws Exception;

	/**
	 * Retrieve String of {@link CSSValue} of the CSS <code>property</code> of
	 * the <code>node</code>.
	 */
	String retrieveCSSProperty(Object node, String property, String pseudo);


	/*--------------- Default Style -----------------*/

	/**
	 * Apply initial style of the object node. If
	 * <code>applyStylesToChildNodes</code> is true, apply style inline to the
	 * child nodes (ex : if node is SWT Composite, styles are applied to the
	 * child controls too).
	 */
	void applyDefaultStyleDeclaration(Object node, boolean applyStylesToChildNodes);

	/**
	 * Get default {@link CSSStyleDeclaration} of the <code>node</code> for
	 * pseudo element <code>pseudoE</code> which can be null.
	 */
	CSSStyleDeclaration getDefaultStyleDeclaration(Object node, String pseudoE);

	/*--------------- Dispose/Reset -----------------*/

	/**
	 * Call reset and dispose all resources
	 */
	void dispose();

	/**
	 * Reset all style sheet registered into CSS Engine. This method must be
	 * called if you want parse and apply new StyleSheet and remove the old
	 * StyleSheet parsed.
	 */
	void reset();

	/*--------------- Resources Registry -----------------*/

	/**
	 * Get the {@link IResourcesRegistry} registered used to cache/dispose
	 * resources.
	 */
	IResourcesRegistry getResourcesRegistry();

	/**
	 * Register {@link IResourcesRegistry} used to cache/dispose resources.
	 */
	void setResourcesRegistry(IResourcesRegistry resourcesRegistry);

	/*--------------- CSS Value Converter -----------------*/

	/**
	 * Register CSSValue converter {@link ICSSValueConverter}.
	 */
	void registerCSSValueConverter(ICSSValueConverter converter);

	/**
	 * Unregister CSSValue converter {@link ICSSValueConverter}.
	 */
	void unregisterCSSValueConverter(ICSSValueConverter converter);

	/**
	 * Get CSSValue converter {@link ICSSValueConverter} which is enable to
	 * convert <code>toType</code> Object.
	 */
	ICSSValueConverter getCSSValueConverter(Object toType);

	/**
	 * Convert CSSValue into Object type of <code>toType</code>. (ex :
	 * convert CSSValue color:red into java.awt.Color). If
	 * {@link IResourcesRegistry} is registered into {@link CSSEngine} this
	 * method search before into cache of {@link IResourcesRegistry} if the
	 * Object was already converted.
	 *
	 * @param context
	 *            can be null. For SWT context is
	 *            org.eclipse.swt.widgets.Display.
	 */
	Object convert(CSSValue value, Object toType, Object context) throws Exception;

	/**
	 * Convert Object type of <code>toType</code> into CSSValue String.
	 */
	String convert(Object value, Object toType, Object context) throws Exception;

	/**
	 * Get the w3c {@link Element} which wrap Object <code>node</code>.
	 */
	Element getElement(Object node);

	/**
	 * Get the {@link CSSElementContext} context of the object <code>node</code>.
	 */
	CSSElementContext getCSSElementContext(Object node);

	/**
	 * Return the set of property names applicable to the provided node.
	 *
	 * @param node
	 *            the DOM node or an element
	 * @return the property names
	 */
	Collection<String> getCSSProperties(Object node);

	/**
	 * Return array of CSS property name of the CSS <code>property</code>.
	 */
	String[] getCSSCompositePropertiesNames(String property);

	/**
	 * Handle exceptions.
	 */
	void handleExceptions(Exception e);

	/**
	 * Reapply the styles to the objects managed by this engine.
	 */
	void reapply();

	/**
	 * Handle disposal of a styled widget.
	 *
	 * @param widget The widget that gets disposed.
	 */
	default void handleWidgetDisposed(Object widget) {
		// empty default implementation
	}
}
