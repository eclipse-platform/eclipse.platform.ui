/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.IActionDelegate;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;


public class AddFromHistoryAction implements IActionDelegate {
	
	private static final String BUNDLE_NAME= "org.eclipse.compare.internal.AddFromHistoryAction"; //$NON-NLS-1$

	private ISelection fSelection;
	
	public AddFromHistoryAction() {
	}
	
	public void selectionChanged(IAction a, ISelection s) {
		fSelection= s;
	}

	public void run(IAction action) {
			
		ResourceBundle bundle= ResourceBundle.getBundle(BUNDLE_NAME);
		String title= Utilities.getString(bundle, "title"); //$NON-NLS-1$
			
		Shell parentShell= CompareUIPlugin.getShell();
		AddFromHistoryDialog dialog= null;

		Object[] s= Utilities.getResources(fSelection);
		
		for (int i= 0; i < s.length; i++) {
			Object o= s[i];
			if (o instanceof IContainer) {
				IContainer container= (IContainer) o;
				
				ProgressMonitorDialog pmdialog= new ProgressMonitorDialog(parentShell);	
				IProgressMonitor pm= pmdialog.getProgressMonitor();
				IFile[] states= null;
				try {
					states= container.findDeletedMembersWithHistory(IContainer.DEPTH_INFINITE, pm);
				} catch (CoreException ex) {
					pm.done();
				}
		
				if (states == null || states.length <= 0) {
					String msg= Utilities.getString(bundle, "noLocalHistoryError"); //$NON-NLS-1$
					MessageDialog.openInformation(parentShell, title, msg);
					return;
				}
		
				if (dialog == null)
					dialog= new AddFromHistoryDialog(parentShell, bundle);
					
				if (dialog.select(container, states)) {		
							
					AddFromHistoryDialog.HistoryInput[] selected= dialog.getSelected();				

					if (selected != null && selected.length > 0) {	
						try {
							updateWorkspace(bundle, parentShell, selected);
	
						} catch (InterruptedException x) {
							// Do nothing. Operation has been canceled by user.
							
						} catch (InvocationTargetException x) {
							String reason= x.getTargetException().getMessage();
							MessageDialog.openError(parentShell, title, Utilities.getFormattedString(bundle, "replaceError", reason));	//$NON-NLS-1$
						}
					}
				}
			}
		}
	}
	
	private void createContainers(IResource resource) throws CoreException {
		IContainer container= resource.getParent();
		if (container instanceof IFolder) {
			IFolder parent= (IFolder) container;
			if (parent != null && !parent.exists()) {
				createContainers(parent);
				parent.create(false, true, null);
			}
		}
	}
	
	private void updateWorkspace(final ResourceBundle bundle, Shell shell,
					final AddFromHistoryDialog.HistoryInput[] selected)
									throws InvocationTargetException, InterruptedException {
		
		WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor pm) throws InvocationTargetException {
				try {
					String taskName= Utilities.getString(bundle, "taskName"); //$NON-NLS-1$
					pm.beginTask(taskName, selected.length);
					
					for (int i= 0; i < selected.length; i++) {
						IFile file= selected[i].fFile;
						IFileState fileState= selected[i].fFileState;
						createContainers(file);
						
						SubProgressMonitor subMonitor= new SubProgressMonitor(pm, 1);
						try {
							file.create(fileState.getContents(), false, subMonitor);
						} finally {
							subMonitor.done();
						}
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					pm.done();
				}
			}
		};
		
		ProgressMonitorDialog pmdialog= new ProgressMonitorDialog(shell);				
		pmdialog.run(false, true, operation);									
	}
}
