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
import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.AntTargetNode;
import org.eclipse.ant.internal.ui.model.AntTaskNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This action opens the selected tasks manual page in an external 
 * browser. 
 */
public class OpenExternalDocAction extends Action implements IEditorActionDelegate {
		
	private AntEditor fEditor;
	
	public OpenExternalDocAction() {
	}
	
	public OpenExternalDocAction(AntEditor antEditor) {
		fEditor= antEditor;
		setActionDefinitionId("org.eclipse.ant.ui.openExternalDoc"); //$NON-NLS-1$
		antEditor.getSite().getKeyBindingService().registerAction(this);

		setText(AntEditorActionMessages.getString("OpenExternalDocAction.1")); //$NON-NLS-1$
		setDescription(AntEditorActionMessages.getString("OpenExternalDocAction.2")); //$NON-NLS-1$
		setToolTipText(AntEditorActionMessages.getString("OpenExternalDocAction.2")); //$NON-NLS-1$
	}
	
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
		URL baseLocation= getBaseLocation();
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
			appendTaskPath((AntTaskNode) node, pathBuffer);
		} 

		try {
			return new URL(pathBuffer.toString());
		} catch (MalformedURLException e) {
			AntUIPlugin.log(e);
		}
		return null;
	}

	private void appendTaskPath(AntTaskNode node, StringBuffer buffer) {
	    String taskName= node.getTask().getTaskName();
	    String taskPart= null;
	    if (taskName.equalsIgnoreCase("path")) {  //$NON-NLS-1$
	        buffer.append("using.html#path"); //$NON-NLS-1$
	        return;
	    } 
	    taskPart= getTaskTypePart(node);
        if (taskPart == null) {
            return;
        }
		buffer.append(taskPart);
		buffer.append('/');
		buffer.append(taskName);
		buffer.append(".html"); //$NON-NLS-1$	
	}

	private URL getBaseLocation() throws MalformedURLException {
		// TODO allow user to set location
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77386
		return new URL("http://ant.apache.org/manual/"); //$NON-NLS-1$
	}
	
	private String getTitle() {
		return AntEditorActionMessages.getString("OpenExternalDocAction.0"); //$NON-NLS-1$
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        fEditor= (AntEditor) targetEditor;
        if (fEditor != null) {
        	fEditor.getSite().getKeyBindingService().registerAction(this);
        }
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
    
    
    private String getTaskTypePart(AntTaskNode node) {
		AntProjectNode projectNode= node.getProjectNode();
    	if (projectNode != null) {
    		Project antProject= projectNode.getProject();
    		AntTypeDefinition definition= ComponentHelper.getComponentHelper(antProject).getDefinition(node.getTask().getTaskName());
    		if (definition == null) {
    			return null;
    		}
    		String className= definition.getClassName();
    		if (className.indexOf("taskdef") != -1) { //$NON-NLS-1$
    		    if (className.indexOf("optional") != -1) { //$NON-NLS-1$
    		        return "OptionalTasks"; //$NON-NLS-1$
    		    } 
    		    return "CoreTasks"; //$NON-NLS-1$
    		} else if (className.indexOf("optional") != -1) { //$NON-NLS-1$
    		    return "OptionalTypes"; //$NON-NLS-1$
    		} else {
    		    return "CoreTypes"; //$NON-NLS-1$
    		}
    	}
    	
        return null;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		run(null);
	}
}