/*
 * Created on Aug 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.team.internal.ui.synchronize.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.part.ResourceTransfer;


/**
 * Based on org.eclipse.ui.views.navigator.CopyAction.
 */

class CopyToClipboardAction extends SelectionListenerAction {
    
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
        List selectedResources = getSelectedResources();
        IResource[] resources = (IResource[]) selectedResources.toArray(new IResource[selectedResources.size()]);

        // Get the file names and a string representation
        final int length = resources.length;
        int actualLength = 0;
        String[] fileNames = new String[length];
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            final IPath location = resources[i].getLocation();
            // location may be null. See bug 29491.
            if (location != null)
                fileNames[actualLength++] = location.toOSString();
            if (i > 0)
                buf.append("\n"); //$NON-NLS-1$
            buf.append(resources[i].getName());
        }
        // was one or more of the locations null?
        if (actualLength < length) {
            String[] tempFileNames = fileNames;
            fileNames = new String[actualLength];
            for (int i = 0; i < actualLength; i++)
                fileNames[i] = tempFileNames[i];
        }
        setClipboard(resources, fileNames, buf.toString());
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
            if (fileNames.length > 0) {
                fClipboard.setContents(new Object[] { resources, fileNames,
                        names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                FileTransfer.getInstance(),
                                TextTransfer.getInstance() });
            } else {
                fClipboard.setContents(new Object[] { resources, names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                TextTransfer.getInstance() });
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

        if (getSelectedNonResources().size() > 0)
            return false;

        List selectedResources = getSelectedResources();
        if (selectedResources.size() == 0)
            return false;

        boolean projSelected = selectionIsOfType(IResource.PROJECT);
        boolean fileFoldersSelected = selectionIsOfType(IResource.FILE
                | IResource.FOLDER);
        if (!projSelected && !fileFoldersSelected)
            return false;

        // selection must be homogeneous
        if (projSelected && fileFoldersSelected)
            return false;

        // must have a common parent	
        IContainer firstParent = ((IResource) selectedResources.get(0))
                .getParent();
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
    }
    
	public void dispose() {
		fClipboard.dispose();
	}
}
