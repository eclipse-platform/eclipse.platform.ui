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
package org.eclipse.e4.ui.css.core.impl.engine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.css.core.dom.ExtendedDocumentCSS;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParser;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParserFactory;
import org.eclipse.e4.ui.css.core.dom.parsers.ICSSParserFactory;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.converters.CSSValueBooleanConverterImpl;
import org.eclipse.e4.ui.css.core.dom.properties.providers.CSSPropertyHandlerLazyProviderImpl;
import org.eclipse.e4.ui.css.core.dom.properties.providers.CSSPropertyHandlerSimpleProviderImpl;
import org.eclipse.e4.ui.css.core.impl.sac.CSSConditionFactoryImpl;
import org.eclipse.e4.ui.css.core.impl.sac.CSSSelectorFactoryImpl;
import org.w3c.css.sac.ConditionFactory;

public abstract class CSSEngineImpl extends AbstractCSSEngine {

	/* the original extension point was misspelled */
	private static final String DEPRECATED_ELEMENT_PROVIDER_EXTPOINT = "org.eclipse.e4.u.css.core.elementProvider";
	private static final String ELEMENT_PROVIDER_EXTPOINT = "org.eclipse.e4.ui.css.core.elementProvider";

	public static final ConditionFactory CONDITIONFACTORY_INSTANCE = new CSSConditionFactoryImpl(
			null, "class", null, "id");

	private CSSPropertyHandlerSimpleProviderImpl handlerProvider = null;

	private CSSPropertyHandlerLazyProviderImpl lazyHandlerProvider = null;
	private IExtensionRegistry registry;

	public CSSEngineImpl() {
		super();

		registry = RegistryFactory.getRegistry();
		if (configureElementProviders(DEPRECATED_ELEMENT_PROVIDER_EXTPOINT)) {
			System.err.println("Extension point "
					+ DEPRECATED_ELEMENT_PROVIDER_EXTPOINT
					+ " is deprecated; use " + ELEMENT_PROVIDER_EXTPOINT);
		}
		configureElementProviders(ELEMENT_PROVIDER_EXTPOINT);

		// Register SWT Boolean CSSValue Converter
		super.registerCSSValueConverter(CSSValueBooleanConverterImpl.INSTANCE);
	}

	/** @return true if some providers were found */
	private boolean configureElementProviders(String extensionPointId) {
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointId);
		if (extPoint == null) {
			return false;
		}
		IExtension[] extensions = extPoint.getExtensions();
		if (extensions.length == 0) {
			return false;
		}
		for (IExtension e : extensions) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				String tmp = ce.getName();
				if (tmp.equals("provider")) {
					try {
						Object tmp2 = ce.createExecutableExtension("class");
						for (IConfigurationElement ce2 : ce.getChildren()) {
							String widget = ce2.getAttribute("class");
							widgetsMap.put(widget, tmp2);
						}
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
		return true;
	}

	public CSSEngineImpl(ExtendedDocumentCSS documentCSS) {
		super(documentCSS);
		// Register SWT Boolean CSSValue Converter
		super.registerCSSValueConverter(CSSValueBooleanConverterImpl.INSTANCE);
	}

	public CSSParser makeCSSParser() {
		// Create CSS Parser
		ICSSParserFactory factory = CSSParserFactory.newInstance();
		CSSParser parser = factory.makeCSSParser();

		// Register Batik CSS Selector factory.
		parser.setSelectorFactory(CSSSelectorFactoryImpl.INSTANCE);

		// Register Custom CSS Condition factory.
		parser.setConditionFactory(CONDITIONFACTORY_INSTANCE);

		return parser;
	}

	public void registerCSSPropertyHandler(Class cl, ICSSPropertyHandler handler) {
		initHandlerProviderIfNeed();
		handlerProvider.registerCSSPropertyHandler(cl, handler);
	}

	private void initHandlerProviderIfNeed() {
		if (handlerProvider == null) {
			handlerProvider = new CSSPropertyHandlerSimpleProviderImpl();
			super.registerCSSPropertyHandlerProvider(handlerProvider);
		}
	}

	public void registerCSSProperty(String propertyName,
			Class propertyHandlerClass) {
		initHandlerProviderIfNeed();
		handlerProvider.registerCSSProperty(propertyName, propertyHandlerClass);
	}

	private void initLazyHandlerProviderIfNeed() {
		if (lazyHandlerProvider == null) {
			lazyHandlerProvider = new CSSPropertyHandlerLazyProviderImpl();
			super.registerCSSPropertyHandlerProvider(lazyHandlerProvider);
		}
	}

	public void registerPackage(String packageName) {
		initLazyHandlerProviderIfNeed();
		lazyHandlerProvider.registerPackage(packageName);
	}

}
