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

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;

public class CSSRuleListImpl implements CSSRuleList {

	private List<CSSRule> ruleList;

	public CSSRuleListImpl() {
		super();
		this.ruleList = new ArrayList<>();
	}

	// W3C CSSRuleList API methods

	@Override
	public int getLength() {
		return ruleList.size();
	}

	@Override
	public CSSRule item(int position) {
		return ruleList.get(position);
	}

	//Additional

	public void add(CSSRule rule) {
		ruleList.add(rule);
	}

	public void remove(int position) {
		ruleList.remove(position);
	}
}
