package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


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
	
	/**
	 * Information control closer that closes the information control only
	 * when it's subject control is disposed.
	 */
	private class InfoControlCloser implements IInformationControlCloser {
		
		/** The subject control */
		private Control fSubjectControl;
		
		/*
		 * @see IInformationControlCloser#setSubjectControl(Control)
		 */
		public void setSubjectControl(Control control) {
			fSubjectControl= control;
		}

		/*
		 * @see IInformationControlCloser#setInformationControl(IInformationControl)
		 */
		public void setInformationControl(IInformationControl control) {
		}		
		
		/*
		 * @see IInformationControlCloser#start(Rectangle)
		 */
		public void start(Rectangle informationArea) {
			fSubjectControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					disposeInformationControl();
				}
			});
		}
		
		/*
		 * @see IInformationControlCloser#stop()
		 */
		public void stop() {
		}
	};
	
	private Table fProposalTable;
	private Thread fThread;
	private boolean fIsReset= false;
	private Object fMutex= new Object();
	private SelectionListener fSelectionListener= new TableSelectionListener();
	private int fDelay;
	
	AdditionalInfoController(IInformationControlCreator creator, int delay) {
		super(creator);
		fDelay= delay;
		setCloser(new InfoControlCloser());
		setAnker(ANKER_RIGHT);
		setFallbackAnkers(new Anker[] { ANKER_LEFT, ANKER_BOTTOM, ANKER_RIGHT });
		setSizeConstraints(50, 10, true, false);
	}
	
	/*
	 * @see AbstractInformationControlManager#install(Control)
	 */
	public void install(Control control) {
		super.install(control);
		
		Assert.isTrue(control instanceof Table);
		fProposalTable= (Table) control;
		fProposalTable.addSelectionListener(fSelectionListener);
		fThread= new Thread(this, JFaceTextMessages.getString("InfoPopup.info_delay_timer_name")); //$NON-NLS-1$
		fThread.start();
	}
	
	/*
	 * @see AbstractInformationControlManager#disposeInformationControl()
	 */	
	 public void disposeInformationControl() {
		
		if (fThread != null) {
			fThread.interrupt();
			fThread= null;
		}
		
		if (fProposalTable != null) {
			fProposalTable.removeSelectionListener(fSelectionListener);
			fProposalTable= null;
		}
		
		super.disposeInformationControl();
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
						fMutex.wait(fDelay);
						if (!fIsReset)
							break;
					}
				}
				
				if (fProposalTable != null && !fProposalTable.isDisposed()) {
					fProposalTable.getDisplay().syncExec(new Runnable() {
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
	
	public void handleTableSelectionChanged() {
		synchronized (fMutex) {
			fIsReset= true;
			fMutex.notifyAll();
		}
	}
			
	/*
	 * @see AbstractInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		
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
			setMargins(0, 0);
			Rectangle area= fProposalTable.getBounds();
			
			// set information & subject area
			setInformation(information, area);
		}
	}
}


