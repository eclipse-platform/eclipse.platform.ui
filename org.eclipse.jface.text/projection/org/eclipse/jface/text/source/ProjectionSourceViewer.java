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


import java.util.Iterator;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISlaveDocumentManager;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.ProjectionDocument;
import org.eclipse.jface.text.ProjectionDocumentManager;


public class ProjectionSourceViewer extends SourceViewer implements ISourceViewer, ITextViewerExtension3 {
	
	/** The projection annotation model */
	private IAnnotationModel fProjectionAnnotationModel;
	
	
	/**
	 * Constructor for ProjectionSourceViewer.
	 * @param parent
	 * @param ruler
	 * @param styles
	 */
	public ProjectionSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super(parent, ruler, styles);
	}
	
	/*
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument document, IAnnotationModel annotationModel, int visibleRegionOffset, int visibleRegionLength) {
		if (getDocument() != null && fProjectionAnnotationModel != null)
			fProjectionAnnotationModel.disconnect(getDocument());
		
		super.setDocument(document, annotationModel, visibleRegionOffset, visibleRegionLength);
		
		if (getDocument() != null && fProjectionAnnotationModel != null)
			fProjectionAnnotationModel.connect(getDocument());
	}
	
	/*
	 * @see TextViewer#handleDispose
	 */
	protected void handleDispose() {
		
		if (getDocument() != null && fProjectionAnnotationModel != null) {
			fProjectionAnnotationModel.disconnect(getDocument());
			fProjectionAnnotationModel= null;
		}

		super.handleDispose();
	}
	
	/**
	 * Returns the projectionAnnotationModel.
	 * @return IAnnotationModel
	 */
	public IAnnotationModel getProjectionAnnotationModel() {
		return fProjectionAnnotationModel;
	}

	/**
	 * Sets the projectionAnnotationModel.
	 * @param projectionAnnotationModel The projectionAnnotationModel to set
	 */
	public void setProjectionAnnotationModel(IAnnotationModel projectionAnnotationModel) {
		fProjectionAnnotationModel= projectionAnnotationModel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.TextViewer#createSlaveDocumentManager()
	 */
	protected ISlaveDocumentManager createSlaveDocumentManager() {
		return new ProjectionDocumentManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.TextViewer#updateVisibleDocument(org.eclipse.jface.text.IDocument, int, int)
	 */
	protected boolean updateVisibleDocument(IDocument visibleDocument, int visibleRegionOffset, int visibleRegionLength) throws BadLocationException {
		if (visibleDocument instanceof ProjectionDocument) {
			ProjectionDocument document= (ProjectionDocument) visibleDocument;
			document.addFragment(visibleRegionOffset, visibleRegionLength);
		}
		return true;
	}
	
	/**
	 * Hides the given range by collapsing it.
	 *
	 * @param offset
	 * @param length
	 */
	public void collapse(int offset, int length) {
		
		IDocument previous= getVisibleDocument();
		IDocument slave= createSlaveDocument(previous);
		
		if (slave instanceof ProjectionDocument) {
			
			StyledText textWidget= getTextWidget();
			try {
			
				if (textWidget != null)
					textWidget.setRedraw(false);
					
				
				int topIndex= getTopIndex();
				Point selection= getSelectedRange();
				
				// adapt selection
				int selectionEnd= selection.x + selection.y;
				if (offset < selectionEnd && selectionEnd <= offset + length) {
					
					int lineEnd= offset;
					
					try {
						IDocument document= getDocument();
						int line= document.getLineOfOffset(offset);
						IRegion lineInfo= document.getLineInformation(Math.max(line -1, 0));
						lineEnd= lineInfo.getOffset() + lineInfo.getLength();
					} catch (BadLocationException x) {
					}
					
					if (offset <= selection.x && selection.x < offset + length) {
						selection.x= lineEnd;
						selection.y= 0;
					} else {
						selection.y= Math.max(lineEnd - selection.x, 0);
					}
					
				} else if (offset <= selection.x && selection.x < offset + length) {
					int delta= offset + length - selection.x;
					selection.x= offset + length;
					selection.y -= delta;
				} 
				
				((ProjectionDocument) slave).hide(offset, length);				
				setVisibleDocument(slave);
				
				setSelectedRange(selection.x, selection.y);
				setTopIndex(topIndex);
				
			} finally {
				if(textWidget != null)
					textWidget.setRedraw(true);
			}
		}
	}

	/**
	 * Makes all hidden ranges in the given range visible again.
	 *
	 * @param offset
	 * @param length
	 */
	public void expand(int offset, int length) {
		if (getVisibleDocument() instanceof ProjectionDocument) {
			ProjectionDocument document= (ProjectionDocument) getVisibleDocument();
			
			StyledText textWidget= getTextWidget();
			try {
				
				if (textWidget != null)
					textWidget.setRedraw(false);
			
				Point selection= getSelectedRange();
				int topIndex= getTopIndex();
				
				document.show(offset, length);
				setVisibleDocument(document);
				
				setSelectedRange(selection.x, selection.y);
				setTopIndex(topIndex);
			
			} finally {
				if (textWidget != null)
					textWidget.setRedraw(true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextViewer#getVisibleRegion()
	 */
	public IRegion getVisibleRegion() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.TextViewer#getVisibleRegionOffset()
	 */
	protected int getVisibleRegionOffset() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.TextViewer#internalGetVisibleRegion()
	 */
	protected IRegion internalGetVisibleRegion() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextViewer#overlapsWithVisibleRegion(int,int)
	 */
	public boolean overlapsWithVisibleRegion(int offset, int length) {
		return false;
	}
	
	public IDocument getVisibleDocument() {
		return super.getVisibleDocument();
	}
	
	protected void handleVerifyEvent(VerifyEvent e) {
		IRegion modelRange= event2ModelRange(e);
		Iterator iterator= fProjectionAnnotationModel.getAnnotationIterator();
		while (iterator.hasNext()) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) iterator.next();
			if (annotation.isFolded()) {
				Position position= fProjectionAnnotationModel.getPosition(annotation);
				if (position.overlapsWith(modelRange.getOffset(), modelRange.getLength()) /* || is a delete at the boundary */ ) {
					e.doit= false;
					annotation.run(this);
				}
			}
		}
	}
}
