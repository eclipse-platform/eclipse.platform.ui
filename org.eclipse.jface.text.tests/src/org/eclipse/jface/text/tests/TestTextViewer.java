/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;




public class TestTextViewer implements ISourceViewer, IDocumentListener {

	
	protected IDocument fDocument;
	protected List fInputListeners= new ArrayList();
	protected List fTextListeners= new ArrayList();
	protected TextPresentation fTextPresentation;
	protected Point fSelection= new Point(-1, -1);
	protected String fDeletion;
	
	/**
	 * @see ITextViewer#setDocument(IDocument, int, int)
	 */
	public void setDocument(IDocument document, int p1, int p2) {
		setDocument(document);
	}

	/**
	 * @see ITextViewer#getDocument()
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/**
	 * @see ITextViewer#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {
		IDocument oldDoc= fDocument;
		fireTextInputChanged(oldDoc, document, true);
		
		if (oldDoc != null)
			oldDoc.removeDocumentListener(this);
		
		fDocument= document;
		
		if (fDocument != null) {
			fireTextChanged(new TestTextEvent(fDocument.get()));
			fDocument.addDocumentListener(this);
		}
			
		fireTextInputChanged(oldDoc, document, false);
	}

	/**
	 * @see ITextViewer#removeTextInputListener(ITextInputListener)
	 */
	public void removeTextInputListener(ITextInputListener listener) {
		fInputListeners.remove(listener);
	}

	/**
	 * @see ITextViewer#addTextInputListener(ITextInputListener)
	 */
	public void addTextInputListener(ITextInputListener listener) {
		if (!fInputListeners.contains(listener)) 
			fInputListeners.add(listener);
	}
	
	protected void fireTextInputChanged(IDocument oldDoc, IDocument newDoc, boolean about) {
		Iterator e= new ArrayList(fInputListeners).iterator();
		while (e.hasNext()) {
			ITextInputListener l= (ITextInputListener) e.next();
			if (about)
				l.inputDocumentAboutToBeChanged(oldDoc, newDoc);
			else
				l.inputDocumentChanged(oldDoc, newDoc);
		}
	}
	
	/**
	 * @see ITextViewer#changeTextPresentation(TextPresentation, boolean)
	 */
	public void changeTextPresentation(TextPresentation presentation, boolean p1) {
		fTextPresentation= presentation;
	}
	
	/**
	 * @see ITextViewer#invalidateTextPresentation()
	 */
	public void invalidateTextPresentation() {
	}
	
	public TextPresentation getTextPresentation() {
		return fTextPresentation;
	}
	
	public void documentAboutToBeChanged(DocumentEvent event) {
		try {
			fDeletion= fDocument.get(event.getOffset(), event.getLength());
		} catch (BadLocationException x) {
		}
	}
	
	public void documentChanged(DocumentEvent event) {
		fireTextChanged(new TestTextEvent(event, fDeletion));
	}	
	
	/**
	 * @see ITextViewer#getFindReplaceTarget()
	 */
	public IFindReplaceTarget getFindReplaceTarget() {
		return null;
	}

	/**
	 * @see ITextViewer#getTextOperationTarget()
	 */
	public ITextOperationTarget getTextOperationTarget() {
		return null;
	}

	/**
	 * @see ITextViewer#setTextColor(Color, int, int, boolean)
	 */
	public void setTextColor(Color p0, int p1, int p2, boolean p3) {
	}

	/**
	 * @see ITextViewer#setTextColor(Color)
	 */
	public void setTextColor(Color p0) {
	}

	/**
	 * @see ITextViewer#adjustVisibleRegion(int, int)
	 */
	public void adjustVisibleRegion(int p0, int p1) {
	}

	/**
	 * @see ITextViewer#overlapsWithVisibleRegion(int, int)
	 */
	public boolean overlapsWithVisibleRegion(int p0, int p1) {
		return false;
	}

	/**
	 * @see ITextViewer#getVisibleRegion()
	 */
	public IRegion getVisibleRegion() {
		return null;
	}

	/**
	 * @see ITextViewer#resetVisibleRegion()
	 */
	public void resetVisibleRegion() {
	}

	/**
	 * @see ITextViewer#setVisibleRegion(int, int)
	 */
	public void setVisibleRegion(int p0, int p1) {
	}

	/**
	 * @see ITextViewer#setIndentPrefixes(String[], String)
	 */
	public void setIndentPrefixes(String[] p0, String p1) {
	}

	/**
	 * @see ITextViewer#setDefaultPrefixes(String, String)
	 */
	public void setDefaultPrefixes(String[] p0, String p1) {
	}

	/**
	 * @see ITextViewer#setAutoIndentStrategy(IAutoIndentStrategy, String)
	 */
	public void setAutoIndentStrategy(IAutoIndentStrategy p0, String p1) {
	}

