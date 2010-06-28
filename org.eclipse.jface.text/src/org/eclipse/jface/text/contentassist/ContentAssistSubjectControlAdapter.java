/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContextInformationPresenter;
import org.eclipse.jface.contentassist.ISubjectControlContextInformationValidator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.contentassist.ContextInformationPopup.ContextFrame;


/**
 * This content assist adapter delegates the calls either to
 * a text viewer or to a content assist subject control.
 *
 * @since 3.0
 */
final class ContentAssistSubjectControlAdapter implements IContentAssistSubjectControl {

	/**
	 * The text viewer which is used as content assist subject control.
	 */
	private ITextViewer fViewer;

	/**
	 * The content assist subject control.
	 */
	private IContentAssistSubjectControl fContentAssistSubjectControl;


	/**
	 * Creates an adapter for the given content assist subject control.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 */
	ContentAssistSubjectControlAdapter(IContentAssistSubjectControl contentAssistSubjectControl) {
		Assert.isNotNull(contentAssistSubjectControl);
		fContentAssistSubjectControl= contentAssistSubjectControl;
	}

	/**
	 * Creates an adapter for the given text viewer.
	 *
	 * @param viewer the text viewer
	 */
	public ContentAssistSubjectControlAdapter(ITextViewer viewer) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
	}

	/*
	 * @see IContentAssistSubjectControl#getLineHeight()
	 */
	public int getLineHeight() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getLineHeight();

		return fViewer.getTextWidget().getLineHeight(getCaretOffset());
	}

	/*
	 * @see IContentAssistSubjectControl#getControl()
	 */
	public Control getControl() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getControl();
		return fViewer.getTextWidget();
	}

	/*
	 * @see IContentAssistSubjectControl#getLocationAtOffset(int)
	 */
	public Point getLocationAtOffset(int offset) {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getLocationAtOffset(offset);
		return fViewer.getTextWidget().getLocationAtOffset(offset);
	}

	/*
	 * @see IContentAssistSubjectControl#getWidgetSelectionRange()
	 */
	public Point getWidgetSelectionRange() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getWidgetSelectionRange();
		return fViewer.getTextWidget().getSelectionRange();
	}

	/*
	 * @see IContentAssistSubjectControl#getSelectedRange()
	 */
	public Point getSelectedRange() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getSelectedRange();
		return fViewer.getSelectedRange();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getCaretOffset()
	 */
	public int getCaretOffset() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getCaretOffset();
		return fViewer.getTextWidget().getCaretOffset();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getLineDelimiter();
		return fViewer.getTextWidget().getLineDelimiter();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener keyListener) {
		if (fContentAssistSubjectControl != null)
			fContentAssistSubjectControl.addKeyListener(keyListener);
		else
			fViewer.getTextWidget().addKeyListener(keyListener);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener keyListener) {
		if (fContentAssistSubjectControl != null)
			fContentAssistSubjectControl.removeKeyListener(keyListener);
		else
			fViewer.getTextWidget().removeKeyListener(keyListener);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getDocument()
	 */
	public IDocument getDocument() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.getDocument();
		return fViewer.getDocument();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#prependVerifyKeyListener(VerifyKeyListener)
	 */
	public boolean prependVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistSubjectControl != null) {
			return fContentAssistSubjectControl.prependVerifyKeyListener(verifyKeyListener);
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
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#appendVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean appendVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.appendVerifyKeyListener(verifyKeyListener);
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
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#removeVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		if (fContentAssistSubjectControl != null) {
			fContentAssistSubjectControl.removeVerifyKeyListener(verifyKeyListener);
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
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#setEventConsumer(org.eclipse.jface.text.contentassist.ContentAssistant.InternalListener)
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		if (fContentAssistSubjectControl != null)
			fContentAssistSubjectControl.setEventConsumer(eventConsumer);
		else
			fViewer.setEventConsumer(eventConsumer);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#setSelectedRange(int, int)
	 */
	public void setSelectedRange(int i, int j) {
		if (fContentAssistSubjectControl != null)
			fContentAssistSubjectControl.setSelectedRange(i, j);
		else
			fViewer.setSelectedRange(i, j);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#revealRange(int, int)
	 */
	public void revealRange(int i, int j) {
		if (fContentAssistSubjectControl != null)
			fContentAssistSubjectControl.revealRange(i, j);
		else
			fViewer.revealRange(i, j);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#canAddVerifyKeyListener()
	 */
	public boolean supportsVerifyKeyListener() {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.supportsVerifyKeyListener();
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
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubjectControl != null)
			return contentAssistant.getCompletionProposalAutoActivationCharacters(fContentAssistSubjectControl, offset);
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
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	char[] getContextInformationAutoActivationCharacters(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubjectControl != null)
			return contentAssistant.getContextInformationAutoActivationCharacters(fContentAssistSubjectControl, offset);
		return contentAssistant.getContextInformationAutoActivationCharacters(fViewer, offset);
	}

	/**
	* Creates and returns a completion proposal popup for the given content assistant.
	*
	* @param contentAssistant the content assistant
	* @param controller the additional info controller, or <code>null</code>
	* @return the completion proposal popup
	*/
	CompletionProposalPopup createCompletionProposalPopup(ContentAssistant contentAssistant, AdditionalInfoController controller) {
		if (fContentAssistSubjectControl != null)
			return new CompletionProposalPopup(contentAssistant, fContentAssistSubjectControl, controller);
		return new CompletionProposalPopup(contentAssistant, fViewer, controller);

	}

	/**
	 * Creates and returns a context info popup for the given content assistant.
	 *
	 * @param contentAssistant the content assistant
	 * @return the context info popup or <code>null</code>
	 */
	ContextInformationPopup createContextInfoPopup(ContentAssistant contentAssistant) {
		if (fContentAssistSubjectControl != null)
			return new ContextInformationPopup(contentAssistant, fContentAssistSubjectControl);
		return new ContextInformationPopup(contentAssistant, fViewer);

	}

	/**
	 * Returns the context information validator that should be used to
	 * determine when the currently displayed context information should
	 * be dismissed. The position is used to determine the appropriate
	 * content assist processor to invoke.
	 *
	 * @param contentAssistant the content assistant
	 * @param offset a document offset
	 * @return an validator
	 */
	public IContextInformationValidator getContextInformationValidator(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubjectControl != null)
			return contentAssistant.getContextInformationValidator(fContentAssistSubjectControl, offset);
		return contentAssistant.getContextInformationValidator(fViewer, offset);
	}

	/**
	 * Returns the context information presenter that should be used to
	 * display context information. The position is used to determine the
	 * appropriate content assist processor to invoke.
	 *
	 * @param contentAssistant the content assistant
	 * @param offset a document offset
	 * @return a presenter
	 */
	public IContextInformationPresenter getContextInformationPresenter(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubjectControl != null)
			return contentAssistant.getContextInformationPresenter(fContentAssistSubjectControl, offset);
		return contentAssistant.getContextInformationPresenter(fViewer, offset);
	}

	/**
	 * Installs this adapter's information validator on the given context frame.
	 *
	 * @param frame the context frame
	 */
	public void installValidator(ContextFrame frame) {
		if (fContentAssistSubjectControl != null) {
			if (frame.fValidator instanceof ISubjectControlContextInformationValidator)
				((ISubjectControlContextInformationValidator)frame.fValidator).install(frame.fInformation, fContentAssistSubjectControl, frame.fOffset);
		} else
			frame.fValidator.install(frame.fInformation, fViewer, frame.fOffset);
	}

	/**
	 * Installs this adapter's information presenter on the given context frame.
	 *
	 * @param frame the context frame
	 */
	public void installContextInformationPresenter(ContextFrame frame) {
		if (fContentAssistSubjectControl != null) {
			if (frame.fPresenter instanceof ISubjectControlContextInformationPresenter)
				((ISubjectControlContextInformationPresenter)frame.fValidator).install(frame.fInformation, fContentAssistSubjectControl, frame.fBeginOffset);
		} else
			frame.fPresenter.install(frame.fInformation, fViewer, frame.fBeginOffset);
	}

	/**
	 * Returns an array of context information objects computed based
	 * on the specified document position. The position is used to determine
	 * the appropriate content assist processor to invoke.
	 *
	 * @param contentAssistant the content assistant
	 * @param offset a document offset
	 * @return an array of context information objects
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ContentAssistant contentAssistant, int offset) {
		if (fContentAssistSubjectControl != null)
			return contentAssistant.computeContextInformation(fContentAssistSubjectControl, offset);
		return contentAssistant.computeContextInformation(fViewer, offset);
	}

	/*
	 * @see IContentAssistSubjectControl#addSelectionListener(SelectionListener)
	 */
	public boolean addSelectionListener(SelectionListener selectionListener) {
		if (fContentAssistSubjectControl != null)
			return fContentAssistSubjectControl.addSelectionListener(selectionListener);
		fViewer.getTextWidget().addSelectionListener(selectionListener);
		return true;
	}

	/*
	 * @see IContentAssistSubjectControl#removeSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener selectionListener) {
		if (fContentAssistSubjectControl != null)
			fContentAssistSubjectControl.removeSelectionListener(selectionListener);
		else
			fViewer.getTextWidget().removeSelectionListener(selectionListener);
	}
}
