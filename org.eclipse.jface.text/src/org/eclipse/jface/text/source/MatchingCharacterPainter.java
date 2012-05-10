/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;

/**
 * Highlights the peer character matching the character near the caret position, or a pair of peer
 * characters enclosing the caret position. This painter can be configured with an
 * {@link org.eclipse.jface.text.source.ICharacterPairMatcher} or an
 * {@link org.eclipse.jface.text.source.ICharacterPairMatcherExtension}.
 * <p>
 * Clients instantiate and configure an object of this class.
 * </p>
 * 
 * @since 2.1
 */
public final class MatchingCharacterPainter implements IPainter, PaintListener {

	/** Indicates whether this painter is active */
	private boolean fIsActive= false;
	/** The source viewer this painter is associated with */
	private ISourceViewer fSourceViewer;
	/** The viewer's widget */
	private StyledText fTextWidget;
	/** The color in which to highlight the peer character */
	private Color fColor;
	/** The paint position manager */
	private IPaintPositionManager fPaintPositionManager;
	/** The strategy for finding matching characters */
	private ICharacterPairMatcher fMatcher;
	/** The position tracking the matching characters */
	private Position fPairPosition= new Position(0, 0);
	/** The anchor indicating whether the character is left or right of the caret */
	private int fAnchor;

	/**
	 * Whether to highlight enclosing peer characters or not.
	 * 
	 * @since 3.8
	 */
	private boolean fHighlightEnclosingPeerCharacters;

	/**
	 * Whether to highlight the character at caret location or not.
	 * 
	 * @since 3.8
	 */
	private boolean fHighlightCharacterAtCaretLocation;

	/**
	 * Whether a character is present at caret location or not.
	 * 
	 * @since 3.8
	 */
	private boolean fCharacterPresentAtCaretLocation;

	/**
	 * The document this painter is associated with, or <code>null</code>.
	 * 
	 * @since 3.8
	 */
	private IDocument fDocument;

	/**
	 * The previous selection, used to determine the need for computing enclosing brackets.
	 * 
	 * @since 3.8
	 */
	private IRegion fPreviousSelection;

	/**
	 * Previous length of the document this painter is associated with.
	 * 
	 * @since 3.8
	 */
	private int fPreviousLengthOfDocument;

	/**
	 * Whether the input document has been replaced or not.
	 * 
	 * @since 3.8
	 */
	private boolean fDocumentChanged;

	/**
	 * The text viewer change listener.
	 * 
	 * @since 3.8
	 */
	private TextListener fTextListener;

	/**
	 * Creates a new MatchingCharacterPainter for the given source viewer using the given character
	 * pair matcher. The character matcher is not adopted by this painter. Thus, it is not disposed.
	 * However, this painter requires exclusive access to the given pair matcher.
	 *
	 * @param sourceViewer the source viewer
	 * @param matcher the character pair matcher
	 */
	public MatchingCharacterPainter(ISourceViewer sourceViewer, ICharacterPairMatcher matcher) {
		fSourceViewer= sourceViewer;
		fMatcher= matcher;
		fTextWidget= sourceViewer.getTextWidget();
		fDocument= fSourceViewer.getDocument();
	}

	/**
	 * Sets whether to highlight the character at caret location or not.
	 * 
	 * @param highlightCharacterAtCaretLocation whether to highlight the character at caret location
	 *            or not
	 * @since 3.8
	 */
	public void setHighlightCharacterAtCaretLocation(boolean highlightCharacterAtCaretLocation) {
		handleDrawRequest(null); // see https://bugs.eclipse.org/372515
		fHighlightCharacterAtCaretLocation= highlightCharacterAtCaretLocation;
	}

	/**
	 * Sets whether to highlight enclosing peer characters or not.
	 * 
	 * @param highlightEnclosingPeerCharcters whether to highlight enclosing peer characters or not
	 * @since 3.8
	 */
	public void setHighlightEnclosingPeerCharacters(boolean highlightEnclosingPeerCharcters) {
		fHighlightEnclosingPeerCharacters= highlightEnclosingPeerCharcters;
		installUninstallTextListener(highlightEnclosingPeerCharcters);
	}

