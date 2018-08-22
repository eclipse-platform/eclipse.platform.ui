/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
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
 *     IBM Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 458342
 *     Karsten Thoms <karste.thoms@itemis.de> - Bug 532869
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.css.core.dom.ExtendedCSSRule;
import org.eclipse.e4.ui.css.core.dom.ExtendedDocumentCSS;
import org.eclipse.e4.ui.css.core.impl.sac.ExtendedSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.stylesheets.StyleSheetList;
import org.w3c.dom.views.DocumentView;


/**
 * {@link ViewCSS} implementation used to compute {@link CSSStyleDeclaration}.
 */
public class ViewCSSImpl implements ViewCSS, ExtendedDocumentCSS.StyleSheetChangeListener {

	protected DocumentCSS documentCSS;
	private boolean ruleCachingEnabled;
	/** Cached state of combined CSS rules for the current stylesheets */
	private List<CSSRule> currentCombinedRules;

	/**
	 * Creates a new ViewCSS.
	 */
	public ViewCSSImpl(DocumentCSS documentCSS) {
		this.documentCSS = documentCSS;
		if (this.documentCSS instanceof ExtendedDocumentCSS) {
			((ExtendedDocumentCSS) this.documentCSS).addStyleSheetChangeListener(this);
			ruleCachingEnabled = true;
		}
	}

	/**
	 * <b>DOM</b>: Implements {@link
	 * org.w3c.dom.views.AbstractView#getDocument()}.
	 */
	@Override
	public DocumentView getDocument() {
		return null;
	}

	/**
	 * Determines the relevant style declaration for an DOM element
	 */
	@Override
	public CSSStyleDeclaration getComputedStyle(Element elt, String pseudoElt) {
		CSSStyleDeclaration styleDeclaration = getComputedStyle(getCombinedRules(), elt, pseudoElt);
		return styleDeclaration;
	}

	/**
	 * Retrieves the combined list of CSS rules for all current stylesheets. This
	 * method returns a cached state when the stylesheets are the same as on its
	 * last call. When the stylesheets differ, the rules are collected from
	 * documentCSS's stylesheets.
	 *
	 * @return CSS rules for all style sheets
	 */
	private List<CSSRule> getCombinedRules() {
		if (this.ruleCachingEnabled && this.currentCombinedRules != null) {
			return this.currentCombinedRules;
		}

		StyleSheetList styleSheetList = documentCSS.getStyleSheets();
		int l = styleSheetList.getLength();

		List<CSSRule> cssRules = new ArrayList<>();

		// Loop over the CSS styleSheet list
		for (int i = 0; i < l; i++) {
			CSSStyleSheet styleSheet = (CSSStyleSheet) styleSheetList.item(i);

			CSSRuleList styleSheetRules = styleSheet.getCssRules();
			int rulesSize = styleSheetRules.getLength();
			for (int j = 0; j < rulesSize; j++) {
				cssRules.add(styleSheetRules.item(j));
			}
		}

		if (this.ruleCachingEnabled) {
			this.currentCombinedRules = cssRules;
		}
		return cssRules;
	}

	public CSSStyleDeclaration getComputedStyle(List<CSSRule> ruleList, Element elt, String pseudoElt) {
		List<StyleWrapper> styleDeclarations = null;
		StyleWrapper firstStyleDeclaration = null;
		int length = ruleList.size();
		int position = 0;
		for (int i = 0; i < length; i++) {
			CSSRule rule = ruleList.get(i);
			if (rule.getType() == CSSRule.STYLE_RULE) {
				CSSStyleRule styleRule = (CSSStyleRule) rule;
				if (rule instanceof ExtendedCSSRule) {
					ExtendedCSSRule r = (ExtendedCSSRule) rule;
					SelectorList selectorList = r.getSelectorList();
					// Loop for SelectorList
					int l = selectorList.getLength();
					for (int j = 0; j < l; j++) {
						Selector selector = selectorList.item(j);
						if (selector instanceof ExtendedSelector) {
							ExtendedSelector extendedSelector = (ExtendedSelector) selector;
							if (extendedSelector.match(elt, pseudoElt)) {
								CSSStyleDeclaration style = styleRule
										.getStyle();
								int specificity = extendedSelector
										.getSpecificity();
								StyleWrapper wrapper = new StyleWrapper(style,
										specificity, position++);
								if (firstStyleDeclaration == null) {
									firstStyleDeclaration = wrapper;
								} else {
									// There is several Style Declarations which
									// match the current element
									if (styleDeclarations == null) {
										styleDeclarations = new ArrayList<>();
										styleDeclarations.add(firstStyleDeclaration);
									}
									styleDeclarations.add(wrapper);
								}
							}
						} else {
							// TODO : selector is not batik ExtendedSelector,
							// Manage this case...
						}
					}
				} else {
					// TODO : CSS rule is not ExtendedCSSRule,
					// Manage this case...
				}
			}
		}
		if (styleDeclarations != null) {
			// There is several Style Declarations wich match
			// the element, merge the CSS Property value.
			return new CSSComputedStyleImpl(styleDeclarations);
		}
		if (firstStyleDeclaration != null) {
			return firstStyleDeclaration.style;
		}
		return null;
	}

	@Override
	public void styleSheetAdded(StyleSheet styleSheet) {
		currentCombinedRules = null;
	}

	@Override
	public void styleSheetRemoved(StyleSheet styleSheet) {
		currentCombinedRules = null;
	}
}
