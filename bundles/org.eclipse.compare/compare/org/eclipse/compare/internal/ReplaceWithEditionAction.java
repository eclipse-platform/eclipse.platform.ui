/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.InputStream;
import java.util.ResourceBundle;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.IActionDelegate;


import org.eclipse.compare.*;


public class ReplaceWithEditionAction implements IActionDelegate {
		
	private static final String BUNDLE_NAME= "org.eclipse.compare.internal.ReplaceWithEditionAction"; //$NON-NLS-1$

	private ISelection fSelection;
	

	public void run(IAction action) {
			
		Object[] s= Utilities.toArray(fSelection);
		
		for (int i= 0; i < s.length; i++) {
			Object o= s[i];
			if (o instanceof IFile) {
				replaceFromHistory((IFile)o);
				continue;
			}
			if (o instanceof IAdaptable) {
				IAdaptable a= (IAdaptable) o;
				Object adapter= a.getAdapter(IResource.class);
				if (adapter instanceof IFile)
					replaceFromHistory((IFile)adapter);
				continue;
			}
		}
	}
	
	public void selectionChanged(IAction a, ISelection s) {
		fSelection= s;
	}

	void replaceFromHistory(final IFile file) {
				
		ResourceBundle bundle= ResourceBundle.getBundle(BUNDLE_NAME);
		String title= Utilities.getString(bundle, "title"); //$NON-NLS-1$
		
		Shell parentShell= CompareUIPlugin.getShell();
		
		IFileState states[]= null;
		try {
			states= file.getHistory(null);
		} catch (CoreException ex) {		
			MessageDialog.openError(parentShell, title, ex.getMessage());
			return;
		}
		
		if (states != null && states.length > 0) {
			
			ITypedElement base= new ResourceNode(file);
		
			ITypedElement[] editions= new ITypedElement[states.length];
			for (int i= 0; i < states.length; i++)
				editions[i]= new HistoryItem(base, states[i]);

			EditionSelectionDialog d= new EditionSelectionDialog(parentShell, bundle);

			final ITypedElement ti= d.selectEdition(base, editions, null);			
			if (ti instanceof IStreamContentAccessor) {
								
				WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
					public void execute(IProgressMonitor pm) throws InvocationTargetException {
						try {
							pm.beginTask("Replacing", IProgressMonitor.UNKNOWN);
							InputStream is= ((IStreamContentAccessor)ti).getContents();
							file.setContents(is, false, true, pm);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						} finally {
							pm.done();
						}
					}
				};
				
				try {
					ProgressMonitorDialog pmdialog= new ProgressMonitorDialog(parentShell);				
					pmdialog.run(false, true, operation);
												
				} catch (InterruptedException x) {
					// Do nothing. Operation has been canceled by user.
					
				} catch (InvocationTargetException x) {
					String reason= x.getTargetException().getMessage();
					MessageDialog.openError(parentShell, title, Utilities.getFormattedString(bundle, "replaceError", reason));	//$NON-NLS-1$
				}
			}
		} else {
			String msg= Utilities.getString(bundle, "noLocalHistoryError"); //$NON-NLS-1$
			MessageDialog.openInformation(parentShell, title, msg);
		}
	}
}
