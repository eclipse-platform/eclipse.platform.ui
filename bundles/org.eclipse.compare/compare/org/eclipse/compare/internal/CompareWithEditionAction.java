/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.compare.*;


public class CompareWithEditionAction implements IActionDelegate {
		
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

	void replaceFromHistory(IFile file) {
				
		Shell parent= CompareUIPlugin.getShell();

		IFileState states[]= null;
		try {
			states= file.getHistory(null);
		} catch (CoreException ex) {		
			MessageDialog.openError(parent, Utilities.getString("CompareWithEditionDialog.action.label"), ex.getMessage()); //$NON-NLS-1$
			return;
		}
		
		if (states != null && states.length > 0) {

			ITypedElement base= new ResourceNode(file);
		
			ITypedElement[] editions= new ITypedElement[states.length];
			for (int i= 0; i < states.length; i++)
				editions[i]= new HistoryItem(base, states[i]);

			ResourceBundle bundle= ResourceBundle.getBundle("org.eclipse.compare.internal.CompareWithEditionAction"); //$NON-NLS-1$
			CompareWithEditionDialog d= new CompareWithEditionDialog(parent, bundle);

			d.selectEdition(base, editions, null);			
		} else
			MessageDialog.openInformation(parent,
				Utilities.getString("CompareWithEditionDialog.action.label"), //$NON-NLS-1$
				Utilities.getString("CompareWithEditionDialog.noEditionMessage")); //$NON-NLS-1$
	}
}

