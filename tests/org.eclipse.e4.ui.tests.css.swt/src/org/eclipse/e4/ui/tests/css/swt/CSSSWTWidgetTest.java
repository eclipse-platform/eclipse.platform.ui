/*******************************************************************************
 *  Copyright (c) 2009, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *      Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.widgets.Widget;
import org.junit.Ignore;
import org.junit.Test;

public class CSSSWTWidgetTest extends CSSSWTTestCase {




	@Ignore
	@Test
	public void testEngineKey() {
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		assertEquals(WidgetElement.getEngine(widget), engine);
	}

	@Test
	public void testIDKey() {
		final String id = "some.test.id";
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		WidgetElement.setID(widget, id);
		assertEquals(WidgetElement.getID(widget), id);
	}


	@Test
	public void testCSSClassKey() {
		final String cssClass = "some.test.cssclassname";
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		WidgetElement.setCSSClass(widget, cssClass);
		assertEquals(WidgetElement.getCSSClass(widget), cssClass);
	}
}
