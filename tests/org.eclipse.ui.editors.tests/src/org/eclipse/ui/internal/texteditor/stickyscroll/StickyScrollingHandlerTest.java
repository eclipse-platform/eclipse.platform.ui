/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.StringJoiner;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;

public class StickyScrollingHandlerTest {

	private Shell shell;
	private SourceViewer sourceViewer;
	private Color lineNumberColor;
	private Color hoverColor;
	private CompositeRuler ruler;
	private IPreferenceStore store;
	private StickyLinesProvider linesProvider;
	private StickyScrollingHandler stickyScrollingHandler;

	@Before
	public void setup() {
		shell = new Shell(Display.getDefault());
		ruler = new CompositeRuler();
		sourceViewer = new SourceViewer(shell, ruler, SWT.None);

		lineNumberColor = new Color(0, 0, 0);
		hoverColor = new Color(1, 1, 1);

		store = createPreferenceStore();
		linesProvider = mock(StickyLinesProvider.class);

		stickyScrollingHandler = new StickyScrollingHandler(sourceViewer, ruler, store, linesProvider);
	}

	@Test
	public void testShowStickyLines() {
		when(linesProvider.get(100, sourceViewer)).thenReturn(List.of(new StickyLine("line 10", 9)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineNumber = getStickyLineNumber();
		String expLineNumber = "10";
		assertEquals(expLineNumber, stickyLineNumber.getText());
		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 10";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testUnistallStickyLines() {
		Canvas stickyControlCanvas = getStickyControlCanvas(this.shell);

		stickyScrollingHandler.uninstall();

		assertTrue(stickyControlCanvas.isDisposed());
	}

	@Test
	public void testPreferencesLoaded() {
		when(linesProvider.get(100, sourceViewer)).thenReturn(List.of(new StickyLine("line 10", 9)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertEquals(lineNumberColor, stickyLineNumber.getStyleRangeAtOffset(0).foreground);
	}

	@Test
	public void testPreferencesUpdated() {
		when(linesProvider.get(100, sourceViewer))
				.thenReturn(List.of(new StickyLine("line 10", 9), new StickyLine("line 20", 19)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 10" + System.lineSeparator() + "line 20";
		assertEquals(expStickyLineText, stickyLineText.getText());

		// change maximum count of sticky lines to 1
		store.setValue(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT, 1);

		expStickyLineText = "line 10";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	private IPreferenceStore createPreferenceStore() {
		store = new PreferenceStore();
		store.setValue(EDITOR_TAB_WIDTH, 4);
		store.setValue(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT, 4);
		store.setValue(EDITOR_LINE_NUMBER_RULER_COLOR, colorToString(lineNumberColor));
		store.setValue(EDITOR_CURRENT_LINE_COLOR, colorToString(hoverColor));
		store.setValue(EDITOR_LINE_NUMBER_RULER, true);
		return store;
	}

	private StyledText getStickyLineNumber() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (StyledText) canvas.getChildren()[0];
	}

	private StyledText getStickyLineText() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (StyledText) canvas.getChildren()[1];
	}

	private Canvas getStickyControlCanvas(Composite composite) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Canvas canvas) {
				if (canvas.getChildren().length == 4) {
					return canvas;
				}
			}
			if (control instanceof Composite childComposite) {
				return getStickyControlCanvas(childComposite);
			}
		}
		return null;
	}

	private String colorToString(Color color) {
		StringJoiner joiner = new StringJoiner(",");
		joiner.add(String.valueOf(color.getRed()));
		joiner.add(String.valueOf(color.getGreen()));
		joiner.add(String.valueOf(color.getBlue()));
		return joiner.toString();
	}

}
