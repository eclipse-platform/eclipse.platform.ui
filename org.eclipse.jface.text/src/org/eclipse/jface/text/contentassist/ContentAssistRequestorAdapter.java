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
		if (fContentAssistRequestor != null)
			return contentAssistant.getCompletionProposalAutoActivationCharacters(fContentAssistRequestor, offset);
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
		if (fContentAssistRequestor != null)
			return contentAssistant.getContextInformationAutoActivationCharacters(fContentAssistRequestor, offset);
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
		if (fContentAssistRequestor != null)
			return new CompletionProposalPopup(contentAssistant, fContentAssistRequestor, controller);
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
		if (fContentAssistRequestor != null)
			return new ContextInformationPopup(contentAssistant, fContentAssistRequestor);
		else
			return new ContextInformationPopup(contentAssistant, fViewer);
		
	}

	/**
	 * @param contentAssistant
	 * @param offset
	 * @return
	 */
	public IContextInformationValidator getContextInformationValidator(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistRequestor != null)
			return contentAssistant.getContextInformationValidator(fContentAssistRequestor, offset);
		else
			return contentAssistant.getContextInformationValidator(fViewer, offset);
	}

	/**
	 * @param contentAssistant
	 * @param offset
	 * @return
	 */
	public IContextInformationPresenter getContextInformationPresenter(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistRequestor != null)
			return contentAssistant.getContextInformationPresenter(fContentAssistRequestor, offset);
		else
			return contentAssistant.getContextInformationPresenter(fViewer, offset);
	}

	/**
	 * @param frame
	 */
	public void installValidator(ContextFrame frame) {
		if (fContentAssistRequestor != null) {
			if (frame.fValidator instanceof IContextInformationValidatorExtension)
				((IContextInformationValidatorExtension)frame.fValidator).install(frame.fInformation, fContentAssistRequestor, frame.fOffset);
		} else
			frame.fValidator.install(frame.fInformation, fViewer, frame.fOffset);
	}

	/**
	 * @param frame
	 */
	public void installContextInformationPresenter(ContextFrame frame) {
		if (fContentAssistRequestor != null) {
			if (frame.fPresenter instanceof IContextInformationPresenterExtension)
				((IContextInformationPresenterExtension)frame.fValidator).install(frame.fInformation, fContentAssistRequestor, frame.fBeginOffset);
		} else
			frame.fPresenter.install(frame.fInformation, fViewer, frame.fBeginOffset);
	}

	/**
	 * @param contentAssistant
	 * @param position
	 * @return
	 */
	public IContextInformation[] computeContextInformation(ContentAssistant contentAssistant, int position) {
		if (fContentAssistRequestor != null)
			return contentAssistant.computeContextInformation(fContentAssistRequestor, position);
		else
			return contentAssistant.computeContextInformation(fViewer, position);
	}

	/*
	 * @see IContentAssistRequestor#addSelectionListener(SelectionListener)
	 */
	public boolean addSelectionListener(SelectionListener selectionListener) {
		if (fContentAssistRequestor != null)
			return fContentAssistRequestor.addSelectionListener(selectionListener);
		else {
			fViewer.getTextWidget().addSelectionListener(selectionListener);
			return true;
		}
	}

	/*
	 * @see IContentAssistRequestor#removeSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener selectionListener) {
		if (fContentAssistRequestor != null)
			fContentAssistRequestor.removeSelectionListener(selectionListener);
		else
			fViewer.getTextWidget().removeSelectionListener(selectionListener);
	}
}
