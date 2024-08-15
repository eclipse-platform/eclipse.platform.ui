/*******************************************************************************
 * Copyright (c) 2024 SAP SE
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.text.source.SourceViewer;

public class TestWhitespaceCharacterPainter {

	private Shell shell;

	@Before
	public void before() {
		shell= new Shell();
		shell.setSize(500, 200);
		shell.setLayout(new FillLayout());
	}

	@After
	public void after() {
		shell.dispose();
	}

	@Test
	public void glyphMetricsTakenIntoAccount() throws Exception {
		SourceViewer sourceViewer= new SourceViewer(shell, null, SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(new Document("first  \nsecond  \nthird  \n"));
		StyledText textWidget= sourceViewer.getTextWidget();
		textWidget.setFont(JFaceResources.getTextFont());
		WhitespaceCharacterPainter whitespaceCharPainter= new WhitespaceCharacterPainter(sourceViewer, true, true, true, true, true, true, true,
				true, true, true, true, 100);
		sourceViewer.addPainter(whitespaceCharPainter);
		textWidget.setStyleRange(createStyleRangeWithMetrics(6));
		textWidget.setStyleRange(createStyleRangeWithMetrics(15));
		Event e= new Event();
		e.widget= textWidget;
		PaintEvent ev= new PaintEvent(e);

		ev.gc= mock(GC.class);
		when(ev.gc.getClipping()).thenReturn(new Rectangle(0, 0, 100, 100));
		when(ev.gc.stringExtent(anyString())).thenAnswer(new Answer<Point>() {
			@Override
			public Point answer(InvocationOnMock invocation) throws Throwable {
				GC gc= new GC(shell);
				gc.setFont(JFaceResources.getTextFont());
				Point result= gc.stringExtent(invocation.getArgument(0));
				gc.dispose();
				return result;
			}
		});
		when(ev.gc.getFontMetrics()).thenAnswer(new Answer<FontMetrics>() {
			@Override
			public FontMetrics answer(InvocationOnMock invocation) throws Throwable {
				GC gc= new GC(shell);
				gc.setFont(JFaceResources.getTextFont());
				FontMetrics metrics= gc.getFontMetrics();
				gc.dispose();
				return metrics;
			}
		});
		ev.x= 0;
		ev.y= 0;
		ev.width= 100;
		ev.height= 100;
		whitespaceCharPainter.paintControl(ev);
		verify(ev.gc, times(5)).drawString(anyString(), anyInt(), anyInt(), anyBoolean());
	}

	private StyleRange createStyleRangeWithMetrics(int start) {
		StyleRange sr= new StyleRange();
		sr.start= start;
		sr.metrics= new GlyphMetrics(20, 20, 20);
		return sr;
	}
}
