/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.jface.util.IPropertyChangeListener;


public class CompareDialog extends Dialog implements IPropertyChangeListener {
	
	private static final String COMMIT_LABEL= "Commit";
	
	private CompareEditorInput fCompareEditorInput;
	private Button fCommitButton;


	CompareDialog(Shell shell, CompareEditorInput input) {
		super(shell);
		setShellStyle(SWT.CLOSE | SWT.APPLICATION_MODAL | SWT.RESIZE);
		
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
		fCommitButton= createButton(parent, IDialogConstants.OK_ID, COMMIT_LABEL, true);
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
	protected Point getInitialSize() {
		Point size= new Point(0, 0);
		Shell shell= getParentShell();
		if (shell != null) {
			Point parentSize= shell.getSize();
			size.x= parentSize.x-100;
			size.y= parentSize.y-100;
		}
		if (size.x < 800)
			size.x= 800;
		if (size.y < 600)
			size.y= 600;
		return size;
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	public int open() {
		
		int rc= super.open();
		
		if (rc == OK && fCompareEditorInput.isSaveNeeded()) {
						
			WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
				public void execute(IProgressMonitor pm) throws CoreException {
					fCompareEditorInput.save(pm);
				}
			};
						
			Shell shell= getParentShell();
			ProgressMonitorDialog pmd= new ProgressMonitorDialog(shell);				
			try {
				operation.run(pmd.getProgressMonitor());				
				
			} catch (InterruptedException x) {
			} catch (OperationCanceledException x) {
			} catch (InvocationTargetException x) {
				//String title= getResourceString("Error.save.title");
				//String msg= getResourceString("Error.save.message");
				String title= "Save Error";
				String msg= "Can't save ";
				MessageDialog.openError(shell, title, msg + x.getTargetException().getMessage());
			}
		}
		
		return rc;
	}
}
