/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;


public class CompareDialog extends ResizableDialog implements IPropertyChangeListener {
		
	private CompareEditorInput fCompareEditorInput;
	private Button fCommitButton;


	CompareDialog(Shell shell, CompareEditorInput input) {
		super(shell, null);
		
		Assert.isNotNull(input);
		fCompareEditorInput= input;
		fCompareEditorInput.addPropertyChangeListener(this);
	}
	
	public boolean close() {
		if (super.close()) {
			if (fCompareEditorInput != null)
				fCompareEditorInput.addPropertyChangeListener(this);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fCommitButton= createButton(parent, IDialogConstants.OK_ID, Utilities.getString("CompareDialog.commitAction.label"), true); //$NON-NLS-1$
		fCommitButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (fCommitButton != null && fCompareEditorInput != null)
			fCommitButton.setEnabled(fCompareEditorInput.isSaveNeeded());
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
						
		Control c= fCompareEditorInput.createContents(parent);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Shell shell= c.getShell();
		shell.setText(fCompareEditorInput.getTitle());
		shell.setImage(fCompareEditorInput.getTitleImage());

		return c;
	}
		
	/* (non-Javadoc)
	 * Method declared on Window.
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
			} catch (OperationCanceledException x) {
			} catch (InvocationTargetException x) {
				String title= Utilities.getString("CompareDialog.saveErrorTitle"); //$NON-NLS-1$
				String msg= Utilities.getString("CompareDialog.saveErrorMessage"); //$NON-NLS-1$
				MessageDialog.openError(shell, title, msg + x.getTargetException().getMessage());
			}
		}
		
		return rc;
	}
}
