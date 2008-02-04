/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.internal.text.link.contentassist;


import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;



/**
 * Displays the additional information available for a completion proposal.
 *
 * @since 2.0
 */
class AdditionalInfoController2 extends AbstractInformationControlManager implements Runnable {

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
	}


	/** The proposal table */
	private Table fProposalTable;
	/** The thread controlling the delayed display of the additional info */
	private Thread fThread;
	/** Indicates whether the display delay has been reset */
	private boolean fIsReset= false;
	/** Object to synchronize display thread and table selection changes */
	private final Object fMutex= new Object();
	/** Thread access lock. */
	private final Object fThreadAccess= new Object();
	/** Object to synchronize initial display of additional info */
	private Object fStartSignal;
	/** The table selection listener */
	private SelectionListener fSelectionListener= new TableSelectionListener();
	/** The delay after which additional information is displayed */
	private int fDelay;


	/**
	 * Creates a new additional information controller.
	 *
	 * @param creator the information control creator to be used by this controller
	 * @param delay time in milliseconds after which additional info should be displayed
	 */
	AdditionalInfoController2(IInformationControlCreator creator, int delay) {
		super(creator);
		fDelay= delay;
		setAnchor(ANCHOR_RIGHT);
		setFallbackAnchors(new Anchor[] {ANCHOR_RIGHT, ANCHOR_LEFT, ANCHOR_BOTTOM });
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
		synchronized (fThreadAccess) {
	 		if (fThread != null)
	 			fThread.interrupt();
			fThread= new Thread(this, ContentAssistMessages.getString("InfoPopup.info_delay_timer_name")); //$NON-NLS-1$

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
	}

	/*
	 * @see AbstractInformationControlManager#disposeInformationControl()
	 */
	 public void disposeInformationControl() {

	 	synchronized (fThreadAccess) {
	 		if (fThread != null) {
	 			fThread.interrupt();
	 			fThread= null;
	 		}
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

		synchronized (fThreadAccess) {
			// only null fThread if it is us!
			if (Thread.currentThread() == fThread)
				fThread= null;
		}
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

			if (d instanceof ICompletionProposalExtension3)
				setCustomInformationControlCreator(((ICompletionProposalExtension3) d).getInformationControlCreator());
			else
				setCustomInformationControlCreator(null);

			// compute subject area
			setMargins(4, -1);
			Rectangle area= fProposalTable.getBounds();
			area.x= 0; // subject area is the whole subject control
			area.y= 0;

			// set information & subject area
			setInformation(information, area);
		}
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#computeSizeConstraints(Control, IInformationControl)
	 */
	protected Point computeSizeConstraints(Control subjectControl, IInformationControl informationControl) {
		// at least as big as the proposal table
		Point sizeConstraint= super.computeSizeConstraints(subjectControl, informationControl);
		Point size= subjectControl.getSize();
		if (sizeConstraint.x < size.x)
			sizeConstraint.x= size.x;
		if (sizeConstraint.y < size.y)
			sizeConstraint.y= size.y;
		return sizeConstraint;
	}
}


