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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.contentassist.ContextInformationPopup.ContextFrame;

/**
 * This content assist adapter delegates the calles either to
 * a text viewer or to a content assist subject.
 * 
 * @since 3.0
 */
final class ContentAssistSubjectAdapter implements IContentAssistSubject {

	/**
	 * The text viewer which is used as content assist subject.
	 */
	private ITextViewer fViewer;
	
	/**
	 * The content assist subject.
	 */
	private IContentAssistSubject fContentAssistSubject;

	
	/**
	 * Creates an adapter for the given content assist subject.
	 * 
	 * @param contentAssistSubject the content assist subject
	 */
	public ContentAssistSubjectAdapter(IContentAssistSubject contentAssistSubject) {
		Assert.isNotNull(contentAssistSubject);
		fContentAssistSubject= contentAssistSubject;
	}

	/**
	 * Creates an adapter for the given text viewer.
	 * 
	 * @param viewer the text viewer
	 */
	public ContentAssistSubjectAdapter(ITextViewer viewer) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
	}

	/*
	 * @see IContentAssistSubject#getLineHeight()
	 */
	public int getLineHeight() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getLineHeight();
		else
			return fViewer.getTextWidget().getLineHeight();
	}

	/*
	 * @see IContentAssistSubject#getControl()
	 */
	public Control getControl() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getControl();
		else
			return fViewer.getTextWidget();
	}

	/*
	 * @see IContentAssistSubject#getLocationAtOffset(int)
	 */
	public Point getLocationAtOffset(int offset) {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getLocationAtOffset(offset);
		else
			return fViewer.getTextWidget().getLocationAtOffset(offset);
	}

	/*
	 * @see IContentAssistSubject#getWidgetSelectionRange()
	 */
	public Point getWidgetSelectionRange() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getWidgetSelectionRange();
		else
			return fViewer.getTextWidget().getSelectionRange();
	}

	/*
	 * @see IContentAssistSubject#getSelectedRange()
	 */
	public Point getSelectedRange() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getSelectedRange();
		else
			return fViewer.getSelectedRange();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#getCaretOffset()
	 */
	public int getCaretOffset() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getCaretOffset();
		else
			return fViewer.getTextWidget().getCaretOffset();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getLineDelimiter();
		else
			return fViewer.getTextWidget().getLineDelimiter();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener keyListener) {
		if (fContentAssistSubject != null)
			fContentAssistSubject.addKeyListener(keyListener);
		else
			fViewer.getTextWidget().addKeyListener(keyListener);
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener keyListener) {
		if (fContentAssistSubject != null)
			fContentAssistSubject.removeKeyListener(keyListener);
		else
			fViewer.getTextWidget().removeKeyListener(keyListener);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#getDocument()
	 */
	public IDocument getDocument() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.getDocument();
		else
			return fViewer.getDocument();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#prependVerifyKeyListener(VerifyKeyListener)
	 */
	public boolean prependVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistSubject != null) {
			return fContentAssistSubject.prependVerifyKeyListener(verifyKeyListener);
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
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#appendVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean appendVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.appendVerifyKeyListener(verifyKeyListener);
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
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#removeVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistSubject != null) {
			fContentAssistSubject.removeVerifyKeyListener(verifyKeyListener);
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
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#setEventConsumer(org.eclipse.jface.text.contentassist.ContentAssistant.InternalListener)
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		if (fContentAssistSubject != null)
			fContentAssistSubject.setEventConsumer(eventConsumer);
		else
			fViewer.setEventConsumer(eventConsumer);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#setSelectedRange(int, int)
	 */
	public void setSelectedRange(int i, int j) {
		if (fContentAssistSubject != null)
			fContentAssistSubject.setSelectedRange(i, j);
		else
			fViewer.getTextWidget().setSelectionRange(i, j);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#revealRange(int, int)
	 */
	public void revealRange(int i, int j) {
		if (fContentAssistSubject != null)
			fContentAssistSubject.revealRange(i, j);
		else
			fViewer.revealRange(i, j);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#canAddVerifyKeyListener()
	 */
	public boolean supportsVerifyKeyListener() {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.supportsVerifyKeyListener();
		else
			return true;
	}
	/**
	 * Returns the characters which when typed by the user should automatically
	 * initiate proposing completions. The position is used to determine the 
	 * appropriate content assist processor to invoke.
	 *
	 * @param contentAssistant the content assistant
	 * @param offset a document offset
	 * @return the auto activation characters
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters
	 */
	public char[] getCompletionProposalAutoActivationCharacters(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubject != null)
			return contentAssistant.getCompletionProposalAutoActivationCharacters(fContentAssistSubject, offset);
		else
			return contentAssistant.getCompletionProposalAutoActivationCharacters(fViewer, offset);
	}
	
	/**
	 * Returns the characters which when typed by the user should automatically
	 * initiate the presentation of context information. The position is used
	 * to determine the appropriate content assist processor to invoke.
	 *
	 * @param contentAssistant the content assistant
	 * @param offset a document offset
	 * @return the auto activation characters
	 *
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters
	 */
	char[] getContextInformationAutoActivationCharacters(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubject != null)
			return contentAssistant.getContextInformationAutoActivationCharacters(fContentAssistSubject, offset);
		else
			return contentAssistant.getContextInformationAutoActivationCharacters(fViewer, offset);
	}

	/**
	* Creates and returns a completion proposal popup for the given content assistant.
	* 
	* @param contentAssistant the content assistant
	* @param controller the additional info controller
	* @return the completion proposal popup
	*/
	CompletionProposalPopup createCompletionProposalPopup(ContentAssistant contentAssistant, AdditionalInfoController controller) {
		if (fContentAssistSubject != null)
			return new CompletionProposalPopup(contentAssistant, fContentAssistSubject, controller);
		else
			return new CompletionProposalPopup(contentAssistant, fViewer, controller);
		
	}

	/**
	 * Creates and returns a context info popup for the given content assistant.
	 * 
	 * @param contentAssistant the content assistant
	 * @return the context info popup or <code>null</code>
	 */
	ContextInformationPopup createContextInfoPopup(ContentAssistant contentAssistant) {
		if (fContentAssistSubject != null)
			return new ContextInformationPopup(contentAssistant, fContentAssistSubject);
		else
			return new ContextInformationPopup(contentAssistant, fViewer);
		
	}

	/**
	 * @param contentAssistant
	 * @param offset
	 * @return
	 */
	public IContextInformationValidator getContextInformationValidator(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubject != null)
			return contentAssistant.getContextInformationValidator(fContentAssistSubject, offset);
		else
			return contentAssistant.getContextInformationValidator(fViewer, offset);
	}

	/**
	 * @param contentAssistant
	 * @param offset
	 * @return
	 */
	public IContextInformationPresenter getContextInformationPresenter(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubject != null)
			return contentAssistant.getContextInformationPresenter(fContentAssistSubject, offset);
		else
			return contentAssistant.getContextInformationPresenter(fViewer, offset);
	}

	/**
	 * @param frame
	 */
	public void installValidator(ContextFrame frame) {
		if (fContentAssistSubject != null) {
			if (frame.fValidator instanceof IContextInformationValidatorExtension)
				((IContextInformationValidatorExtension)frame.fValidator).install(frame.fInformation, fContentAssistSubject, frame.fOffset);
		} else
			frame.fValidator.install(frame.fInformation, fViewer, frame.fOffset);
	}

	/**
	 * @param frame
	 */
	public void installContextInformationPresenter(ContextFrame frame) {
		if (fContentAssistSubject != null) {
			if (frame.fPresenter instanceof IContextInformationPresenterExtension)
				((IContextInformationPresenterExtension)frame.fValidator).install(frame.fInformation, fContentAssistSubject, frame.fBeginOffset);
		} else
			frame.fPresenter.install(frame.fInformation, fViewer, frame.fBeginOffset);
	}

	/**
	 * @param contentAssistant
	 * @param position
	 * @return
	 */
	public IContextInformation[] computeContextInformation(ContentAssistant contentAssistant, int position) {
		if (fContentAssistSubject != null)
			return contentAssistant.computeContextInformation(fContentAssistSubject, position);
		else
			return contentAssistant.computeContextInformation(fViewer, position);
	}

	/*
	 * @see IContentAssistSubject#addSelectionListener(SelectionListener)
	 */
	public boolean addSelectionListener(SelectionListener selectionListener) {
		if (fContentAssistSubject != null)
			return fContentAssistSubject.addSelectionListener(selectionListener);
		else {
			fViewer.getTextWidget().addSelectionListener(selectionListener);
			return true;
		}
	}

	/*
	 * @see IContentAssistSubject#removeSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener selectionListener) {
		if (fContentAssistSubject != null)
			fContentAssistSubject.removeSelectionListener(selectionListener);
		else
			fViewer.getTextWidget().removeSelectionListener(selectionListener);
	}
}
