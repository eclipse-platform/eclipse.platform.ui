/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.eclipse.e4.ui.css.swt.dom.StyledTextElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StyledTextScrollbarTest extends CSSSWTTestCase {

	Shell shell;

	@Before
	public void setUpShell() {
		shell = new Shell(display, SWT.SHELL_TRIM);
	};

	@After
	public void tearDownShell() {
		shell.dispose();
	}

	private StyledText createStyledText() {
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		StyledText styledText = new StyledText(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		StringBuffer buf = new StringBuffer(500);
		for (int i = 0; i < 1000; i++) {
			buf.append(i
					+ "  - This is a really really really really really really long line to be shown in the styled text\n");
		}
		styledText.setText(buf.toString());
		shell.setSize(400, 400);
		return styledText;
	}

	@Test
	public void testScrollBar() throws Exception {
		StyledText styledText = createStyledText();

		engine = createEngine("", display);

		engine.applyStyles(styledText, true);
		StyledTextElement styledTextElement = new StyledTextElement(styledText, engine);
		styledTextElement.setScrollBarForegroundColor(new Color(display, 255, 0, 0));
		styledTextElement.setScrollBarBackgroundColor(new Color(display, 0, 0, 255));
		styledTextElement.setScrollBarWidth(6);
		styledTextElement.setMouseNearScrollScrollBarWidth(15);
		styledTextElement.setScrollBarBorderRadius(15);
		styledTextElement.setVerticalScrollBarVisible(true);
		styledTextElement.setHorizontalScrollBarVisible(true);
		styledTextElement.setScrollBarThemed("true");

	}

	@Test
	public void testScrollBarEngine() throws Exception {
		StyledText styledText = createStyledText();

		StringBuffer buf = new StringBuffer();
		buf.append("StyledText{");

		// It's here mostly as a way for non-themed variants to set it to false
		// to remove it.
		buf.append("  swt-scrollbar-themed: true;");

		buf.append("  swt-scrollbar-background-color: rgb(0,0,255);");
		buf.append("  swt-scrollbar-foreground-color: rgb(0,255,0);");
		buf.append("  swt-scrollbar-width: 1px;");
		buf.append("  swt-scrollbar-mouse-near-scroll-width: 3;");
		buf.append("  swt-scrollbar-vertical-visible: true;");
		buf.append("  swt-scrollbar-horizontal-visible: false;");
		buf.append("  swt-scrollbar-border-radius: 3;");
		buf.append("}");
		engine = createEngine(buf.toString(), display);

		engine.applyStyles(styledText, true);

		// Using reflection to test it as there's no public API.
		Object adapter = styledText.getData("StyledTextThemedScrollBarAdapter");
		assertEquals(new RGB(0, 0, 255), ((Color) invoke(adapter, "getScrollBarBackgroundColor")).getRGB());
		assertEquals(new RGB(0, 255, 0), ((Color) invoke(adapter, "getScrollBarForegroundColor")).getRGB());
		assertEquals(1, invoke(adapter, "getScrollBarWidth"));
		assertEquals(true, invoke(adapter, "getVerticalScrollBarVisible"));
		assertEquals(false, invoke(adapter, "getHorizontalScrollBarVisible"));
		assertEquals(3, invoke(adapter, "getScrollBarBorderRadius"));
		assertEquals(true, invoke(adapter, "getScrollBarThemed"));
		assertEquals(false, styledText.getVerticalBar().getVisible());

		// Remove the theming now
		buf = new StringBuffer();
		buf.append("StyledText{");
		buf.append("  swt-scrollbar-themed: false;");
		buf.append("}");

		engine = createEngine(buf.toString(), display);

		engine.applyStyles(styledText, true);
		assertEquals(false, invoke(adapter, "getScrollBarThemed"));
		assertTrue(styledText.getVerticalBar().getVisible());
	}

	private Object invoke(Object adapter, String string) {
		try {
			Method method = adapter.getClass().getMethod(string);
			return method.invoke(adapter);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
