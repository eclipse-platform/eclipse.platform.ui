/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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
package org.eclipse.e4.ui.css.core.dom.parsers;

import java.io.IOException;

import org.eclipse.e4.ui.css.core.sac.DocumentHandlerFactory;
import org.eclipse.e4.ui.css.core.sac.ExtendedDocumentHandler;
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
 * CSS Parser interface to parse with SAC {@link Parser} :
 * <ul>
 * <li>CSS Style sheet and return {@link CSSStyleSheet}.</li>
 * <li>CSS Style declaration and return {@link CSSStyleDeclaration}.</li>
 * <li>CSS value and return {@link CSSValue}.</li>
 * <li>CSS rule and return {@link CSSRule}.</li>
 * </ul>
 */
public interface CSSParser {

	/**
	 * Parse CSS <code>source</code> style sheet with SAC {@link Parser} and
	 * return {@link CSSStyleSheet} instance.
	 *
	 * @param source
	 *            style sheet.
	 */
	public CSSStyleSheet parseStyleSheet(InputSource source) throws IOException;

	/**
	 * Set the parent {@link CSSStyleSheet}.
	 */
	public void setParentStyleSheet(CSSStyleSheet parentStyleSheet);

	/**
	 * Parse CSS <code>source</code> style declaration with SAC {@link Parser}
	 * and return {@link CSSStyleDeclaration} instance.
	 *
	 * @param source
	 *            style declaration.
	 */
	public CSSStyleDeclaration parseStyleDeclaration(InputSource source)
			throws IOException;

	/**
	 * Parse CSS <code>source</code> style declaration with SAC {@link Parser}
	 * and update the <code>styleDecelaration</code>.
	 */
	public void parseStyleDeclaration(CSSStyleDeclaration styleDeclaration,
			InputSource source) throws IOException;

	/**
	 * Parse CSS <code>source</code> value with SAC {@link Parser} and return
	 * {@link CSSValue} instance.
	 *
	 * @param source
	 *            CSS value.
	 */
	public CSSValue parsePropertyValue(InputSource source) throws IOException;

	/**
	 * Parse CSS <code>source</code> rule value with SAC {@link Parser} and
	 * return {@link CSSRule} instance.
	 *
	 * @param source
	 *            CSS rule.
	 */
	public CSSRule parseRule(InputSource source) throws IOException;

	/**
	 * Parse CSS <code>source</code> selectors value with SAC {@link Parser}
	 * and return {@link SelectorList} instance.
	 */
	public SelectorList parseSelectors(InputSource source) throws IOException;

	/*------- SAC parser configuration methods ------- */

	/**
	 * Set the SAC {@link DocumentHandlerFactory} factory to get SAC
	 * {@link ExtendedDocumentHandler} handler used by SAC {@link Parser}.
	 */
	public void setDocumentHandlerFactory(
			DocumentHandlerFactory documentHandlerFactory);

	/**
	 * Get the SAC {@link ConditionFactory} used by SAC {@link Parser}.
	 */
	public ConditionFactory getConditionFactory();

	/**
	 * Set the SAC {@link ConditionFactory} used by SAC {@link Parser}.
	 */
	public void setConditionFactory(ConditionFactory conditionFactory);

	/**
	 * Get the SAC {@link SelectorFactory} used by SAC {@link Parser}.
	 */
	public SelectorFactory getSelectorFactory();

	/**
	 * Set the SAC {@link SelectorFactory} used by SAC {@link Parser}.
	 */
	public void setSelectorFactory(SelectorFactory selectorFactory);

}
