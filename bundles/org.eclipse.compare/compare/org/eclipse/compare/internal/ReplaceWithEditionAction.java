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


public class ReplaceWithEditionAction implements IActionDelegate {
		
	private static final String BUNDLE_NAME= "org.eclipse.compare.internal.ReplaceWithEditionAction";

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
				
		ResourceBundle bundle= ResourceBundle.getBundle(BUNDLE_NAME);
		String title= Utilities.getString(bundle, "title");
		
		Shell parent= CompareUIPlugin.getShell();
		
		IFileState states[]= null;
		try {
			states= file.getHistory(null);
		} catch (CoreException ex) {		
			MessageDialog.openError(parent, title, ex.getMessage());
			return;
		}
		
		if (states != null && states.length > 0) {
			
			ITypedElement base= new ResourceNode(file);
		
			ITypedElement[] editions= new ITypedElement[states.length];
			for (int i= 0; i < states.length; i++)
				editions[i]= new HistoryItem(base, states[i]);

			EditionSelectionDialog d= new EditionSelectionDialog(parent, bundle);

			ITypedElement ti= d.selectEdition(base, editions, null);			
			if (ti instanceof IStreamContentAccessor) {
				try {
					InputStream is= ((IStreamContentAccessor)ti).getContents();
					file.setContents(is, false, true, null);
				} catch (CoreException ex) {
					MessageDialog.openError(parent, title, ex.getMessage());
				}
			}
		} else {
			String msg= Utilities.getString(bundle, "noLocalHistoryError");
			MessageDialog.openInformation(parent, title, msg);
		}
	}
}

