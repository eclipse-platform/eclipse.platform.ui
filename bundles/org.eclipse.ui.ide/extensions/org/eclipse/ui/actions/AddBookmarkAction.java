/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *        IBM Corporation - initial API and implementation 
 *   Sebastian Davids <sdavids@gmx.de>
 *     - Fix for bug 20510 - Add Bookmark action has wrong label in navigator or
 *       packages view
 *********************************************************************/
package org.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for adding a bookmark to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class AddBookmarkAction extends SelectionListenerAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".AddBookmarkAction"; //$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */
    private Shell shell;

    /**
     * Whether to prompt the user for the bookmark name.
     */
    private boolean promptForName = true;

    /**
     * Creates a new bookmark action. By default, prompts the user for the
     * bookmark name.
     *
     * @param shell the shell for any dialogs
     */
    public AddBookmarkAction(Shell shell) {
        this(shell, true);
    }

    /**
     * Creates a new bookmark action.
     *
     * @param shell the shell for any dialogs
     * @param promptForName whether to ask the user for the bookmark name
     */
    public AddBookmarkAction(Shell shell, boolean promptForName) {
        super(IDEWorkbenchMessages.getString("AddBookmarkLabel")); //$NON-NLS-1$
        setId(ID);
        if (shell == null) {
            throw new IllegalArgumentException();
        }
        this.shell = shell;
        this.promptForName = promptForName;
        setToolTipText(IDEWorkbenchMessages.getString("AddBookmarkToolTip")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IIDEHelpContextIds.ADD_BOOKMARK_ACTION);
    }

    /**
     * Creates a marker of the given type on each of the files in the
     * current selection.
     *
     * @param markerType the marker type
     */
    void createMarker(String markerType) {
        IStructuredSelection selection = getStructuredSelection();
        for (Iterator i = selection.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof IFile) {
                createMarker((IFile) o, markerType);
            } else if (o instanceof IAdaptable) {
                Object resource = ((IAdaptable) o).getAdapter(IResource.class);
                if (resource instanceof IFile)
                    createMarker((IFile) resource, markerType);
            }
        }
    }

    /**
     * Creates a marker of the given type on the given file resource.
     *
     * @param file the file resource
     * @param markerType the marker type
     */
    void createMarker(final IFile file, final String markerType) {
        try {
            file.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    String markerMessage = file.getName();
                    if (promptForName)
                        markerMessage = askForLabel(markerMessage);
                    if (markerMessage != null) {
                        IMarker marker = file.createMarker(markerType);
                        marker.setAttribute(IMarker.MESSAGE, markerMessage);
                    }
                }
            }, null);
        } catch (CoreException e) {
            IDEWorkbenchPlugin.log(null, e.getStatus()); // We don't care
        }
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        createMarker(IMarker.BOOKMARK);
    }

    /**
     * The <code>AddBookmarkAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables the action only
     * if the selection is not empty and contains just file resources.
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        // @issue typed selections
        return super.updateSelection(selection) && !selection.isEmpty()
                && selectionIsOfType(IResource.FILE);
    }

    /**
     * Asks the user for a bookmark name.
     *
     * @param proposal the suggested bookmark name
     * @return the bookmark name or <code>null</code> if cancelled.
     */
    String askForLabel(String proposal) {
        String title = IDEWorkbenchMessages
                .getString("AddBookmarkDialog.title"); //$NON-NLS-1$
        String message = IDEWorkbenchMessages
                .getString("AddBookmarkDialog.message"); //$NON-NLS-1$

        IInputValidator inputValidator = new IInputValidator() {
            public String isValid(String newText) {
                return (newText == null || newText.length() == 0) ? " " : null; //$NON-NLS-1$
            }
        };
        InputDialog dialog = new InputDialog(shell, title, message, proposal,
                inputValidator);

        if (dialog.open() != Window.CANCEL) {
            String name = dialog.getValue();
            if (name == null)
                return null;
            name = name.trim();
            return (name.length() == 0) ? null : name;
        } else {
            return null;
        }
    }
}