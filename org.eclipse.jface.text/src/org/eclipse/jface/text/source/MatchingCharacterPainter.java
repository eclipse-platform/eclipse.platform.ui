/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;

/**
 * Highlights the peer character matching the character at the current
 * selection.
 */
public final class MatchingCharacterPainter implements IPainter, PaintListener {
			
	private boolean fIsActive= false;
	private ISourceViewer fSourceViewer;
	private StyledText fTextWidget;
	private Color fColor;
	
	private IPaintPositionManager fPaintPositionManager;
	private ICharacterPairMatcher fMatcher;
	private Position fPairPosition= new Position(0, 0);
	private int fAnchor;

	
	/**
	 * Creates a new MatchingCharacterPainter for the given source viewer using
	 * the given character pair matcher. The character matcher is not adopted by
	 * this painter. Thus,  it is not disposed. However, this painters requires
	 * exlucsive access to the given pair matcher.
	 * 
	 * @param sourceViewer
	 * @param matcher
	 */
	public MatchingCharacterPainter(ISourceViewer sourceViewer, ICharacterPairMatcher matcher) {
		fSourceViewer= sourceViewer;
		fMatcher= matcher;
		fTextWidget= sourceViewer.getTextWidget();
	}
	
	public void setColor(Color color) {
		fColor= color;
	}
				
	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		if (fMatcher != null) {
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
	
	private void handleDrawRequest(GC gc) {
		
		if (fPairPosition.isDeleted)
			return;
			
		int offset= fPairPosition.getOffset();
		int length= fPairPosition.getLength();
		if (length < 1)
			return;
			
		if (fSourceViewer instanceof ITextViewerExtension3) {
			ITextViewerExtension3 extension= (ITextViewerExtension3) fSourceViewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
			if (widgetRange == null)
				return;
				
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			IRegion region= fSourceViewer.getVisibleRegion();
			if (region.getOffset() > offset || region.getOffset() + region.getLength() < offset + length)
				return;
			offset -= region.getOffset();
		}
			
		if (ICharacterPairMatcher.RIGHT == fAnchor)
			draw(gc, offset, 1);
		else 
			draw(gc, offset + length -1, 1);
	}
	
	private void draw(GC gc, int offset, int length) {
		if (gc != null) {
			Point left= fTextWidget.getLocationAtOffset(offset);
			Point right= fTextWidget.getLocationAtOffset(offset + length);
			
			gc.setForeground(fColor);
			gc.drawRectangle(left.x, left.y, right.x - left.x - 1, gc.getFontMetrics().getHeight() - 1);
								
		} else {
			fTextWidget.redrawRange(offset, length, true);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IPainter#paint(int)
	 */
	public void paint(int reason) {

		IDocument document= fSourceViewer.getDocument();
		if (document == null) {
			deactivate(false);
			return;
		}

		Point selection= fSourceViewer.getSelectedRange();
		if (selection.y > 0) {
			deactivate(true);
			return;
		}
		
		IRegion pair= fMatcher.match(document, selection.x);
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
					fMatcher.getAnchor() != fAnchor) {
				
				// otherwise only do something if position is different
				
				// remove old highlighting
				handleDrawRequest(null);
				// update position
				fPairPosition.isDeleted= false;
				fPairPosition.offset= pair.getOffset();
				fPairPosition.length= pair.getLength();
				fAnchor= fMatcher.getAnchor();
				// apply new highlighting
				handleDrawRequest(null);
			
			}
		} else {
			
			fIsActive= true;
			
			fPairPosition.isDeleted= false;
			fPairPosition.offset= pair.getOffset();
			fPairPosition.length= pair.getLength();
			fAnchor= fMatcher.getAnchor();
			
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
}
