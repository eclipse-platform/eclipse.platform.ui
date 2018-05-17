/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;


public class RGBColorImplTest {

	@Test
	public void testGetCssText() throws Exception {
		CSSEngine engine = ParserTestUtil.createEngine();
		CSSValue value = engine.parsePropertyValue("#FF8000");
		assertTrue( value instanceof RGBColor );
		assertEquals( "rgb(255, 128, 0)", value.getCssText() );

		// Out-of-range values should be clipped
		// http://www.w3.org/TR/CSS21/syndata.html#value-def-color
		value = engine.parsePropertyValue("rgb( 300, -10, 42 )");
		assertTrue( value instanceof RGBColor );
		//		assertEquals( "rgb(255, 0, 42)", value.getCssText() );

		//		value = engine.parsePropertyValue("rgb( 110%, 50%, -10% )");
		//		assertTrue( value instanceof RGBColor );
		//		assertEquals( "rgb(100%, 50%, 0%)", value.getCssText() );
	}
}
