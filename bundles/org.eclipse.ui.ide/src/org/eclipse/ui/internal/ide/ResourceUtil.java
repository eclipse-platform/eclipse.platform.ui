/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Utility class for manipulating resources and determining correspondences 
 * between resources and workbench objects.
 * 
 * @since 3.1
 */
public final class ResourceUtil {

    private ResourceUtil() {
        // prevent instantiation
    }

    /**
     * Returns the file corresponding to the given editor input, or <code>null</code>
     * if there is no applicable file.
     * 
     * @param editorInput the editor input
     * @return the file corresponding to the editor input, or <code>null</code>
     */
    public static IFile getFile(IEditorInput editorInput) {
        // Note: do not treat IFileEditorInput as a special case.  Use the adapter mechanism instead.
        // See Bug 87288 [IDE] [EditorMgmt] Should avoid explicit checks for [I]FileEditorInput
        Object o = editorInput.getAdapter(IFile.class);
        if (o instanceof IFile)
            return (IFile) o;
        return null;
    }

    /**
     * Returns the resource corresponding to the given editor input, or <code>null</code>
     * if there is no applicable resource.
     * 
     * @param editorInput the editor input
     * @return the file corresponding to the editor input, or <code>null</code>
     */
    public static IResource getResource(IEditorInput editorInput) {
        // Note: do not treat IFileEditorInput as a special case.  Use the adapter mechanism instead.
        // See Bug 87288 [IDE] [EditorMgmt] Should avoid explicit checks for [I]FileEditorInput
        Object o = editorInput.getAdapter(IResource.class);
        if (o instanceof IResource)
            return (IResource) o;
        // the input may adapt to IFile but not IResource
        return getFile(editorInput);
    }

    /**
     * Returns the editor in the given page whose input represents the given file,
     * or <code>null</code> if there is no such editor.
     * 
     * @param page the workbench page
     * @param file the file
     * @return the matching editor, or <code>null</code>
     */
    public static IEditorPart findEditor(IWorkbenchPage page, IFile file) {
        // handle the common case where the editor input is a FileEditorInput
        IEditorPart editor = page.findEditor(new FileEditorInput(file));
        if (editor != null) {
            return editor;
        }
        // check for editors that have their own kind of input that adapts to IFile,
        // being careful not to force loading of the editor
        IEditorReference[] refs = page.getEditorReferences();
        for (int i = 0; i < refs.length; i++) {
            IEditorReference ref = refs[i];
            IEditorPart part = ref.getEditor(false);
            if (part != null) {
                IFile editorFile = getFile(part.getEditorInput());
                if (editorFile != null && file.equals(editorFile)) {
                    return part;
                }
            }
        }
        return null;
    }
    
}
