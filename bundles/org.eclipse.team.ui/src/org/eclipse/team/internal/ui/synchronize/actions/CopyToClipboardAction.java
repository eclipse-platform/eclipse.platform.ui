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
package org.eclipse.team.internal.ui.synchronize.actions;

import java.util.*;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Based on org.eclipse.ui.views.navigator.CopyAction with the additional support for
 * copying any non-resource object in the selection and putting the toString() as
 * a text transfer.
 * 
 * @since 3.1
 */
class CopyToClipboardAction extends SelectionListenerAction {
    
    private static final String EOL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    
    private final static String ID= TeamUIPlugin.PLUGIN_ID + ".synchronize.action.copy";  //$NON-NLS-1$
    
    private final Shell fShell;
    private final Clipboard fClipboard;

    protected CopyToClipboardAction(Shell shell) {
        super(Policy.bind("CopyToClipboardAction.1")); //$NON-NLS-1$
        Assert.isNotNull(shell);
        fShell= shell;
        fClipboard= new Clipboard(shell.getDisplay());
        setToolTipText(Policy.bind("CopyToClipboardAction.2")); //$NON-NLS-1$
        setId(ID);
    }
    
    public void run() {
		copyResources(getSelectedResources(), getTextualClipboardContents());
	}
    
    /*
     * Return a text representation of all selected elements.
     * Use the name from the tree node so what is copied
     * matches what appears in the tree.
     */
    private String getTextualClipboardContents() {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		for (Iterator it = getStructuredSelection().iterator(); it.hasNext();) {
			Object element = it.next();
			if (element instanceof ITypedElement) {
				if (i > 0)
					buf.append(EOL);
				buf.append(((ITypedElement)element).getName());
				i++;
			}
		}
		return buf.toString();
	}

	private void copyResources(List selectedResources, String text) {
		IResource[] resources = (IResource[]) selectedResources.toArray(new IResource[selectedResources.size()]);
		// Get the file names and a string representation
		final int length = resources.length;
		int actualLength = 0;
		String[] fileNames = new String[length];
		for (int i = 0; i < length; i++) {
			final IPath location = resources[i].getLocation();
			// location may be null. See bug 29491.
			if (location != null)
				fileNames[actualLength++] = location.toOSString();
		}
		// was one or more of the locations null?
		if (actualLength < length) {
			String[] tempFileNames = fileNames;
			fileNames = new String[actualLength];
			for (int i = 0; i < actualLength; i++)
				fileNames[i] = tempFileNames[i];
		}
		setClipboard(resources, fileNames, text);
	}

	/**
	 * Set the clipboard contents. Prompt to retry if clipboard is busy.
	 * 
	 * @param resources the resources to copy to the clipboard
	 * @param fileNames file names of the resources to copy to the clipboard
	 * @param names string representation of all names
	 */
    private void setClipboard(IResource[] resources, String[] fileNames, String names) {
        try {
            // set the clipboard contents
            if (resources.length > 0 && fileNames.length > 0) {
                fClipboard.setContents(new Object[] { resources, fileNames,
                        names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                FileTransfer.getInstance(),
                                TextTransfer.getInstance() });
            } else if(resources.length > 0 ) {
                fClipboard.setContents(new Object[] { resources, names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                TextTransfer.getInstance() });
            } else if(resources.length == 0) {
            	fClipboard.setContents(new Object[] { names },
                        new Transfer[] { TextTransfer.getInstance() });
            }
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
                throw e;
            if (MessageDialog.openQuestion(fShell, Policy.bind("CopyToClipboardAction.3"), Policy.bind("CopyToClipboardAction.4")))  //$NON-NLS-1$//$NON-NLS-2$
                setClipboard(resources, fileNames, names);
        }
    }
    
    protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection))
			return false;
		// Calling our own selection utility because the elements in the
		// synchronize view can't adapt to IResource because we don't want the usual object
		// contribution/ on them.
		List selectedResources = getSelectedResources();
		List selectedNonResources = getSelectedNonResources();
		if (selectedResources.size() > 0 && selectedNonResources.size() == 0) {
			boolean projSelected = selectionIsOfType(IResource.PROJECT);
			boolean fileFoldersSelected = selectionIsOfType(IResource.FILE | IResource.FOLDER);
			if (!projSelected && !fileFoldersSelected)
				return false;
			// selection must be homogeneous
			if (projSelected && fileFoldersSelected)
				return false;
			// must have a common parent
			IContainer firstParent = ((IResource) selectedResources.get(0)).getParent();
			if (firstParent == null)
				return false;
			Iterator resourcesEnum = selectedResources.iterator();
			while (resourcesEnum.hasNext()) {
				IResource currentResource = (IResource) resourcesEnum.next();
				if (!currentResource.getParent().equals(firstParent))
					return false;
				// resource location must exist
				if (currentResource.getLocation() == null)
					return false;
			}
			return true;
		} else if (selectedNonResources.size() > 0 && selectedResources.size() == 0) {
			return true;
		}
		return false;
	}
     
	protected List getSelectedNonResources() {
		return Arrays.asList(Utils.getNonResources(getStructuredSelection().toArray()));
	}
	
	protected List getSelectedResources() {
    	// Calling our own selection utility because the elements in the
		// synchronize view can't adapt to IResource because we don't want the usual object
		// contribution/ on them.
		return Arrays.asList(Utils.getResources(getStructuredSelection().toArray()));
	}
    
	public void dispose() {
		fClipboard.dispose();
	}
}
