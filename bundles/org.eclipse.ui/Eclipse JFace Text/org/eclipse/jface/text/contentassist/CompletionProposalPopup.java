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
import org.eclipse.swt.graphics.Color;
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
	private AdditionalInfoController fAdditionalInfoController;
	private int fListenerCount= 0;

	private PopupCloser fPopupCloser= new PopupCloser();
	private Shell fProposalShell;
	private Table fProposalTable;
	private ICompletionProposal[] fProposalInput;
	private boolean fIgnoreConsumedEvents= false;
	
	private String fLineDelimiter= null;

	public CompletionProposalPopup(ContentAssistant contentAssistant, ITextViewer viewer, AdditionalInfoController infoController) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
		fAdditionalInfoController= infoController;
	}

	public String showProposals(final boolean autoActivated) {
		final StyledText styledText= fViewer.getTextWidget();
		BusyIndicator.showWhile(styledText.getDisplay(), new Runnable() {
			public void run() {
				
				ICompletionProposal[] proposals= computeProposals();
				
				int count= (proposals == null ? 0 : proposals.length);
				if (count == 0) {
					
					if (!autoActivated)
						styledText.getDisplay().beep();
				
				} else {
					
					if (count == 1 && !autoActivated)
						
						insertProposal(proposals[0], (char) 0);
					
					else {
					
						if (fLineDelimiter == null)
							fLineDelimiter= styledText.getLineDelimiter();
						
						createProposalSelector();
						setProposals(proposals);
						displayProposals();
					}
				}
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
		fProposalTable= new Table(fProposalShell, SWT.H_SCROLL | SWT.V_SCROLL);
		
		int height= fProposalTable.getItemHeight() * 10;
		fProposalShell.setSize(302, height + 2);
		fProposalTable.setSize(300, height);
		fProposalTable.setLocation(1, 1);
		
		fProposalShell.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		Color c= fContentAssistant.getProposalSelectorBackground();
		if (c == null)
			c= control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		fProposalTable.setBackground(c);
		
		c= fContentAssistant.getProposalSelectorForeground();
		if (c == null)
			c= control.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		fProposalTable.setForeground(c);
		
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
	
	private ICompletionProposal getSelectedProposal() {
		int i= fProposalTable.getSelectionIndex();
		if (i < 0 || i >= fProposalInput.length)
			return null;
		return fProposalInput[i];
	}
	
	private void insertSelectedProposal() {
		ICompletionProposal p= getSelectedProposal();
		if (p != null)
			insertProposal(p, (char) 0);
	}
	
	private void insertProposal(ICompletionProposal p, char trigger) {
			
		// Turn off event consumption while the text is replaced.
		// This is important for the case that the selection
		// being inserted contains newlines.
		fIgnoreConsumedEvents= true;
		IDocument document= fViewer.getDocument();
		
		if (p instanceof ICompletionProposalExtension) {
			ICompletionProposalExtension e= (ICompletionProposalExtension) p;
			e.apply(document, trigger);
		} else {
			p.apply(document);
		}
		
		Point selection= p.getSelection(document);
		if (selection != null)
			fViewer.setSelectedRange(selection.x, selection.y);
		
		fIgnoreConsumedEvents= false;
		
		int position= selection.x + selection.y;
		IContextInformation info= p.getContextInformation();
		if (info != null) {				
			
			if (p instanceof ICompletionProposalExtension) {
				ICompletionProposalExtension e= (ICompletionProposalExtension) p;
				position= e.getContextInformationPosition();
			}
			
			fContentAssistant.showContextInformation(info, position);
		}
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
				item.setData(p);
			}

			Point currentLocation= fProposalShell.getLocation();
			Point newLocation= getLocation();
			if ((newLocation.x < currentLocation.x && newLocation.y == currentLocation.y) || newLocation.y < currentLocation.y) 
				fProposalShell.setLocation(newLocation);

			selectProposal(0);
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
		if (fAdditionalInfoController != null) {
			fAdditionalInfoController.install(fProposalTable);		
			fAdditionalInfoController.handleTableSelectionChanged();
		}
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
			
			selectProposal(newSelection);
			
			e.doit= false;
			return false;

		} else if (key == 0x1B) {
			hide(); // Terminate on Esc
		} else {
			filterProposal();
		}
		return true;
	}
	
	private void selectProposal(int index) {
		fProposalTable.setSelection(index);
		fProposalTable.showSelection();
		if (fAdditionalInfoController != null)
			fAdditionalInfoController.handleTableSelectionChanged();
	}
	
	public void processEvent(VerifyEvent event) {
		if (Helper.okToUse(fProposalShell))
			proposalProcessEvent(event);
	}

	private boolean contains(char[] characters, char c) {
		
		if (characters == null)
			return false;
			
		for (int i= 0; i < characters.length; i++) {
			if (c == characters[i])
				return true;
		}
		
		return false;
	}
	
	private void proposalProcessEvent(VerifyEvent e) {

		if (fIgnoreConsumedEvents)
			return;
			
		if (e.text == null || e.text.length() == 0) {
			filterProposal();
			return;
		}
		
		if (e.text.equals(fLineDelimiter)) {
			e.doit= false;
			insertSelectedProposal();
			hide();
			return;
		}
		
		if (e.text.length() == 1) {
			char trigger= e.text.charAt(0);
			ICompletionProposal p= getSelectedProposal();
			if (p instanceof ICompletionProposalExtension) {
				ICompletionProposalExtension t= (ICompletionProposalExtension) p;
				char[] triggers= t.getTriggerCharacters();
				if (contains(triggers, trigger)) {		
					e.doit= false;
					insertProposal(p, trigger);
					hide();
				}
			}
		}
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


