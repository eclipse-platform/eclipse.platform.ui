/*******************************************************************************
 * Copyright (c) 2025 SAP S.E. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP S.E. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.codemining;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;

import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * A code mining that draws zero-width characters (like zero-width spaces) as
 * line content code minings.
 *
 * @see ZeroWidthCharactersLineContentCodeMiningProvider
 */
class ZeroWidthCharactersLineContentCodeMining extends LineContentCodeMining {

	private static final String ZW_CHARACTERS_MINING = "ZWSP"; //$NON-NLS-1$
	private final IPreferenceStore store;
	private final int offset;

	public ZeroWidthCharactersLineContentCodeMining(int offset, ICodeMiningProvider provider, IPreferenceStore store) {
		super(new Position(offset, 1), false, provider);
		this.store = store;
		this.offset = offset;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public String getLabel() {
		return ZW_CHARACTERS_MINING;
	}

	@Override
	public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
		int oldAlpha = -1;
		boolean isAdvancedGraphicsPresent = gc.getAdvanced();
		if (isAdvancedGraphicsPresent) {
			int alpha = store.getInt(AbstractTextEditor.PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE);
			oldAlpha = gc.getAlpha();
			gc.setAlpha(alpha);
		}
		try {
			gc.setForeground(getColor(textWidget));
			Point point = super.draw(gc, textWidget, color, x, y);
			gc.setForeground(color);
			return point;
		} finally {
			if (oldAlpha != -1) {
				gc.setAlpha(oldAlpha);
			}
		}
	}

	private Color getColor(StyledText textWidget) {
		int off = offset - 1;
		Color fg;
		boolean isFullSelectionStyle = (textWidget.getStyle() & SWT.FULL_SELECTION) != SWT.NONE;
		if (!textWidget.getBlockSelection() && isFullSelectionStyle && isOffsetSelected(textWidget, off)) {
			fg = textWidget.getSelectionForeground();
		} else {
			if (off < 0 || off >= textWidget.getCharCount()) {
				fg = textWidget.getForeground();
			} else {
				StyleRange styleRange = textWidget.getStyleRangeAtOffset(off);
				if (styleRange == null || styleRange.foreground == null) {
					fg = textWidget.getForeground();
				} else {
					fg = styleRange.foreground;
				}
			}
		}
		return fg;
	}

	private static final boolean isOffsetSelected(StyledText widget, int offset) {
		Point selection = widget.getSelection();
		return offset >= selection.x && offset < selection.y;
	}
}
