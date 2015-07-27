/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *
 */
public interface CSSEngine {

	/*--------------- Parse style sheet -----------------*/

	/**
	 * Parse style sheet from Reader reader.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public StyleSheet parseStyleSheet(Reader reader) throws IOException;

	/**
	 * Parse style sheet from InputStream stream.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public StyleSheet parseStyleSheet(InputStream stream) throws IOException;

	/**
	 * Parse style sheet from InputSource source.
	 *
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public StyleSheet parseStyleSheet(InputSource source) throws IOException;

	/*--------------- Parse style declaration -----------------*/

	/**
	 * Parse style declaration from String style.
	 *
	 * @param style
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseStyleDeclaration(String style)
			throws IOException;

	/**
	 * Parse style declaration from Reader reader.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseStyleDeclaration(Reader reader)
			throws IOException;

	/**
	 * Parse style declaration from InputStream stream.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseStyleDeclaration(InputStream stream)
			throws IOException;

	/**
	 * Parse style declaration from InputSource source.
	 *
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseStyleDeclaration(InputSource source)
			throws IOException;

	/*--------------- Parse CSS Property Value-----------------*/

	/**
	 * Parse CSSValue from String value.
	 *
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public CSSValue parsePropertyValue(String value) throws IOException;

	/**
	 * Parse CSSValue from InputStream stream.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public CSSValue parsePropertyValue(InputStream stream) throws IOException;

	/**
	 * Parse CSSValue from Reader reader.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public CSSValue parsePropertyValue(Reader reader) throws IOException;

	/**
	 * Parse CSSValue from InputSource source.
	 *
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public CSSValue parsePropertyValue(InputSource source) throws IOException;

	/*--------------- Apply styles -----------------*/

	/**
	 * Parse Selectors from String value.
	 *
	 * @param text
	 * @return
	 * @throws IOException
	 */
	public SelectorList parseSelectors(String text) throws IOException;

	/**
	 * Parse Selectors from InputSource value.
	 *
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public SelectorList parseSelectors(InputSource source) throws IOException;

	/**
	 * Parse Selectors from InputStream.
	 *
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public SelectorList parseSelectors(InputStream stream) throws IOException;

	/**
	 * Parse Selectors from String value.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public SelectorList parseSelectors(Reader reader) throws IOException;

	/**
	 * Check if the <code>selector</code> matches the object <code>node</code>.
	 *
	 * @param selector
	 * @param element
	 * @param pseudo
	 * @return
	 */
	public boolean matches(Selector selector, Object node, String pseudo);

	/*--------------- Apply styles -----------------*/

	/**
	 * Apply styles to the Object node (SWT Text,...). If
	 * <code>applyStylesToChildNodes</code> is true, apply styles to the child
	 * nodes (ex : if node is SWT Composite, styles are applied to the child
	 * controls too).
	 *
	 * @param node
	 * @param applyStylesToChildNodes
	 */
	public void applyStyles(Object node, boolean applyStylesToChildNodes);

	/**
	 * Apply styles to the Object node (SWT Text,...). If
	 * <code>applyStylesToChildNodes</code> is true, apply styles to the child
	 * nodes (ex : if node is SWT Composite, styles are applied to the child
	 * controls too). If <code>computeDefaultStyle</code> is true, default
	 * style is computed before apply styles.
	 *
	 * @param node
	 * @param applyStylesToChildNodes
	 * @param computeDefaultStyle
	 */
	public void applyStyles(Object node, boolean applyStylesToChildNodes,
			boolean computeDefaultStyle);

	/*--------------- Apply style declaration -----------------*/

	/**
	 * Apply style declaration to the object node.
	 *
	 * @param node
	 * @param style
	 * @param pseudo
	 */
	public void applyStyleDeclaration(Object node, CSSStyleDeclaration style,
			String pseudo);

	/**
	 * Parse and apply style declaration from Reader reader.
	 *
	 * @param node
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			Reader reader) throws IOException;

	/**
	 * Parse and apply style declaration from InputStream stream.
	 *
	 * @param node
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			InputStream stream) throws IOException;

	/**
	 * Parse and apply style declaration from InputSource source.
	 *
	 * @param node
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			InputSource sourcee) throws IOException;

	/**
	 * Parse and apply style declaration from String style.
	 *
	 * @param node
	 * @param style
	 * @return
	 * @throws IOException
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			String style) throws IOException;

	/*--------------- Apply inline style -----------------*/

	/**
	 * Apply inline style of the object node. If
	 * <code>applyStylesToChildNodes</code> is true, apply style inline to the
	 * child nodes (ex : if node is SWT Composite, styles are applied to the
	 * child controls too).
	 *
	 * @param node
	 * @param applyStylesToChildNodes
	 * @throws IOException
	 */
	public void applyInlineStyle(Object node, boolean applyStylesToChildNodes)
			throws IOException;

	/**
	 * Return {@link CSSErrorHandler} used to handles exception error.
	 *
	 * @return
	 */
	public CSSErrorHandler getErrorHandler();

	/**
	 * Register the {@link CSSErrorHandler} used to handles exception error.
	 *
	 * @param errorHandler
	 */
	public void setErrorHandler(CSSErrorHandler errorHandler);

	/*--------------- Resources -----------------*/

	/**
	 * Set the {@link IResourcesLocatorManager} to use manage resources.
	 *
	 * @param resourcesLocatorManager
	 */
	public void setResourcesLocatorManager(
			IResourcesLocatorManager resourcesLocatorManager);

	/**
	 * Get the {@link IResourcesLocatorManager} to use manage resources.
	 *
	 * @return
	 */
	public IResourcesLocatorManager getResourcesLocatorManager();

