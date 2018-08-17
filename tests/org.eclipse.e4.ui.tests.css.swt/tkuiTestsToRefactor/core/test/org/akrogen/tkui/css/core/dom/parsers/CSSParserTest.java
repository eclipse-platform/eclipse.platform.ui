/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
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
package org.akrogen.tkui.css.core.dom.parsers;

import org.akrogen.tkui.css.core.resources.CSSCoreResources;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

public class CSSParserTest {

	public static void main(String[] args) {
		try {
			ICSSParserFactory factory = CSSParserFactory.newInstance();
			CSSParser cssParser = factory.makeCSSParser();

			InputSource styleSheetSource = new InputSource();
			styleSheetSource.setByteStream(CSSCoreResources.getHTMLSimple());

			CSSStyleSheet styleSheet = cssParser
					.parseStyleSheet(styleSheetSource);

			// Loop for CSS Rules list parsed
			CSSRuleList ruleList = styleSheet.getCssRules();
			int length = ruleList.getLength();
			for (int i = 0; i < length; i++) {
				CSSRule rule = ruleList.item(i);
				System.out.println(rule);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
