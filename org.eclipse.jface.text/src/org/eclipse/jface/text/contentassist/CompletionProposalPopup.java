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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.TextUtilities;



/**
 * This class is used to present proposals to the user. If additional
 * information exists for a proposal, then selecting that proposal
 * will result in the information being displayed in a secondary
 * window.
 *
 * @see org.eclipse.jface.text.contentassist.ICompletionProposal
 * @see org.eclipse.jface.text.contentassist.AdditionalInfoController
 */
class CompletionProposalPopup implements IContentAssistListener {
	
	/** The associated text viewer */
	private ITextViewer fViewer;
	/** The associated content assistant */
	private ContentAssistant fContentAssistant;
	/** The used additional info controller */
	private AdditionalInfoController fAdditionalInfoController;
	/** The closing strategy for this completion proposal popup */
	private PopupCloser fPopupCloser= new PopupCloser();
	/** The popup shell */
	private Shell fProposalShell;
	/** The proposal table */
	private Table fProposalTable;
	/** Indicates whether a completion proposal is being inserted */
	private boolean fInserting= false;
	/** The key listener to control navigation */
	private KeyListener fKeyListener;
	/** List of document events used for filtering proposals */
	private List fDocumentEvents= new ArrayList();
	/** Listener filling the document event queue */
	private IDocumentListener fDocumentListener;
	/** Reentrance count for <code>filterProposals</code> */
	private long fInvocationCounter= 0;
	/** The filter list of proposals */
	private ICompletionProposal[] fFilteredProposals;
	/** The computed list of proposals */
	private ICompletionProposal[] fComputedProposals;
	/** The offset for which the proposals have been computed */
	private int fInvocationOffset;
	/** The offset for which the computed proposaks have been filtered */
	private int fFilterOffset;
	/** The default line delimiter of the viewer's widget */
	private String fLineDelimiter;

	
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
					
		if (fKeyListener == null) {
			fKeyListener= new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (!Helper.okToUse(fProposalShell))
						return;
					
					if (e.character == 0 && e.keyCode == SWT.MOD1) {
						// http://dev.eclipse.org/bugs/show_bug.cgi?id=34754
						int index= fProposalTable.getSelectionIndex();
						if (index >= 0)
							selectProposal(index, true);
					}									
				}

