/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.ProgressMonitorPart;

import org.eclipse.search.internal.ui.SearchMessages;


public abstract class ExtendedDialogWindow extends Dialog  implements IRunnableContext {
	
	private Control fContents;
	private Button fCancelButton;
	private Set fActionButtons;
	// The number of long running operation executed from the dialog.	
	private long fActiveRunningOperations;

	// The progress monitor
	private ProgressMonitorPart fProgressMonitorPart;
	private MessageDialog fWindowClosingDialog;
	private static final String FOCUS_CONTROL= "focusControl"; //$NON-NLS-1$
	private Cursor fWaitCursor;
	private Cursor fArrowCursor;


	public ExtendedDialogWindow(Shell shell) {
		super(shell);
		fActionButtons= new HashSet();
	}
	
	//---- Hooks to reimplement in subclasses -----------------------------------
	
	/**
	 * Hook called when the user has pressed the button to perform
	 * the dialog's action. If the method returns <code>false</code>
	 * the dialog stays open. Otherwise the dialog is going to be closed.
	 */
	protected boolean performAction(int buttonId) {
		return true;
	}
	 
	/**
	 * Hook called when the user has pressed the button to cancel
	 * the dialog. If the method returns <code>false</code> the dialog 
	 * stays open. Otherwise the dialog is going to be closed.
	 */
	protected boolean performCancel() {
		return true;
	}
	 
	//---- UI creation ----------------------------------------------------------

	/**
	 * Create the page area.
	 */
	protected abstract Control createPageArea(Composite parent); 
	 
	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses may override.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fCancelButton= createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	protected Button createActionButton(Composite parent, int id, String label,
			boolean defaultButton) {
		Button actionButton= createButton(parent, id, label, defaultButton);
		fActionButtons.add(actionButton);
		return actionButton;
	}
	 
	/**
	 * Creates the layout of the extended dialog window.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite result= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fContents= createPageArea(result);
		fContents.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Insert a progress monitor
		GridLayout pmlayout= new GridLayout();
		pmlayout.numColumns= 1;
		fProgressMonitorPart= new ProgressMonitorPart(result, pmlayout, SWT.DEFAULT);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		fProgressMonitorPart.setLayoutData(gd);
		fProgressMonitorPart.setVisible(false);


		
		Label separator= new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);
		
		applyDialogFont(result);
		return result;
	}

	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case IDialogConstants.CANCEL_ID:
				if (fActiveRunningOperations == 0)
					close();
				break;	
			default:
				if (performAction(buttonId))
					close();
		}
	}
	
	//---- Setters and Getters --------------------------------------------------
	
	/**
	 * Set the enable state of the perform action button.
	 */
	public void setPerformActionEnabled(boolean state) {
		for (Iterator buttons = fActionButtons.iterator(); buttons.hasNext(); ) {
			Button element = (Button) buttons.next();
			element.setEnabled(state);
		}
	} 

	//---- Operation stuff ------------------------------------------------------
	
	/**
	 * Runs the given <code>IRunnableWithProgress</code> with the progress monitor for this
	 * wizard dialog.  
	 * @param fork if true, it is run in a separate thread
	 * @param cancelable specifies whether to enable the cancel button or not
	 * @param runnable the runnable to run
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		// The operation can only be canceled if it is executed in a separate thread.
		// Otherwise the UI is blocked anyway.
		Object state= null;
		try {
			fActiveRunningOperations++;
			state= aboutToStart(fork && cancelable);
			ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
		} finally {
			if (state != null)
				stopped(state);
			fActiveRunningOperations--;
		}
	}

	/**
	 * Returns the progress monitor. If the wizard dialog doesn't
	 * have a progress monitor <code>null</code> is returned.
	 */
	protected IProgressMonitor getProgressMonitor() {
		return fProgressMonitorPart;
	}
	
	/**
	 * About to start a long running operation tiggered through
	 * the wizard. So show the progress monitor and disable
	 * the wizard.
	 * @return The saved UI state.
	 */
	protected synchronized Object aboutToStart(boolean enableCancelButton) {
		HashMap savedState= null;
		Shell shell= getShell();
		if (shell != null) {
			Display d= shell.getDisplay();
			
			// Save focus control
			Control focusControl= d.getFocusControl();
			if (focusControl != null && focusControl.getShell() != shell)
				focusControl= null;
				
			// Set the busy cursor to all shells.
			fWaitCursor= new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(d, fWaitCursor);
					
			// Set the arrow cursor to the cancel component.
			fArrowCursor= new Cursor(d, SWT.CURSOR_ARROW);
			fCancelButton.setCursor(fArrowCursor);
	
			// Deactivate shell
			savedState= saveUIState(enableCancelButton);
			if (focusControl != null)
				savedState.put(FOCUS_CONTROL, focusControl);
				
			// Attach the progress monitor part to the cancel button
			fProgressMonitorPart.attachToCancelComponent(fCancelButton);
			fProgressMonitorPart.setVisible(true);
		}
		
		return savedState;
	}
	
