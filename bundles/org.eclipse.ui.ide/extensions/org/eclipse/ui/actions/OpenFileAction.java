/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Standard action for opening an editor on the currently selected file 
 * resource(s).
 * <p>
 * Note that there is a different action for opening closed projects:
 * <code>OpenResourceAction</code>.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenFileAction extends OpenSystemEditorAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".OpenFileAction";//$NON-NLS-1$

    /**
     * The editor to open.
     */
    private IEditorDescriptor editorDescriptor;

    /**
     * Creates a new action that will open editors on the then-selected file 
     * resources. Equivalent to <code>OpenFileAction(page,null)</code>.
     *
     * @param page the workbench page in which to open the editor
     */
    public OpenFileAction(IWorkbenchPage page) {
        this(page, null);
    }

    /**
     * Creates a new action that will open instances of the specified editor on 
     * the then-selected file resources.
     *
     * @param page the workbench page in which to open the editor
     * @param descriptor the editor descriptor, or <code>null</code> if unspecified
     */
    public OpenFileAction(IWorkbenchPage page, IEditorDescriptor descriptor) {
        super(page);
        setText(descriptor == null ? IDEWorkbenchMessages.OpenFileAction_text : descriptor.getLabel());
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.OPEN_FILE_ACTION);
        setToolTipText(IDEWorkbenchMessages.OpenFileAction_toolTip);
        setId(ID);
        this.editorDescriptor = descriptor;
    }

    /**
     * Ensures that the contents of the given file resource are local.
     *
     * @param file the file resource
     * @return <code>true</code> if the file is local, and <code>false</code> if
     *   it could not be made local for some reason
     */
    boolean ensureFileLocal(final IFile file) {
        //Currently fails due to Core PR.  Don't do it for now
        //1G5I6PV: ITPCORE:WINNT - IResource.setLocal() attempts to modify immutable tree
        //file.setLocal(true, IResource.DEPTH_ZERO);
        return true;
    }

    /**
     * Opens an editor on the given file resource.
     *
     * @param file the file resource
     */
    void openFile(IFile file) {
        try {
            boolean activate = OpenStrategy.activateOnOpen();
            if (editorDescriptor == null) {
                IDE.openEditor(getWorkbenchPage(), file, activate);
            } else {
                if (ensureFileLocal(file)) {
                    getWorkbenchPage().openEditor(new FileEditorInput(file),
                            editorDescriptor.getId(), activate);
                }
            }
        } catch (PartInitException e) {
            DialogUtil.openError(getWorkbenchPage().getWorkbenchWindow()
                    .getShell(), IDEWorkbenchMessages.OpenFileAction_openFileShellTitle,
                    e.getMessage(), e);
        }
    }

}
