/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;

/**
 * Tests that the canvas behind a {@link SourceViewer} tracks the background of its text
 * widget, so that the gap the ruler layout leaves is not visible.
 */
public class SourceViewerBackgroundTest {

	private Shell shell;

	@BeforeEach
	public void setUp() {
		shell= new Shell();
	}

	@AfterEach
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testCanvasBackgroundMatchesTextWidgetOnCreation() {
		SourceViewer viewer= createViewer();

		assertEquals(viewer.getTextWidget().getBackground(), viewer.getControl().getBackground());
	}

	@Test
	public void testCanvasBackgroundFollowsTextWidgetBackground() {
		SourceViewer viewer= createViewer();
		StyledText textWidget= viewer.getTextWidget();
		Color background= Display.getDefault().getSystemColor(SWT.COLOR_RED);

		textWidget.setBackground(background);
		textWidget.notifyListeners(SWT.Paint, new Event());

		assertEquals(background, viewer.getControl().getBackground());
	}

	@Test
	public void testCanvasBackgroundFollowsResetToDefault() {
		SourceViewer viewer= createViewer();
		StyledText textWidget= viewer.getTextWidget();
		Color defaultBackground= textWidget.getBackground();

		textWidget.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		textWidget.notifyListeners(SWT.Paint, new Event());
		textWidget.setBackground(null);
		textWidget.notifyListeners(SWT.Paint, new Event());

		assertEquals(defaultBackground, viewer.getControl().getBackground());
	}

	private SourceViewer createViewer() {
		SourceViewer viewer= new SourceViewer(shell, new VerticalRuler(12), SWT.NONE);
		viewer.setDocument(new Document("content")); //$NON-NLS-1$
		return viewer;
	}
}
