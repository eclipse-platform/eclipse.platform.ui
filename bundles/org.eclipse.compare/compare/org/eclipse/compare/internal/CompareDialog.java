/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;

/**
 * This is a dialog that can host a {@link CompareEditorInput}. Clients should instantiate
 * the dialog and then call {@link #prepareAndOpen()} to prepare the compare editor input
 * and open the dialog if there is a compare result to show.
 * <p>
 * This class can be used as is or can be subclassed.
 * 
 * @since 3.3
 */
public class CompareDialog extends TrayDialog implements IPropertyChangeListener, ICompareContainer {
	
	/**
	 * Constant returned from {@link #prepareAndOpen()} if the compare result
	 * was not OK.
	 */
	public static final int NO_COMPARE_RESULT = 100;
	
	private final CompareEditorInput fCompareEditorInput;
	private Button fCommitButton;
	private Label statusLabel;
	boolean hasSettings = true;
	
	/**
	 * Create a dialog to host the given input.
	 * @param shell a shell
	 * @param input the dialog input
	 */
	public CompareDialog(Shell shell, CompareEditorInput input) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		Assert.isNotNull(input);
		fCompareEditorInput= input;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.internal.ResizableDialog#close()
	 */
	public boolean close() {
		if (super.close()) {
			if (fCompareEditorInput != null)
				fCompareEditorInput.removePropertyChangeListener(this);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if (isInputEditable()) {
			fCommitButton= createButton(parent, IDialogConstants.OK_ID, CompareMessages.CompareDialog_commit_button, true);
			fCommitButton.setEnabled(false);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		} else {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
		}
	}

	/**
	 * Return whether the compare editor input of this dialog is editable.
	 * By default, the input is editable if the compare configuration
	 * indicates that either the left or right sides are editable.
	 * Subclasses may override.
	 * @return whether the compare editor input of this dialog is editable
	 * @see CompareConfiguration#isLeftEditable()
	 * @see CompareConfiguration#isRightEditable()
	 */
	protected boolean isInputEditable() {
		return fCompareEditorInput.getCompareConfiguration().isLeftEditable() 
			|| fCompareEditorInput.getCompareConfiguration().isRightEditable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(CompareEditorInput.DIRTY_STATE)) {
			if (fCommitButton != null && fCompareEditorInput != null)
				fCommitButton.setEnabled(fCompareEditorInput.isSaveNeeded());
		} else if (event.getProperty().equals(CompareEditorInput.PROP_TITLE)) {
			getShell().setText(fCompareEditorInput.getTitle());
		} else if (event.getProperty().equals(CompareEditorInput.PROP_TITLE_IMAGE)) {
			getShell().setImage(fCompareEditorInput.getTitleImage());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent2) {
						
		Composite parent= (Composite) super.createDialogArea(parent2);

		Control c= fCompareEditorInput.createContents(parent);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		IPreferenceStore store= fCompareEditorInput.getCompareConfiguration().getPreferenceStore();
		if (store != null) {
			if (store.getBoolean(ComparePreferencePage.SHOW_MORE_INFO)) {
				statusLabel = new Label(parent, SWT.NONE);
				statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
		}
		
		Shell shell= c.getShell();
		shell.setText(fCompareEditorInput.getTitle());
		shell.setImage(fCompareEditorInput.getTitleImage());
		applyDialogFont(parent);
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		// Before opening, set the container of the input and listen
		// for changes to the input
		fCompareEditorInput.addPropertyChangeListener(this);
		fCompareEditorInput.setContainer(this);
		return super.open();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK && fCompareEditorInput.isSaveNeeded()) {
			if (!saveChanges())
				return;
		}
		super.buttonPressed(buttonId);
	}

	private boolean saveChanges() {
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						fCompareEditorInput.saveChanges(monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			
			});
			return true;
		} catch (InterruptedException x) {
			// Ignore
		} catch (OperationCanceledException x) {
			// Ignore
		} catch (InvocationTargetException x) {
			ErrorDialog.openError(getParentShell(), CompareMessages.CompareDialog_error_title, null, 
				new Status(IStatus.ERROR, CompareUIPlugin.PLUGIN_ID, 0, 
					NLS.bind(CompareMessages.CompareDialog_error_message, x.getTargetException().getMessage()), x.getTargetException()));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void addCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		input.addCompareInputChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#getActionBars()
	 */
	public IActionBars getActionBars() {
		// No action bas available
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#getServiceLocator()
	 */
	public IServiceLocator getServiceLocator() {
		// No service locator available
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(MenuManager menu,
			ISelectionProvider selectionProvider) {
		// Nothing to register
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void removeCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		input.removeCompareInputChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#setStatusMessage(java.lang.String)
	 */
	public void setStatusMessage(String message) {
		if (statusLabel != null && !statusLabel.isDisposed()) {
			if (message == null) {
				statusLabel.setText(""); //$NON-NLS-1$
			} else {
				statusLabel.setText(message);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings compareSettings = CompareUIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		IDialogSettings dialogSettings = compareSettings.getSection(sectionName);
		if (dialogSettings == null) {
			hasSettings = false;
			dialogSettings = compareSettings.addNewSection(sectionName);
		}
		return dialogSettings;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.internal.ResizableDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (getHelpContextId() != null)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, getHelpContextId());
	}

	/**
	 * Return the help content id for this dialog or <code>null</code>.
	 * By default, a generic help content id is returned. Subclasses may
	 * override.
	 * @return the help content id for this dialog or <code>null</code>
	 */
	public String getHelpContextId() {
		return ICompareContextIds.COMPARE_DIALOG;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point initialSize = super.getInitialSize();
		if (hasSettings) {
			return initialSize;
		}
		return getDefaultSize();
	}

	/**
	 * If we don't have settings we need to come up with a reasonable default
	 * since we can't depend on the compare editor input layout returning a 
	 * good default size.
	 * @return the default size of the dialog
	 */
	protected Point getDefaultSize() {
		int width= 0;
		int height= 0;
		Shell shell= getParentShell();
		if (shell != null) {
			Point parentSize= shell.getSize();
			width= parentSize.x-100;
			height= parentSize.y-100;
		}
		if (width < 700)
			width= 700;
		if (height < 500)
			height= 500;
		return new Point(width, height);
	}
	
	/**
	 * Initialize the compare editor input of this dialog by invoking the
	 * {@link CompareEditorInput#run(IProgressMonitor)} using the provided
	 * context. If no context is provided, a default context (most likely the
	 * progress service) will be used.
	 * @param context a runnable context or <code>null</code> if the default context is desired.
	 * @return whether the compare editor input has a valid compare result 
	 * 		(see {@link CompareEditorInput#getCompareResult()})
	 */
	protected boolean prepareInput(IRunnableContext context) {
		return CompareUIPlugin.getDefault().compareResultOK(fCompareEditorInput, context);
	}
	
	/**
	 * Prepare the compare editor input of this dialog and then open the dialog
	 * if the compare result was OK. If the compare result was not OK, then
	 * {@link #NO_COMPARE_RESULT} is returned.
	 * @return the result returned from the {@link #open()} method or
	 * {@link #NO_COMPARE_RESULT} if there was no compare result and the 
	 * dialog was not opened.
	 */
	public int prepareAndOpen() {
		if (prepareInput(null)) {
			return open();
		}
		return NO_COMPARE_RESULT;
	}

	/**
	 * Return the compare editor input for this dialog.
	 * @return the compare editor input for this dialog
	 */
	protected final CompareEditorInput getInput() {
		return fCompareEditorInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		dialog.run(fork, cancelable, runnable);
	}

}
