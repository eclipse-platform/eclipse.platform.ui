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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlCreatorExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension3;



/**
 * This manager controls the layout, content, and visibility of an information
 * control in reaction to mouse hover events issued by the vertical ruler of a
 * source viewer.
 * @since 2.0
 */
public class AnnotationBarHoverManager extends AbstractHoverInformationControlManager {
	
	/** The source viewer the manager is connected to */
	private ISourceViewer fSourceViewer;
	/** The vertical ruler the manager is registered with */
	private IVerticalRulerInfo fVerticalRulerInfo;
	/** The annotation hover the manager uses to retrieve the information to display */
	private IAnnotationHover fAnnotationHover;
	/**
	 * The custom information control creator.
	 * @since 3.0
	 */
	private volatile IInformationControlCreator fCustomInformationControlCreator;	
	/**
	 * Tells whether a custom information control is in use.
	 * @since 3.0
	 */
	private boolean fIsCustomInformtionControl= false;
	

	/**
	 * Creates an annotation hover manager with the given parameters. In addition,
	 * the hovers anchor is RIGHT and the margin is 5 points to the right.
	 *
	 * @param sourceViewer the source viewer this manager connects to
	 * @param ruler the vertical ruler this manager connects to
	 * @param annotationHover the annotation hover providing the information to be displayed
	 * @param creator the information control creator
	 * @deprecated As of 2.1, replaced by {@link AnnotationBarHoverManager#AnnotationBarHoverManager(IVerticalRulerInfo, ISourceViewer, IAnnotationHover, IInformationControlCreator)}
	 */
	public AnnotationBarHoverManager(ISourceViewer sourceViewer, IVerticalRuler ruler, IAnnotationHover annotationHover, IInformationControlCreator creator) {
		this(ruler, sourceViewer, annotationHover, creator);
	}
	
	/**
	 * Creates an annotation hover manager with the given parameters. In addition,
	 * the hovers anchor is RIGHT and the margin is 5 points to the right.
	 *
	 * @param ruler the vertical ruler this manager connects to
	 * @param sourceViewer the source viewer this manager connects to
	 * @param annotationHover the annotation hover providing the information to be displayed
	 * @param creator the information control creator
	 * @since 2.1
	 */
	public AnnotationBarHoverManager(IVerticalRulerInfo rulerInfo, ISourceViewer sourceViewer, IAnnotationHover annotationHover, IInformationControlCreator creator) {
		super(creator);
		
		Assert.isNotNull(sourceViewer);
		Assert.isNotNull(annotationHover);
		
		fSourceViewer= sourceViewer;
		fVerticalRulerInfo= rulerInfo;
		fAnnotationHover= annotationHover;
		
		setAnchor(ANCHOR_RIGHT);
		setMargins(5, 0);
	}	
	
	/*
	 * @see AbstractHoverInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {

		fCustomInformationControlCreator= null;
		if (fAnnotationHover instanceof IAnnotationHoverExtension)
			fCustomInformationControlCreator= ((IAnnotationHoverExtension) fAnnotationHover).getInformationControlCreator();	
		
		Point location= getHoverEventLocation();
		int line= fVerticalRulerInfo.toDocumentLineNumber(location.y);
		setInformation(fAnnotationHover.getHoverInfo(fSourceViewer, line), computeArea(line));
	}
	
	/**
	 * Returns for the widget line number for the given document line number.
	 * 
	 * @param line the absolute line number
	 * @return the line number relative to the viewer's visible region
	 * @throws BadLocationException if <code>line</code> is not valid in the viewer's document
	 */
	private int getWidgetLineNumber(int line) throws BadLocationException {
		if (fSourceViewer instanceof ITextViewerExtension3) {
			ITextViewerExtension3 extension= (ITextViewerExtension3) fSourceViewer;
			return extension.modelLine2WidgetLine(line);
		}
		
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
			int y= getWidgetLineNumber(line) * lineHeight - text.getTopPixel();
			Point size= fVerticalRulerInfo.getControl().getSize();
			return new Rectangle(0, y, size.x, lineHeight);
		} catch (BadLocationException x) {
		}
		return null;
	}
	
	/**
	 * Returns the annotation hover for this hover manager.
	 * 
	 * @return the annotation hover for this hover manager
	 * @since 2.1
	 */
	protected IAnnotationHover getAnnotationHover() {
		return fAnnotationHover;
	}

	/**
	 * Returns the source viewer for this hover manager.
	 * 
	 * @return the source viewer for this hover manager
	 * @since 2.1
	 */
	protected ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}

	/**
	 * Returns the vertical ruler info for this hover manager
	 * 
	 * @return the vertical ruler info for this hover manager
	 * @since 2.1
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fVerticalRulerInfo;
	}
	
	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#getInformationControl()
	 * @since 3.0
	 */
	protected IInformationControl getInformationControl() {
		
		if (fCustomInformationControlCreator == null || fDisposed) {
			if (fIsCustomInformtionControl) {
				fInformationControl.dispose();
				fInformationControl= null;
			}
			fIsCustomInformtionControl= false;
			return super.getInformationControl();
		}

		if ((fCustomInformationControlCreator instanceof IInformationControlCreatorExtension)
				&& ((IInformationControlCreatorExtension) fCustomInformationControlCreator).canBeReused(fInformationControl))
			return fInformationControl;

		if (fInformationControl != null)
			fInformationControl.dispose();
			
		fInformationControl= fCustomInformationControlCreator.createInformationControl(getSubjectControl().getShell());
		fIsCustomInformtionControl= true;
		fInformationControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleInformationControlDisposed();
			}
		});
			
		if (fInformationControlCloser != null)
			fInformationControlCloser.setInformationControl(fInformationControl);

		return fInformationControl;
	}
}

