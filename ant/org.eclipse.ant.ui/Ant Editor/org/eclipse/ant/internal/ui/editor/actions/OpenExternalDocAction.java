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
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.AntTargetNode;
import org.eclipse.ant.internal.ui.model.AntTaskNode;
import org.eclipse.jdt.internal.ui.JavaPlugin;
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
			URL url= getExternalLocation(node);
			if (url != null) {
				AntUtil.openBrowser(url.toString(), shell, getTitle());
			} 		
		} catch (MalformedURLException e) {
           AntUIPlugin.log(e);
        }
	}
	
	public URL getExternalLocation(AntElementNode node) throws MalformedURLException {
		URL baseLocation= getBaseLocation(node);
		if (baseLocation == null) {
			return null;
		}

		String urlString= baseLocation.toExternalForm();

		StringBuffer pathBuffer= new StringBuffer(urlString);
		if (!urlString.endsWith("/")) { //$NON-NLS-1$
			pathBuffer.append('/');
		}

		if (node instanceof AntProjectNode) {
			pathBuffer.append("using.html#projects"); //$NON-NLS-1$
		} else if (node instanceof AntTargetNode) {
			pathBuffer.append("using.html#targets"); //$NON-NLS-1$
		} else if (node instanceof AntTaskNode) {
			appendCoreTaskPath((AntTaskNode) node, pathBuffer);
		} 

		try {
			return new URL(pathBuffer.toString());
		} catch (MalformedURLException e) {
			JavaPlugin.log(e);
		}
		return null;
	}
	
	private void appendCoreTaskPath(AntTaskNode node, StringBuffer buffer) {
		buffer.append("CoreTasks"); //$NON-NLS-1$
		buffer.append('/');
		String typePath= node.getTask().getTaskName();
		buffer.append(typePath);
		buffer.append(".html"); //$NON-NLS-1$	
	}

	/**
	 * @param node
	 * @return
	 */
	private static URL getBaseLocation(AntElementNode node) throws MalformedURLException {
		// TODO allow user to set location
		return new URL("http://ant.apache.org/manual/"); //$NON-NLS-1$
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