	/**
	 * @see ITextViewer#setTextDoubleClickStrategy(ITextDoubleClickStrategy, String)
	 */
	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy p0, String p1) {
	}

	/**
	 * @see ITextViewer#setUndoManager(IUndoManager)
	 */
	public void setUndoManager(IUndoManager p0) {
	}

	/**
	 * @see ITextViewer#getTextWidget()
	 */
	public StyledText getTextWidget() {
		return null;
	}

	public void setTextHover(ITextHover p0, String p1) {
	}

	/**
	 * @see ITextViewer#activatePlugins()
	 */
	public void activatePlugins() {
	}
	
	/**
	 * @see ITextViewer#resetPlugins()
	 */
	public void resetPlugins() {
	}

	/**
	 * @see ITextViewer#getTopInset()
	 */
	public int getTopInset() {
		return 0;
	}

	/**
	 * @see ITextViewer#getBottomIndexEndOffset()
	 */
	public int getBottomIndexEndOffset() {
		return 0;
	}

	/**
	 * @see ITextViewer#getBottomIndex()
	 */
	public int getBottomIndex() {
		return 0;
	}

	/**
	 * @see ITextViewer#getTopIndexStartOffset()
	 */
	public int getTopIndexStartOffset() {
		return 0;
	}

	/**
	 * @see ITextViewer#getTopIndex()
	 */
	public int getTopIndex() {
		return 0;
	}

	/**
	 * @see ITextViewer#setTopIndex(int)
	 */
	public void setTopIndex(int p0) {
	}

	/**
	 * @see ITextViewer#revealRange(int, int)
	 */
	public void revealRange(int p0, int p1) {
	}

	/**
	 * @see ITextViewer#getSelectedRange()
	 */
	public Point getSelectedRange() {
		return fSelection;
	}

	/**
	 * @see ITextViewer#setSelectedRange(int, int)
	 */
	public void setSelectedRange(int offset, int length) {
		fSelection.x= offset;
		fSelection.y= length;
	}

	/**
	 * @see ITextViewer#isEditable()
	 */
	public boolean isEditable() {
		return true;
	}

	/**
	 * @see ITextViewer#setEditable(boolean)
	 */
	public void setEditable(boolean p0) {
	}

	/**
	 * @see ITextViewer#setEventConsumer(IEventConsumer)
	 */
	public void setEventConsumer(IEventConsumer p0) {
	}

	/**
	 * @see ITextViewer#removeTextListener(ITextListener)
	 */
	public void removeTextListener(ITextListener listener) {
		fTextListeners.remove(listener);
	}
	
	protected void fireTextChanged(TextEvent event) {
		Iterator e= new ArrayList(fTextListeners).iterator();
		while (e.hasNext()) {
			ITextListener l= (ITextListener) e.next();
			l.textChanged(event);
		}
	}

	/**
	 * @see ITextViewer#addTextListener(ITextListener)
	 */
	public void addTextListener(ITextListener listener) {
		if (!fTextListeners.contains(listener))
			fTextListeners.add(listener);
	}

	/**
	 * @see ITextViewer#removeViewportListener(IViewportListener)
	 */
	public void removeViewportListener(IViewportListener p0) {
	}

	/**
	 * @see ITextViewer#addViewportListener(IViewportListener)
	 */
	public void addViewportListener(IViewportListener p0) {
	}

	/**
	 * @see ISourceViewer#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * @see ISourceViewer#showAnnotations(boolean)
	 */
	public void showAnnotations(boolean p0) {
	}

	/**
	 * @see ISourceViewer#removeRangeIndication()
	 */
	public void removeRangeIndication() {
	}

	/**
	 * @see ISourceViewer#getRangeIndication()
	 */
	public IRegion getRangeIndication() {
		return null;
	}

	/**
	 * @see ISourceViewer#setRangeIndication(int, int, boolean)
	 */
	public void setRangeIndication(int p0, int p1, boolean p2) {
	}

	/**
	 * @see ISourceViewer#setRangeIndicator(Annotation)
	 */
	public void setRangeIndicator(Annotation p0) {
	}

	/**
	 * @see ISourceViewer#getAnnotationModel()
	 */
	public IAnnotationModel getAnnotationModel() {
		return null;
	}

	/**
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	public void setDocument(IDocument p0, IAnnotationModel p1, int p2, int p3) {
	}

	/**
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel)
	 */
	public void setDocument(IDocument p0, IAnnotationModel p1) {
	}

	/**
	 * @see ISourceViewer#setAnnotationHover(IAnnotationHover)
	 */
	public void setAnnotationHover(IAnnotationHover p0) {
	}

	/**
	 * @see ISourceViewer#configure(SourceViewerConfiguration)
	 */
	public void configure(SourceViewerConfiguration p0) {
	}
}
