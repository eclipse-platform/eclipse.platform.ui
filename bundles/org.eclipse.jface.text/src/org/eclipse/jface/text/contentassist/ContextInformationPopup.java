/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Stephan Wahlbrink <sw@wahlbrink.eu> - Bug 512251 - Fix IllegalArgumentException in ContextInformationPopup
 *     Stephan Wahlbrink <sw@wahlbrink.eu> - Bug 94106 - Fix hide/cleanup of content information popup stack
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import static org.eclipse.jface.util.Util.isValid;

import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.TextPresentation;


/**
 * This class is used to present context information to the user.
 * If multiple contexts are valid at the current cursor location,
 * a list is presented from which the user may choose one context.
 * Once the user makes their choice, or if there was only a single
 * possible context, the context information is shown in a tool tip like popup. <p>
 * If the tool tip is visible and the user wants to see context information of
 * a context embedded into the one for which context information is displayed,
 * context information for the embedded context is shown. As soon as the
 * cursor leaves the embedded context area, the context information for
 * the embedding context is shown again.
 *
 * @see IContextInformation
 * @see IContextInformationValidator
 */
class ContextInformationPopup implements IContentAssistListener {


	/**
	 * Represents the state necessary for embedding contexts.
	 *
	 * @since 2.0
	 */
	static class ContextFrame {

		final int fBeginOffset;
		final int fOffset;
		final int fVisibleOffset;
		final IContextInformation fInformation;
		final IContextInformationValidator fValidator;
		final IContextInformationPresenter fPresenter;

		/*
		 * @since 3.1
		 */
		public ContextFrame(IContextInformation information, int beginOffset, int offset, int visibleOffset, IContextInformationValidator validator, IContextInformationPresenter presenter) {
			fInformation = information;
			fBeginOffset = beginOffset;
			fOffset = offset;
			fVisibleOffset = visibleOffset;
			fValidator = validator;
			fPresenter = presenter;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ContextFrame) {
				ContextFrame frame= (ContextFrame) obj;
				return fInformation.equals(frame.fInformation) && fBeginOffset == frame.fBeginOffset;
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return (fInformation.hashCode() << 16) | fBeginOffset;
		}
	}

	private ITextViewer fViewer;
	private ContentAssistant fContentAssistant;

	private PopupCloser fPopupCloser= new PopupCloser();
	private Shell fContextSelectorShell;
	private Point fContextSelectorPopupSize;
	private Table fContextSelectorTable;
	private IContextInformation[] fContextSelectorInput;
	private String fLineDelimiter= null;

	private Shell fContextInfoPopup;
	private StyledText fContextInfoText;
	private TextPresentation fTextPresentation;

	private Stack<ContextFrame> fContextFrameStack= new Stack<>();
	/**
	 * The content assist subject control.
	 *
	 * @since 3.0
	 */
	private IContentAssistSubjectControl fContentAssistSubjectControl;
	/**
	 * The content assist subject control adapter.
	 *
	 * @since 3.0
	 */
	private ContentAssistSubjectControlAdapter fContentAssistSubjectControlAdapter;

	/**
	 * Selection listener on the text widget which is active
	 * while a context information pop up is shown.
	 *
	 * @since 3.0
	 */
	private SelectionListener fTextWidgetSelectionListener;

	/**
	 * The last removed context frame is remembered in order to not re-query the
	 * user about which context should be used.
	 *
	 * @since 3.0
	 */
	private ContextFrame fLastContext= null;

