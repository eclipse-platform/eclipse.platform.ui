package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.ITextViewer;



/**
 * Displays the additional information available for a completion proposal.
 */
class AdditionalInfoPopup implements SelectionListener, DisposeListener, Runnable {
	
	private ICompletionProposal[] fProposals;
	private ITextViewer fTextViewer;
	private Shell fProposalShell;
	private Table fProposalTable;

	private Thread fThread;
	private int fDelayInterval;
	private boolean fIsReset= false;
	private Object fMutex= new Object();
	private Shell fInfoPopup;
	private Label fInfoLabel;
	
	protected AdditionalInfoPopup(int delayInterval) {
		fDelayInterval= delayInterval;
	}
	protected void createInfoPopup() {
		if (Helper.okToUse(fInfoPopup))
			return;
			
		fInfoPopup= new Shell(fProposalShell, SWT.NO_TRIM | SWT.ON_TOP);
		fInfoLabel= new Label(fInfoPopup, SWT.LEFT | SWT.WRAP);
		
		Display display= fProposalShell.getDisplay();
		fInfoPopup.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		fInfoLabel.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
	}
	protected void displayInfoPopup() {
		
		Point size= fInfoLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		fInfoLabel.setSize(size.x + 3, size.y);
		fInfoPopup.setSize(size.x + 5, size.y + 2);
		
		fInfoLabel.setLocation(1,1);
		
		Point location= getLocation();
		if (location != null) {
			fInfoPopup.setLocation(location);
			fInfoPopup.setVisible(true);
		}
	}
	protected Point getLocation() {
		TableItem[] selection= fProposalTable.getSelection();
		if (selection != null && selection.length > 0) {
			Rectangle r= selection[0].getBounds(0);
			Point location= fProposalShell.toDisplay(fProposalTable.getLocation());
			int offset= (fProposalTable.getSelectionIndex() - fProposalTable.getTopIndex() + 1) * fProposalTable.getItemHeight();
			return new Point(location.x + r.x + (r.width / 2), location.y + offset);
		} 
		
		return null;
	}
	public void install(ICompletionProposal[] proposals, ITextViewer viewer, Shell proposalShell, Table proposalTable) {
		
		if (fThread != null) {
			reset();
			removeProposalPopupListeners();
		}
		
		fProposals= proposals;
		fTextViewer= viewer;
		fProposalShell= proposalShell;
		fProposalTable= proposalTable;
		
		fProposalTable.addSelectionListener(this);
		fProposalShell.addDisposeListener(this);
		
		if (fThread == null) {
			fThread= new Thread(this, "AdditionalInfo Delay"); //$NON-NLS-1$
			fThread.start();
		}
	}
	protected void removeProposalPopupListeners() {
		
		if (Helper.okToUse(fProposalShell))
			fProposalShell.removeDisposeListener(this);
		fProposalShell= null;
		
		if (Helper.okToUse(fProposalTable))
			fProposalTable.removeSelectionListener(this);
		fProposalTable= null;
	}
	protected void reset() {
		
		if (fInfoPopup != null && !fInfoPopup.isDisposed())
			fInfoPopup.setVisible(false);
			
		synchronized (fMutex) {
			fIsReset= true;
			fMutex.notifyAll();
		}
	}
	public void run() {
		try {
			while (true) {
				synchronized (fMutex) {
					// Wait for a selection event to occur.
					fMutex.wait();
					while (true) {
						fIsReset= false;
						// Delay before showing the popup.
						fMutex.wait(fDelayInterval);
						if (!fIsReset)
							break;
					}
				}
				showInfoPopup();
			}
		} catch (InterruptedException e) {
		}
		fThread= null;
	}
	protected void setInfo(String additionalInfo) {
		fInfoLabel.setText(additionalInfo);
	}
	protected void showInfoPopup() {
		Control control= fTextViewer.getTextWidget();
		Display d= control.getDisplay();
		if (d != null) {
			try {
				d.syncExec(new Runnable() {
					public void run() {
						
						if (fProposalTable != null && !fProposalTable.isDisposed()) {
							
							String additionalInfo= null;
							
							try {
								int selectionIndex= fProposalTable.getSelectionIndex();
								additionalInfo= fProposals[selectionIndex].getAdditionalProposalInfo();
							} catch (ArrayIndexOutOfBoundsException x) {
							}
							
							if (additionalInfo == null || additionalInfo.length() == 0) {
								if (Helper.okToUse(fInfoPopup))
									fInfoPopup.setVisible(false);
							
							} else {
								createInfoPopup();
								setInfo(additionalInfo);
								displayInfoPopup();
							}
						}
					}
				});
			} catch (SWTError e) {
			}
		}
	}
	protected void stop() {
		if (fThread != null) {
			fThread.interrupt();
		}
	}
	public void widgetDefaultSelected(SelectionEvent e) {}
	public void widgetDisposed(DisposeEvent event) {
		
		stop();
		
		removeProposalPopupListeners();
		
		if (Helper.okToUse(fInfoPopup)) {
			fInfoPopup.setVisible(false);
			fInfoPopup.dispose();
		}
		fInfoPopup= null;
	}
	public void widgetSelected(SelectionEvent e) {
		reset();
	}
}
