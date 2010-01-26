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

package org.eclipse.jface.internal.text.link.contentassist;


import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;


/**
 * This class is used to present context information to the user.
 * If multiple contexts are valid at the current cursor location,
 * a list is presented from which the user may choose one context.
 * Once the user makes their choice, or if there was only a single
 * possible context, the context information is shown in a tooltip like popup. <p>
 * If the tooltip is visible and the user wants to see context information of
 * a context embedded into the one for which context information is displayed,
 * context information for the embedded context is shown. As soon as the
 * cursor leaves the embedded context area, the context information for
 * the embedding context is shown again.
 *
 * @see IContextInformation
 * @see IContextInformationValidator
 */
class ContextInformationPopup2 implements IContentAssistListener2 {



	/**
	 * Represents the state necessary for embedding contexts.
	 * @since 2.0
	 */
	static class ContextFrame {
		public int fBeginOffset;
		public int fOffset;
		public int fVisibleOffset;
		public IContextInformation fInformation;
		public IContextInformationValidator fValidator;
		public IContextInformationPresenter fPresenter;
	}

	private ITextViewer fViewer;
	private ContentAssistant2 fContentAssistant;

	private PopupCloser2 fPopupCloser= new PopupCloser2();
	private Shell fContextSelectorShell;
	private Table fContextSelectorTable;
	private IContextInformation[] fContextSelectorInput;
	private String fLineDelimiter= null;

	private Shell fContextInfoPopup;
	private StyledText fContextInfoText;
	private TextPresentation fTextPresentation;

	private Stack fContextFrameStack= new Stack();