	/**
	 * Creates a new context information popup.
	 *
	 * @param contentAssistant the content assist for computing the context information
	 * @param viewer the viewer on top of which the context information is shown
	 */
	public ContextInformationPopup(ContentAssistant contentAssistant, ITextViewer viewer) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
		fContentAssistSubjectControlAdapter= new ContentAssistSubjectControlAdapter(fViewer);
	}

	/**
	 * Creates a new context information popup.
	 *
	 * @param contentAssistant the content assist for computing the context information
	 * @param contentAssistSubjectControl the content assist subject control on top of which the context information is shown
	 * @since 3.0
	 */
	public ContextInformationPopup(ContentAssistant contentAssistant, IContentAssistSubjectControl contentAssistSubjectControl) {
		fContentAssistant= contentAssistant;
		fContentAssistSubjectControl= contentAssistSubjectControl;
		fContentAssistSubjectControlAdapter= new ContentAssistSubjectControlAdapter(fContentAssistSubjectControl);
	}

	/**
	 * Shows all possible contexts for the given cursor position of the viewer.
	 *
	 * @param autoActivated <code>true</code>  if auto activated
	 * @return  a potential error message or <code>null</code> in case of no error
	 */
	public String showContextProposals(final boolean autoActivated) {
		final Control control= fContentAssistSubjectControlAdapter.getControl();
		BusyIndicator.showWhile(control.getDisplay(), () -> {

			int offset= fContentAssistSubjectControlAdapter.getSelectedRange().x;

			IContextInformation[] contexts= computeContextInformation(offset);
			int count= (contexts == null ? 0 : contexts.length);
			if (count == 1) {

				ContextFrame frame1= createContextFrame(contexts[0], offset);
				if (isDuplicate(frame1))
					validateContextInformation();
				else
					// Show context information directly
					internalShowContextInfo(frame1);

			} else if (count > 0) {

				// if any of the proposed context matches any of the contexts on the stack,
				// assume that one (so, if context info is invoked repeatedly, the current
				// info is kept)
				int index= 0;
				for (int i= 0; i < contexts.length; i++) {
					IContextInformation info= contexts[i];
					ContextFrame frame2= createContextFrame(info, offset);

					// check top of stack and stored context
					if (isDuplicate(frame2)) {
						validateContextInformation();
						return;
					}

					if (isLastFrame(frame2)) {
						index= i;
					}

					// also check all other contexts
					for (ContextFrame stackFrame : fContextFrameStack) {
						if (stackFrame.equals(frame2)) {
							validateContextInformation();
							return;
						}
					}
				}

				// otherwise:
				// Precise context must be selected

				if (fLineDelimiter == null)
					fLineDelimiter= fContentAssistSubjectControlAdapter.getLineDelimiter();

				createContextSelector();
				setContexts(contexts, index);
				displayContextSelector();
			}
		});

		return getErrorMessage();
	}

	/**
	 * Displays the given context information for the given offset.
	 *
	 * @param info the context information
	 * @param offset the offset
	 * @since 2.0
	 */
	public void showContextInformation(final IContextInformation info, final int offset) {
		Control control= fContentAssistSubjectControlAdapter.getControl();
		BusyIndicator.showWhile(control.getDisplay(), () -> {
			if (info == null)
				validateContextInformation();
			else {
				ContextFrame frame= createContextFrame(info, offset);
				if (isDuplicate(frame))
					validateContextInformation();
				else
					internalShowContextInfo(frame);
				hideContextSelector();
			}
		});
	}

	/**
	 * Displays the given context information for the given offset.
	 *
	 * @param frame the context frame to display, or <code>null</code>
	 * @since 3.0
	 */
	private void internalShowContextInfo(ContextFrame frame) {
		if (frame != null && canShowFrame(frame)) {
			fContextFrameStack.push(frame);
			if (fContextFrameStack.size() == 1)
				fLastContext= null;
			internalShowContextFrame(frame, fContextFrameStack.size() == 1);
			validateContextInformation();
		}
	}

	/**
	 * Creates a context frame for the given offset.
	 *
	 * @param information the context information
	 * @param offset the offset
	 * @return the created context frame
	 * @since 3.0
	 */
	private ContextFrame createContextFrame(IContextInformation information, int offset) {
		IContextInformationValidator validator= fContentAssistSubjectControlAdapter.getContextInformationValidator(fContentAssistant, offset);

		if (validator != null) {
			int beginOffset= (information instanceof IContextInformationExtension) ? ((IContextInformationExtension) information).getContextInformationPosition() : offset;
			if (beginOffset == -1) beginOffset= offset;
			int visibleOffset= fContentAssistSubjectControlAdapter.getWidgetSelectionRange().x - (offset - beginOffset);
			IContextInformationPresenter presenter = fContentAssistSubjectControlAdapter.getContextInformationPresenter(fContentAssistant, offset);
			return new ContextFrame(information, beginOffset, offset, visibleOffset, validator, presenter);
		}

		return null;
	}

	/**
	 * Compares <code>frame</code> with the top of the stack, returns <code>true</code>
	 * if the frames are the same.
	 *
	 * @param frame the frame to check
	 * @return <code>true</code> if <code>frame</code> matches the top of the stack
	 * @since 3.0
	 */
	private boolean isDuplicate(ContextFrame frame) {
		if (frame == null)
			return false;
		if (fContextFrameStack.isEmpty())
			return false;
		// stack not empty
		ContextFrame top= fContextFrameStack.peek();
		return frame.equals(top);
	}

	/**
	 * Compares <code>frame</code> with most recently removed context frame, returns <code>true</code>
	 * if the frames are the same.
	 *
	 * @param frame the frame to check
	 * @return <code>true</code> if <code>frame</code> matches the most recently removed
	 * @since 3.0
	 */
	private boolean isLastFrame(ContextFrame frame) {
		return frame != null && frame.equals(fLastContext);
	}

	/**
	 * Pre-checks if the given context frame can be (re)shown.
	 *
	 * The function checks if the frame has valid position data. It does not call the context
	 * information validator.
	 *
	 * @param frame the frame to check
	 * @return <code>true</code> if the context frame OK to use, otherwise <code>false</code>
	 */
	private boolean canShowFrame(ContextFrame frame) {
		return fContentAssistSubjectControlAdapter.isValidWidgetOffset(frame.fVisibleOffset);
	}

	/**
	 * Shows the given context frame.
	 *
	 * @param frame the frame to display
	 * @param initial <code>true</code> if this is the first frame to be displayed
	 * @since 2.0
	 */
	private void internalShowContextFrame(ContextFrame frame, boolean initial) {

		fContentAssistSubjectControlAdapter.installValidator(frame);

		if (frame.fPresenter != null) {
			if (fTextPresentation == null)
				fTextPresentation= new TextPresentation();
			fContentAssistSubjectControlAdapter.installContextInformationPresenter(frame);
			frame.fPresenter.updatePresentation(frame.fOffset, fTextPresentation);
		}

		createContextInfoPopup();

		fContextInfoText.setText(frame.fInformation.getInformationDisplayString());
		if (fTextPresentation != null)
			TextPresentation.applyTextPresentation(fTextPresentation, fContextInfoText);
		resize(frame.fVisibleOffset);

		if (initial) {
			if (fContentAssistant.addContentAssistListener(this, ContentAssistant.CONTEXT_INFO_POPUP)) {
				if (fContentAssistSubjectControlAdapter.getControl() != null) {
					fTextWidgetSelectionListener= new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							validateContextInformation();
						}};
					fContentAssistSubjectControlAdapter.addSelectionListener(fTextWidgetSelectionListener);
				}
				fContentAssistant.addToLayout(this, fContextInfoPopup, ContentAssistant.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP, frame.fVisibleOffset);
				fContextInfoPopup.setVisible(true);
			}
		} else {
			fContentAssistant.layout(ContentAssistant.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP, frame.fVisibleOffset);
		}
	}

	/**
	 * Computes all possible context information for the given offset.
	 *
	 * @param offset the offset
	 * @return all possible context information for the given offset
	 * @since 2.0
	 */
	private IContextInformation[] computeContextInformation(int offset) {
		return fContentAssistSubjectControlAdapter.computeContextInformation(fContentAssistant, offset);
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
	 * Creates the context information popup. This is the tool tip like overlay window.
	 */
	private void createContextInfoPopup() {
		if (isValid(fContextInfoPopup))
			return;

		Control control= fContentAssistSubjectControlAdapter.getControl();
		Display display= control.getDisplay();

		fContextInfoPopup= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		fContextInfoPopup.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

		fContextInfoText= new StyledText(fContextInfoPopup, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);

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
	 *
	 * @param offset the caret offset in widget coordinates
	 * @since 2.0
	 */
	private void resize(int offset) {
		Point size= fContextInfoText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		final int TEXT_PAD= 0;
		final int BORDER_PAD= 2;
		final int PAD= TEXT_PAD + BORDER_PAD;
		size.x += PAD;
		Rectangle bounds= fContentAssistant.getLayoutManager().computeBoundsAboveBelow(fContextInfoPopup, size, offset);
		if (bounds.width < size.x)
			// we don't fit on the screen - try again and wrap
			size= fContextInfoText.computeSize(bounds.width - PAD, SWT.DEFAULT, true);

		size.x += TEXT_PAD;
		fContextInfoText.setSize(size);
		fContextInfoText.setLocation(1,1);
		size.x += BORDER_PAD;
		size.y += BORDER_PAD;
		fContextInfoPopup.setSize(size);
	}

	/**
	 * Hides the context information popup.
	 *
	 * @param all <code>true</code> to hide popups at all,
	 *     <code>false</code> to restore previous context frame if possible
	 */
	private void hideContextInfoPopup(boolean all) {

		if (isValid(fContextInfoPopup)) {

			int size= fContextFrameStack.size();
			while (size > 0) {
				fLastContext= fContextFrameStack.pop();
				-- size;

				if (size > 0 && !all) {
					ContextFrame current= fContextFrameStack.peek();
					if (canShowFrame(current)) {
						internalShowContextFrame(current, false);
						return;
					}
					// continue - try next
				}
			}
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.CONTEXT_INFO_POPUP);

			if (fContentAssistSubjectControlAdapter.getControl() != null)
				fContentAssistSubjectControlAdapter.removeSelectionListener(fTextWidgetSelectionListener);
			fTextWidgetSelectionListener= null;

			fContextInfoPopup.setVisible(false);
			fContextInfoPopup.dispose();
			fContextInfoPopup= null;

			if (fTextPresentation != null) {
				fTextPresentation.clear();
				fTextPresentation= null;
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
		if (isValid(fContextSelectorShell))
			return;

		Control control= fContentAssistSubjectControlAdapter.getControl();
		fContextSelectorShell= new Shell(control.getShell(), SWT.ON_TOP | SWT.RESIZE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		fContextSelectorShell.setLayout(layout);
		fContextSelectorShell.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_BLACK));

		fContextSelectorShell.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				fContextSelectorPopupSize= fContextSelectorShell.getSize();
			}
		});


		if (fViewer instanceof ITextViewerExtension) {
			final ITextViewerExtension textViewerExtension= (ITextViewerExtension)fViewer;
			final StyledText textWidget= fViewer.getTextWidget();

			final VerifyKeyListener verifyListener= event -> {
				if (isActive() && event.keyCode == 13 && event.character == '\r' && event.widget == textWidget) {
					event.doit= false;
					insertSelectedContext();
					hideContextSelector();
				}
			};

			textViewerExtension.prependVerifyKeyListener(verifyListener);

			fContextSelectorShell.addDisposeListener(e -> textViewerExtension.removeVerifyKeyListener(verifyListener));
		}

		fContextSelectorTable= new Table(fContextSelectorShell, SWT.H_SCROLL | SWT.V_SCROLL);
		fContextSelectorTable.setLocation(1, 1);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= fContextSelectorTable.getItemHeight() * 10;
		gd.widthHint= 300;
		fContextSelectorTable.setLayoutData(gd);

		Point size= fContentAssistant.restoreContextSelectorPopupSize();
		if (size != null)
			fContextSelectorShell.setSize(size);
		else
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
			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				insertSelectedContext();
				hideContextSelector();
			}
		});

		fPopupCloser.install(fContentAssistant, fContextSelectorTable);

		fContextSelectorTable.setHeaderVisible(false);
		fContentAssistant.addToLayout(this, fContextSelectorShell, ContentAssistant.LayoutManager.LAYOUT_CONTEXT_SELECTOR, fContentAssistant.getSelectionOffset());
	}

	/**
	 * Returns the minimal required height for the popup, may return 0 if the popup has not been
	 * created yet.
	 *
	 * @return the minimal height
	 * @since 3.3
	 */
	int getMinimalHeight() {
		int height= 0;
		if (isValid(fContextSelectorTable)) {
			int items= fContextSelectorTable.getItemHeight() * 10;
			Rectangle trim= fContextSelectorTable.computeTrim(0, 0, SWT.DEFAULT, items);
			height= trim.height;
		}
		return height;
	}

	/**
	 * Returns the size of the context selector pop-up.
	 *
	 * @return a Point containing the size
	 * @since 3.9
	 */
	Point getContextSelectorPopupSize() {
		return fContextSelectorPopupSize;
	}

	/**
	 * Causes the context information of the context selected in the context selector
	 * to be displayed in the context information popup.
	 */
	private void insertSelectedContext() {
		int i= fContextSelectorTable.getSelectionIndex();

		if (i < 0 || i >= fContextSelectorInput.length)
			return;

		int offset= fContentAssistSubjectControlAdapter.getSelectedRange().x;
		internalShowContextInfo(createContextFrame(fContextSelectorInput[i], offset));
	}

	/**
	 * Sets the contexts in the context selector to the given set.
	 *
	 * @param contexts the possible contexts
	 * @param selectionIndex the index of the proposal to select
	 */
	private void setContexts(IContextInformation[] contexts, int selectionIndex) {
		if (isValid(fContextSelectorTable)) {

			fContextSelectorInput= contexts;

			fContextSelectorTable.setRedraw(false);
			fContextSelectorTable.removeAll();

			TableItem item;
			IContextInformation t;
			for (IContextInformation context : contexts) {
				t= context;
				item= new TableItem(fContextSelectorTable, SWT.NULL);
				if (t.getImage() != null)
					item.setImage(t.getImage());
				item.setText(t.getContextDisplayString());
			}

			fContextSelectorTable.select(selectionIndex);
			fContextSelectorTable.setRedraw(true);
		}
	}

	/**
	 * Displays the context selector.
	 */
	private void displayContextSelector() {
		if (fContentAssistant.addContentAssistListener(this, ContentAssistant.CONTEXT_SELECTOR))
			fContextSelectorShell.setVisible(true);
	}

	/**
	 * Hides the context selector.
	 */
	private void hideContextSelector() {
		if (isValid(fContextSelectorShell)) {
			fContentAssistant.storeContextSelectorPopupSize();
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.CONTEXT_SELECTOR);

			fPopupCloser.uninstall();
			fContextSelectorShell.setVisible(false);
			fContextSelectorShell.dispose();
			fContextSelectorShell= null;
		}

		if (!isValid(fContextInfoPopup))
			fContentAssistant.contextInformationClosed();
	}

	/**
	 *Returns whether the context selector has the focus.
	 *
	 * @return <code>true</code> if the context selector has the focus
	 */
	public boolean hasFocus() {
		if (isValid(fContextSelectorShell))
			return fContextSelectorShell.getDisplay().getActiveShell() == fContextSelectorShell;

		return false;
	}

	/**
	 * Hides context selector and context information popup.
	 */
	public void hide() {
		hideContextSelector();
		hideContextInfoPopup(true);
	}

	/**
	 * Returns whether this context information popup is active. I.e., either
	 * a context selector or context information is displayed.
	 *
	 * @return <code>true</code> if the context selector is active
	 */
	public boolean isActive() {
		return (isValid(fContextInfoPopup) || isValid(fContextSelectorShell));
	}

	@Override
	public boolean verifyKey(VerifyEvent e) {
		if (isValid(fContextSelectorShell))
			return contextSelectorKeyPressed(e);
		if (isValid(fContextInfoPopup))
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

			int newSelection= fContextSelectorTable.getSelectionIndex();
			int visibleRows= (fContextSelectorTable.getSize().y / fContextSelectorTable.getItemHeight()) - 1;
			int itemCount= fContextSelectorTable.getItemCount();
			switch (e.keyCode) {
				case SWT.ARROW_UP :
					newSelection -= 1;
					if (newSelection < 0)
						newSelection= itemCount - 1;
					break;

				case SWT.ARROW_DOWN :
					newSelection += 1;
					if (newSelection > itemCount - 1)
						newSelection= 0;
					break;

				case SWT.PAGE_DOWN :
					newSelection += visibleRows;
					if (newSelection >= itemCount)
						newSelection= itemCount - 1;
					break;

				case SWT.PAGE_UP :
					newSelection -= visibleRows;
					if (newSelection < 0)
						newSelection= 0;
					break;

				case SWT.HOME :
					newSelection= 0;
					break;

				case SWT.END :
					newSelection= itemCount - 1;
					break;

				default :
					if (e.keyCode != SWT.CAPS_LOCK && e.keyCode != SWT.MOD1 && e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4)
						hideContextSelector();
					return true;
			}

			fContextSelectorTable.setSelection(newSelection);
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
					if (e.keyCode != SWT.CAPS_LOCK && e.keyCode != SWT.MOD1 && e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4)
						hideContextInfoPopup(true);
					break;
			}

		} else if (key == SWT.ESC) {
			e.doit= false;
			hideContextInfoPopup(false);
		} else {
			validateContextInformation();
		}
		return true;
	}

	@Override
	public void processEvent(VerifyEvent event) {
		if (isValid(fContextSelectorShell))
			contextSelectorProcessEvent(event);
		if (isValid(fContextInfoPopup))
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
		if (e.start != e.end && (e.text == null || e.text.isEmpty()))
			validateContextInformation();
	}

	/**
	 * Validates the context information for the viewer's actual cursor position.
	 */
	private void validateContextInformation() {
		/*
		 * Post the code in the event queue in order to ensure that the
		 * action described by this verify key event has already been executed.
		 * Otherwise, we'd validate the context information based on the
		 * pre-key-stroke state.
		 */
		if (!isValid(fContextInfoPopup))
			return;

		fContextInfoPopup.getDisplay().asyncExec(new Runnable() {

			private ContextFrame fFrame= fContextFrameStack.peek();

			@Override
			public void run() {
				// only do this if no other frames have been added in between
				if (!fContextFrameStack.isEmpty() && fFrame == fContextFrameStack.peek()) {
					int offset= fContentAssistSubjectControlAdapter.getSelectedRange().x;

					// iterate all contexts on the stack
					while (isValid(fContextInfoPopup) && !fContextFrameStack.isEmpty()) {
						ContextFrame top= fContextFrameStack.peek();
						if (top.fValidator == null || !top.fValidator.isContextInformationValid(offset)) {
							hideContextInfoPopup(false); // loop variant: reduces the number of contexts on the stack
						} else if (top.fPresenter != null && top.fPresenter.updatePresentation(offset, fTextPresentation)) {
							int widgetOffset= fContentAssistSubjectControlAdapter.getWidgetSelectionRange().x;
							TextPresentation.applyTextPresentation(fTextPresentation, fContextInfoText);
							resize(widgetOffset);
							break;
						} else
							break;
					}
				}
			}
		});
	}
}