				public void keyReleased(KeyEvent e) {
					if (!Helper.okToUse(fProposalShell))
						return;

					if (e.character == 0 && e.keyCode == SWT.MOD1) {
						// http://dev.eclipse.org/bugs/show_bug.cgi?id=34754
						int index= fProposalTable.getSelectionIndex();
						if (index >= 0)
							selectProposal(index, false);
					}
				}
			};
		}

		final StyledText styledText= fViewer.getTextWidget();
		if (styledText != null && !styledText.isDisposed())
			styledText.addKeyListener(fKeyListener);

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
						
						insertProposal(fComputedProposals[0], (char) 0, 0, fInvocationOffset);
					
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
		fProposalShell= new Shell(control.getShell(), SWT.ON_TOP | SWT.RESIZE );
		fProposalTable= new Table(fProposalShell, SWT.H_SCROLL | SWT.V_SCROLL);
		
		fProposalTable.setLocation(0, 0);
		if (fAdditionalInfoController != null)
			fAdditionalInfoController.setSizeConstraints(50, 10, true, false);
		
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;		
		fProposalShell.setLayout(layout);		

		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= fProposalTable.getItemHeight() * 10;
		data.widthHint= 300;
		fProposalTable.setLayoutData(data);

		fProposalShell.pack();
		
		if (fAdditionalInfoController != null) {
			fProposalShell.addControlListener(new ControlListener() {
				
				public void controlMoved(ControlEvent e) {}
				
				public void controlResized(ControlEvent e) {
					// resets the cached resize constraints
					fAdditionalInfoController.setSizeConstraints(50, 10, true, false);
				}
			});
		}
		
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
			
			public void widgetSelected(SelectionEvent e) {}

			public void widgetDefaultSelected(SelectionEvent e) {
				selectProposalWithMask(e.stateMask);
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
	 * 
	 * @param stateMask the state mask
	 * @since 2.1
	 */
	private void selectProposalWithMask(int stateMask) {
		ICompletionProposal p= getSelectedProposal();
		hide();
		if (p != null)
			insertProposal(p, (char) 0, stateMask, fViewer.getSelectedRange().x);
	}
	
	/**
	 * Applies the given proposal at the given offset. The given character is the
	 * one that triggered the insertion of this proposal.
	 * 
	 * @param p the completion proposal
	 * @param trigger the trigger character
	 * @param offset the offset
	 * @since 2.1
	 */
	private void insertProposal(ICompletionProposal p, char trigger, int stateMask, int offset) {
			
		fInserting= true;
		IRewriteTarget target= null;
		
		try {
			
			IDocument document= fViewer.getDocument();
			
			if (fViewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= (ITextViewerExtension) fViewer;
				target= extension.getRewriteTarget();
			}
			
			if (target != null)
				target.beginCompoundChange();
			
			if (p instanceof ICompletionProposalExtension2) {
				ICompletionProposalExtension2 e= (ICompletionProposalExtension2) p;
				e.apply(fViewer, trigger, stateMask, offset);				
			} else if (p instanceof ICompletionProposalExtension) {
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
			if (target != null)
				target.endCompoundChange();
			fInserting= false;
		}
	}
	
	/**
	 * Returns whether this popup has the focus.
	 * 
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

		if (fDocumentListener != null) {
			IDocument document= fViewer.getDocument();
			if (document != null)
				document.removeDocumentListener(fDocumentListener);
			fDocumentListener= null;
		}
		fDocumentEvents.clear();		

		StyledText styledText= fViewer.getTextWidget();
		if (fKeyListener != null && styledText != null && !styledText.isDisposed())
			styledText.removeKeyListener(fKeyListener);

		if (Helper.okToUse(fProposalTable)) {
			ICompletionProposal proposal= getSelectedProposal();
			if (proposal instanceof ICompletionProposalExtension2) {
				ICompletionProposalExtension2 extension= (ICompletionProposalExtension2) proposal;
				extension.unselected(fViewer);
			}
		}

		if (Helper.okToUse(fProposalShell)) {
			
			fContentAssistant.removeContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR);
			
			fPopupCloser.uninstall();
			fProposalShell.setVisible(false);
			fProposalShell.dispose();
			fProposalShell= null;
		}
		
		fFilteredProposals= null;
		
		fContentAssistant.possibleCompletionsClosed();
	}
	
	/**
	 *Returns whether this popup is active. It is active if the propsal selector is visible.
	 *
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

			ICompletionProposal oldProposal= getSelectedProposal();
			if (oldProposal instanceof ICompletionProposalExtension2)
				((ICompletionProposalExtension2) oldProposal).unselected(fViewer);

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

			selectProposal(0, false);
			fProposalTable.setRedraw(true);
		}
	}
	
	/**
	 * Returns the graphical location at which this popup should be made visible.
	 * 
	 * @return the location of this popup
	 */
	private Point getLocation() {
		StyledText text= fViewer.getTextWidget();
		int caret= text.getCaretOffset();
		Point p= text.getLocationAtOffset(caret);
		if (p.x < 0) p.x= 0;
		if (p.y < 0) p.y= 0;
		p= new Point(p.x, p.y + text.getLineHeight());
		p= text.toDisplay(p);
		if (p.x < 0) p.x= 0;
		if (p.y < 0) p.y= 0;
		return p;
	}

	/**
	 *Displays this popup and install the additional info controller, so that additional info
	 * is displayed when a proposal is selected and additional info is available.
	 */
	private void displayProposals() {
		if (fContentAssistant.addContentAssistListener(this, ContentAssistant.PROPOSAL_SELECTOR)) {
			
			if (fDocumentListener == null)
				fDocumentListener=  new IDocumentListener()  {
					public void documentAboutToBeChanged(DocumentEvent event) {
						if (!fInserting)
							fDocumentEvents.add(event);
					}
	
					public void documentChanged(DocumentEvent event) {
						if (!fInserting)
							filterProposals();
					}
				};
			IDocument document= fViewer.getDocument();
			if (document != null)
				document.addDocumentListener(fDocumentListener);		
				
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
			boolean smartToggle= false;
			switch (e.keyCode) {

				case SWT.ARROW_LEFT :
				case SWT.ARROW_RIGHT :
					filterProposals();
					return true;

				case SWT.ARROW_UP :
					newSelection -= 1;
					if (newSelection < 0)
						newSelection= fProposalTable.getItemCount() - 1;
					break;

				case SWT.ARROW_DOWN :
					newSelection += 1;
					if (newSelection > fProposalTable.getItemCount() - 1)
						newSelection= 0;
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
					
				default :
					if (e.keyCode != SWT.MOD1 && e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3 && e.keyCode != SWT.MOD4)
						hide();
					return true;
			}
			
			selectProposal(newSelection, smartToggle);
			
			e.doit= false;
			return false;

		} else {
			
			switch (key) {
				case 0x1B: // Esc
					e.doit= false;
					hide();
					break;
					
				case '\n': // Ctrl-Enter on w2k
				case '\r': // Enter
					e.doit= false;
					selectProposalWithMask(e.stateMask);
					break;
				
				case '\t':
					e.doit= false;
					fProposalShell.setFocus();
					return false;
					
				default:			
					ICompletionProposal p= getSelectedProposal();
					if (p instanceof ICompletionProposalExtension) {
						ICompletionProposalExtension t= (ICompletionProposalExtension) p;
						char[] triggers= t.getTriggerCharacters();
						if (contains(triggers, key)) {		
							e.doit= false;
							hide();
							insertProposal(p, key, e.stateMask, fViewer.getSelectedRange().x);
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
	 * @param smartToggle <code>true</code> if the smart toogle key has been pressed
	 * @since 2.1
	 */
	private void selectProposal(int index, boolean smartToggle) {

		ICompletionProposal oldProposal= getSelectedProposal();
		if (oldProposal instanceof ICompletionProposalExtension2)
			((ICompletionProposalExtension2) oldProposal).unselected(fViewer);

		ICompletionProposal proposal= fFilteredProposals[index];
		if (proposal instanceof ICompletionProposalExtension2)
			((ICompletionProposalExtension2) proposal).selected(fViewer, smartToggle);
	
		
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
	}
	
	/**
	 * Filters the displayed proposal based on the given cursor position and the 
	 * offset of the original invocation of the content assistant.
	 */
	private void filterProposals() {
		++ fInvocationCounter;
		Control control= fViewer.getTextWidget();
		control.getDisplay().asyncExec(new Runnable() {
			long fCounter= fInvocationCounter;
			public void run() {
				
				if (fCounter != fInvocationCounter) return;
				
				int offset= fViewer.getSelectedRange().x;
				ICompletionProposal[] proposals= null;
				try  {
					if (offset > -1) {
						DocumentEvent event= TextUtilities.mergeProcessedDocumentEvents(fDocumentEvents);
						proposals= computeFilteredProposals(offset, event);
					}
				} catch (BadLocationException x)  {
				} finally  {
					fDocumentEvents.clear();
				}
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
	 * @param event the merged document event
	 * @return the set of filtered proposals
	 * @since 2.0
	 */
	private ICompletionProposal[] computeFilteredProposals(int offset, DocumentEvent event) {
		
		if (offset == fInvocationOffset && event == null)
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
				
			if (proposals[i] instanceof ICompletionProposalExtension2) {

				ICompletionProposalExtension2 p= (ICompletionProposalExtension2) proposals[i];				
				if (p.validate(document, offset, event))
					filtered.add(p);
			
			} else if (proposals[i] instanceof ICompletionProposalExtension) {
								
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

	/**
	 * Requests the proposal shell to take focus.
	 * 
	 * @since 3.0
	 */
	public void setFocus() {
		if (Helper.okToUse(fProposalShell)) {
			fProposalShell.setFocus();
		}		
	}
	
}
