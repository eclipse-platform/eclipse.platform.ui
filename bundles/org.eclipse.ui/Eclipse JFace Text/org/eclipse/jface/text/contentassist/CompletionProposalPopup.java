/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.contentassist;


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
 * @see org.eclipse.jface.text.contentassist.ICompletionProposal
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

	
	/**
	 * Creates a new completion proposal popup for the given elements.
	 * 
	 * @param contentAssistant the content assistant feeding this popup
	 * @param viewer the viewer on top of which this popup appears
	 * @param infoController the info control collaborating with this popup
	 * @since 2.0
	 */
	public CompletionProposalPopup(ContentAssistant contentAssistant, ITextViewer viewer, AdditionalInfoController infoController) {
		fContentAssistant= contentAssistant;
		fViewer= viewer;
		fAdditionalInfoController= infoController;
	}

	/**
	 * Computes and presents completion proposals. The flag indicates whether this call has
	 * be made out of an auto activation context.
	 * 
	 * @param autoActivated <code>true</code> if auto activation context
	 * @return an error message or <code>null</code> in case of no error
	 */
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
	
	/**
	 * Returns the completion proposal available at the given offset of the
	 * viewer's document. Delegates the work to the content assistant.
	 * 
	 * @param offset the offset
	 * @return the completion proposals available at this offset
	 */
	private ICompletionProposal[] computeProposals(int offset) {
		return fContentAssistant.computeCompletionProposals(fViewer, offset);
	}
	
	/**
	 * Returns the error message.
	 * 
	 * @return the error message
	 */
	private String getErrorMessage() {
		return fContentAssistant.getErrorMessage();
	}
	
	/**
	 * Creates the proposal selector.
	 */
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
	
	/**
	 * Returns the proposal selected in the proposal selector.
	 * 
	 * @return the selected proposal
	 * @since 2.0
	 */
	private ICompletionProposal getSelectedProposal() {
		int i= fProposalTable.getSelectionIndex();
		if (i < 0 || i >= fFilteredProposals.length)
			return null;
		return fFilteredProposals[i];
	}
		
	/**
	 * Takes the selected proposal and applies it.
	 * @since 2.0
	 */
	private void selectProposal() {
		ICompletionProposal p= getSelectedProposal();
		hide();
		if (p != null)
			insertProposal(p, (char) 0, fViewer.getSelectedRange().x);
	}
	
	/**
	 * Applies the given proposal at the given offset. The given character is the
	 * one that triggered the insertion of this proposal.
	 * 
	 * @param p the completion proposal
	 * @param trigger the trigger character
	 * @param offset the offset
	 * 
	 * @since 2.0
	 */
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
	
	/**
	 * Returns whether this popup has the focus.
	 * @return <code>true</code> if the popup has the focus
	 */
	public boolean hasFocus() {
		if (Helper.okToUse(fProposalShell))
			return (fProposalShell.isFocusControl() || fProposalTable.isFocusControl());

		return false;
	}
	
	/**
	 * Hides this popup.
	 */
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
	
	/**
	 *Returns whether this popup is active. It is active if the propsal selector is visible.
	 * @return <code>true</code> if this popup is active
	 */
	public boolean isActive() {
		return fProposalShell != null && !fProposalShell.isDisposed();
	}
	
	/**
	 * Initializes the proposal selector with these given proposals.
	 * 
	 * @param proposals the proposals
	 */
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
	
	/**
	 * Returns the graphical location at which this popup should be made visible.
	 * @return the location of this popup
	 */
	private Point getLocation() {
		StyledText text= fViewer.getTextWidget();
		int caret= text.getCaretOffset();
		Point p= text.getLocationAtOffset(caret);
		p= new Point(p.x, p.y + text.getLineHeight());
		return text.toDisplay(p);
	}

	/**
	 *Displays this popup and install the additional info controller, so that additional info
	 * is displayed when a proposal is selected and additional info is available.
	 */
	private void displayProposals() {
		if (fContentAssistant.addContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR)) {
			fProposalShell.setVisible(true);
			if (fAdditionalInfoController != null) {
				fAdditionalInfoController.install(fProposalTable);		
				fAdditionalInfoController.handleTableSelectionChanged();
			}
		}
	}
	
	/*
	 * @see IContentAssistListener#verifyKey(VerifyEvent)
	 */
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
	
	/**
	 * Selects the entry with the given index in the proposal selector and feeds
	 * the selection to the additional info controller.
	 * 
	 * @param index the index in the list
	 * @since 2.0
	 */
	private void selectProposal(int index) {
		fProposalTable.setSelection(index);
		fProposalTable.showSelection();
		if (fAdditionalInfoController != null)
			fAdditionalInfoController.handleTableSelectionChanged();
	}
	
	/**
	 * Returns whether the given character is contained in the given array of 
	 * characters.
	 * 
	 * @param characters the list of characters
	 * @param c the character to look for in the list
	 * @return <code>true</code> if character belongs to the list
	 * @since 2.0
	 */
	private boolean contains(char[] characters, char c) {
		
		if (characters == null)
			return false;
			
		for (int i= 0; i < characters.length; i++) {
			if (c == characters[i])
				return true;
		}
		
		return false;
	}
	
	/*
	 * @see IEventConsumer#processEvent(VerifyEvent)
	 */
	public void processEvent(VerifyEvent e) {
		if (!fInserting)
			filterProposal();
	}
	
	/**
	 * Filters the displayed proposal based on the given cursor position and the 
	 * offset of the original invocation of the content assistant.
	 */
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
	
	/**
	 * Computes the subset of already computed propsals that are still valid for
	 * the given offset.
	 * 
	 * @param offset the offset
	 * @return the set of filtered proposals
	 * @since 2.0
	 */
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


