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
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSUnknownRule;

public class CSSUnknownRuleImpl extends CSSRuleImpl implements CSSUnknownRule {

	public CSSUnknownRuleImpl(CSSStyleSheet parentStyleSheet, CSSRule parentRule,
			String atRule) {
		super(parentStyleSheet, parentRule);
		// TODO Auto-generated constructor stub
	}

	@Override
	public short getType() {
		return CSSRule.UNKNOWN_RULE;
	}
}
