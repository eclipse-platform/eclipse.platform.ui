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
package org.eclipse.ant.internal.ui.editor.actions;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This action opens the selected tasks manual page in an external 
 * browser. 
 */
public class OpenExternalDocAction implements IEditorActionDelegate {
		
	private AntEditor fEditor;
	
    private Shell getShell() {
       return fEditor.getEditorSite().getShell();
    }
    
	private void doAction(AntElementNode node) {
		Shell shell= getShell();
		try {
			URL baseURL= new URL("http://ant.apache.org/manual/"); //$NON-NLS-1$
			//TODO compose the URL from the node selected
			URL url= baseURL;
			if (url != null) {
				AntUtil.openBrowser(url.toString(), shell, getTitle());
			} 		
		} catch (MalformedURLException e) {
           AntUIPlugin.log(e);
        }
	}
	
	//TODO this will be used once we are attempting to correctly resolve a URL based on the selection
	private static void showMessage(final Shell shell, final String message, final boolean isError) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (isError) {
					MessageDialog.openError(shell, getTitle(), message); //$NON-NLS-1$
				} else {
					MessageDialog.openInformation(shell, getTitle(), message); //$NON-NLS-1$
				}
			}
		});
	}
	
	private static String getTitle() {
		return AntEditorActionMessages.getString("OpenExternalDocAction.0"); //$NON-NLS-1$
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        fEditor= (AntEditor) targetEditor;
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        ISelection selection= fEditor.getSelectionProvider().getSelection();
		AntElementNode node= null;
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)selection;
			int textOffset= textSelection.getOffset();
			AntModel model= fEditor.getAntModel();
			if (model != null) {
				node= model.getNode(textOffset, false);
			}
			if (node != null) {
				doAction(node);
			}
		}
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {        
    }	
}