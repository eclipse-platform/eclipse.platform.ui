/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom.parsers;

import java.io.IOException;
import java.util.Stack;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParser;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleDeclarationImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSValueFactory;
import org.eclipse.e4.ui.css.core.sac.DocumentHandlerFactory;
import org.eclipse.e4.ui.css.core.sac.ExtendedDocumentHandler;
import org.eclipse.e4.ui.css.core.sac.ISACParserFactory;
import org.eclipse.e4.ui.css.core.sac.ParserNotFoundException;
import org.eclipse.e4.ui.css.core.sac.SACParserFactory;
import org.w3c.css.sac.ConditionFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract {@link CSSParser} implementation.
 */
public class AbstractCSSParser implements CSSParser {

	private static DocumentHandlerFactory defaultDocumentHandlerFactory;
	private static ISACParserFactory defaultParserFactory;
	static {
		defaultDocumentHandlerFactory = DocumentHandlerFactory.newInstance();
		defaultParserFactory = SACParserFactory.newInstance();
	}

	// SAC
	private Parser parser;
	private DocumentHandlerFactory documentHandlerFactory;
	private ISACParserFactory parserFactory;

	private ConditionFactory conditionFactory;
	private SelectorFactory selectorFactory;

	@Override
	public CSSStyleSheet parseStyleSheet(InputSource source) throws IOException {
		ExtendedDocumentHandler documentHandler = getDocumentHandlerFactory().makeDocumentHandler();
		Parser parser = getParser();
		parser.setDocumentHandler(documentHandler);
		parser.parseStyleSheet(source);
		return (CSSStyleSheet) documentHandler.getNodeRoot();
	}

	@Override
	public CSSStyleDeclaration parseStyleDeclaration(InputSource source) throws IOException {
		CSSStyleDeclarationImpl styleDeclaration = new CSSStyleDeclarationImpl(null);
		parseStyleDeclaration(((styleDeclaration)), source);
		return styleDeclaration;
	}

	@Override
	public void parseStyleDeclaration(CSSStyleDeclaration styleDeclaration, InputSource source) throws IOException {
		Stack<Object> stack = new Stack<>();
		stack.push(styleDeclaration);
		ExtendedDocumentHandler documentHandler = getDocumentHandlerFactory().makeDocumentHandler();
		documentHandler.setNodeStack(stack);
		Parser parser = getParser();
		parser.setDocumentHandler(documentHandler);
		parser.parseStyleDeclaration(source);
	}

	@Override
	public CSSValue parsePropertyValue(InputSource source) throws IOException {
		Parser parser = getParser();
		ExtendedDocumentHandler documentHandler = getDocumentHandlerFactory().makeDocumentHandler();
		parser.setDocumentHandler(documentHandler);
		return CSSValueFactory.newValue(parser.parsePropertyValue(source));
	}

	@Override
	public CSSRule parseRule(InputSource source) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SelectorList parseSelectors(InputSource source) throws IOException {
		ExtendedDocumentHandler documentHandler = getDocumentHandlerFactory().makeDocumentHandler();
		Parser parser = getParser();
		parser.setDocumentHandler(documentHandler);
		return parser.parseSelectors(source);
	}

	@Override
	public void setParentStyleSheet(CSSStyleSheet parentStyleSheet) {

	}

	/**
	 * Return instance of {@link DocumentHandlerFactory}.
	 *
	 * @return
	 */
	public DocumentHandlerFactory getDocumentHandlerFactory() {
		if (documentHandlerFactory == null) {
			return defaultDocumentHandlerFactory;
		}
		return documentHandlerFactory;
	}

	/**
	 * Set instance of {@link DocumentHandlerFactory}.
	 *
	 * @param documentHandlerFactory
	 */
	@Override
	public void setDocumentHandlerFactory(DocumentHandlerFactory documentHandlerFactory) {
		this.documentHandlerFactory = documentHandlerFactory;
	}

	/**
	 * Return SAC {@link Parser} to use.
	 *
	 * @return
	 */
	public Parser getParser() {
		if (parser == null) {
			try {
				parser = getSACParserFactory().makeParser();
				if (conditionFactory != null) {
					parser.setConditionFactory(conditionFactory);
				}
				if (selectorFactory != null) {
					parser.setSelectorFactory(selectorFactory);
				}
			} catch (Exception e) {
				// TODO : manage error.
				// e.printStackTrace();
				throw new ParserNotFoundException(e);
			}
		}
		return parser;
	}

	/**
	 * Set SAC {@link Parser} to use.
	 *
	 * @param parser
	 */
	public void setParser(Parser parser) {
		this.parser = parser;
	}

	/**
	 * Return factory {@link ISACParserFactory} to use.
	 *
	 * @return
	 */
	public ISACParserFactory getSACParserFactory() {
		if (parserFactory == null) {
			return defaultParserFactory;
		}
		return parserFactory;
	}

	/**
	 * Set factory {@link ISACParserFactory} to use.
	 *
	 * @param parserFactory
	 */
	public void setSACParserFactory(ISACParserFactory parserFactory) {
		this.parserFactory = parserFactory;
	}

	@Override
	public ConditionFactory getConditionFactory() {
		return conditionFactory;
	}

	@Override
	public void setConditionFactory(ConditionFactory conditionFactory) {
		this.conditionFactory = conditionFactory;
	}

	@Override
	public SelectorFactory getSelectorFactory() {
		return selectorFactory;
	}

	@Override
	public void setSelectorFactory(SelectorFactory selectorFactory) {
		this.selectorFactory = selectorFactory;
	}

}
