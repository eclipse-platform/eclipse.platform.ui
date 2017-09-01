/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Display;

public class BracketMatchingReconciler implements IReconciler{
	private RGBA fBoxingRGB = new RGBA(155, 155, 155, 50);
	private MatchingCharacterPainter fMatchingCharacterPainter;
	private SourceViewer fSourceViewer;
	private ICharacterPairMatcher fCharacterPairMatcher = new ICharacterPairMatcher() {
		@Override
		public IRegion match(IDocument document, int offset){
			try {
				String before = document.get(0, offset);
				String after = document.get(offset, document.getLength() - offset);
				int closingIndex = after.indexOf('>');
				int openingIndex = before.lastIndexOf('<');
				int previousClosingIndex = after.indexOf('<');
				int previousOpeningIndex = before.lastIndexOf('>');
				if((previousClosingIndex != -1 && closingIndex > previousClosingIndex)
						|| (previousOpeningIndex != -1 && openingIndex < previousOpeningIndex)) {
					return null;
				}
				return new Region(openingIndex, offset - openingIndex + closingIndex + 1);
			} catch (BadLocationException e) {
				return null;
			}
		}

		@Override
		public int getAnchor() {
			return ICharacterPairMatcher.RIGHT;
		}

		@Override
		public void dispose() {
			if(fMatchingCharacterPainter != null) {
				fMatchingCharacterPainter.dispose();
			}
		}

		@Override
		public void clear() {
			// No memory implemented
		}
	};

	@Override
	public void install(ITextViewer textViewer) {
		if (textViewer instanceof ITextViewerExtension2 && textViewer instanceof SourceViewer) {
			fSourceViewer = (SourceViewer)textViewer;
			fMatchingCharacterPainter = new MatchingCharacterPainter(fSourceViewer, fCharacterPairMatcher);
			fMatchingCharacterPainter.setColor(new Color (Display.getCurrent(), fBoxingRGB));
			fMatchingCharacterPainter.setHighlightCharacterAtCaretLocation(true);
			fMatchingCharacterPainter.setHighlightEnclosingPeerCharacters(true);
			fSourceViewer.addPainter(fMatchingCharacterPainter);
		}
	}

	@Override
	public void uninstall() {
		fSourceViewer.removePainter(fMatchingCharacterPainter);
	}

	@Override
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		return null;
	}
}
