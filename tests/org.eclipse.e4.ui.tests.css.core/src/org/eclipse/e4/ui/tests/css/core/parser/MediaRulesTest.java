/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Assert that <code>@media</code> rules are ignored.
 */
public class MediaRulesTest {

	@Test
	void testMediaRule() throws Exception {
		String css = """
@media screen, print {
	BODY { line-height: 1.2 }
	Label { background-color: #FFFFFF }
}
BODY { line-height: 1.3 }

@media screen, print {
	BODY { line-height: 1.4 }
}
""";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		// This one is provided only inside the @media so it shouldn't be there
		assertThat(findCssRuleThatContains(styleSheet, "background-color")).isEmpty();
		// This one is provided outside the @media and it shouldn't be overwritten by
		// any of them
		assertThat(findCssRuleThatContains(styleSheet, "line-height")).containsOnly("BODY { line-height: 1.3; }");
	}

	private Stream<String> findCssRuleThatContains(CSSStyleSheet styleSheet, String text) {
		Stream<String> cssRulesText = IntStream.range(0, styleSheet.getCssRules().getLength())
				.mapToObj(i -> styleSheet.getCssRules().item(i).getCssText());
		return cssRulesText.filter(r -> r.contains(text));
	}
}
