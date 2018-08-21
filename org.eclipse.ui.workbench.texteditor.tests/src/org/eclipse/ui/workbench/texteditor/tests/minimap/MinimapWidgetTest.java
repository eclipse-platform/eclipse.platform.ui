/*******************************************************************************
 * Copyright (c) 2018 Angelo ZERR.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - [minimap] Initialize minimap view - Bug 535450
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests.minimap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.views.minimap.MinimapWidget;

/**
 * Minimap widget tests to check that changed of {@link ITextViewer} of the editor update the
 * {@link StyledText} of the Minimap:
 * 
 * <ul>
 * <li>check that {@link StyledText} content of minimap is synchronized with the text of the
 * {@link ITextViewer} of the editor.</li>
 * <li>check that {@link StyledText} styles of minimap is synchronized with the styles of the
 * {@link ITextViewer} of the editor.</li>
 * </ul>
 * 
 * @since 3.11
 */
public class MinimapWidgetTest {

	private ITextViewer editorViewer;

	private StyledText editorStyledText;

	private StyledText minimapStyledText;

	@Before
	public void createMinimap() {
		Composite parent= new Shell();
		editorViewer= new TextViewer(parent, SWT.NONE);
		MinimapWidget minimapWidget= new MinimapWidget(parent, editorViewer);
		minimapWidget.install();

		editorStyledText= editorViewer.getTextWidget();
		minimapStyledText= (StyledText) minimapWidget.getControl();

	}

	@Test
	public void testMinimapContent() {
		editorStyledText.setText("abcd");
		Assert.assertEquals("abcd", minimapStyledText.getText());

		editorStyledText.replaceTextRange(1, 0, "ABCD");
		Assert.assertEquals("aABCDbcd", minimapStyledText.getText());
	}

	@Test
	public void testMinimapSetStyles() {
		// As it doesn't exists listener to track styles changed of StyledText, update styles directly in StyledText of the editor doesn't update the styles of StyledText of the minimap.
		StyleRange[] orginalMinimapStyles = minimapStyledText.getStyleRanges();
		editorStyledText.setText("abcd");
		StyleRange[] ranges= new StyleRange[] { new StyleRange(0, 1, editorStyledText.getDisplay().getSystemColor(SWT.COLOR_BLACK), null) };
		editorStyledText.setStyleRanges(ranges);
		// Styles of minimap doesn't changed
		Assert.assertArrayEquals(orginalMinimapStyles, minimapStyledText.getStyleRanges());
	}

	@Test
	public void testMinimapSetStylesWithTextPresentation() {
		// Track styles changed of TextPresentation, update the styles of StyledText of the minimap.
		editorStyledText.setText("abcd");

		StyleRange[] ranges= new StyleRange[] { new StyleRange(0, 1, editorStyledText.getDisplay().getSystemColor(SWT.COLOR_BLACK), null) };
		TextPresentation presentation= new TextPresentation();
		presentation.mergeStyleRanges(ranges);
		editorViewer.changeTextPresentation(presentation, false);
		StyleRange[] expectedRanges= new StyleRange[] { new StyleRange(0, 1, editorStyledText.getDisplay().getSystemColor(SWT.COLOR_BLACK), null) };
		Assert.assertArrayEquals(expectedRanges, minimapStyledText.getStyleRanges());

		ranges= new StyleRange[] { new StyleRange(1, 1, editorStyledText.getDisplay().getSystemColor(SWT.COLOR_RED), null) };
		presentation= new TextPresentation();
		presentation.mergeStyleRanges(ranges);
		editorViewer.changeTextPresentation(presentation, false);
		expectedRanges= new StyleRange[] { new StyleRange(0, 1, editorStyledText.getDisplay().getSystemColor(SWT.COLOR_BLACK), null),
				new StyleRange(1, 1, editorStyledText.getDisplay().getSystemColor(SWT.COLOR_RED), null) };
		Assert.assertArrayEquals(expectedRanges, minimapStyledText.getStyleRanges());
	}
}
