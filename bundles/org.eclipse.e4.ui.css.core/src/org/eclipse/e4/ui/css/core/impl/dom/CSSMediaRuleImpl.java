/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSMediaRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;

public class CSSMediaRuleImpl extends CSSRuleImpl implements CSSMediaRule {

	private static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED"; //$NON-NLS-1$

	public CSSMediaRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule,
			MediaListImpl mediaListImpl) {
		super(parentStyleSheet, parentRule);
	}

	@Override
	public short getType() {
		return CSSRule.MEDIA_RULE;
	}

	// W3C CSSMediaRule API methods

	@Override
	public void deleteRule(int index) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public CSSRuleList getCssRules() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public MediaList getMedia() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public int insertRule(String rule, int index) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}


	// Additional methods

	public void setRuleList(CSSRuleList rules) {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

}
