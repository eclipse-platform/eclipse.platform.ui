package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

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

	private PopupCloser fPopupCloser= new PopupCloser();
	private Shell fProposalShell;
	private Table fProposalTable;
	private boolean fInserting= false;
	
	private long fInvocationCounter= 0;
	private ICompletionProposal[] fFilteredProposals;
	private ICompletionProposal[] fComputedProposals;
	private int fInvocationOffset;
	private int fFilterOffset;
	
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
				
				
				fInvocationOffset= fViewer.getSelectedRange().x;
				fComputedProposals= computeProposals(fInvocationOffset);
				
				
				int count= (fComputedProposals == null ? 0 : fComputedProposals.length);
				if (count == 0) {
					
					if (!autoActivated)
						styledText.getDisplay().beep();
				
				} else {
					
					if (count == 1 && !autoActivated && fContentAssistant.isAutoInserting())
						
						insertProposal(fComputedProposals[0], (char) 0, fInvocationOffset);
					
					else {
					
						if (fLineDelimiter == null)
							fLineDelimiter= styledText.getLineDelimiter();
						
						createProposalSelector();
						setProposals(fComputedProposals);
						displayProposals();
					}
				}
			}
		});
		
		return getErrorMessage();
	}
	
	private ICompletionProposal[] computeProposals(int offset) {
		return fContentAssistant.computeCompletionProposals(fViewer, offset);
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
				selectProposal();
			}
		});

		fPopupCloser.install(fContentAssistant, fProposalTable);
		
		fProposalTable.setHeaderVisible(false);
		fContentAssistant.addToLayout(this, fProposalShell, ContentAssistant.LayoutManager.LAYOUT_PROPOSAL_SELECTOR, fContentAssistant.getSelectionOffset());
	}
	
	private ICompletionProposal getSelectedProposal() {
		int i= fProposalTable.getSelectionIndex();
		if (i < 0 || i >= fFilteredProposals.length)
			return null;
		return fFilteredProposals[i];
	}
		
	private void selectProposal() {
		ICompletionProposal p= getSelectedProposal();
		hide();
		if (p != null)
			insertProposal(p, (char) 0, fViewer.getSelectedRange().x);
	}
	
	private void insertProposal(ICompletionProposal p, char trigger, int offset) {
			
		fInserting= true;
		
		try {
			IDocument document= fViewer.getDocument();
			
			if (p instanceof ICompletionProposalExtension) {
				ICompletionProposalExtension e= (ICompletionProposalExtension) p;
				e.apply(document, trigger, offset);
			} else {
				p.apply(document);
			}
			
			Point selection= p.getSelection(document);
			if (selection != null) {
				fViewer.setSelectedRange(selection.x, selection.y);
				fViewer.revealRange(selection.x, selection.y);
			}
			
			IContextInformation info= p.getContextInformation();
			if (info != null) {				
				
				int position;
				if (p instanceof ICompletionProposalExtension) {
					ICompletionProposalExtension e= (ICompletionProposalExtension) p;
					position= e.getContextInformationPosition();
				} else {
					if (selection == null)
						selection= fViewer.getSelectedRange();
					position= selection.x + selection.y;
				}
				
				fContentAssistant.showContextInformation(info, position);
			}
		
		} finally {
			fInserting= false;
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

		fFilteredProposals= null;
	}
	
	public boolean isActive() {
		return fProposalShell != null && !fProposalShell.isDisposed();
	}
	
	private void setProposals(ICompletionProposal[] proposals) {
		if (Helper.okToUse(fProposalTable)) {

			fFilteredProposals= proposals;

			fProposalTable.setRedraw(false);
			fProposalTable.removeAll();

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
		if (fContentAssistant.addContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR)) {
			fProposalShell.setVisible(true);
			if (fAdditionalInfoController != null) {
				fAdditionalInfoController.install(fProposalTable);		
				fAdditionalInfoController.handleTableSelectionChanged();
			}
		}
	}
	
	public boolean verifyKey(VerifyEvent e) {
		if (!Helper.okToUse(fProposalShell))
			return true;
		
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

		} else {
			
			switch (key) {
				case 0x1B : // Esc
					e.doit= false;
					hide();
					break;
					
				case 0x0D : // Enter
					e.doit= false;
					selectProposal();
					break;
					
				default:
				
					if ('\t' == key) {
						e.doit= false;
						fProposalShell.setFocus();
						return false;
					}
				
					ICompletionProposal p= getSelectedProposal();
					if (p instanceof ICompletionProposalExtension) {
						ICompletionProposalExtension t= (ICompletionProposalExtension) p;
						char[] triggers= t.getTriggerCharacters();
						if (contains(triggers, key)) {		
							e.doit= false;
							hide();
							insertProposal(p, key, fViewer.getSelectedRange().x);
						}
					}
			}
		}
		
		return true;
	}
	
	private void selectProposal(int index) {
		fProposalTable.setSelection(index);
		fProposalTable.showSelection();
		if (fAdditionalInfoController != null)
			fAdditionalInfoController.handleTableSelectionChanged();
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
	
	public void processEvent(VerifyEvent e) {
		if (!fInserting)
			filterProposal();
	}
	
	private void filterProposal() {
		++ fInvocationCounter;
		Control control= fViewer.getTextWidget();
		control.getDisplay().asyncExec(new Runnable() {
			long fCounter= fInvocationCounter;
			public void run() {
				
				if (fCounter != fInvocationCounter) return;
				
				int offset= fViewer.getSelectedRange().x;
				ICompletionProposal[] proposals= (offset == -1 ? null : computeFilteredProposals(offset));
				fFilterOffset= offset;
				
				if (proposals != null && proposals.length > 0)
					setProposals(proposals);
				else
					hide();
			}
		});
	}
	
	private ICompletionProposal[] computeFilteredProposals(int offset) {
		
		if (offset == fInvocationOffset)
			return fComputedProposals;
			
		if (offset < fInvocationOffset) {
			fInvocationOffset= offset;
			fComputedProposals= computeProposals(fInvocationOffset);
			return fComputedProposals;
		}
		
		ICompletionProposal[] proposals= fComputedProposals;
		if (offset > fFilterOffset)
			proposals= fFilteredProposals;
			
		if (proposals == null)
			return null;
			
		IDocument document= fViewer.getDocument();
		int length= proposals.length;
		List filtered= new ArrayList(length);
		for (int i= 0; i < length; i++) {
			if (proposals[i] instanceof ICompletionProposalExtension) {
				
				ICompletionProposalExtension p= (ICompletionProposalExtension) proposals[i];
				if (p.isValidFor(document, offset))
					filtered.add(p);
					
			} else {
				// restore original behavior
				fInvocationOffset= offset;
				fComputedProposals= computeProposals(fInvocationOffset);
				return fComputedProposals;
			}
		}
		
		ICompletionProposal[] p= new ICompletionProposal[filtered.size()];
		filtered.toArray(p); 		
		return p;
	}
}


