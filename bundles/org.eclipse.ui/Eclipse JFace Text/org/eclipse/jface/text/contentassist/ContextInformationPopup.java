package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;

import java.util.Stack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistant.LayoutManager;


/**
 * This class is used to present context information to the user. 
 * If multiple contexts are valid at the current cursor location, 
 * a list is presented from which the user may choose one context. 
 * Once the user makes their choice, or if there was only a single 
 * posible context, the context information is shown in a tooltip like popup.
 *
 * @see IContextInformation
 * @see IContextInformationValidator
 */
class ContextInformationPopup implements IContentAssistListener {
	
	
	
	static class ContextFrame {
		public int fBeginOffset;
		public int fOffset;
		public int fVisibleOffset;
		public IContextInformation fInformation;
		public IContextInformationValidator fValidator;
		public IContextInformationPresenter fPresenter;
	};

	private ITextViewer fViewer;
	private ContentAssistant fContentAssistant;
	private int fListenerCount= 0;

	private PopupCloser fPopupCloser= new PopupCloser();
	private Shell fContextSelectorShell;
	private Table fContextSelectorTable;
	private IContextInformation[] fContextSelectorInput;
	private String fLineDelimiter= null;

	private Shell fContextInfoPopup;
	private StyledText fContextInfoText;
	private TextPresentation fTextPresentation;
	
