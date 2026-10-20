/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.source.inlined;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewer;

public class InlinedAnnotationFontTest {

	private Shell fParent;

	private Font fFont1;

	private Font fFont2;

	@BeforeEach
	public void setUp() {
		fParent= new Shell();
		String family= fParent.getFont().getFontData()[0].getName();
		fFont1= new Font(fParent.getDisplay(), family, 10, SWT.NORMAL);
		fFont2= new Font(fParent.getDisplay(), family, 10, SWT.BOLD);
	}

	@AfterEach
	public void tearDown() {
		fParent.dispose();
		fFont1.dispose();
		fFont2.dispose();
	}

	/** The italic annotation font follows a widget font change of the same height and is disposed with the widget. */
	@Test
	public void testAnnotationFontFollowsWidgetFont() throws Exception {
		assumeFalse(fFont1.equals(fFont2));
		SourceViewer viewer= new SourceViewer(fParent, null, SWT.NONE);
		StyledText textWidget= viewer.getTextWidget();
		Class<?> strategyClass= Class.forName("org.eclipse.jface.text.source.inlined.InlinedAnnotationDrawingStrategy");
		var constructor= strategyClass.getConstructor(ITextViewer.class);
		constructor.setAccessible(true);
		Object strategy= constructor.newInstance(viewer);
		Method getAnnotationFont= strategyClass.getDeclaredMethod("getAnnotationFont", StyledText.class);
		getAnnotationFont.setAccessible(true);

		textWidget.setFont(fFont1);
		Font annotationFont1= (Font) getAnnotationFont.invoke(strategy, textWidget);
		assertSame(annotationFont1, getAnnotationFont.invoke(strategy, textWidget));
		for (FontData data : annotationFont1.getFontData()) {
			assertTrue((data.getStyle() & SWT.ITALIC) != 0);
		}

		textWidget.setFont(fFont2);
		Font annotationFont2= (Font) getAnnotationFont.invoke(strategy, textWidget);
		assertNotSame(annotationFont1, annotationFont2);
		assertTrue(annotationFont1.isDisposed());
		assertFalse(annotationFont2.isDisposed());

		textWidget.dispose();
		assertTrue(annotationFont2.isDisposed());
	}
}
