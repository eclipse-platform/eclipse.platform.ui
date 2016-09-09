/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *     Red Hat Inc. (mistria) - Fixes suggested by FindBugs
 *     Red Hat Inc. (mistria) - Bug 413348: fix stream leak
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 428715
 *     Brian de Alwis (MTI) - Performance tweaks (Bug 430829)
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 479896
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 500402
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.ChildVisibilityAwareElement;
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
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.core.resources.ResourceRegistryKeyFactory;
import org.eclipse.e4.ui.css.core.util.impl.resources.ResourcesLocatorManager;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.eclipse.e4.ui.css.core.utils.StringUtils;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
	 * Archives are deliberately identified by exclamation mark in URLs
	 */
	private static final String ARCHIVE_IDENTIFIER = "!";

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

	private boolean parseImport;

	private ResourceRegistryKeyFactory keyFactory;

	public AbstractCSSEngine() {
		this(new DocumentCSSImpl());
	}

	public AbstractCSSEngine(ExtendedDocumentCSS documentCSS) {
		this.documentCSS = documentCSS;
		this.viewCSS = new ViewCSSImpl(documentCSS);
		keyFactory = new ResourceRegistryKeyFactory();
	}

	/*--------------- Parse style sheet -----------------*/

	@Override
	public StyleSheet parseStyleSheet(Reader reader) throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parseStyleSheet(source);
	}

	@Override
	public StyleSheet parseStyleSheet(InputStream stream) throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parseStyleSheet(source);
	}

	@Override
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
			if (rule.getType() != CSSRule.IMPORT_RULE) {
				break;
			}
			// processing an import CSS
			CSSImportRule importRule = (CSSImportRule) rule;
			URL url = null;
			if (importRule.getHref().startsWith("platform")) {
				url = FileLocator.resolve(new URL(importRule.getHref()));
			} else {
				Path p = new Path(source.getURI());
				IPath trim = p.removeLastSegments(1);
				boolean isArchive = source.getURI().contains(ARCHIVE_IDENTIFIER);
				url = FileLocator.resolve(new URL(trim.addTrailingSeparator()
						.toString() + ((CSSImportRule) rule).getHref()));
				File testFile = new File(url.getFile());
				if (!isArchive&&!testFile.exists()) {
					// look in platform default
					String path = getResourcesLocatorManager().resolve(
							(importRule).getHref());
					testFile = new File(new URL(path).getFile());
					if (testFile.exists()) {
						url = new URL(path);
					}
				}
			}
			InputStream stream = null;
			try {
				stream = url.openStream();
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
			} finally {
				if (stream != null) {
					stream.close();
				}
			}
		}

		// add remaining non import rules
		for (int i = counter; i < length; i++) {
			masterList.add(rules.item(i));
		}

		// final stylesheet
		CSSStyleSheetImpl s = new CSSStyleSheetImpl();
		s.setRuleList(masterList);
		if (!parseImport) {
			documentCSS.addStyleSheet(s);
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
		if (reader == null && stream == null) {
			throw new IOException(
					"CharacterStream or ByteStream cannot be null for the InputSource.");
		}
	}

	/*--------------- Parse style declaration -----------------*/

	@Override
	public CSSStyleDeclaration parseStyleDeclaration(String style)
			throws IOException {
		Reader reader = new StringReader(style);
		return parseStyleDeclaration(reader);
	}

	@Override
	public CSSStyleDeclaration parseStyleDeclaration(Reader reader)
			throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parseStyleDeclaration(source);
	}

	@Override
	public CSSStyleDeclaration parseStyleDeclaration(InputStream stream)
			throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parseStyleDeclaration(source);
	}

	@Override
	public CSSStyleDeclaration parseStyleDeclaration(InputSource source)
			throws IOException {
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		CSSStyleDeclaration styleDeclaration = parser
				.parseStyleDeclaration(source);
		return styleDeclaration;
	}

	/*--------------- Parse CSS Selector -----------------*/

	@Override
	public SelectorList parseSelectors(String selector) throws IOException {
		Reader reader = new StringReader(selector);
		return parseSelectors(reader);
	}

	@Override
	public SelectorList parseSelectors(Reader reader) throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parseSelectors(source);
	}

	@Override
	public SelectorList parseSelectors(InputStream stream) throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parseSelectors(source);
	}

	@Override
	public SelectorList parseSelectors(InputSource source) throws IOException {
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		SelectorList list = parser.parseSelectors(source);
		return list;
	}

	/*--------------- Parse CSS Property Value-----------------*/

	@Override
	public CSSValue parsePropertyValue(Reader reader) throws IOException {
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		return parsePropertyValue(source);
	}

	@Override
	public CSSValue parsePropertyValue(InputStream stream) throws IOException {
		InputSource source = new InputSource();
		source.setByteStream(stream);
		return parsePropertyValue(source);
	}

	@Override
	public CSSValue parsePropertyValue(String value) throws IOException {
		Reader reader = new StringReader(value);
		return parsePropertyValue(reader);
	}

	@Override
	public CSSValue parsePropertyValue(InputSource source) throws IOException {
		checkInputSource(source);
		CSSParser parser = makeCSSParser();
		return parser.parsePropertyValue(source);
	}

	/*--------------- Apply styles -----------------*/

	@Override
	public void applyStyles(Object element, boolean applyStylesToChildNodes) {
		applyStyles(element, applyStylesToChildNodes, computeDefaultStyle);
	}

	@Override
	public void applyStyles(Object element, boolean applyStylesToChildNodes,
			boolean computeDefaultStyle) {
		Element elt = getElement(element);
		if (elt != null) {
			if (!isVisible(elt)) {
				return;
			}

			/*
			 * Compute new Style to apply.
			 */
			CSSStyleDeclaration style = viewCSS.getComputedStyle(elt, null);
			if (computeDefaultStyle) {
				if (applyStylesToChildNodes) {
					this.computeDefaultStyle = computeDefaultStyle;
				}
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
				for (String pseudoInstance : pseudoInstances) {
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
				NodeList nodes = elt instanceof ChildVisibilityAwareElement
						? ((ChildVisibilityAwareElement) elt).getVisibleChildNodes() : elt.getChildNodes();
				if (nodes != null) {
					for (int k = 0; k < nodes.getLength(); k++) {
						applyStyles(nodes.item(k), applyStylesToChildNodes);
					}
					onStylesAppliedToChildNodes(elt, nodes);
				}
			}
		}

	}

	/**
	 * Allow the CSS engine to skip particular elements if they are not visible.
	 * Elements need to be restyled when they become visible.
	 *
	 * @param elt
	 * @return true if the element is visible, false if not visible.
	 */
	protected boolean isVisible(Element elt) {
		Node parentNode = elt.getParentNode();
		if (parentNode instanceof ChildVisibilityAwareElement) {
			NodeList l = ((ChildVisibilityAwareElement) parentNode)
					.getVisibleChildNodes();
			if (l != null) {
				for (int i = 0; i < l.getLength(); i++) {
					if (l.item(i) == elt) {
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	private void applyConditionalPseudoStyle(ExtendedCSSRule parentRule, String pseudoInstance, Object element, CSSStyleDeclaration styleWithPseudoInstance) {
		SelectorList selectorList = parentRule.getSelectorList();
		for (int j = 0; j < selectorList.getLength(); j++) {
			Selector item = selectorList.item(j);
			// search for conditional selectors
			ConditionalSelector conditional = null;
			if (item instanceof ConditionalSelector) {
				conditional = (ConditionalSelector) item;
			} else if (item instanceof DescendantSelector) {
				if (((DescendantSelector) item).getSimpleSelector() instanceof ConditionalSelector) {
					conditional = (ConditionalSelector) ((DescendantSelector) item).getSimpleSelector();
				} else if (((DescendantSelector) item).getAncestorSelector() instanceof ConditionalSelector) {
					conditional = (ConditionalSelector) ((DescendantSelector) item).getAncestorSelector();
				}
			}
			if (conditional != null) {
				Condition condition = conditional.getCondition();
				// we're only interested in attribute selector conditions
				AttributeCondition attr = null;
				if (condition instanceof AttributeCondition) {
					attr = (AttributeCondition) condition;
				} else if (condition instanceof CombinatorCondition) {
					if (((CombinatorCondition) condition).getSecondCondition() instanceof AttributeCondition) {
						attr = (AttributeCondition) ((CombinatorCondition) condition).getSecondCondition();
					} else if (((CombinatorCondition) condition).getFirstCondition() instanceof AttributeCondition) {
						attr = (AttributeCondition) ((CombinatorCondition) condition).getFirstCondition();
					}
				}
				if (attr != null) {
					String value = attr.getValue();
					if (value.equals(pseudoInstance)) {
						// if we match the pseudo, apply the style
						applyStyleDeclaration(element, styleWithPseudoInstance, pseudoInstance);
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
		if (element instanceof CSSStylableElement) {
			((CSSStylableElement) element).onStylesApplied(nodes);
		}
	}

	/*--------------- Apply style declaration -----------------*/

	@Override
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
					if (handlers2 == null) {
						handlers2 = new ArrayList<ICSSPropertyHandler2>();
					}
					if (!handlers2.contains(propertyHandler2)) {
						handlers2.add(propertyHandler2);
					}
				}
			} catch (Exception e) {
				if (throwError
						|| (!throwError && !(e instanceof UnsupportedPropertyException))) {
					handleExceptions(e);
				}
			}
		}
		if (handlers2 != null) {
			for (ICSSPropertyHandler2 handler2 : handlers2) {
				try {
					handler2.onAllCSSPropertiesApplyed(element, this, pseudo);
				} catch (Exception e) {
					handleExceptions(e);
				}
			}
		}
		if (avoidanceCacheInstalled) {
			currentCSSPropertiesApplyed = null;
		}

	}

	@Override
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			Reader reader) throws IOException {
		CSSStyleDeclaration style = parseStyleDeclaration(reader);
		this.applyStyleDeclaration(node, style, null);
		return style;
	}

	@Override
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			InputStream stream) throws IOException {
		CSSStyleDeclaration style = parseStyleDeclaration(stream);
		this.applyStyleDeclaration(node, style, null);
		return style;
	}

	@Override
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			InputSource source) throws IOException {
		CSSStyleDeclaration style = parseStyleDeclaration(source);
		this.applyStyleDeclaration(node, style, null);
		return style;
	}

	@Override
	public CSSStyleDeclaration parseAndApplyStyleDeclaration(Object node,
			String style) throws IOException {
		CSSStyleDeclaration styleDeclaration = parseStyleDeclaration(style);
		this.applyStyleDeclaration(node, styleDeclaration, null);
		return styleDeclaration;
	}

	/*--------------- Apply inline style -----------------*/

	@Override
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

	@Override
	public CSSStyleDeclaration getDefaultStyleDeclaration(Object element,
			String pseudoE) {
		return getDefaultStyleDeclaration(element, null, pseudoE);
	}

	public CSSStyleDeclaration getDefaultStyleDeclaration(Object widget,
			CSSStyleDeclaration newStyle, String pseudoE) {
		CSSStyleDeclaration style = null;
		for (ICSSPropertyHandlerProvider provider : propertyHandlerProviders) {
			try {
				style = provider.getDefaultCSSStyleDeclaration(this, widget,
						newStyle, pseudoE);
			} catch (Exception e) {
				handleExceptions(e);
			}
		}
		return style;
	}

	@Override
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
	@Override
	public ICSSPropertyHandler applyCSSProperty(Object element,
			String property,
			CSSValue value, String pseudo) throws Exception {
		if (currentCSSPropertiesApplyed != null
				&& currentCSSPropertiesApplyed.containsKey(property)) {
			// CSS Property was already applied, ignore it.
			return null;
		}

		element = getElement(element); // in case we're passed a node
		if ("inherit".equals(value.getCssText())) {
			// go to parent node
			Element actualElement = (Element) element;
			Node parentNode = actualElement.getParentNode();
			// get CSS property value
			String parentValueString = retrieveCSSProperty(parentNode,
					property, pseudo);
			// and convert it to a CSS value, overriding the "inherit" setting
			// with the parent value
			value = parsePropertyValue(parentValueString);
		}

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
							|| (!throwError && !(e instanceof UnsupportedPropertyException))) {
						handleExceptions(e);
					}
				}
			}
		}

		return null;
	}

	@Override
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
					if (!StringUtils.isEmpty(value)) {
						return value;
					}
				}
			}
		} catch (Exception e) {
			handleExceptions(e);
		}
		return null;
	}

	@Override
	public String[] getCSSCompositePropertiesNames(String property) {
		try {
			Collection<ICSSPropertyHandler> handlers = getCSSPropertyHandlers(property);
			if (handlers == null) {
				return null;
			}
			for (ICSSPropertyHandler handler : handlers) {
				if (handler instanceof ICSSPropertyCompositeHandler) {
					ICSSPropertyCompositeHandler compositeHandler = (ICSSPropertyCompositeHandler) handler;
					if (compositeHandler.isCSSPropertyComposite(property)) {
						return compositeHandler.getCSSPropertiesNames(property);
					}
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
	@Override
	public Collection<String> getCSSProperties(Object element) {
		Set<String> properties = new HashSet<String>();
		for (ICSSPropertyHandlerProvider provider : propertyHandlerProviders) {
			properties.addAll(provider.getCSSProperties(element));
		}
		return properties;
	}

	/*--------------- Dynamic pseudo classes -----------------*/

	@Override
	public IElementProvider getElementProvider() {
		return elementProvider;
	}

	@Override
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
	@Override
	public Element getElement(Object element) {
		Element elt = null;
		CSSElementContext elementContext = getCSSElementContext(element);
		if (elementContext != null) {
			if (!elementContext.elementMustBeRefreshed(elementProvider)) {
				return elementContext.getElement();
			}
		}
		if (element instanceof Element) {
			elt = (Element) element;
		} else if (elementProvider != null) {
			elt = elementProvider.getElement(element, this);
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
		if (elementsContext != null) {
			elementsContext.remove(widget);
		}
	}

	public Object getDocument() {
		return null;
	}

	@Override
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
		if (elementsContext == null) {
			elementsContext = new HashMap<Object, CSSElementContext>();
		}
		return elementsContext;
	}

	@Override
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
	@Override
	public void handleExceptions(Exception e) {
		if (errorHandler != null) {
			errorHandler.error(e);
		}
	}

	@Override
	public CSSErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Set the CSS Error Handler to manage exception.
	 */
	@Override
	public void setErrorHandler(CSSErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/*--------------- Resources Locator Manager -----------------*/

	@Override
	public IResourcesLocatorManager getResourcesLocatorManager() {
		if (resourcesLocatorManager == null) {
			return defaultResourcesLocatorManager;
		}
		return resourcesLocatorManager;
	}

	@Override
	public void setResourcesLocatorManager(
			IResourcesLocatorManager resourcesLocatorManager) {
		this.resourcesLocatorManager = resourcesLocatorManager;
	}

	/*--------------- Document/View CSS -----------------*/

	@Override
	public DocumentCSS getDocumentCSS() {
		return documentCSS;
	}

	@Override
	public ViewCSS getViewCSS() {
		return viewCSS;
	}

	@Override
	public void dispose() {
		reset();
		// Call dispose for each CSSStylableElement which was registered
		Collection<CSSElementContext> contexts = elementsContext.values();
		for (CSSElementContext context : contexts) {
			Element element = context.getElement();
			if (element instanceof CSSStylableElement) {
				((CSSStylableElement) element).dispose();
			}
		}
		// FIXME: should dispose element provider and the property handler
		// providers
		elementsContext = null;
		if (resourcesRegistry != null) {
			resourcesRegistry.dispose();
		}
	}

	@Override
	public void reset() {
		// Remove All Style Sheets
		documentCSS.removeAllStyleSheets();
	}

	/*--------------- Resources Registry -----------------*/

	@Override
	public IResourcesRegistry getResourcesRegistry() {
		return resourcesRegistry;
	}

	@Override
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

	@Override
	public void registerCSSValueConverter(ICSSValueConverter converter) {
		if (valueConverters == null) {
			valueConverters = new HashMap<Object, ICSSValueConverter>();
		}
		valueConverters.put(converter.getToType(), converter);
	}

	@Override
	public void unregisterCSSValueConverter(ICSSValueConverter converter) {
		if (valueConverters == null) {
			return;
		}
		valueConverters.remove(converter);
	}

	@Override
	public ICSSValueConverter getCSSValueConverter(Object toType) {
		if (valueConverters != null) {
			return valueConverters.get(toType);
		}
		return null;
	}

	@Override
	public Object convert(CSSValue value, Object toType, Object context)
			throws Exception {
		Object key = keyFactory.createKey(value);
		Object newValue = getResource(toType, key);

		if (newValue == null) {
			ICSSValueConverter converter = getCSSValueConverter(toType);
			if (converter != null) {
				newValue = converter.convert(value, this, context);
				// cache it
				registerResource(toType, key, newValue);
			}
		}
		return newValue;
	}

	private Object getResource(Object toType, Object key) {
		if (key != null && getResourcesRegistry() != null) {
			return getResourcesRegistry().getResource(toType, key);
		}
		return null;
	}

	private void registerResource(Object toType, Object key, Object resource) {
		if (key != null && resource != null && getResourcesRegistry() != null) {
			getResourcesRegistry().registerResource(toType, key, resource);
		}
	}

	@Override
	public String convert(Object value, Object toType, Object context)
			throws Exception {
		if (value == null) {
			return null;
		}
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

	protected void setResourceRegistryKeyFactory(
			ResourceRegistryKeyFactory keyFactory) {
		this.keyFactory = keyFactory;
	}
}
