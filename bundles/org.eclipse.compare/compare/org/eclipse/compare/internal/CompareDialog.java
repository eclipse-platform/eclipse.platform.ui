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

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ICompareContainer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.services.IServiceLocator;


public class CompareDialog extends ResizableDialog implements IPropertyChangeListener, ICompareContainer {
		
	private CompareEditorInput fCompareEditorInput;
	private Button fCommitButton;
	private Label statusLabel;

	CompareDialog(Shell shell, CompareEditorInput input) {
		super(shell, null);
		
		Assert.isNotNull(input);
		fCompareEditorInput= input;
		fCompareEditorInput.addPropertyChangeListener(this);
		fCompareEditorInput.setContainer(this);
		setHelpContextId(ICompareContextIds.COMPARE_DIALOG);
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
		fCommitButton= createButton(parent, IDialogConstants.OK_ID, Utilities.getString("CompareDialog.commitAction.label"), true); //$NON-NLS-1$
		fCommitButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
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
		
		int rc= super.open();
		
		if (rc == OK && fCompareEditorInput.isSaveNeeded()) {
						
			WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor pm) throws CoreException {
					fCompareEditorInput.saveChanges(pm);
				}
			};
						
			Shell shell= getParentShell();
			ProgressMonitorDialog pmd= new ProgressMonitorDialog(shell);				
			try {
				operation.run(pmd.getProgressMonitor());				
				
			} catch (InterruptedException x) {
				// NeedWork
			} catch (OperationCanceledException x) {
				// NeedWork
			} catch (InvocationTargetException x) {
				String title= Utilities.getString("CompareDialog.saveErrorTitle"); //$NON-NLS-1$
				String msg= Utilities.getString("CompareDialog.saveErrorMessage"); //$NON-NLS-1$
				MessageDialog.openError(shell, title, msg + x.getTargetException().getMessage());
			}
		}
		
		return rc;
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

	public void setStatusMessage(String message) {
		if (statusLabel != null && !statusLabel.isDisposed()) {
			if (message == null) {
				statusLabel.setText(""); //$NON-NLS-1$
			} else {
				statusLabel.setText(message);
			}
		}
	}
}
