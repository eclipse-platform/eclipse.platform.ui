/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.compare.*;


public class EditionAction implements IActionDelegate {

	private ISelection fSelection;
	private String fBundleName;
	private boolean fReplaceMode;
	
	EditionAction(boolean replaceMode, String bundleName) {
		fReplaceMode= replaceMode;
		fBundleName= bundleName;
	}

	public void selectionChanged(IAction a, ISelection s) {
		fSelection= s;
	}

	public void run(IAction action) {
			
		Object[] s= Utilities.toArray(fSelection);
		
		for (int i= 0; i < s.length; i++) {
			Object o= s[i];
			if (o instanceof IFile) {
				doFromHistory((IFile)o);
				continue;
			}
			if (o instanceof IAdaptable) {
				IAdaptable a= (IAdaptable) o;
				Object adapter= a.getAdapter(IResource.class);
				if (adapter instanceof IFile) {
					doFromHistory((IFile)adapter);
				}
				continue;
			}
		}
	}

	private void doFromHistory(final IFile file) {
						
		final ResourceBundle bundle= ResourceBundle.getBundle(fBundleName);
		String title= Utilities.getString(bundle, "title"); //$NON-NLS-1$
		
		//IDocument doc= getDocument(file);
		//System.out.println("doc: " + doc);
	
		Shell parentShell= CompareUIPlugin.getShell();
		
		IFileState states[]= null;
		try {
			states= file.getHistory(null);
		} catch (CoreException ex) {		
			MessageDialog.openError(parentShell, title, ex.getMessage());
			return;
		}
		
		if (states == null || states.length <= 0) {
			String msg= Utilities.getString(bundle, "noLocalHistoryError"); //$NON-NLS-1$
			MessageDialog.openInformation(parentShell, title, msg);
			return;
		}
		
		ITypedElement base= new ResourceNode(file);
	
		ITypedElement[] editions= new ITypedElement[states.length+1];
		editions[0]= base;
		for (int i= 0; i < states.length; i++)
			editions[i+1]= new HistoryItem(base, states[i]);

		EditionSelectionDialog d= new EditionSelectionDialog(parentShell, bundle);
		d.setHideIdenticalEntries(false);
		
		if (fReplaceMode) {
			final ITypedElement ti= d.selectEdition(base, editions, null);			
			if (ti instanceof IStreamContentAccessor) {
								
				WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
					public void execute(IProgressMonitor pm) throws InvocationTargetException {
						try {
							String taskName= Utilities.getString(bundle, "taskName"); //$NON-NLS-1$
							pm.beginTask(taskName, IProgressMonitor.UNKNOWN);
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
			d.setCompareMode(true);

			d.selectEdition(base, editions, null);			
		}
	}
	
	private IDocument getDocument(IFile file) {
		IWorkbench wb= PlatformUI.getWorkbench();
		if (wb == null)
			return null;
		IWorkbenchWindow[] ws= wb.getWorkbenchWindows();
		if (ws == null)
			return null;
			
		FileEditorInput test= new FileEditorInput(file);
		
		for (int i= 0; i < ws.length; i++) {
			IWorkbenchWindow w= ws[i];
			IWorkbenchPage[] wps= w.getPages();
			if (wps != null) {
				for (int j= 0; j < wps.length; j++) {
					IWorkbenchPage wp= wps[j];
					IEditorPart[] eps= wp.getEditors();
					if (eps != null) {
						for (int k= 0; k < eps.length; k++) {
							IEditorPart ep= eps[k];
							IEditorInput ei= ep.getEditorInput();
							if (test.equals(ei)) {
								if (ep instanceof ITextEditor) {
									ITextEditor te= (ITextEditor) ep;
									IDocumentProvider dp= te.getDocumentProvider();
									if (dp != null) {
										IDocument doc= dp.getDocument(ei);
										if (doc != null)
											return doc;
									}
									
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
}

