/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Brian de Alwis (MTI) - move out registry-specific element provisioning
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

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

	public static final ConditionFactory CONDITIONFACTORY_INSTANCE = new CSSConditionFactoryImpl(
			null, "class", null, "id");

	private CSSPropertyHandlerSimpleProviderImpl handlerProvider;

	private CSSPropertyHandlerLazyProviderImpl lazyHandlerProvider;

	public CSSEngineImpl() {
		super();

		// Register SWT Boolean CSSValue Converter
		super.registerCSSValueConverter(CSSValueBooleanConverterImpl.INSTANCE);
	}

	public CSSEngineImpl(ExtendedDocumentCSS documentCSS) {
		super(documentCSS);
		// Register SWT Boolean CSSValue Converter
		super.registerCSSValueConverter(CSSValueBooleanConverterImpl.INSTANCE);
	}

	@Override
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

	public void registerCSSPropertyHandler(Class<?> cl, ICSSPropertyHandler handler) {
		initHandlerProviderIfNeed();
		handlerProvider.registerCSSPropertyHandler(cl, handler);
	}

	private void initHandlerProviderIfNeed() {
		if (handlerProvider == null) {
			handlerProvider = new CSSPropertyHandlerSimpleProviderImpl();
			super.registerCSSPropertyHandlerProvider(handlerProvider);
		}
	}

	public void registerCSSProperty(String propertyName, Class<? extends ICSSPropertyHandler> propertyHandlerClass) {
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
