/*******************************************************************************
 * Copyright (c) 2019 itemis AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Karsten Thoms <karsten.thoms@itemis.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.e4.ui.css.core.dom.properties.providers.CSSPropertyHandlerLazyProviderImpl;
import org.junit.jupiter.api.Test;

public class CSSPropertyHandlerProviderTest {
	static class TestCSSPropertyHandlerLazyProviderImpl extends CSSPropertyHandlerLazyProviderImpl {
		public String _getHandlerClassName(String property) {
			return super.getHandlerClassName(property);
		}
	}

	@Test
	void test_getHandlerClassName() {
		TestCSSPropertyHandlerLazyProviderImpl provider = new TestCSSPropertyHandlerLazyProviderImpl();
		assertEquals("CSSPropertyBackgroundHandler", provider._getHandlerClassName("background"));
		assertEquals("CSSPropertyBackgroundColorHandler", provider._getHandlerClassName("background-color"));
	}
}
