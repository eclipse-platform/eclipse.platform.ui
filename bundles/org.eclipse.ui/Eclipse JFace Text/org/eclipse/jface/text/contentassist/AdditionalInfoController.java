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


import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.util.Assert;



/**
 * Displays the additional information available for a completion proposal.
 * 
 * @since 2.0
 */
class AdditionalInfoController extends AbstractInformationControlManager implements Runnable {
	
	/**
	 * Internal table selection listener.
	 */
	private class TableSelectionListener implements SelectionListener {
		
		/*
		 * @see SelectionListener#widgetSelected(SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			handleTableSelectionChanged();
		}
		
		/*
		 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};
	
		
	private Table fProposalTable;
	private Thread fThread;
	private boolean fIsReset= false;
	
	private Object fMutex= new Object();
	private Object fStartSignal;
	
	private SelectionListener fSelectionListener= new TableSelectionListener();
	private int fDelay;
	
	
	/**
	 * Creates a new additional information controller.
	 * 
	 * @param creator the information control creator to be used by this controller
	 * @param delay time in milliseconds after which additional info should be displayed
	 */
	AdditionalInfoController(IInformationControlCreator creator, int delay) {
		super(creator);
		fDelay= delay;
		setAnchor(ANCHOR_RIGHT);
		setFallbackAnchors(new Anchor[] { ANCHOR_LEFT, ANCHOR_BOTTOM, ANCHOR_RIGHT });
		setSizeConstraints(50, 10, true, false);
	}
	
	/*
	 * @see AbstractInformationControlManager#install(Control)
	 */
	public void install(Control control) {

		if (fProposalTable == control) {
			// already installed
			return;
		}
		
		super.install(control);
		
		Assert.isTrue(control instanceof Table);
		fProposalTable= (Table) control;
		fProposalTable.addSelectionListener(fSelectionListener);
		fThread= new Thread(this, JFaceTextMessages.getString("InfoPopup.info_delay_timer_name")); //$NON-NLS-1$
		
		fStartSignal= new Object();
		synchronized (fStartSignal) {
			fThread.start();
			try {
				// wait until thread is ready
				fStartSignal.wait();
			} catch (InterruptedException x) {
			}
		}
	}
	
	/*
	 * @see AbstractInformationControlManager#disposeInformationControl()
	 */	
	 public void disposeInformationControl() {
		
		if (fThread != null) {
			fThread.interrupt();
			fThread= null;
		}
		
		if (fProposalTable != null && !fProposalTable.isDisposed()) {
			fProposalTable.removeSelectionListener(fSelectionListener);
			fProposalTable= null;
		}
		
		super.disposeInformationControl();
	}
	
	/*
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (true) {
				
				synchronized (fMutex) {
					
					if (fStartSignal != null) {
						synchronized (fStartSignal) {
							fStartSignal.notifyAll();
							fStartSignal= null;
						}
					}
					
					// Wait for a selection event to occur.
					fMutex.wait();
					
					while (true) {
						fIsReset= false;
						// Delay before showing the popup.
						fMutex.wait(fDelay);
						if (!fIsReset)
							break;
					}
				}
				
				if (fProposalTable != null && !fProposalTable.isDisposed()) {
					fProposalTable.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!fIsReset)
								showInformation();
						}
					});
				}
				
			}
		} catch (InterruptedException e) {
		}
		
		fThread= null;
	}
	
	/**
	 *Handles a change of the line selected in the associated selector.
	 */
	public void handleTableSelectionChanged() {
		
		if (fProposalTable != null && !fProposalTable.isDisposed() && fProposalTable.isVisible()) {
			synchronized (fMutex) {
				fIsReset= true;
				fMutex.notifyAll();
			}
		}
	}
			
	/*
	 * @see AbstractInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		
		if (fProposalTable == null || fProposalTable.isDisposed())
			return;
			
		TableItem[] selection= fProposalTable.getSelection();
		if (selection != null && selection.length > 0) {
			
			TableItem item= selection[0];
			
			// compute information
			String information= null;
			Object d= item.getData();
			if (d instanceof ICompletionProposal) {
				ICompletionProposal p= (ICompletionProposal) d;
				information= p.getAdditionalProposalInfo();
			}
			
			// compute subject area
			setMargins(4, -1);
			Rectangle area= fProposalTable.getBounds();
			area.x= 0; // subject area is the whole subject control
			area.y= 0;
			
			// set information & subject area
			setInformation(information, area);
		}
	}
}


