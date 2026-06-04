/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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

	@Override
	public void setDocument(IDocument document, int p1, int p2) {
		setDocument(document);
	}

	@Override
	public IDocument getDocument() {
		return fDocument;
	}

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

	@Override
	public void removeTextInputListener(ITextInputListener listener) {
		fInputListeners.remove(listener);
	}

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

	@Override
	public void changeTextPresentation(TextPresentation presentation, boolean p1) {
		fTextPresentation= presentation;
	}

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

	@Override
	public IFindReplaceTarget getFindReplaceTarget() {
		return null;
	}

	@Override
	public ITextOperationTarget getTextOperationTarget() {
		return null;
	}

	@Override
	public void setTextColor(Color p0, int p1, int p2, boolean p3) {
	}

	@Override
	public void setTextColor(Color p0) {
	}

	@Override
	public boolean overlapsWithVisibleRegion(int p0, int p1) {
		return false;
	}

	@Override
	public IRegion getVisibleRegion() {
		return null;
	}

	@Override
	public void resetVisibleRegion() {
	}

	@Override
	public void setVisibleRegion(int p0, int p1) {
	}

	@Override
	public void setIndentPrefixes(String[] p0, String p1) {
	}

	@Override
	public void setDefaultPrefixes(String[] p0, String p1) {
	}

	@Deprecated
	@Override
	public void setAutoIndentStrategy(IAutoIndentStrategy p0, String p1) {
	}

	@Override
	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy p0, String p1) {
	}

	@Override
	public void setUndoManager(IUndoManager p0) {
	}

	@Override
	public StyledText getTextWidget() {
		return null;
	}

	@Override
	public void setTextHover(ITextHover p0, String p1) {
	}

	@Override
	public void activatePlugins() {
	}

	@Override
	public void resetPlugins() {
	}

	@Override
	public int getTopInset() {
		return 0;
	}

	@Override
	public int getBottomIndexEndOffset() {
		return 0;
	}

	@Override
	public int getBottomIndex() {
		return 0;
	}

	@Override
	public int getTopIndexStartOffset() {
		return 0;
	}

	@Override
	public int getTopIndex() {
		return 0;
	}

	@Override
	public void setTopIndex(int p0) {
	}

	@Override
	public void revealRange(int p0, int p1) {
	}

	@Override
	public Point getSelectedRange() {
		return fSelection;
	}

	@Override
	public void setSelectedRange(int offset, int length) {
		fSelection.x= offset;
		fSelection.y= length;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public void setEditable(boolean p0) {
	}

	@Override
	public void setEventConsumer(IEventConsumer p0) {
	}

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

	@Override
	public void addTextListener(ITextListener listener) {
		if (!fTextListeners.contains(listener))
			fTextListeners.add(listener);
	}

	@Override
	public void removeViewportListener(IViewportListener p0) {
	}

	@Override
	public void addViewportListener(IViewportListener p0) {
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public void showAnnotations(boolean p0) {
	}

	@Override
	public void removeRangeIndication() {
	}

	@Override
	public IRegion getRangeIndication() {
		return null;
	}

	@Override
	public void setRangeIndication(int p0, int p1, boolean p2) {
	}

	@Override
	public void setRangeIndicator(Annotation p0) {
	}

	@Override
	public IAnnotationModel getAnnotationModel() {
		return null;
	}

	@Override
	public void setDocument(IDocument p0, IAnnotationModel p1, int p2, int p3) {
	}

	@Override
	public void setDocument(IDocument p0, IAnnotationModel p1) {
	}

	@Override
	public void setAnnotationHover(IAnnotationHover p0) {
	}

	@Override
	public void configure(SourceViewerConfiguration p0) {
	}
}
