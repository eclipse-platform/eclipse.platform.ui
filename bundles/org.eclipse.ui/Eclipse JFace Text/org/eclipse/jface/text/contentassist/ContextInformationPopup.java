package org.eclipse.jface.text.contentassist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;



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

	private ITextViewer fViewer;
	private ContentAssistant fContentAssistant;
	private int fListenerCount= 0;

	private PopupCloser fPopupCloser= new PopupCloser();
	private Shell fContextSelectorShell;
	private Table fContextSelectorTable;
	private IContextInformation[] fContextSelectorInput;

	private Shell fContextInfoPopup;
	private Label fContextInfoLabel;
	
	public ContextInformationPopup(ContentAssistant contentAssistant, ITextViewer viewer) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
	}
	private IContextInformation[] computeContextInformation() {
		int pos= fViewer.getSelectedRange().x;
		return fContentAssistant.computeContextInformation(fViewer, pos);
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
			hideContextInfoPopup(); // Terminate on Esc
		} else {
			validateContextInformation();
		}
		return true;
	}
	private void contextInfoPopupProcessEvent(VerifyEvent e) {
		if (e.start != e.end && (e.text == null || e.text.length() == 0))
			validateContextInformation();
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

		} else {
			hideContextSelector(); // Terminate on Esc
			return true;
		}
	}
	private void contextSelectorProcessEvent(VerifyEvent e) {
		if (e.start == e.end && e.text != null && e.text.indexOf('\n') >= 0) {
			e.doit= false;
			insertSelectedContext();
		}

		hideContextSelector();
	}
	private void createContextInfoPopup() {
		if (Helper.okToUse(fContextInfoPopup))
			return;
		
		Control control= fViewer.getTextWidget();
		fContextInfoPopup= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		Color c= fContextInfoPopup.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		fContextInfoPopup.setBackground(c);
		fContextInfoLabel= new Label(fContextInfoPopup, SWT.LEFT | SWT.WRAP);
		c= fContentAssistant.getContextInfoPopupBackground();
		if (c != null)
			fContextInfoLabel.setBackground(c);
	}
	private void createContextSelector() {
		if (Helper.okToUse(fContextSelectorShell))
			return;
		
		Control control= fViewer.getTextWidget();
		fContextSelectorShell= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		fContextSelectorTable= new Table(fContextSelectorShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		fContextSelectorShell.setSize(300, fContextSelectorTable.getItemHeight() * 10);
		fContextSelectorTable.setBounds(fContextSelectorShell.getClientArea());

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
		fContentAssistant.addToLayout(this, fContextSelectorShell, ContentAssistant.LayoutManager.LAYOUT_CONTEXT_SELECTOR);
	}
	private void displayContextInfoPopup() {
		
		Point size= fContextInfoLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		size.x += 3;
		fContextInfoLabel.setSize(size);
		fContextInfoLabel.setLocation(1,1);
		size.x += 2;
		size.y += 2;
		fContextInfoPopup.setSize(size);

		fContentAssistant.addToLayout(this, fContextInfoPopup, ContentAssistant.LayoutManager.LAYOUT_CONTEXT_INFO_POPUP);
		fContextInfoPopup.setVisible(true);
	}
	private void displayContextSelector() {
		fContentAssistant.addContentAssistListener(this, ContentAssistant.CONTEXT_SELECTOR);
		fContextSelectorShell.setVisible(true);
	}
	private String getErrorMessage() {
		return fContentAssistant.getErrorMessage();
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
	private void hideContextInfoPopup() {
		if (Helper.okToUse(fContextInfoPopup)) {
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.CONTEXT_INFO_POPUP);
			
			fContextInfoPopup.setVisible(false);
			fContextInfoPopup.dispose();
			fContextInfoPopup= null;
		}
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
	private void insertSelectedContext() {
		int i= fContextSelectorTable.getSelectionIndex();

		if (i < 0 || i >= fContextSelectorInput.length)
			return;
			
		internalShowContextInfo(fContextSelectorInput[i]);
	}
	private void internalShowContextInfo(IContextInformation information) {
		createContextInfoPopup();
		setContextInformation(information);
		displayContextInfoPopup();
		
		
		int pos= fViewer.getSelectedRange().x;
		IContextInformationValidator validator= fContentAssistant.getContextInformationValidator(fViewer.getDocument(), pos);
		validator.install(information, fViewer, pos);
		fContentAssistant.addContentAssistListener(this, ContentAssistant.CONTEXT_INFO_POPUP);
	}
	public boolean isActive() {
		return (Helper.okToUse(fContextInfoPopup) || Helper.okToUse(fContextSelectorShell));
	}
	public void processEvent(VerifyEvent event) {
		if (Helper.okToUse(fContextSelectorShell))
			contextSelectorProcessEvent(event);
		if (Helper.okToUse(fContextInfoPopup))
			contextInfoPopupProcessEvent(event);
	}
	private void setContextInformation(IContextInformation information) {
		if (Helper.okToUse(fContextInfoLabel)) {

			fContextInfoLabel.setRedraw(false);

			Display display= fContextInfoLabel.getDisplay();
			if (information.getImage() != null)
				fContextInfoLabel.setImage(information.getImage());
			fContextInfoLabel.setText(information.getInformationDisplayString());

			fContextInfoLabel.setRedraw(true);
		}
	}
	private void setContexts(IContextInformation[] contexts) {
		if (Helper.okToUse(fContextSelectorTable)) {

			fContextSelectorInput= contexts;

			fContextSelectorTable.setRedraw(false);
			fContextSelectorTable.removeAll();

			Display display= fContextSelectorTable.getDisplay();

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
	public void showContextInformation(final IContextInformation info) {
		Control control= fViewer.getTextWidget();
		BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
			public void run() {
				internalShowContextInfo(info);
				hideContextSelector();
			}
		});
	}
	public String showContextProposals(final boolean beep) {
		final Control control= fViewer.getTextWidget();
		BusyIndicator.showWhile(control.getDisplay(), new Runnable() {
			public void run() {
				IContextInformation[] contexts= computeContextInformation();
				int count = (contexts == null ? 0 : contexts.length);
				if (count == 1) {
					// Show context information directly
					showContextInformation(contexts[0]);
				} else if (count > 0) {
					// Precise context must be selected
					createContextSelector();
					setContexts(contexts);
					displayContextSelector();
					hideContextInfoPopup();
				} else if (beep) {
					control.getDisplay().beep();
				}
			}
		});
		
		return getErrorMessage();
	}
	private void validateContextInformation() {
		IDocument doc= fViewer.getDocument();
		int pos= fViewer.getSelectedRange().x;
		
		IContextInformationValidator validator= fContentAssistant.getContextInformationValidator(doc, pos);
		if (validator == null || !validator.isContextInformationValid(pos))
			hideContextInfoPopup();
	}
	public boolean verifyKey(VerifyEvent e) {
		if (Helper.okToUse(fContextSelectorShell))
			return contextSelectorKeyPressed(e);
		if (Helper.okToUse(fContextInfoPopup))
			return contextInfoPopupKeyPressed(e);
		return true;
	}
}