	/**
	 * Sets the color in which to highlight the match character.
	 *
	 * @param color the color
	 */
	public void setColor(Color color) {
		fColor= color;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		if (fMatcher != null) {
			if (fMatcher instanceof ICharacterPairMatcherExtension && fTextListener != null) {
				installUninstallTextListener(false);
			}

			fMatcher.clear();
			fMatcher= null;
		}

		fColor= null;
		fTextWidget= null;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fTextWidget.removePaintListener(this);
			if (fPaintPositionManager != null)
				fPaintPositionManager.unmanagePosition(fPairPosition);
			if (redraw)
				handleDrawRequest(null);
		}
	}

	/*
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null)
			handleDrawRequest(event.gc);
	}

	/**
	 * Handles a redraw request.
	 *
	 * @param gc the GC to draw into or <code>null</code> to send a redraw request if necessary 
	 */
	private void handleDrawRequest(GC gc) {

		if (fPairPosition.isDeleted)
			return;

		int offset= fPairPosition.getOffset();
		int length= fPairPosition.getLength();
		if (length < 1)
			return;

		if (fSourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
			if (widgetRange == null)
				return;

			try {
				// don't draw if the pair position is really hidden and widgetRange just
				// marks the coverage around it.
				IDocument doc= fSourceViewer.getDocument();
				int startLine= doc.getLineOfOffset(offset);
				int endLine= doc.getLineOfOffset(offset + length);
				if (extension.modelLine2WidgetLine(startLine) == -1 || extension.modelLine2WidgetLine(endLine) == -1)
					return;
			} catch (BadLocationException e) {
				return;
			}

			offset= widgetRange.getOffset();
			length= widgetRange.getLength();

		} else {
			IRegion region= fSourceViewer.getVisibleRegion();
			if (region.getOffset() > offset || region.getOffset() + region.getLength() < offset + length)
				return;
			offset -= region.getOffset();
		}

		if (fHighlightCharacterAtCaretLocation || (fHighlightEnclosingPeerCharacters && !fCharacterPresentAtCaretLocation)) {
			draw(gc, offset, 1);
			draw(gc, offset + length - 1, 1);
		} else {
			if (ICharacterPairMatcher.RIGHT == fAnchor)
				draw(gc, offset, 1);
			else
				draw(gc, offset + length - 1, 1);
		}
	}

	/**
	 * Highlights the given widget region.
	 *
	 * @param gc the GC to draw into or <code>null</code> to send a redraw request
	 * @param offset the offset of the widget region
	 * @param length the length of the widget region
	 */
	private void draw(GC gc, int offset, int length) {
		if (gc != null) {

			gc.setForeground(fColor);

			Rectangle bounds;
			if (length > 0)
				bounds= fTextWidget.getTextBounds(offset, offset + length - 1);
			else {
				Point loc= fTextWidget.getLocationAtOffset(offset);
				bounds= new Rectangle(loc.x, loc.y, 1, fTextWidget.getLineHeight(offset));
			}

			// draw box around line segment
			gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);

			// draw box around character area
//			int widgetBaseline= fTextWidget.getBaseline();
//			FontMetrics fm= gc.getFontMetrics();
//			int fontBaseline= fm.getAscent() + fm.getLeading();
//			int fontBias= widgetBaseline - fontBaseline;

//			gc.drawRectangle(left.x, left.y + fontBias, right.x - left.x - 1, fm.getHeight() - 1);

		} else {
			fTextWidget.redrawRange(offset, length, true);
		}
	}

	/**
	 * Returns the signed current selection. The length will be negative if the resulting selection
	 * is right-to-left.
	 * <p>
	 * The selection offset is model based.
	 * </p>
	 * 
	 * @param sourceViewer the source viewer
	 * @return a region denoting the current signed selection, for a resulting RtoL selections
	 *         length is < 0
	 * @since 3.8
	 */
	private static final IRegion getSignedSelection(ISourceViewer sourceViewer) {
		Point viewerSelection= sourceViewer.getSelectedRange();

		StyledText text= sourceViewer.getTextWidget();
		Point selection= text.getSelectionRange();
		if (text.getCaretOffset() == selection.x) {
			viewerSelection.x= viewerSelection.x + viewerSelection.y;
			viewerSelection.y= -viewerSelection.y;
		}

		return new Region(viewerSelection.x, viewerSelection.y);
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#paint(int)
	 */
	public void paint(int reason) {

		IDocument document= fDocument;
		if (document == null) {
			deactivate(false);
			return;
		}

		IRegion selection= getSignedSelection(fSourceViewer);
		IRegion pair;
		boolean characterPresentAtCaretLocation;

		if (fMatcher instanceof ICharacterPairMatcherExtension) {
			ICharacterPairMatcherExtension matcher= (ICharacterPairMatcherExtension)fMatcher;
			pair= matcher.match(document, selection.getOffset(), selection.getLength());
			characterPresentAtCaretLocation= (pair != null);
			if (pair == null && fHighlightEnclosingPeerCharacters) {

				int length= document.getLength();
				boolean lengthChanged= length != fPreviousLengthOfDocument;
				fPreviousLengthOfDocument= length;

				if (reason != IPainter.CONFIGURATION && !fDocumentChanged && !lengthChanged && selection.equals(fPreviousSelection)) {
					return;
				}

				if (reason == IPainter.TEXT_CHANGE) {
					fPreviousSelection= selection;
					return;
				}

				fDocumentChanged= false;
				if (reason != IPainter.CONFIGURATION && !lengthChanged && fPreviousSelection != null && reason != IPainter.INTERNAL) {
					if (!matcher.isRecomputationOfEnclosingPairRequired(document, selection, fPreviousSelection)) {
						if (fCharacterPresentAtCaretLocation && !fHighlightCharacterAtCaretLocation) {
							fCharacterPresentAtCaretLocation= false;
							handleDrawRequest(null);
						}
						fPreviousSelection= selection;
						return;
					}
				}
				pair= matcher.findEnclosingPeerCharacters(document, selection.getOffset(), selection.getLength());
			}
		} else {
			if (Math.abs(selection.getLength()) > 0) {
				deactivate(true);
				return;
			}
			pair= fMatcher.match(document, selection.getOffset());
			characterPresentAtCaretLocation= (pair != null);
		}

		fPreviousSelection= selection;
		if (pair == null) {
			deactivate(true);
			return;
		}

		if (fIsActive) {

			if (IPainter.CONFIGURATION == reason) {

				// redraw current highlighting
				handleDrawRequest(null);

			} else if (pair.getOffset() != fPairPosition.getOffset() ||
					pair.getLength() != fPairPosition.getLength() ||
					fMatcher.getAnchor() != fAnchor ||
					characterPresentAtCaretLocation != fCharacterPresentAtCaretLocation) {
				// otherwise only do something if position is different

				// remove old highlighting
				handleDrawRequest(null);
				// update position
				fPairPosition.isDeleted= false;
				fPairPosition.offset= pair.getOffset();
				fPairPosition.length= pair.getLength();
				fAnchor= fMatcher.getAnchor();
				fCharacterPresentAtCaretLocation= characterPresentAtCaretLocation;
				// apply new highlighting
				handleDrawRequest(null);

			}
		} else {

			fIsActive= true;

			fPairPosition.isDeleted= false;
			fPairPosition.offset= pair.getOffset();
			fPairPosition.length= pair.getLength();
			fAnchor= fMatcher.getAnchor();
			fCharacterPresentAtCaretLocation= characterPresentAtCaretLocation;

			fTextWidget.addPaintListener(this);
			fPaintPositionManager.managePosition(fPairPosition);
			handleDrawRequest(null);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		fPaintPositionManager= manager;
	}

	/**
	 * Installs or uninstalls the text listener depending on the boolean parameter.
	 * 
	 * @param install <code>true</code> to install the text listener, <code>false</code> to uninstall
	 * 
	 * @since 3.8
	 */
	private void installUninstallTextListener(boolean install) {
		if (!(fMatcher instanceof ICharacterPairMatcherExtension))
			return;
	
		if (install) {
			fTextListener= new TextListener();
			fSourceViewer.addTextInputListener(fTextListener);
			fSourceViewer.addTextListener(fTextListener);
		} else {
			if (fTextListener != null) {
				fSourceViewer.removeTextInputListener(fTextListener);
				fSourceViewer.removeTextListener(fTextListener);
				fTextListener= null;
			}
		}
	}

	/**
	 * Listens to document changes and if required by those document changes causes a re-computation
	 * of matching characters.
	 * 
	 * @since 3.8
	 */
	private class TextListener implements ITextListener, ITextInputListener {

		/**
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
		 *      org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			fDocument= null;
		}

		/**
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
		 *      org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			fDocument= newInput;
			fDocumentChanged= true;
		}

		/**
		 * @see org.eclipse.jface.text.ITextListener#textChanged(org.eclipse.jface.text.TextEvent)
		 */
		public void textChanged(TextEvent event) {
			if (!fHighlightEnclosingPeerCharacters || !(fMatcher instanceof ICharacterPairMatcherExtension))
				return;

			if (!event.getViewerRedrawState())
				return;

			String text= event.getText();
			String replacedText= event.getReplacedText();
			ICharacterPairMatcherExtension matcher= (ICharacterPairMatcherExtension)fMatcher;
			if (searchForCharacters(text, matcher) || searchForCharacters(replacedText, matcher))
				paint(IPainter.INTERNAL);
		}

		/**
		 * Searches for matched characters in the given string.
		 * 
		 * @param text the string to search
		 * @param matcher the pair matcher
		 * @return <code>true</code> if a matched character is found, <code>false</code> otherwise
		 */
		private boolean searchForCharacters(String text, ICharacterPairMatcherExtension matcher) {
			if (text == null)
				return false;
			for (int i= 0; i < text.length(); i++) {
				if (matcher.isMatchedChar(text.charAt(i))) {
					return true;
				}
			}
			return false;
		}
	}
}
