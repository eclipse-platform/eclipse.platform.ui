/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.source;


import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.Assert;



/**
 * This manager controls the layout, content, visibility, etc. of an information
 * control in reaction to mouse hover events issued by the vertical ruler of a
 * source viewer.
 * @since 2.0
 */
class AnnotationBarHoverManager extends AbstractHoverInformationControlManager {
	
	/** The source viewer the manager is connected to */
	private ISourceViewer fSourceViewer;
	/** The vertical ruler the manager is registered with */
	private IVerticalRuler fVerticalRuler;
	/** The annotation hover the manager uses to retrieve the information to display */
	private IAnnotationHover fAnnotationHover;
	
	
	/**
	 * Creates an annotation hover manager with the given parameters. In addition,
	 * the hovers anchor is RIGHT and the margin is 5 points to the right.
	 *
	 * @param sourceViewer the source viewer this manager connects to
	 * @param ruler the vertical ruler this manager connects to
	 * @param annotationHover the annotation hover providing the information to be displayed
	 * @param creator the information control creator
	 */
	public AnnotationBarHoverManager(ISourceViewer sourceViewer, IVerticalRuler ruler, IAnnotationHover annotationHover, IInformationControlCreator creator) {
		super(creator);
		
		Assert.isNotNull(sourceViewer);
		Assert.isNotNull(annotationHover);
		
		fSourceViewer= sourceViewer;
		fVerticalRuler= ruler;
		fAnnotationHover= annotationHover;
		
		setAnchor(ANCHOR_RIGHT);
		setMargins(5, 0);
	}	
	
	/*
	 * @see AbstractHoverInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		Point location= getHoverEventLocation();
		int line= fVerticalRuler.toDocumentLineNumber(location.y);
		setInformation(fAnnotationHover.getHoverInfo(fSourceViewer, line), computeArea(line));
	}
	
	/**
	 * Returns for a given absolute line number the corresponding line
	 * number relative to the viewer's visible region.
	 *
	 * @param line the absolute line number
	 * @return the line number relative to the viewer's visible region
	 * @throws BadLocationException if <code>line</code> is not valid in the viewer's document
	 */
	private int getRelativeLineNumber(int line) throws BadLocationException {
		IRegion region= fSourceViewer.getVisibleRegion();
		int firstLine= fSourceViewer.getDocument().getLineOfOffset(region.getOffset());
		return line - firstLine;
	}
	
	/**
	 * Determines graphical area covered by the given line.
	 *
	 * @param line the number of the line in the viewer whose graphical extend in the vertical ruler must be computed
	 * @return the graphical extend of the given line
	 */
	private Rectangle computeArea(int line) {
		try {
			StyledText text= fSourceViewer.getTextWidget();
			int lineHeight= text.getLineHeight();
			int y= getRelativeLineNumber(line) * lineHeight - text.getTopPixel();
			Point size= fVerticalRuler.getControl().getSize();
			return new Rectangle(0, y, size.x, lineHeight);
		} catch (BadLocationException x) {
		}
		return null;
	}
}

