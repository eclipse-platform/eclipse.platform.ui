package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;



/**
 * This class is used to present proposals to the user. If additional
 * information exists for a proposal, then selecting that proposal
 * will result in the information being displayed in a secondary
 * window.
 *
 * @see IContentAssistProposal
 * @see IAdditionalInfoPopup
 */
class CompletionProposalPopup implements IContentAssistListener {
	
	private ITextViewer fViewer;
	private ContentAssistant fContentAssistant;
	private AdditionalInfoPopup fAdditionalInfoPopup;
	private int fListenerCount= 0;

	private PopupCloser fPopupCloser= new PopupCloser();
	private Shell fProposalShell;
	private Table fProposalTable;
	private ICompletionProposal[] fProposalInput;
	private boolean fIgnoreConsumedEvents= false;
	private String fLineDelimiter= null;

	public CompletionProposalPopup(ContentAssistant contentAssistant, ITextViewer viewer, AdditionalInfoPopup presenter) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
		fAdditionalInfoPopup= presenter;
	}

	public String showProposals(final boolean beep) {
		final StyledText styledText= fViewer.getTextWidget();
		BusyIndicator.showWhile(styledText.getDisplay(), new Runnable() {
			public void run() {
				ICompletionProposal[] proposals= computeProposals();
				int count= (proposals == null ? 0 : proposals.length);
				if (count > 0) {
					
					if (fLineDelimiter == null)
						fLineDelimiter= styledText.getLineDelimiter();
					
					createProposalSelector();
					setProposals(proposals);
					displayProposals();
					
					fAdditionalInfoPopup.install(fProposalInput, fViewer, fProposalShell, fProposalTable);
				
				} else if (beep) 
					styledText.getDisplay().beep();
			}
		});
		
		return getErrorMessage();
	}
	
	private ICompletionProposal[] computeProposals() {
		int pos= fViewer.getSelectedRange().x;
		return fContentAssistant.computeCompletionProposals(fViewer, pos);
	}
	
	private String getErrorMessage() {
		return fContentAssistant.getErrorMessage();
	}

	private void createProposalSelector() {
		if (Helper.okToUse(fProposalShell))
			return;
		
		Control control= fViewer.getTextWidget();
		fProposalShell= new Shell(control.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
		fProposalTable= new Table(fProposalShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		fProposalShell.setSize(300, fProposalTable.getItemHeight() * 10);
		fProposalTable.setBounds(fProposalShell.getClientArea());

		fProposalTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				insertSelectedProposal();
				hide();
			}
		});

		fPopupCloser.install(fContentAssistant, fProposalTable);

		fProposalTable.setHeaderVisible(false);
		fContentAssistant.addToLayout(this, fProposalShell, ContentAssistant.LayoutManager.LAYOUT_PROPOSAL_SELECTOR);
	}
	
	private void insertSelectedProposal() {
		int i= fProposalTable.getSelectionIndex();

		if (i < 0 || i >= fProposalInput.length)
			return;

		// Turn off event consumption while the text is replaced.
		// This is important for the case that the selection
		// being inserted contains newlines.
		fIgnoreConsumedEvents= true;
		IDocument document= fViewer.getDocument();
		fProposalInput[i].apply(document);
		Point selection= fProposalInput[i].getSelection(document);
		if (selection != null)
			fViewer.setSelectedRange(selection.x, selection.y);
		
		fIgnoreConsumedEvents= false;
		
		IContextInformation info= fProposalInput[i].getContextInformation();
		if (info != null)
			fContentAssistant.showContextInformation(info);
	}
	
	public boolean hasFocus() {
		if (Helper.okToUse(fProposalShell))
			return (fProposalShell.isFocusControl() || fProposalTable.isFocusControl());

		return false;
	}
	
	public void hide() {
		if (Helper.okToUse(fProposalShell)) {
			
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR);
			
			fPopupCloser.uninstall();
			fProposalShell.setVisible(false);
			fProposalShell.dispose();
			fProposalShell= null;
		}
	}
	
	public boolean isActive() {
		return fProposalShell != null && !fProposalShell.isDisposed();
	}
	
	private void setProposals(ICompletionProposal[] proposals) {
		if (Helper.okToUse(fProposalTable)) {

			fProposalInput= proposals;

			fProposalTable.setRedraw(false);
			fProposalTable.removeAll();

			Display display= fProposalTable.getDisplay();

			TableItem item;
			ICompletionProposal p;
			for (int i= 0; i < proposals.length; i++) {
				p= proposals[i];
				item= new TableItem(fProposalTable, SWT.NULL);
				if (p.getImage() != null)
					item.setImage(p.getImage());
				item.setText(p.getDisplayString());
			}

			Point currentLocation= fProposalShell.getLocation();
			Point newLocation= getLocation();
			if ((newLocation.x < currentLocation.x && newLocation.y == currentLocation.y) || newLocation.y < currentLocation.y) 
				fProposalShell.setLocation(newLocation);

			fProposalTable.select(0);
			fProposalTable.setRedraw(true);
		}
	}
	
	private Point getLocation() {
		StyledText text= fViewer.getTextWidget();
		int caret= text.getCaretOffset();
		Point p= text.getLocationAtOffset(caret);
		p= new Point(p.x, p.y + text.getLineHeight());
		return text.toDisplay(p);
	}

	private void displayProposals() {
		fContentAssistant.addContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR);
		fProposalShell.setVisible(true);
	}
	
	public boolean verifyKey(VerifyEvent e) {
		if (Helper.okToUse(fProposalShell))
			return proposalKeyPressed(e);
		return true;
	}
	
	private boolean proposalKeyPressed(VerifyEvent e) {

		char key= e.character;
		if (key == 0) {
			int newSelection= fProposalTable.getSelectionIndex();
			int visibleRows= (fProposalTable.getSize().y / fProposalTable.getItemHeight()) - 1;
			switch (e.keyCode) {

				case SWT.ARROW_LEFT :
				case SWT.ARROW_RIGHT :
					filterProposal();
					return true;

				case SWT.ARROW_UP :
					newSelection -= (newSelection > 0 ? 1 : 0);
					break;

				case SWT.ARROW_DOWN :
					newSelection += (newSelection < fProposalTable.getItemCount() - 1 ? 1 : 0);
					break;
					
				case SWT.PAGE_DOWN :
					newSelection += visibleRows;
					if (newSelection >= fProposalTable.getItemCount())
						newSelection= fProposalTable.getItemCount() - 1;
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
					newSelection= fProposalTable.getItemCount() - 1;
					break;
					
				case SWT.CTRL :
				case SWT.SHIFT :
					return true;

				default :
					hide();
					return true;
			}
			
			fProposalTable.setSelection(newSelection);
			fProposalTable.showSelection();
			e.doit= false;
			return false;

		} else if (key == 0x1B) {
			hide(); // Terminate on Esc
		} else {
			filterProposal();
		}
		return true;
	}
	
	public void processEvent(VerifyEvent event) {
		if (Helper.okToUse(fProposalShell))
			proposalProcessEvent(event);
	}

	private void proposalProcessEvent(VerifyEvent e) {

		if (fIgnoreConsumedEvents)
			return;

		if (e.start == e.end && e.text != null && e.text.equals(fLineDelimiter)) {
			e.doit= false;
			insertSelectedProposal();
			hide();
			return;
		}

		if (e.start != e.end && (e.text == null || e.text.length() == 0))
			filterProposal();
	}

	private void filterProposal() {
		Control control= fViewer.getTextWidget();
		Display d= control.getDisplay();
		d.asyncExec(new Runnable() {
			public void run() {
				ICompletionProposal[] proposals= computeProposals();
				if (proposals != null && proposals.length > 0)
					setProposals(proposals);
				else
					hide();
			}
		});
	}
}


