/*******************************************************************************
 * Copyright (c) 2008, 2012 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.ExtendedCSSRule;
import org.eclipse.e4.ui.css.core.dom.ExtendedDocumentCSS;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParser;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyCompositeHandler;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2Delegate;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandlerProvider;
import org.eclipse.e4.ui.css.core.dom.properties.converters.ICSSValueConverter;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.core.exceptions.UnsupportedPropertyException;
import org.eclipse.e4.ui.css.core.impl.dom.CSSRuleListImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.DocumentCSSImpl;
import org.eclipse.e4.ui.css.core.impl.dom.ViewCSSImpl;
import org.eclipse.e4.ui.css.core.impl.sac.ExtendedSelector;
import org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers;
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.core.util.impl.resources.ResourcesLocatorManager;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.eclipse.e4.ui.css.core.utils.StringUtils;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.stylesheets.StyleSheet;

/**
 * Abstract CSS Engine manage style sheet parsing and store the
 * {@link CSSStyleSheet} into {@link DocumentCSS}.
 * 
 * To apply styles, call the {@link #applyStyles(Object, boolean, boolean)}
 * method. This method check if {@link ICSSPropertyHandler} is registered for
 * apply the CSS property.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractCSSEngine implements CSSEngine {

	/**
	 * Default {@link IResourcesLocatorManager} used to get InputStream, Reader
	 * resource like Image.
	 */
	private final static IResourcesLocatorManager defaultResourcesLocatorManager = ResourcesLocatorManager.INSTANCE;

	/**
	 * w3c {@link DocumentCSS}.
	 */
	private ExtendedDocumentCSS documentCSS;

	/**
	 * w3c {@link ViewCSS}.
	 */
	private ViewCSS viewCSS;

	/**
	 * {@link IElementProvider} used to retrieve w3c Element linked to the
	 * widget.
	 */
	private IElementProvider elementProvider;

	protected boolean computeDefaultStyle = false;

	private Map<Object, CSSElementContext> elementsContext = null;

	/**
	 * CSS Error Handler to intercept error while parsing, applying styles.
	 */
	private CSSErrorHandler errorHandler;

	private IResourcesLocatorManager resourcesLocatorManager;

	private IResourcesRegistry resourcesRegistry;

	/**
	 * An ordered list of ICSSPropertyHandlerProvider
	 */
	protected List<ICSSPropertyHandlerProvider> propertyHandlerProviders = new ArrayList<ICSSPropertyHandlerProvider>();

	private Map<String, String> currentCSSPropertiesApplyed;

	private boolean throwError;

	private Map<Object, ICSSValueConverter> valueConverters = null;

	protected HashMap widgetsMap = new HashMap();
	
	private boolean parseImport;
	
	public AbstractCSSEngine() {
		this(new DocumentCSSImpl());
	}

	public AbstractCSSEngine(ExtendedDocumentCSS documentCSS) {
		this.documentCSS = documentCSS;
		this.viewCSS = new ViewCSSImpl(documentCSS);
	}

	/*--------------- Parse style sheet -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleSheet(java.io.Reader)
	 */
	public StyleSheet parseStyleSheet(Reader reader) throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parseStyleSheet(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleSheet(java.io.InputStream)
	 */
	public StyleSheet parseStyleSheet(InputStream stream) throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parseStyleSheet(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleSheet(org.w3c.css.sac.InputSource)
	 */
	public StyleSheet parseStyleSheet(InputSource source) throws IOException {
		// Check that CharacterStream or ByteStream is not null
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		CSSStyleSheet styleSheet = parser.parseStyleSheet(source);
		
		CSSRuleList rules = styleSheet.getCssRules();
		int length = rules.getLength();
		CSSRuleListImpl masterList = new CSSRuleListImpl();
		int counter;
		for (counter = 0; counter < length; counter++) {
			CSSRule rule = rules.item(counter);
			if (rule.getType() !=  CSSRule.IMPORT_RULE) {
				break;
			}
			Path p = new Path(source.getURI());
			IPath trim = p.removeLastSegments(1);
		
			URL url = FileLocator.resolve(new URL(trim.addTrailingSeparator().toString() + ((CSSImportRule) rule).getHref()));
		    File testFile = new File(url.getFile());
		    if (!testFile.exists()) {
		    	//look in platform default
		    	String path = getResourcesLocatorManager().resolve(((CSSImportRule) rule).getHref());
		    	testFile = new File(new URL(path).getFile());
		    	if (testFile.exists()) {
		    		url = new URL(path);
		    	}
		    }
			InputStream stream = url.openStream();
			InputSource tempStream = new InputSource();
			tempStream.setURI(url.toString());
			tempStream.setByteStream(stream);
			parseImport = true;
			styleSheet = (CSSStyleSheet) this.parseStyleSheet(tempStream);
			parseImport = false;
			CSSRuleList tempRules = styleSheet.getCssRules();
			for (int j = 0; j < tempRules.getLength(); j++) {
				masterList.add(tempRules.item(j));
			}
		}
		
		//add remaining non import rules
		for (int i = counter; i < length; i++) {
			masterList.add(rules.item(i));
		}
		
		//final stylesheet
		CSSStyleSheetImpl s = new CSSStyleSheetImpl();
		s.setRuleList(masterList);
		if (documentCSS instanceof ExtendedDocumentCSS) {
			if (!parseImport) {
				documentCSS.addStyleSheet(s);
			}
		}
		return s;
	}

	/**
	 * Return true if <code>source</code> is valid and false otherwise.
	 * 
	 * @param source
	 * @throws IOException
	 */
	private void checkInputSource(InputSource source) throws IOException {
		Reader reader = source.getCharacterStream();
		InputStream stream = source.getByteStream();
		if (reader == null && stream == null)
			throw new IOException(
					"CharacterStream or ByteStream cannot be null for the InputSource.");
	}

	/*--------------- Parse style declaration -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleDeclaration(java.lang.String)
	 */
	public CSSStyleDeclaration parseStyleDeclaration(String style)
			throws IOException {
		Reader reader = new StringReader(style);
		return parseStyleDeclaration(reader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleDeclaration(java.io.Reader)
	 */
	public CSSStyleDeclaration parseStyleDeclaration(Reader reader)
			throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parseStyleDeclaration(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleDeclaration(java.io.InputStream)
	 */
	public CSSStyleDeclaration parseStyleDeclaration(InputStream stream)
			throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parseStyleDeclaration(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseStyleDeclaration(org.w3c.css.sac.InputSource)
	 */
	public CSSStyleDeclaration parseStyleDeclaration(InputSource source)
			throws IOException {
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		CSSStyleDeclaration styleDeclaration = parser
				.parseStyleDeclaration(source);
		return styleDeclaration;
	}

	/*--------------- Parse CSS Selector -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.core.css.engine.CSSEngine#parseSelectors(java.lang.
	 * String)
	 */
	public SelectorList parseSelectors(String selector) throws IOException {
		Reader reader = new StringReader(selector);
		return parseSelectors(reader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.core.css.engine.CSSEngine#parseSelectors(java.io.Reader
	 * )
	 */
	public SelectorList parseSelectors(Reader reader) throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parseSelectors(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseSelectors(java.io.
	 * InputStream)
	 */
	public SelectorList parseSelectors(InputStream stream) throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parseSelectors(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.core.css.engine.CSSEngine#parseSelectors(org.w3c.css
	 * .sac.InputSource)
	 */
	public SelectorList parseSelectors(InputSource source) throws IOException {
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		SelectorList list = parser.parseSelectors(source);
		return list;
	}

	/*--------------- Parse CSS Property Value-----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parsePropertyValue(java.io.Reader)
	 */
	public CSSValue parsePropertyValue(Reader reader) throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parsePropertyValue(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parsePropertyValue(java.io.InputStream)
	 */
	public CSSValue parsePropertyValue(InputStream stream) throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parsePropertyValue(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parsePropertyValue(java.lang.String)
	 */
	public CSSValue parsePropertyValue(String value) throws IOException {
		Reader reader = new StringReader(value);
		return parsePropertyValue(reader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parsePropertyValue(org.w3c.css.sac.InputSource)
	 */
	public CSSValue parsePropertyValue(InputSource source) throws IOException {
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		return parser.parsePropertyValue(source);
	}

	/*--------------- Apply styles -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.engine.CSSEngine#applyStyles(java.lang.Object,
	 *      boolean)
	 */
	public void applyStyles(Object element, boolean applyStylesToChildNodes) {
		applyStyles(element, applyStylesToChildNodes, computeDefaultStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.engine.CSSEngine#applyStyles(java.lang.Object,
	 *      boolean, boolean)
	 */
	public void applyStyles(Object element, boolean applyStylesToChildNodes,
			boolean computeDefaultStyle) {
		Element elt = getElement(element);
		if (elt != null) {
			/*
			 * Compute new Style to apply.
			 */
			CSSStyleDeclaration style = viewCSS.getComputedStyle(elt, null);
			if (computeDefaultStyle) {
				if (applyStylesToChildNodes)
					this.computeDefaultStyle = computeDefaultStyle;
				/*
				 * Apply default style.
				 */
				applyDefaultStyleDeclaration(element, false, style, null);
			}

			/*
			 * Manage static pseudo instances
			 */
			String[] pseudoInstances = getStaticPseudoInstances(elt);
			if (pseudoInstances != null) {
				// there are static pseudo instances definied, loop for it and
				// apply styles for each pseudo instance.
				for (int i = 0; i < pseudoInstances.length; i++) {
					String pseudoInstance = pseudoInstances[i];
					CSSStyleDeclaration styleWithPseudoInstance = viewCSS
							.getComputedStyle(elt, pseudoInstance);
					if (computeDefaultStyle) {
						/*
						 * Apply default style for the current pseudo instance.
						 */
						applyDefaultStyleDeclaration(element, false,
								styleWithPseudoInstance, pseudoInstance);
					}
					
					if (styleWithPseudoInstance != null) {
						CSSRule parentRule = styleWithPseudoInstance.getParentRule();
						if (parentRule instanceof ExtendedCSSRule) {
							applyConditionalPseudoStyle((ExtendedCSSRule) parentRule, pseudoInstance, element, styleWithPseudoInstance);
						} else {
//							applyStyleDeclaration(element, styleWithPseudoInstance,
//									pseudoInstance);	
							applyStyleDeclaration(elt, styleWithPseudoInstance, pseudoInstance);
						}
					}
				}
			}

			if (style != null) {
				//applyStyleDeclaration(element, style, null);
				applyStyleDeclaration(elt, style, null);
			}
			try {
				// Apply inline style
				applyInlineStyle(elt, false);
			} catch (Exception e) {
				handleExceptions(e);
			}

			if (applyStylesToChildNodes) {
				/*
				 * Style all children recursive.
				 */
				NodeList nodes = elt.getChildNodes();
				if (nodes != null) {
					for (int k = 0; k < nodes.getLength(); k++) {
						applyStyles(nodes.item(k), applyStylesToChildNodes);
					}
					onStylesAppliedToChildNodes(elt, nodes);
				}
			}
		}

	}
	
	private void applyConditionalPseudoStyle(ExtendedCSSRule parentRule, String pseudoInstance, Object element, CSSStyleDeclaration styleWithPseudoInstance) {
		SelectorList selectorList = parentRule.getSelectorList();
		for (int j = 0; j < selectorList.getLength(); j++) {
			Selector item = selectorList.item(j);
			// search for conditional selectors
			if (item instanceof ConditionalSelector) {
				Condition condition = ((ConditionalSelector) item).getCondition();
				// we're only interested in attribute selector conditions
				if (condition instanceof AttributeCondition) {
					String value = ((AttributeCondition) condition).getValue();
					if (value.equals(pseudoInstance)) {
						// if we match the pseudo, apply the style
						applyStyleDeclaration(element, styleWithPseudoInstance,
								pseudoInstance);
						return;
					}										
				}
			}
		}
	}

	protected String[] getStaticPseudoInstances(Element element) {
		if (element instanceof CSSStylableElement) {
			CSSStylableElement stylableElement = (CSSStylableElement) element;
			return stylableElement.getStaticPseudoInstances();
		}
		return null;
	}

	/**
	 * Callback method called when styles applied of <code>nodes</code>
	 * children of the <code>element</code>.
	 * 
	 * @param element
	 * @param nodes
	 */
	protected void onStylesAppliedToChildNodes(Element element, NodeList nodes) {
		if (element instanceof CSSStylableElement)
			((CSSStylableElement) element).onStylesApplied(nodes);
	}

	/*--------------- Apply style declaration -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#applyStyleDeclaration(java.lang.Object,
	 *      org.w3c.dom.css.CSSStyleDeclaration, java.lang.String)
	 */
	public void applyStyleDeclaration(Object element,
			CSSStyleDeclaration style, String pseudo) {
		// Apply style
		boolean avoidanceCacheInstalled = currentCSSPropertiesApplyed == null;
		if (avoidanceCacheInstalled) {
			currentCSSPropertiesApplyed = new HashMap<String, String>();
		}
		List<ICSSPropertyHandler2> handlers2 = null;
		for (int i = 0; i < style.getLength(); i++) {
			String property = style.item(i);
			CSSValue value = style.getPropertyCSSValue(property);
			try {
				ICSSPropertyHandler handler = this.applyCSSProperty(element,
						property, value, pseudo);
				ICSSPropertyHandler2 propertyHandler2 = null;
				if (handler instanceof ICSSPropertyHandler2) {
					propertyHandler2 = (ICSSPropertyHandler2) handler;
				} else {
					if (handler instanceof ICSSPropertyHandler2Delegate) {
						propertyHandler2 = ((ICSSPropertyHandler2Delegate) handler)
								.getCSSPropertyHandler2();
					}
				}
				if (propertyHandler2 != null) {
					if (handlers2 == null)
						handlers2 = new ArrayList<ICSSPropertyHandler2>();
					if (!handlers2.contains(propertyHandler2))
						handlers2.add(propertyHandler2);
				}
			} catch (Exception e) {
				if (throwError
						|| (!throwError && !(e instanceof UnsupportedPropertyException)))
					handleExceptions(e);
			}
		}
		if (handlers2 != null) {
			for (Iterator<ICSSPropertyHandler2> iterator = handlers2.iterator(); iterator
					.hasNext();) {
				ICSSPropertyHandler2 handler2 = iterator
						.next();
				try {
					handler2.onAllCSSPropertiesApplyed(element, this);
				} catch (Exception e) {
					handleExceptions(e);
				}
			}
		}
		if (avoidanceCacheInstalled) {
			currentCSSPropertiesApplyed = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseAndApplyStyleDeclaration(java.io.Reader,
	 *      java.lang.Object)
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			Reader reader) throws IOException {
		CSSStyleDeclaration style = parseStyleDeclaration(reader);
		this.applyStyleDeclaration(node, style, null);
		return style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseAndApplyStyleDeclaration(java.io.InputStream,
	 *      java.lang.Object)
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			InputStream stream) throws IOException {
		CSSStyleDeclaration style = parseStyleDeclaration(stream);
		this.applyStyleDeclaration(node, style, null);
		return style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseAndApplyStyleDeclaration(org.w3c.css.sac.InputSource,
	 *      java.lang.Object)
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			InputSource source) throws IOException {
		CSSStyleDeclaration style = parseStyleDeclaration(source);
		this.applyStyleDeclaration(node, style, null);
		return style;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#parseAndApplyStyleDeclaration(java.lang.Object,
	 *      java.lang.String)
	 */
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			String style) throws IOException {
		CSSStyleDeclaration styleDeclaration = parseStyleDeclaration(style);
		this.applyStyleDeclaration(node, styleDeclaration, null);
		return styleDeclaration;
	}

	/*--------------- Apply inline style -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.engine.CSSEngine#applyInlineStyle(java.lang.Object,
	 *      boolean)
	 */
	public void applyInlineStyle(Object node, boolean applyStylesToChildNodes)
			throws IOException {
		Element elt = getElement(node);
		if (elt != null) {
			if (elt instanceof CSSStylableElement) {
				CSSStylableElement stylableElement = (CSSStylableElement) elt;
				String style = stylableElement.getCSSStyle();
				if (style != null && style.length() > 0) {
					parseAndApplyStyleDeclaration(stylableElement
							.getNativeWidget(), style);
				}
			}
			if (applyStylesToChildNodes) {
				/*
				 * Style all children recursive.
				 */
				NodeList nodes = elt.getChildNodes();
				if (nodes != null) {
					for (int k = 0; k < nodes.getLength(); k++) {
						applyInlineStyle(nodes.item(k), applyStylesToChildNodes);
					}
				}
			}
		}
	}

	/*--------------- Initial Style -----------------*/

	public CSSStyleDeclaration getDefaultStyleDeclaration(Object element,
			String pseudoE) {
		return getDefaultStyleDeclaration(element, null, pseudoE);
	}

	public CSSStyleDeclaration getDefaultStyleDeclaration(Object widget,
			CSSStyleDeclaration newStyle, String pseudoE) {
		CSSStyleDeclaration style = null;
		for (Iterator<ICSSPropertyHandlerProvider> iterator = propertyHandlerProviders
				.iterator(); iterator
				.hasNext();) {
			ICSSPropertyHandlerProvider provider = iterator
					.next();
			try {
				style = provider.getDefaultCSSStyleDeclaration(this, widget,
						newStyle, pseudoE);
			} catch (Exception e) {
				handleExceptions(e);
			}
		}
		return style;
	}

	public void applyDefaultStyleDeclaration(Object element,
			boolean applyStylesToChildNodes) {
		applyDefaultStyleDeclaration(element, applyStylesToChildNodes, null,
				null);
	}

	public void applyDefaultStyleDeclaration(Object element,
			boolean applyStylesToChildNodes, CSSStyleDeclaration newStyle,
			String pseudoE) {
		// Initial styles must be computed or applied
		Element elt = getElement(element);
		if (elt != null) {
			if (elt instanceof CSSStylableElement) {
				CSSStylableElement stylableElement = (CSSStylableElement) elt;
				CSSStyleDeclaration oldDefaultStyleDeclaration = stylableElement
						.getDefaultStyleDeclaration(pseudoE);
				// CSSStyleDeclaration defaultStyleDeclaration =
				// computeDefaultStyleDeclaration(
				// stylableElement, newStyle);
				CSSStyleDeclaration defaultStyleDeclaration = getDefaultStyleDeclaration(
						element, newStyle, pseudoE);
				if (oldDefaultStyleDeclaration != null) {
					// Second apply styles, apply the initial style
					// before apply the new style
					try {
						throwError = false;
						applyStyleDeclaration(element, defaultStyleDeclaration,
								pseudoE);
					} finally {
						throwError = true;
					}
				}
			}
			if (applyStylesToChildNodes) {
				/*
				 * Style all children recursive.
				 */
				NodeList nodes = elt.getChildNodes();
				if (nodes != null) {
					for (int k = 0; k < nodes.getLength(); k++) {
						applyDefaultStyleDeclaration(nodes.item(k),
								applyStylesToChildNodes);
					}
					onStylesAppliedToChildNodes(elt, nodes);
				}
			}
		}
	}

	/**
	 * Delegates the handle method.
	 * 
	 * @param element
	 *            may be a widget or a node or some object
	 * @param property
	 * @param value
	 * @param pseudo
	 */
	public ICSSPropertyHandler applyCSSProperty(Object element,
			String property,
			CSSValue value, String pseudo) throws Exception {
		if (currentCSSPropertiesApplyed != null
				&& currentCSSPropertiesApplyed.containsKey(property)) {
			// CSS Property was already applied, ignore it.
			return null;
		}

		element = getElement(element); // in case we're passed a node
		for (ICSSPropertyHandlerProvider provider : propertyHandlerProviders) {
			Collection<ICSSPropertyHandler> handlers = provider
					.getCSSPropertyHandlers(element, property);
			if (handlers == null) {
				continue;
			}
			for (ICSSPropertyHandler handler : handlers) {
				try {
					boolean result = handler.applyCSSProperty(element,
							property,
							value, pseudo, this);
					if (result) {
						// Add CSS Property to flag that this CSS Property was
						// applied.
						if (currentCSSPropertiesApplyed != null) {
							currentCSSPropertiesApplyed.put(property, property);
						}
						return handler;
					}
				} catch (Exception e) {
					if (throwError
							|| (!throwError && !(e instanceof UnsupportedPropertyException)))
						handleExceptions(e);
				}
			}
		}

		return null;
	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo) {
		try {
			element = getElement(element); // in case we're passed a node
			for (ICSSPropertyHandlerProvider provider : propertyHandlerProviders) {
				Collection<ICSSPropertyHandler> handlers = provider
						.getCSSPropertyHandlers(element, property);
				if (handlers == null) {
					continue;
				}
				for (ICSSPropertyHandler handler : handlers) {
					String value = handler.retrieveCSSProperty(element,
							property, pseudo, this);
					if (!StringUtils.isEmpty(value))
						return value;
				}
			}
		} catch (Exception e) {
			handleExceptions(e);
		}
		return null;
	}

	public String[] getCSSCompositePropertiesNames(String property) {
		try {
			Collection<ICSSPropertyHandler> handlers = getCSSPropertyHandlers(property);
			if (handlers == null) {
				return null;
			}
			for (Iterator<ICSSPropertyHandler> iterator = handlers.iterator(); iterator
					.hasNext();) {
				ICSSPropertyHandler handler = iterator.next();
				if (handler instanceof ICSSPropertyCompositeHandler) {
					ICSSPropertyCompositeHandler compositeHandler = (ICSSPropertyCompositeHandler) handler;
					if (compositeHandler.isCSSPropertyComposite(property))
						return compositeHandler.getCSSPropertiesNames(property);
				}
			}
		} catch (Exception e) {
			handleExceptions(e);
		}
		return null;
	}

	protected Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			String property) throws Exception {
		Collection<ICSSPropertyHandler> handlers = new ArrayList<ICSSPropertyHandler>();
		for (ICSSPropertyHandlerProvider provider : propertyHandlerProviders) {
			Collection<ICSSPropertyHandler> h = provider
					.getCSSPropertyHandlers(property);
			if (handlers == null) {
				handlers = h;
			} else {
				handlers = new ArrayList<ICSSPropertyHandler>(handlers);
				handlers.addAll(h);
			}
		}
		return handlers;
	}

	/**
	 * Return the set of property names and handlers for the provided node.
	 * 
	 * @param node
	 * @return the property names and handlers
	 */
	public Collection<String> getCSSProperties(Object element) {
		Set<String> properties = new HashSet<String>();
		for (ICSSPropertyHandlerProvider provider : propertyHandlerProviders) {
			properties.addAll(provider.getCSSProperties(element));
		}
		return properties;
	}

	/*--------------- Dynamic pseudo classes -----------------*/

	public IElementProvider getElementProvider() {
		return elementProvider;
	}

	public void setElementProvider(IElementProvider elementProvider) {
		this.elementProvider = elementProvider;
		// this.elementsContext = null;
	}

	/**
	 * Return the w3c Element linked to the Object element.
	 * 
	 * @param element
	 * @return
	 */
	public Element getElement(Object element) {
		Element elt = null;
		CSSElementContext elementContext = getCSSElementContext(element);
		if (elementContext != null) {
			if (!elementContext.elementMustBeRefreshed(elementProvider)) {
				return elementContext.getElement();
			}
		}
		if (element instanceof Element)
			elt = (Element) element;
		else if (elementProvider != null) {
			elt = elementProvider.getElement(element, this);
		} else if (elementProvider == null) {
			Object tmp = widgetsMap.get(element.getClass().getName());
			Class parent = element.getClass();
			while (tmp == null && parent != Object.class) {
					parent = parent.getSuperclass();
					tmp = widgetsMap.get(parent.getName());
			}
			if(tmp != null && tmp instanceof IElementProvider) {
				elt = ((IElementProvider)tmp).getElement(element, this);
			}
		}
		if (elt != null) {
			if (elementContext == null) {
				elementContext = new CSSElementContextImpl();
				Object nativeWidget = getNativeWidget(element);
				hookNativeWidget(nativeWidget);
				getElementsContext().put(nativeWidget,
						elementContext);
			}
			elementContext.setElementProvider(elementProvider);
			elementContext.setElement(elt);
			if (elt instanceof CSSStylableElement) {
					// Initialize CSS stylable element
					((CSSStylableElement)elt).initialize();
			}

		}
		return elt;
	}

	/**
	 * Called when an element context is created for a native widget and
	 * registered with this engine. Subclasses should override and install
	 * a listener on the widget that will call {@link #handleWidgetDisposed(Object)}
	 * when the widget is disposed.
	 * <p>
	 * The default implementation of this method does nothing.
	 * </p> 
	 * 
	 * @param widget the native widget to hook 
	 */
	protected void hookNativeWidget(Object widget) {
	}
	
	/**
	 * Called when a widget is disposed. Removes the element context
	 * from the element contexts map and the widgets map. Overriding
	 * classes must call the super implementation.
	 */
	protected void handleWidgetDisposed(Object widget) {
		if (widgetsMap != null)
			widgetsMap.remove(widget);
		if (elementsContext != null)
			elementsContext.remove(widget);
	}

	public Object getDocument() {
		return null;
	}

	public CSSElementContext getCSSElementContext(Object element) {
		Object o = getNativeWidget(element);
		return getElementsContext().get(o);
	}

	public Object getNativeWidget(Object element) {
		Object o = element;
		if (element instanceof CSSStylableElement) {
			o = ((CSSStylableElement) o).getNativeWidget();
		}
		return o;
	}

	protected Map<Object, CSSElementContext> getElementsContext() {
		if (elementsContext == null)
			elementsContext = new HashMap<Object, CSSElementContext>();
		return elementsContext;
	}

	public boolean matches(Selector selector, Object element, String pseudoElt) {
		Element elt = getElement(element);
		if (elt == null) {
			return false;
		}
		if (selector instanceof ExtendedSelector) {
			ExtendedSelector extendedSelector = (ExtendedSelector) selector;
			return extendedSelector.match(elt, pseudoElt);
		} else {
			// TODO : selector is not batik ExtendedSelector,
			// Manage this case...
		}
		return false;
	}

	/*--------------- Error Handler -----------------*/

	/**
	 * Handle exceptions thrown while parsing, applying styles. By default this
	 * method call CSS Error Handler if it is initialized.
	 * 
	 */
	public void handleExceptions(Exception e) {
		if (errorHandler != null)
			errorHandler.error(e);
	}

	public CSSErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Set the CSS Error Handler to manage exception.
	 */
	public void setErrorHandler(CSSErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/*--------------- Resources Locator Manager -----------------*/

	public IResourcesLocatorManager getResourcesLocatorManager() {
		if (resourcesLocatorManager == null)
			return defaultResourcesLocatorManager;
		return resourcesLocatorManager;
	}

	public void setResourcesLocatorManager(
			IResourcesLocatorManager resourcesLocatorManager) {
		this.resourcesLocatorManager = resourcesLocatorManager;
	}

	/*--------------- Document/View CSS -----------------*/

	public DocumentCSS getDocumentCSS() {
		return documentCSS;
	}

	public ViewCSS getViewCSS() {
		return viewCSS;
	}

	public void dispose() {
		reset();
		// Call dispose for each CSSStylableElement which was registered
		Collection<CSSElementContext> contexts = elementsContext.values();
		for (Iterator<CSSElementContext> iterator = contexts.iterator(); iterator
				.hasNext();) {
			CSSElementContext context = iterator.next();
			Element element = context.getElement();
			if (element instanceof CSSStylableElement) {
				((CSSStylableElement) element).dispose();
			}
		}
		elementsContext = null;
		widgetsMap = null;
		if (resourcesRegistry != null)
			resourcesRegistry.dispose();
	}

	public void reset() {
		// Remove All Style Sheets
		((ExtendedDocumentCSS) documentCSS).removeAllStyleSheets();
	}

	/*--------------- Resources Registry -----------------*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.engine.CSSEngine#getResourcesRegistry()
	 */
	public IResourcesRegistry getResourcesRegistry() {
		return resourcesRegistry;
	}

	public void setResourcesRegistry(IResourcesRegistry resourcesRegistry) {
		this.resourcesRegistry = resourcesRegistry;
	}

	public void registerCSSPropertyHandlerProvider(
			ICSSPropertyHandlerProvider handlerProvider) {
		propertyHandlerProviders.add(handlerProvider);
	}

	public void unregisterCSSPropertyHandlerProvider(
			ICSSPropertyHandlerProvider handlerProvider) {
		propertyHandlerProviders.remove(handlerProvider);
	}

	/*--------------- CSS Value Converter -----------------*/

	public void registerCSSValueConverter(ICSSValueConverter converter) {
		if (valueConverters == null)
			valueConverters = new HashMap<Object, ICSSValueConverter>();
		valueConverters.put(converter.getToType(), converter);
	}

	public void unregisterCSSValueConverter(ICSSValueConverter converter) {
		if (valueConverters == null)
			return;
		valueConverters.remove(converter);
	}

	public ICSSValueConverter getCSSValueConverter(Object toType) {
		if (valueConverters != null) {
			return valueConverters.get(toType);
		}
		return null;
	}

	public Object convert(CSSValue value, Object toType, Object context)
			throws Exception {
		Object newValue = null;
		String key = CSSResourcesHelpers.getCSSValueKey(value);
		IResourcesRegistry resourcesRegistry = getResourcesRegistry();
		if (resourcesRegistry != null) {
			if (key != null)
				newValue = resourcesRegistry.getResource(toType, key);
		}
		if (newValue == null) {
			ICSSValueConverter converter = getCSSValueConverter(toType);
			if (converter != null) {
				newValue = converter.convert(value, this, context);
				if (newValue != null) {
					// cache it
					if (resourcesRegistry != null) {
						if (key != null)
							resourcesRegistry.registerResource(toType, key,
									newValue);
					}
				}
			}
		}
		return newValue;
	}

	public String convert(Object value, Object toType, Object context)
			throws Exception {
		if (value == null)
			return null;
		ICSSValueConverter converter = getCSSValueConverter(toType);
		if (converter != null) {
			return converter.convert(value, this, context);
		}
		return null;
	}

	/*--------------- Abstract methods -----------------*/

	/**
	 * Return instance of CSS Parser.
	 */
	public abstract CSSParser makeCSSParser();

}