	/**
	 * A long running operation triggered through the wizard
	 * was stopped either by user input or by normal end.
	 * @param savedState The saveState returned by <code>aboutToStart</code>.
	 * @see #aboutToStart(boolean)
	 */
	protected synchronized void stopped(Object savedState) {
		Assert.isTrue( savedState instanceof HashMap);
		Shell shell= getShell();
		if (shell != null) {
	
			fProgressMonitorPart.setVisible(false);	
			fProgressMonitorPart.removeFromCancelComponent(fCancelButton);
					
			HashMap state= (HashMap)savedState;
			restoreUIState(state);
	
			setDisplayCursor(shell.getDisplay(), null);	
			fCancelButton.setCursor(null);
			fWaitCursor.dispose();
			fWaitCursor= null;
			fArrowCursor.dispose();
			fArrowCursor= null;
			Control focusControl= (Control)state.get(FOCUS_CONTROL);
			if (focusControl != null && ! focusControl.isDisposed())
				focusControl.setFocus();
		}
	}
	
	private void setDisplayCursor(Display d, Cursor c) {
		Shell[] shells= d.getShells();
		for (int i= 0; i < shells.length; i++)
			shells[i].setCursor(c);
	}	

	//---- UI state save and restoring ---------------------------------------------
	
	private void restoreUIState(HashMap state) {
		restoreEnableState(fCancelButton, state); //$NON-NLS-1$
		for (Iterator actionButtons = fActionButtons.iterator(); actionButtons.hasNext(); ) {
			Button button = (Button) actionButtons.next();
			restoreEnableState(button, state);
		}
		ControlEnableState pageState= (ControlEnableState)state.get("tabForm"); //$NON-NLS-1$
		pageState.restore();
	}
	
	/**
	 * Restores the enable state of the given control.
	 * @private
	 */
	protected void restoreEnableState(Control w, HashMap h) {
		if (!w.isDisposed()) {
			Boolean b= (Boolean)h.get(w);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}
	
	private HashMap saveUIState(boolean keepCancelEnabled) {
		HashMap savedState= new HashMap(10);
		saveEnableStateAndSet(fCancelButton, savedState, keepCancelEnabled); //$NON-NLS-1$
		for (Iterator actionButtons = fActionButtons.iterator(); actionButtons.hasNext(); ) {
			Button button = (Button) actionButtons.next();
			saveEnableStateAndSet(button, savedState, false);
		}
		savedState.put("tabForm", ControlEnableState.disable(fContents)); //$NON-NLS-1$
		
		return savedState;
	}
	
	private void saveEnableStateAndSet(Control w, HashMap h, boolean enabled) {
		if (!w.isDisposed()) {
			h.put(w, new Boolean(w.isEnabled()));
			w.setEnabled(enabled);
		}	
	}	

	protected void handleShellCloseEvent() {
		if (okToClose())
			super.handleShellCloseEvent();
	}

	/**
	 * The dialog is going to be closed. Check if there is a running
	 * operation. If so, post an alert saying that the wizard can't
	 * be closed.
	 */
	public boolean okToClose() {
		if (fActiveRunningOperations > 0) {
			synchronized (this) {
				fWindowClosingDialog= createClosingDialog();
			}	
			fWindowClosingDialog.open();
			synchronized (this) {
				fWindowClosingDialog= null;
			}
			return false;
		}
		return true;
	}
	
	private MessageDialog createClosingDialog() {
		MessageDialog result= 
			new MessageDialog(
				getShell(),
				SearchMessages.getString("SearchDialogClosingDialog.title"),  //$NON-NLS-1$
				null, 
				SearchMessages.getString("SearchDialogClosingDialog.message"),  //$NON-NLS-1$
				MessageDialog.QUESTION, 
				new String[] {IDialogConstants.OK_LABEL}, 
				0); 
		return result;		
	}

	/**
	 * Returns the cancel component that is to be used to cancel 
	 * a long running operation.
	 */
	protected Control getCancelComponent() {
		return fCancelButton;
	}	
}
