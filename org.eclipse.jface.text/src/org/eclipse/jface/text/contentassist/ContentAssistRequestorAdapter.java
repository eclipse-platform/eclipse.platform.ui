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

package org.eclipse.jface.text.contentassist;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;

/**
 * This content assist adapter delegates the calles either to
 * a text viewer or to a content assist requestor.
 * 
 * @since 3.0
 */
final class ContentAssistRequestorAdapter implements IContentAssistRequestor {

	/**
	 * The text viewer which is used as content assist requestor.
	 */
	private ITextViewer fViewer;
	
	/**
	 * The content assist requestor.
	 */
	private IContentAssistRequestor fContentAssistRequestor;

	
	/**
	 * Creates an adapter for the given content assist requestor.
	 * 
	 * @param contentAssistRequestor the content assist requestor
	 */
	public ContentAssistRequestorAdapter(IContentAssistRequestor contentAssistRequestor) {
		Assert.isNotNull(contentAssistRequestor);
		fContentAssistRequestor= contentAssistRequestor;
	}

	/**
	 * Creates an adapter for the given text viewer.
	 * 
	 * @param viewer the text viewer
	 */
	public ContentAssistRequestorAdapter(ITextViewer viewer) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
	}

	/*
	 * @see IContentAssistRequestor#getLineHeight()
	 */
	public int getLineHeight() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getLineHeight();
		else
			return fViewer.getTextWidget().getLineHeight();
	}

	/*
	 * @see IContentAssistRequestor#getControl()
	 */
	public Control getControl() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getControl();
		else
			return fViewer.getTextWidget();
	}

	/*
	 * @see IContentAssistRequestor#getLocationAtOffset(int)
	 */
	public Point getLocationAtOffset(int offset) {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getLocationAtOffset(offset);
		else
			return fViewer.getTextWidget().getLocationAtOffset(offset);
	}

	/*
	 * @see IContentAssistRequestor#getWidgetSelectionRange()
	 */
	public Point getWidgetSelectionRange() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getWidgetSelectionRange();
		else
			return fViewer.getTextWidget().getSelectionRange();
	}

	/*
	 * @see IContentAssistRequestor#getSelectedRange()
	 */
	public Point getSelectedRange() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getSelectedRange();
		else
			return fViewer.getSelectedRange();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#getCaretOffset()
	 */
	public int getCaretOffset() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getCaretOffset();
		else
			return fViewer.getTextWidget().getCaretOffset();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getLineDelimiter();
		else
			return fViewer.getTextWidget().getLineDelimiter();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener keyListener) {
		if (fContentAssistRequestor != null)
			fContentAssistRequestor.addKeyListener(keyListener);
		else
			fViewer.getTextWidget().addKeyListener(keyListener);
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener keyListener) {
		if (fContentAssistRequestor != null)
			fContentAssistRequestor.removeKeyListener(keyListener);
		else
			fViewer.getTextWidget().removeKeyListener(keyListener);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#getDocument()
	 */
	public IDocument getDocument() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.getDocument();
		else
			return fViewer.getDocument();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#prependVerifyKeyListener(VerifyKeyListener)
	 */
	public boolean prependVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistRequestor != null) {
			return fContentAssistRequestor.prependVerifyKeyListener(verifyKeyListener);
		} else if (fViewer instanceof ITextViewerExtension) {
			ITextViewerExtension e= (ITextViewerExtension) fViewer;
			e.prependVerifyKeyListener(verifyKeyListener);
			return true;
		} else {
			
			StyledText textWidget= fViewer.getTextWidget();
			if (Helper.okToUse(textWidget)) {
				textWidget.addVerifyKeyListener(verifyKeyListener);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#appendVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean appendVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.appendVerifyKeyListener(verifyKeyListener);
		else if (fViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension)fViewer;
			extension.appendVerifyKeyListener(verifyKeyListener);
			return true;
		} else {
			StyledText textWidget= fViewer.getTextWidget();
			if (Helper.okToUse(textWidget)) {
				textWidget.addVerifyKeyListener(verifyKeyListener);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#removeVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistRequestor != null) {
			fContentAssistRequestor.removeVerifyKeyListener(verifyKeyListener);
		} else if (fViewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension= (ITextViewerExtension) fViewer;
			extension.removeVerifyKeyListener(verifyKeyListener);
		} else {
			StyledText textWidget= fViewer.getTextWidget();
			if (Helper.okToUse(textWidget))
				textWidget.removeVerifyKeyListener(verifyKeyListener);
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#setEventConsumer(org.eclipse.jface.text.contentassist.ContentAssistant.InternalListener)
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		if (fContentAssistRequestor != null)
			fContentAssistRequestor.setEventConsumer(eventConsumer);
		else
			fViewer.setEventConsumer(eventConsumer);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#setSelectedRange(int, int)
	 */
	public void setSelectedRange(int i, int j) {
		if (fContentAssistRequestor != null)
			fContentAssistRequestor.setSelectedRange(i, j);
		else
			fViewer.getTextWidget().setSelectionRange(i, j);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#revealRange(int, int)
	 */
	public void revealRange(int i, int j) {
		if (fContentAssistRequestor != null)
			fContentAssistRequestor.revealRange(i, j);
		else
			fViewer.revealRange(i, j);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistRequestor#canAddVerifyKeyListener()
	 */
	public boolean supportsVerifyKeyListener() {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.supportsVerifyKeyListener();
		else
			return true;
	}
}
