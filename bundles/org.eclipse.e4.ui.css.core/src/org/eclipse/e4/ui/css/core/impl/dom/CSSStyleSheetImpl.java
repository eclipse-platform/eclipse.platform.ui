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

import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;
import org.w3c.dom.stylesheets.StyleSheet;

public class CSSStyleSheetImpl extends AbstractCSSNode implements CSSStyleSheet {

	private static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED"; //$NON-NLS-1$

	private CSSRuleList rules = null;

	public CSSStyleSheetImpl() {
		super();
	}

	// W3C CSSStyleSheet API methods

	@Override
	public void deleteRule(int position) throws DOMException {
		try {
			((CSSRuleListImpl) rules).remove(position);
		} catch (IndexOutOfBoundsException ex) {
			throw new DOMExceptionImpl(DOMException.INDEX_SIZE_ERR, DOMExceptionImpl.ARRAY_OUT_OF_BOUNDS, ex.getMessage());
		}
	}

	@Override
	public CSSRuleList getCssRules() {
		return rules;
	}

	@Override
	public CSSRule getOwnerRule() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public int insertRule(String arg0, int arg1) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}


	// org.w3c.dom.stylesheet.StyleSheet API methods

	@Override
	public boolean getDisabled() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public String getHref() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public MediaList getMedia() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public Node getOwnerNode() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public StyleSheet getParentStyleSheet() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public String getTitle() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public String getType() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public void setDisabled(boolean disabled) {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	// Additional

	public void setRuleList(CSSRuleList rules) {
		this.rules = rules;
	}
}
