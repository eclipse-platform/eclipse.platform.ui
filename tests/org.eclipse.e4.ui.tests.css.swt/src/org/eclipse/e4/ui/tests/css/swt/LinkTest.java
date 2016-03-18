/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Sadau <lars@sadau-online.de>
 *     Fabio Zadrozny <fabiofz at gmail dot com>
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.Link;
import org.junit.Test;

public class LinkTest extends CSSSWTTestCase {

	@Test
	public void testLinkColors() {
		Link widgetToTest = createTestLink(
				"Link { background-color: #FF0000; color: #00FF00; swt-link-foreground-color: #0000FF;}");
		assertEquals(RED, widgetToTest.getBackground().getRGB());
		assertEquals(GREEN, widgetToTest.getForeground().getRGB());
		assertEquals(BLUE, widgetToTest.getLinkForeground().getRGB());
	}

}
