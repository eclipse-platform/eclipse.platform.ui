/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import junit.framework.TestCase;

import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Assert that <code>@media</code> rules are ignored.
 */
public class MediaRulesTest extends TestCase {

	public void testMediaRule() throws Exception {
		String css = "@media screen, print {\n"
			+ "BODY { line-height: 1.2 }\n"
			+ "}\n" + "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		CSSRuleList rules = styleSheet.getCssRules();
		
		//THIS TEST KNOWN TO FAIL Dec 16/08
		assertEquals(1, rules.getLength());
	}
}
