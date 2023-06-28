/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	protected List<ITextInputListener> fInputListeners= new ArrayList<>();
	protected List<ITextListener> fTextListeners= new ArrayList<>();
	protected TextPresentation fTextPresentation;
	protected Point fSelection= new Point(-1, -1);
	protected String fDeletion;

	/**
	 * @see ITextViewer#setDocument(IDocument, int, int)
	 */
	@Override
	public void setDocument(IDocument document, int p1, int p2) {
		setDocument(document);
	}

	/**
	 * @see ITextViewer#getDocument()
	 */
	@Override
	public IDocument getDocument() {
		return fDocument;
	}

	/**
	 * @see ITextViewer#setDocument(IDocument)
	 */
	@Override
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
	@Override
	public void removeTextInputListener(ITextInputListener listener) {
		fInputListeners.remove(listener);
	}

	/**
	 * @see ITextViewer#addTextInputListener(ITextInputListener)
	 */
	@Override
	public void addTextInputListener(ITextInputListener listener) {
		if (!fInputListeners.contains(listener))
			fInputListeners.add(listener);
	}

	protected void fireTextInputChanged(IDocument oldDoc, IDocument newDoc, boolean about) {
		Iterator<ITextInputListener> e= new ArrayList<>(fInputListeners).iterator();
		while (e.hasNext()) {
			ITextInputListener l= e.next();
			if (about)
				l.inputDocumentAboutToBeChanged(oldDoc, newDoc);
			else
				l.inputDocumentChanged(oldDoc, newDoc);
		}
	}

	/**
	 * @see ITextViewer#changeTextPresentation(TextPresentation, boolean)
	 */
	@Override
	public void changeTextPresentation(TextPresentation presentation, boolean p1) {
		fTextPresentation= presentation;
	}

	/**
	 * @see ITextViewer#invalidateTextPresentation()
	 */
	@Override
	public void invalidateTextPresentation() {
	}

	public TextPresentation getTextPresentation() {
		return fTextPresentation;
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		try {
			fDeletion= fDocument.get(event.getOffset(), event.getLength());
		} catch (BadLocationException x) {
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		fireTextChanged(new TestTextEvent(event, fDeletion));
	}

	/**
	 * @see ITextViewer#getFindReplaceTarget()
	 */
	@Override
	public IFindReplaceTarget getFindReplaceTarget() {
		return null;
	}

	/**
	 * @see ITextViewer#getTextOperationTarget()
	 */
	@Override
	public ITextOperationTarget getTextOperationTarget() {
		return null;
	}

	/**
	 * @see ITextViewer#setTextColor(Color, int, int, boolean)
	 */
	@Override
	public void setTextColor(Color p0, int p1, int p2, boolean p3) {
	}

	/**
	 * @see ITextViewer#setTextColor(Color)
	 */
	@Override
	public void setTextColor(Color p0) {
	}

	/**
	 * @see ITextViewer#overlapsWithVisibleRegion(int, int)
	 */
	@Override
	public boolean overlapsWithVisibleRegion(int p0, int p1) {
		return false;
	}

	/**
	 * @see ITextViewer#getVisibleRegion()
	 */
	@Override
	public IRegion getVisibleRegion() {
		return null;
	}

	/**
	 * @see ITextViewer#resetVisibleRegion()
	 */
	@Override
	public void resetVisibleRegion() {
	}

	/**
	 * @see ITextViewer#setVisibleRegion(int, int)
	 */
	@Override
	public void setVisibleRegion(int p0, int p1) {
	}

	/**
	 * @see ITextViewer#setIndentPrefixes(String[], String)
	 */
	@Override
	public void setIndentPrefixes(String[] p0, String p1) {
	}

	/**
	 * @see ITextViewer#setDefaultPrefixes(String[], String)
	 */
	@Override
	public void setDefaultPrefixes(String[] p0, String p1) {
	}

	/**
	 * @see ITextViewer#setAutoIndentStrategy(IAutoIndentStrategy, String)
	 */
	@Override
	public void setAutoIndentStrategy(IAutoIndentStrategy p0, String p1) {
	}

	/**
	 * @see ITextViewer#setTextDoubleClickStrategy(ITextDoubleClickStrategy, String)
	 */
	@Override
	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy p0, String p1) {
	}

	/**
	 * @see ITextViewer#setUndoManager(IUndoManager)
	 */
	@Override
	public void setUndoManager(IUndoManager p0) {
	}

	/**
	 * @see ITextViewer#getTextWidget()
	 */
	@Override
	public StyledText getTextWidget() {
		return null;
	}

	@Override
	public void setTextHover(ITextHover p0, String p1) {
	}

	/**
	 * @see ITextViewer#activatePlugins()
	 */
	@Override
	public void activatePlugins() {
	}

	/**
	 * @see ITextViewer#resetPlugins()
	 */
	@Override
	public void resetPlugins() {
	}

	/**
	 * @see ITextViewer#getTopInset()
	 */
	@Override
	public int getTopInset() {
		return 0;
	}

	/**
	 * @see ITextViewer#getBottomIndexEndOffset()
	 */
	@Override
	public int getBottomIndexEndOffset() {
		return 0;
	}

	/**
	 * @see ITextViewer#getBottomIndex()
	 */
	@Override
	public int getBottomIndex() {
		return 0;
	}

	/**
	 * @see ITextViewer#getTopIndexStartOffset()
	 */
	@Override
	public int getTopIndexStartOffset() {
		return 0;
	}

	/**
	 * @see ITextViewer#getTopIndex()
	 */
	@Override
	public int getTopIndex() {
		return 0;
	}

	/**
	 * @see ITextViewer#setTopIndex(int)
	 */
	@Override
	public void setTopIndex(int p0) {
	}

	/**
	 * @see ITextViewer#revealRange(int, int)
	 */
	@Override
	public void revealRange(int p0, int p1) {
	}

	/**
	 * @see ITextViewer#getSelectedRange()
	 */
	@Override
	public Point getSelectedRange() {
		return fSelection;
	}

	/**
	 * @see ITextViewer#setSelectedRange(int, int)
	 */
	@Override
	public void setSelectedRange(int offset, int length) {
		fSelection.x= offset;
		fSelection.y= length;
	}

	/**
	 * @see ITextViewer#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return true;
	}

	/**
	 * @see ITextViewer#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean p0) {
	}

	/**
	 * @see ITextViewer#setEventConsumer(IEventConsumer)
	 */
	@Override
	public void setEventConsumer(IEventConsumer p0) {
	}

	/**
	 * @see ITextViewer#removeTextListener(ITextListener)
	 */
	@Override
	public void removeTextListener(ITextListener listener) {
		fTextListeners.remove(listener);
	}

	protected void fireTextChanged(TextEvent event) {
		Iterator<ITextListener> e= new ArrayList<>(fTextListeners).iterator();
		while (e.hasNext()) {
			ITextListener l= e.next();
			l.textChanged(event);
		}
	}

	/**
	 * @see ITextViewer#addTextListener(ITextListener)
	 */
	@Override
	public void addTextListener(ITextListener listener) {
		if (!fTextListeners.contains(listener))
			fTextListeners.add(listener);
	}

	/**
	 * @see ITextViewer#removeViewportListener(IViewportListener)
	 */
	@Override
	public void removeViewportListener(IViewportListener p0) {
	}

	/**
	 * @see ITextViewer#addViewportListener(IViewportListener)
	 */
	@Override
	public void addViewportListener(IViewportListener p0) {
	}

	/**
	 * @see ISourceViewer#getSelectionProvider()
	 */
	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * @see ISourceViewer#showAnnotations(boolean)
	 */
	@Override
	public void showAnnotations(boolean p0) {
	}

	/**
	 * @see ISourceViewer#removeRangeIndication()
	 */
	@Override
	public void removeRangeIndication() {
	}

	/**
	 * @see ISourceViewer#getRangeIndication()
	 */
	@Override
	public IRegion getRangeIndication() {
		return null;
	}

	/**
	 * @see ISourceViewer#setRangeIndication(int, int, boolean)
	 */
	@Override
	public void setRangeIndication(int p0, int p1, boolean p2) {
	}

	/**
	 * @see ISourceViewer#setRangeIndicator(Annotation)
	 */
	@Override
	public void setRangeIndicator(Annotation p0) {
	}

	/**
	 * @see ISourceViewer#getAnnotationModel()
	 */
	@Override
	public IAnnotationModel getAnnotationModel() {
		return null;
	}

	/**
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
	 */
	@Override
	public void setDocument(IDocument p0, IAnnotationModel p1, int p2, int p3) {
	}

	/**
	 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel)
	 */
	@Override
	public void setDocument(IDocument p0, IAnnotationModel p1) {
	}

	/**
	 * @see ISourceViewer#setAnnotationHover(IAnnotationHover)
	 */
	@Override
	public void setAnnotationHover(IAnnotationHover p0) {
	}

	/**
	 * @see ISourceViewer#configure(SourceViewerConfiguration)
	 */
	@Override
	public void configure(SourceViewerConfiguration p0) {
	}
}
