/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * An editor area drop adapter to handle transfer types
 * <code>EditorInputTransfer</code>, <code>MarkerTransfer</code>,
 * and <code>ResourceTransfer</code>.
 */
public class EditorAreaDropAdapter extends DropTargetAdapter {
    private IWorkbenchWindow window;

    /**
     * Constructs a new EditorAreaDropAdapter.
     * @param window the workbench window
     */
    public EditorAreaDropAdapter(IWorkbenchWindow window) {
        this.window = window;
    }

    public void dragEnter(DropTargetEvent event) {
        // always indicate a copy
        event.detail = DND.DROP_COPY;
        event.feedback = DND.FEEDBACK_NONE;
    }

    public void dragOver(DropTargetEvent event) {
        // always indicate a copy
        event.detail = DND.DROP_COPY;
        event.feedback = DND.FEEDBACK_NONE;
    }

    public void dragOperationChanged(DropTargetEvent event) {
        // always indicate a copy
        event.detail = DND.DROP_COPY;
        event.feedback = DND.FEEDBACK_NONE;
    }

    public void drop(final DropTargetEvent event) {
        Display d = window.getShell().getDisplay();
        final IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            d.asyncExec(new Runnable() {
                public void run() {
                    asyncDrop(event, page);
                }
            });
        }
    }

    private void asyncDrop(DropTargetEvent event, IWorkbenchPage page) {

        /* Open Editor for generic IEditorInput */
        if (EditorInputTransfer.getInstance().isSupportedType(
                event.currentDataType)) {
            /* event.data is an array of EditorInputData, which contains an IEditorInput and 
             * the corresponding editorId */
            Assert.isTrue(event.data instanceof EditorInputTransfer.EditorInputData[]);
            EditorInputTransfer.EditorInputData[] editorInputs = (EditorInputTransfer.EditorInputData []) event.data;
            for (int i = 0; i < editorInputs.length; i++) {
                IEditorInput editorInput = editorInputs[i].input;
                String editorId = editorInputs[i].editorId;
                openNonExternalEditor(page, editorInput, editorId);
            }
        }

        /* Open Editor for Marker (e.g. Tasks, Bookmarks, etc) */
        else if (MarkerTransfer.getInstance().isSupportedType(
                event.currentDataType)) {
            Assert.isTrue(event.data instanceof IMarker[]);
            IMarker[] markers = (IMarker[]) event.data;
            for (int i = 0; i < markers.length; i++) {
                openNonExternalEditor(page, markers[i]);
            }
        }

        /* Open Editor for resource */
        else if (ResourceTransfer.getInstance().isSupportedType(
                event.currentDataType)) {
            Assert.isTrue(event.data instanceof IResource[]);
            IResource[] files = (IResource[]) event.data;
            for (int i = 0; i < files.length; i++) {
                if (files[i] instanceof IFile) {
                    IFile file = (IFile) files[i];
                    
                    if (!file.isPhantom())
                    	openNonExternalEditor(page, file);
                }
            }
        }

        /* Open Editor for file from local file system */
        else if (FileTransfer.getInstance().isSupportedType(
                event.currentDataType)) {
            Assert.isTrue(event.data instanceof String[]);
            String[] paths = (String[]) event.data;
            for (int i = 0; i < paths.length; i++) {
            	IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(paths[i]));
            	try {
					IDE.openEditorOnFileStore(page, fileStore);
				} catch (PartInitException e) {
					// silently ignore problems opening the editor
				}
            }
        }

    }

    /**
     * Opens an editor for the given file on the given workbench page in response
     * to a drop on the workbench editor area. In contrast to other ways of opening
     * an editor, we never open an external editor in this case (since external
     * editors appear in their own window and not in the editor area).
     * The operation fails silently if there is no suitable editor to open.
     * 
     * @param page the workbench page
     * @param file the file to open
     * @return the editor part that was opened, or <code>null</code> if no editor
     * was opened
     */
    private IEditorPart openNonExternalEditor(IWorkbenchPage page, IFile file) {
        IEditorPart result;
        try {
            // find out which editor we would normal open
            IEditorDescriptor defaultEditorDesc = IDE.getDefaultEditor(file);
            if (defaultEditorDesc != null
                    && !defaultEditorDesc.isOpenExternal()) {
                // open an internal or in-place editor
                result = IDE.openEditor(page, file, true);
            } else {
                // never open an external editor in response to a drop
                // check the OS for in-place editor (OLE on Win32)
                IEditorRegistry editorReg = PlatformUI.getWorkbench()
                        .getEditorRegistry();
                IEditorDescriptor editorDesc = null;
                if (editorReg.isSystemInPlaceEditorAvailable(file.getName())) {
                    editorDesc = editorReg
                            .findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
                }

                // next lookup the default text editor
                if (editorDesc == null) {
                    editorDesc = editorReg
                            .findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
                }

                // if no valid editor found, bail out
                if (editorDesc == null) {
                    throw new PartInitException(IDEWorkbenchMessages.IDE_noFileEditorFound);
                }

                // open the editor on the file
                result = page.openEditor(new FileEditorInput(file), editorDesc
                        .getId(), true);
            }
        } catch (PartInitException e) {
            // silently ignore problems opening the editor
            result = null;
        }
        return result;
    }

    /**
     * Opens an editor for the given marker on the given workbench page in response
     * to a drop on the workbench editor area. In contrast to other ways of opening
     * an editor, we never open an external editor in this case (since external
     * editors appear in their own window and not in the editor area).
     * The operation fails silently if there is no suitable editor to open.
     * 
     * @param page the workbench page
     * @param marker the marker to open
     * @return the editor part that was opened, or <code>null</code> if no editor
     * was opened
     */
    private IEditorPart openNonExternalEditor(IWorkbenchPage page,
            IMarker marker) {
        IEditorPart result;
        try {
            // get the marker resource file
            if (!(marker.getResource() instanceof IFile)) {
                return null;
            }
            IFile file = (IFile) marker.getResource();

            // get the preferred editor id from the marker
            IEditorDescriptor editorDesc = null;
            try {
                String editorID = (String) marker
                        .getAttribute(IDE.EDITOR_ID_ATTR);
                if (editorID != null) {
                    IEditorRegistry editorReg = PlatformUI.getWorkbench()
                            .getEditorRegistry();
                    editorDesc = editorReg.findEditor(editorID);
                }
            } catch (CoreException e) {
                // ignore problems with getting the marker
            }

            // open the editor on the marker resource file
            if (editorDesc != null && !editorDesc.isOpenExternal()) {
                result = page.openEditor(new FileEditorInput(file), editorDesc
                        .getId(), true);
            } else {
                result = openNonExternalEditor(page, file);
            }

            // get the editor to update its position based on the marker
            if (result != null) {
                IDE.gotoMarker(result, marker);
            }

        } catch (PartInitException e) {
            // silently ignore problems opening the editor
            result = null;
        }
        return result;
    }

    /**
     * Opens an editor for the given editor input and editor id combination on the
     * given workbench page in response to a drop on the workbench editor area.
     * In contrast to other ways of opening an editor, we never open an external
     * editor in this case (since external editors appear in their own window and
     * not in the editor area). The operation fails silently if the editor
     * cannot be opened.
     * 
     * @param page the workbench page
     * @param editorInput the editor input
     * @param editorId the editor id
     * @return the editor part that was opened, or <code>null</code> if no editor
     * was opened
     */
    private IEditorPart openNonExternalEditor(IWorkbenchPage page,
            IEditorInput editorInput, String editorId) {
        IEditorPart result;
        try {
            IEditorRegistry editorReg = PlatformUI.getWorkbench()
                    .getEditorRegistry();
            IEditorDescriptor editorDesc = editorReg.findEditor(editorId);
            if (editorDesc != null && !editorDesc.isOpenExternal()) {
                result = page.openEditor(editorInput, editorId);
            } else {
                result = null;
            }
        } catch (PartInitException e) {
            // silently ignore problems opening the editor
            result = null;
        }
        return result;
    }

}