	/*--------------- Document/View CSS -----------------*/

	/**
	 * Return the {@link DocumentCSS} used to store {@link CSSStyleSheet}.
	 *
	 * @return
	 */
	public DocumentCSS getDocumentCSS();

	/**
	 * Return the {@link ViewCSS} used to compute {@link CSSStyleDeclaration}.
	 *
	 * @return
	 */
	public ViewCSS getViewCSS();

	/*--------------- w3c Element -----------------*/

	/**
	 * Get {@link IElementProvider} registered used to retrieve w3c
	 * {@link Element} which wrap native widget.
	 *
	 * @return
	 */
	public IElementProvider getElementProvider();

	/**
	 * Set {@link IElementProvider} registered used to retrieve w3c
	 * {@link Element} which wrap native widget.
	 *
	 * @param elementProvider
	 */
	public void setElementProvider(IElementProvider elementProvider);

	/*--------------- CSS Property Handler -----------------*/

	/**
	 * Apply CSS property <code>property</code> to the <code>node</code>
	 * with <code>value</code>.
	 *
	 * @param node
	 * @param property
	 * @param value
	 * @param pseudo
	 * @return
	 * @throws Exception
	 */
	public ICSSPropertyHandler applyCSSProperty(Object node, String property,
			CSSValue value, String pseudo) throws Exception;

	/**
	 * Retrieve String of {@link CSSValue} of the CSS <code>property</code> of
	 * the <code>node</code>.
	 *
	 * @param widget
	 * @param property
	 * @param pseudo
	 * @return
	 */
	public String retrieveCSSProperty(Object node, String property, String pseudo);


	/*--------------- Default Style -----------------*/

	/**
	 * Apply initial style of the object node. If
	 * <code>applyStylesToChildNodes</code> is true, apply style inline to the
	 * child nodes (ex : if node is SWT Composite, styles are applied to the
	 * child controls too).
	 *
	 * @param node
	 * @param applyStylesToChildNodes
	 * @throws IOException
	 */
	public void applyDefaultStyleDeclaration(Object node,
			boolean applyStylesToChildNodes);

	/**
	 * Get default {@link CSSStyleDeclaration} of the <code>node</code> for
	 * pseudo element <code>pseudoE</code> which can be null.
	 *
	 * @param element
	 * @return
	 */
	public CSSStyleDeclaration getDefaultStyleDeclaration(Object node,
			String pseudoE);

	/*--------------- Dispose/Reset -----------------*/

	/**
	 * Call reset and dispose all resources
	 */
	public void dispose();

	/**
	 * Reset all style sheet registered into CSS Engine. This method must be
	 * called if you want parse and apply new StyleSheet and remove the old
	 * StyleSheet parsed.
	 */
	public void reset();

	/*--------------- Resources Registry -----------------*/

	/**
	 * Get the {@link IResourcesRegistry} registered used to cache/dispose
	 * resources.
	 *
	 * @return
	 */
	public IResourcesRegistry getResourcesRegistry();

	/**
	 * Register {@link IResourcesRegistry} used to cache/dispose resources.
	 *
	 * @param resourcesRegistry
	 */
	public void setResourcesRegistry(IResourcesRegistry resourcesRegistry);

	/*--------------- CSS Value Converter -----------------*/

	/**
	 * Register CSSValue converter {@link ICSSValueConverter}.
	 *
	 * @param converter
	 */
	public void registerCSSValueConverter(ICSSValueConverter converter);

	/**
	 * Unregister CSSValue converter {@link ICSSValueConverter}.
	 *
	 * @param converter
	 */
	public void unregisterCSSValueConverter(ICSSValueConverter converter);

	/**
	 * Get CSSValue converter {@link ICSSValueConverter} which is enable to
	 * convert <code>toType</code> Object.
	 *
	 * @param toType
	 * @return
	 */
	public ICSSValueConverter getCSSValueConverter(Object toType);

	/**
	 * Convert CSSValue into Object type of <code>toType</code>. (ex :
	 * convert CSSValue color:red into java.awt.Color). If
	 * {@link IResourcesRegistry} is registered into {@link CSSEngine} this
	 * method search before into cache of {@link IResourcesRegistry} if the
	 * Object was already converted.
	 *
	 * @param value
	 * @param toType
	 * @param context
	 *            can be null. For SWT context is
	 *            org.eclipse.swt.widgets.Display.
	 * @return
	 * @throws Exception
	 */
	public Object convert(CSSValue value, Object toType, Object context)
			throws Exception;

	/**
	 * Convert Object type of <code>toType</code> into CSSValue String.
	 *
	 * @param value
	 * @param toType
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public String convert(Object value, Object toType, Object context)
			throws Exception;

	/**
	 * Get the w3c {@link Element} which wrap Object <code>node</code>.
	 *
	 * @param node
	 * @return
	 */
	public Element getElement(Object node);

	/**
	 * Get the {@link CSSElementContext} context of the object <code>node</code>.
	 *
	 * @param node
	 * @return
	 */
	public CSSElementContext getCSSElementContext(Object node);

	/**
	 * Return the set of property names applicable to the provided node.
	 *
	 * @param node
	 *            the DOM node or an element
	 * @return the property names
	 */
	public Collection<String> getCSSProperties(Object node);

	/**
	 * Return array of CSS property name of the CSS <code>property</code>.
	 *
	 * @param property
	 * @return
	 */
	public String[] getCSSCompositePropertiesNames(String property);

	/**
	 * Handle exceptions.
	 *
	 * @param e
	 */
	public void handleExceptions(Exception e);

	/**
	 * Reapply the styles to the objects managed by this engine.
	 */
	public void reapply();
}
