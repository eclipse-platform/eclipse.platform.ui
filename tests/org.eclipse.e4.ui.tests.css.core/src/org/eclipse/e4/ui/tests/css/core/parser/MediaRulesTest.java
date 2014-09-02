/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Assert that <code>@media</code> rules are ignored.
 */
public class MediaRulesTest {

	@Test
	@Ignore("//THIS TEST KNOWN TO FAIL Dec 16/08")
	public void testMediaRule() throws Exception {
		String css = "@media screen, print {\n"
				+ "BODY { line-height: 1.2 }\n"
				+ "}\n" + "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		CSSRuleList rules = styleSheet.getCssRules();


		assertEquals(1, rules.getLength());
	}
}