	private Stack fContextFrameStack= new Stack();
	
	
	public ContextInformationPopup(ContentAssistant contentAssistant, ITextViewer viewer) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
	}

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
					
				} else if (!autoActivated) {
					styledText.getDisplay().beep();
				}
			}
		});
		
		return getErrorMessage();
	}
	
	public void showContextInformation(final IContextInformation info, final int position) {
		Control control= fViewer.getTextWidget();
		BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
			public void run() {
				internalShowContextInfo(info, position);
				hideContextSelector();
			}
		});
	}
	
		private void internalShowContextInfo(IContextInformation information, int offset) {
					
			IContextInformationValidator validator= fContentAssistant.getContextInformationValidator(fViewer.getDocument(), offset);
			
			if (validator != null) {
				ContextFrame current= new ContextFrame();
				current.fInformation= information;
				current.fBeginOffset= (information instanceof IContextInformationExtension)
					? ((IContextInformationExtension) information).getContextInformationPosition()
					: offset;										
				current.fOffset= offset;
				current.fVisibleOffset= fViewer.getTextWidget().getSelectionRange().x - (offset - current.fBeginOffset);
				current.fValidator= validator;
				current.fPresenter= fContentAssistant.getContextInformationPresenter(fViewer.getDocument(), offset);
				
				fContextFrameStack.push(current);
				
				internalShowContextFrame(current, fContextFrameStack.size() == 1);
			}
		}
	
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
		if (fTextPresentation != null) {
			TextPresentation.applyTextPresentation(fTextPresentation, fContextInfoText);
			resize();
		}
		
		if (initial) {
			if (fContentAssistant.addContentAssistListener(this, ContentAssistant.CONTEXT_INFO_POPUP)) {	
				fContentAssistant.addToLayout(this, fContextInfoPopup, ContentAssistant.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP, frame.fVisibleOffset);
				fContextInfoPopup.setVisible(true);
			}
		} else {
			fContentAssistant.layout(ContentAssistant.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP, frame.fVisibleOffset);
		}
	}
	
	private IContextInformation[] computeContextInformation(int position) {
		return fContentAssistant.computeContextInformation(fViewer, position);
	}
	
	private String getErrorMessage() {
		return fContentAssistant.getErrorMessage();
	}

	private void createContextInfoPopup() {
		if (Helper.okToUse(fContextInfoPopup))
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
		
	private void resize() {
		Point size= fContextInfoText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		size.x += 3;
		fContextInfoText.setSize(size);
		fContextInfoText.setLocation(1,1);
		size.x += 2;
		size.y += 2;
		fContextInfoPopup.setSize(size);
	}
	
	private void hideContextInfoPopup() {
		
		if (Helper.okToUse(fContextInfoPopup)) {
			
			int size= fContextFrameStack.size();
			if (size > 0) {
				fContextFrameStack.pop();
				-- size;
			}
			
			if (size > 0) {
				ContextFrame current= (ContextFrame) fContextFrameStack.peek();
				internalShowContextFrame(current, false);
			} else {
				
				fContentAssistant.removeContentAssistListener(this, ContentAssistant.CONTEXT_INFO_POPUP);
				
				fContextInfoPopup.setVisible(false);
				fContextInfoPopup.dispose();
				fContextInfoPopup= null;
				
				if (fTextPresentation != null) {
					fTextPresentation.clear();
					fTextPresentation= null;
				}
			}
		}
	}
	
	private void createContextSelector() {
		if (Helper.okToUse(fContextSelectorShell))
			return;
		
		Control control= fViewer.getTextWidget();
		fContextSelectorShell= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		fContextSelectorTable= new Table(fContextSelectorShell, SWT.H_SCROLL | SWT.V_SCROLL);

		int height= fContextSelectorTable.getItemHeight() * 10;
		fContextSelectorShell.setSize(302, height + 2);
		fContextSelectorTable.setSize(300, height);
		fContextSelectorTable.setLocation(1, 1);
		
		fContextSelectorShell.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
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
		fContentAssistant.addToLayout(this, fContextSelectorShell, ContentAssistant.LayoutManager.LAYOUT_CONTEXT_SELECTOR, fContentAssistant.getSelectionOffset());
	}
	
	private void insertSelectedContext() {
		int i= fContextSelectorTable.getSelectionIndex();

		if (i < 0 || i >= fContextSelectorInput.length)
			return;
		
		int position= fViewer.getSelectedRange().x;
		internalShowContextInfo(fContextSelectorInput[i], position);
	}
	
	private void setContexts(IContextInformation[] contexts) {
		if (Helper.okToUse(fContextSelectorTable)) {

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
	
	private void displayContextSelector() {
		if (fContentAssistant.addContentAssistListener(this, ContentAssistant.CONTEXT_SELECTOR))
			fContextSelectorShell.setVisible(true);
	}

	private void hideContextSelector() {
		if (Helper.okToUse(fContextSelectorShell)) {
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.CONTEXT_SELECTOR);
			
			fPopupCloser.uninstall();
			fContextSelectorShell.setVisible(false);
			fContextSelectorShell.dispose();
			fContextSelectorShell= null;
		}
	}
	
	public boolean hasFocus() {
		if (Helper.okToUse(fContextSelectorShell))
			return (fContextSelectorShell.isFocusControl() || fContextSelectorTable.isFocusControl());

		return false;
	}
	
	public void hide() {
		hideContextSelector();
		hideContextInfoPopup();
	}
	
	public boolean isActive() {
		return (Helper.okToUse(fContextInfoPopup) || Helper.okToUse(fContextSelectorShell));
	}
	
	public boolean verifyKey(VerifyEvent e) {
		if (Helper.okToUse(fContextSelectorShell))
			return contextSelectorKeyPressed(e);
		if (Helper.okToUse(fContextInfoPopup))
			return contextInfoPopupKeyPressed(e);
		return true;
	}
	
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
					
				case SWT.CTRL:
				case SWT.SHIFT:
					return true;
				default:
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
		} else if (key == 0x1B) {
			// terminate on Esc
			hideContextSelector();
		}
		
		return true;
	}

	private boolean contextInfoPopupKeyPressed(KeyEvent e) {

		char key= e.character;
		if (key == 0) {
			
			switch (e.keyCode) {

				case SWT.ARROW_LEFT:
				case SWT.ARROW_RIGHT:
					validateContextInformation();
					break;
				case SWT.CTRL:
				case SWT.SHIFT:
					break;
				default:
					hideContextInfoPopup();
					break;
			}
			
		} else if (key == 0x1B) {
			// terminate on Esc
 			hideContextInfoPopup();
		} else {
			validateContextInformation();
		}
		return true;
	}
	
	public void processEvent(VerifyEvent event) {
		if (Helper.okToUse(fContextSelectorShell))
			contextSelectorProcessEvent(event);
		if (Helper.okToUse(fContextInfoPopup))
			contextInfoPopupProcessEvent(event);
	}

	private void contextSelectorProcessEvent(VerifyEvent e) {
		
		if (e.start == e.end && e.text != null && e.text.equals(fLineDelimiter)) {
			e.doit= false;
			insertSelectedContext();
		}

		hideContextSelector();
	}

	private void contextInfoPopupProcessEvent(VerifyEvent e) {
		if (e.start != e.end && (e.text == null || e.text.length() == 0))
			validateContextInformation();
	}
		
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
				if (Helper.okToUse(fContextInfoPopup) && fFrame == fContextFrameStack.peek()) {
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