	/**
	 * Creates a new context information popup.
	 *
	 * @param contentAssistant the content assist for computing the context information
	 * @param viewer the viewer on top of which the context information is shown
	 */
	public ContextInformationPopup2(ContentAssistant2 contentAssistant, ITextViewer viewer) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
	}

	/**
	 * Shows all possible contexts for the given cursor position of the viewer.
	 *
	 * @param autoActivated <code>true</code>  if auto activated
	 * @return  a potential error message or <code>null</code> in case of no error
	 */
	public String showContextProposals(final boolean autoActivated) {
		final StyledText styledText= fViewer.getTextWidget();
		BusyIndicator.showWhile(styledText.getDisplay(), new Runnable() {
			public void run() {

				int position= fViewer.getSelectedRange().x;

				IContextInformation[] contexts= computeContextInformation(position);
				int count = (contexts == null ? 0 : contexts.length);
				if (count == 1) {

					// Show context information directly
					internalShowContextInfo(contexts[0], position);

				} else if (count > 0) {
					// Precise context must be selected

					if (fLineDelimiter == null)
						fLineDelimiter= styledText.getLineDelimiter();

					createContextSelector();
					setContexts(contexts);
					displayContextSelector();
					hideContextInfoPopup();
				}
			}
		});

		return getErrorMessage();
	}

	/**
	 * Displays the given context information for the given offset.
	 *
	 * @param info the context information
	 * @param position the offset
	 * @since 2.0
	 */
	public void showContextInformation(final IContextInformation info, final int position) {
		Control control= fViewer.getTextWidget();
		BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
			public void run() {
				internalShowContextInfo(info, position);
				hideContextSelector();
			}
		});
	}

	/**
	 * Displays the given context information for the given offset.
	 *
	 * @param information the context information
	 * @param offset the offset
	 * @since 2.0
	 */

	private void internalShowContextInfo(IContextInformation information, int offset) {

		IContextInformationValidator validator= fContentAssistant.getContextInformationValidator(fViewer, offset);

		if (validator != null) {
			ContextFrame current= new ContextFrame();
			current.fInformation= information;
			current.fBeginOffset= (information instanceof IContextInformationExtension) ? ((IContextInformationExtension) information).getContextInformationPosition() : offset;
			if (current.fBeginOffset == -1) current.fBeginOffset= offset;
			current.fOffset= offset;
			current.fVisibleOffset= fViewer.getTextWidget().getSelectionRange().x - (offset - current.fBeginOffset);
			current.fValidator= validator;
			current.fPresenter= fContentAssistant.getContextInformationPresenter(fViewer, offset);

			fContextFrameStack.push(current);

			internalShowContextFrame(current, fContextFrameStack.size() == 1);
		}
	}

	/**
	 * Shows the given context frame.
	 *
	 * @param frame the frane to display
	 * @param initial <code>true</code> if this is the first frame to be displayed
	 * @since 2.0
	 */
	private void internalShowContextFrame(ContextFrame frame, boolean initial) {

		frame.fValidator.install(frame.fInformation, fViewer, frame.fOffset);

		if (frame.fPresenter != null) {
			if (fTextPresentation == null)
				fTextPresentation= new TextPresentation();
			frame.fPresenter.install(frame.fInformation, fViewer, frame.fBeginOffset);
			frame.fPresenter.updatePresentation(frame.fOffset, fTextPresentation);
		}

		createContextInfoPopup();

		fContextInfoText.setText(frame.fInformation.getInformationDisplayString());
		if (fTextPresentation != null)
			TextPresentation.applyTextPresentation(fTextPresentation, fContextInfoText);
		resize();

		if (initial) {
			if (fContentAssistant.addContentAssistListener(this, ContentAssistant2.CONTEXT_INFO_POPUP)) {
				fContentAssistant.addToLayout(this, fContextInfoPopup, ContentAssistant2.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP, frame.fVisibleOffset);
				fContextInfoPopup.setVisible(true);
			}
		} else {
			fContentAssistant.layout(ContentAssistant2.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP, frame.fVisibleOffset);
		}
	}

	/**
	 * Computes all possible context information for the given offset.
	 *
	 * @param position the offset
	 * @return all possible context information for the given offset
	 * @since 2.0
	 */
	private IContextInformation[] computeContextInformation(int position) {
		return fContentAssistant.computeContextInformation(fViewer, position);
	}

	/**
	 *Returns the error message generated while computing context information.
	 *
	 * @return the error message
	 */
	private String getErrorMessage() {
		return fContentAssistant.getErrorMessage();
	}

	/**
	 * Creates the context information popup. This is the tooltip like overlay window.
	 */
	private void createContextInfoPopup() {
		if (Helper2.okToUse(fContextInfoPopup))
			return;

		Control control= fViewer.getTextWidget();
		Display display= control.getDisplay();

		fContextInfoPopup= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		fContextInfoPopup.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

		fContextInfoText= new StyledText(fContextInfoPopup, SWT.MULTI | SWT.READ_ONLY);

		Color c= fContentAssistant.getContextInformationPopupBackground();
		if (c == null)
			c= display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		fContextInfoText.setBackground(c);

		c= fContentAssistant.getContextInformationPopupForeground();
		if (c == null)
			c= display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		fContextInfoText.setForeground(c);
	}

	/**
	 * Resizes the context information popup.
	 * @since 2.0
	 */
	private void resize() {
		Point size= fContextInfoText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		size.x += 3;
		fContextInfoText.setSize(size);
		fContextInfoText.setLocation(1,1);
		size.x += 2;
		size.y += 2;
		fContextInfoPopup.setSize(size);
	}

	/**
	 *Hides the context information popup.
	 */
	private void hideContextInfoPopup() {

		if (Helper2.okToUse(fContextInfoPopup)) {

			int size= fContextFrameStack.size();
			if (size > 0) {
				fContextFrameStack.pop();
				-- size;
			}

			if (size > 0) {
				ContextFrame current= (ContextFrame) fContextFrameStack.peek();
				internalShowContextFrame(current, false);
			} else {

				fContentAssistant.removeContentAssistListener(this, ContentAssistant2.CONTEXT_INFO_POPUP);

				fContextInfoPopup.setVisible(false);
				fContextInfoPopup.dispose();
				fContextInfoPopup= null;

				if (fTextPresentation != null) {
					fTextPresentation.clear();
					fTextPresentation= null;
				}
			}
		}

		if (fContextInfoPopup == null)
			fContentAssistant.contextInformationClosed();
	}

	/**
	 * Creates the context selector in case the user has the choice between multiple valid contexts
	 * at a given offset.
	 */
	private void createContextSelector() {
		if (Helper2.okToUse(fContextSelectorShell))
			return;

		Control control= fViewer.getTextWidget();
		fContextSelectorShell= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		fContextSelectorShell.setLayout(layout);
		fContextSelectorShell.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_BLACK));


		fContextSelectorTable= new Table(fContextSelectorShell, SWT.H_SCROLL | SWT.V_SCROLL);
		fContextSelectorTable.setLocation(1, 1);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= fContextSelectorTable.getItemHeight() * 10;
		gd.widthHint= 300;
		fContextSelectorTable.setLayoutData(gd);

		fContextSelectorShell.pack(true);

		Color c= fContentAssistant.getContextSelectorBackground();
		if (c == null)
			c= control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		fContextSelectorTable.setBackground(c);

		c= fContentAssistant.getContextSelectorForeground();
		if (c == null)
			c= control.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		fContextSelectorTable.setForeground(c);

		fContextSelectorTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				insertSelectedContext();
				hideContextSelector();
			}
		});

		fPopupCloser.install(fContentAssistant, fContextSelectorTable);

		fContextSelectorTable.setHeaderVisible(false);
		fContentAssistant.addToLayout(this, fContextSelectorShell, ContentAssistant2.LayoutManager.LAYOUT_CONTEXT_SELECTOR, fContentAssistant.getSelectionOffset());
	}

	/**
	 * Causes the context information of the context selected in the context selector
	 * to be displayed in the context information popup.
	 */
	private void insertSelectedContext() {
		int i= fContextSelectorTable.getSelectionIndex();

		if (i < 0 || i >= fContextSelectorInput.length)
			return;

		int position= fViewer.getSelectedRange().x;
		internalShowContextInfo(fContextSelectorInput[i], position);
	}

	/**
	 * Sets the contexts in the context selector to the given set.
	 *
	 * @param contexts the possible contexts
	 */
	private void setContexts(IContextInformation[] contexts) {
		if (Helper2.okToUse(fContextSelectorTable)) {

			fContextSelectorInput= contexts;

			fContextSelectorTable.setRedraw(false);
			fContextSelectorTable.removeAll();

			TableItem item;
			IContextInformation t;
			for (int i= 0; i < contexts.length; i++) {
				t= contexts[i];
				item= new TableItem(fContextSelectorTable, SWT.NULL);
				if (t.getImage() != null)
					item.setImage(t.getImage());
				item.setText(t.getContextDisplayString());
			}

			fContextSelectorTable.select(0);
			fContextSelectorTable.setRedraw(true);
		}
	}

	/**
	 * Displays the context selector.
	 */
	private void displayContextSelector() {
		if (fContentAssistant.addContentAssistListener(this, ContentAssistant2.CONTEXT_SELECTOR))
			fContextSelectorShell.setVisible(true);
	}

	/**
	 * Hodes the context selector.
	 */
	private void hideContextSelector() {
		if (Helper2.okToUse(fContextSelectorShell)) {
			fContentAssistant.removeContentAssistListener(this, ContentAssistant2.CONTEXT_SELECTOR);

			fPopupCloser.uninstall();
			fContextSelectorShell.setVisible(false);
			fContextSelectorShell.dispose();
			fContextSelectorShell= null;
		}

		if (!Helper2.okToUse(fContextInfoPopup))
			fContentAssistant.contextInformationClosed();
	}

	/**
	 *Returns whether the context selector has the focus.
	 *
	 * @return <code>true</code> if teh context selector has the focus
	 */
	public boolean hasFocus() {
		if (Helper2.okToUse(fContextSelectorShell))
			return (fContextSelectorShell.isFocusControl() || fContextSelectorTable.isFocusControl());

		return false;
	}

	/**
	 * Hides context selector and context information popup.
	 */
	public void hide() {
		hideContextSelector();
		hideContextInfoPopup();
	}

	/**
	 * Returns whether this context information popup is active. I.e., either
	 * a context selector or context information is displayed.
	 *
	 * @return <code>true</code> if the context selector is active
	 */
	public boolean isActive() {
		return (Helper2.okToUse(fContextInfoPopup) || Helper2.okToUse(fContextSelectorShell));
	}

	/*
	 * @see IContentAssistListener#verifyKey(VerifyEvent)
	 */
	public boolean verifyKey(VerifyEvent e) {
		if (Helper2.okToUse(fContextSelectorShell))
			return contextSelectorKeyPressed(e);
		if (Helper2.okToUse(fContextInfoPopup))
			return contextInfoPopupKeyPressed(e);
		return true;
	}

	/**
	 * Processes a key stroke in the context selector.
	 *
	 * @param e the verify event describing the key stroke
	 * @return <code>true</code> if processing can be stopped
	 */
	private boolean contextSelectorKeyPressed(VerifyEvent e) {

		char key= e.character;
		if (key == 0) {

			int change;
			int visibleRows= (fContextSelectorTable.getSize().y / fContextSelectorTable.getItemHeight()) - 1;
			int selection= fContextSelectorTable.getSelectionIndex();

			switch (e.keyCode) {

				case SWT.ARROW_UP:
					change= (fContextSelectorTable.getSelectionIndex() > 0 ? -1 : 0);
					break;

				case SWT.ARROW_DOWN:
					change= (fContextSelectorTable.getSelectionIndex() < fContextSelectorTable.getItemCount() - 1 ? 1 : 0);
					break;

				case SWT.PAGE_DOWN :
					change= visibleRows;
					if (selection + change >= fContextSelectorTable.getItemCount())
						change= fContextSelectorTable.getItemCount() - selection;
					break;

				case SWT.PAGE_UP :
					change= -visibleRows;
					if (selection + change < 0)
						change= -selection;
					break;

				case SWT.HOME :
					change= -selection;
					break;

				case SWT.END :
					change= fContextSelectorTable.getItemCount() - selection;
					break;

				default:
					if (e.keyCode != SWT.MOD1 && e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4)
						hideContextSelector();
					return true;
			}

			fContextSelectorTable.setSelection(selection + change);
			fContextSelectorTable.showSelection();
			e.doit= false;
			return false;

		} else if ('\t' == key) {
			// switch focus to selector shell
			e.doit= false;
			fContextSelectorShell.setFocus();
			return false;
		} else if (key == SWT.ESC) {
			e.doit= false;
			hideContextSelector();
		}

		return true;
	}

	/**
	 * Processes a key stroke while the info popup is up.
	 *
	 * @param e the verify event describing the key stroke
	 * @return <code>true</code> if processing can be stopped
	 */
	private boolean contextInfoPopupKeyPressed(KeyEvent e) {

		char key= e.character;
		if (key == 0) {

			switch (e.keyCode) {
				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
					validateContextInformation();
					break;
				default:
					if (e.keyCode != SWT.MOD1 && e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4)
						hideContextInfoPopup();
					break;
			}

		} else if (key == SWT.ESC) {
			e.doit= false;
 			hideContextInfoPopup();
		} else {
			validateContextInformation();
		}
		return true;
	}

	/*
	 * @see IEventConsumer#processEvent(VerifyEvent)
	 */
	public void processEvent(VerifyEvent event) {
		if (Helper2.okToUse(fContextSelectorShell))
			contextSelectorProcessEvent(event);
		if (Helper2.okToUse(fContextInfoPopup))
			contextInfoPopupProcessEvent(event);
	}

	/**
	 * Processes a key stroke in the context selector.
	 *
	 * @param e the verify event describing the key stroke
	 */
	private void contextSelectorProcessEvent(VerifyEvent e) {

		if (e.start == e.end && e.text != null && e.text.equals(fLineDelimiter)) {
			e.doit= false;
			insertSelectedContext();
		}

		hideContextSelector();
	}

	/**
	 * Processes a key stroke while the info popup is up.
	 *
	 * @param e the verify event describing the key stroke
	 */
	private void contextInfoPopupProcessEvent(VerifyEvent e) {
		if (e.start != e.end && (e.text == null || e.text.length() == 0))
			validateContextInformation();
	}

	/**
	 * Validates the context information for the viewer's actual cursor position.
	 */
	private void validateContextInformation() {
		/*
		 * Post the code in the event queue in order to ensure that the
		 * action described by this verify key event has already beed executed.
		 * Otherwise, we'd validate the context information based on the
		 * pre-key-stroke state.
		 */
		fContextInfoPopup.getDisplay().asyncExec(new Runnable() {

			private ContextFrame fFrame= (ContextFrame) fContextFrameStack.peek();

			public void run() {
				if (Helper2.okToUse(fContextInfoPopup) && fFrame == fContextFrameStack.peek()) {
					int offset= fViewer.getSelectedRange().x;
					if (fFrame.fValidator == null || !fFrame.fValidator.isContextInformationValid(offset)) {
						hideContextInfoPopup();
					} else if (fFrame.fPresenter != null && fFrame.fPresenter.updatePresentation(offset, fTextPresentation)) {
						TextPresentation.applyTextPresentation(fTextPresentation, fContextInfoText);
						resize();
					}
				}
			}
		});
	}
}
