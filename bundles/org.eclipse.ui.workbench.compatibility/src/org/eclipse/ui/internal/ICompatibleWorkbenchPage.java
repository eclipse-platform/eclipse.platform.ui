/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

/**
 * Internal interface used in providing increased binary compatibility for
 * pre-3.0 plug-ins. This declaration masks the empty interface of the same name
 * declared in the Workbench proper. This interface declares IWorkbenchPage that
 * existed in 2.1 but were removed in 3.0 because they referenced resource API.
 * <p>
 * Plug-ins should not refer to this type or its containing fragment from their
 * class path. It is intended only to provide binary compatibility for pre-3.0
 * plug-ins, and should not be referenced at development time.
 * </p>
 * 
 * @since 3.0
 */
public interface ICompatibleWorkbenchPage {

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If this page already has an editor open on the target object that editor
     * is activated; otherwise, a new editor is opened.
     * <p>
     * <p>
     * An appropriate editor for the input is determined using a multistep
     * process.
     * </p>
     * <ol>
     * <li>The workbench editor registry is consulted to determine if an editor
     * extension has been registered for the file type. If so, an instance of
     * the editor extension is opened on the file. See
     * <code>IEditorRegistry.getDefaultEditor(IFile)</code>.
     * <li>Next, the native operating system will be consulted to determine if
     * a native editor exists for the file type. If so, a new process is started
     * and the native editor is opened on the file.
     * <li>If all else fails the file will be opened in a default text editor.
     * </li>
     * </ol>
     * </p>
     * 
     * @param input
     *            the file to edit
     * @return an open and active editor, or <code>null</code> if a system
     *         editor was opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @deprecated In 3.0 this resource-specific method moved from this
     *             interface to
     *             <code>org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage,IFile)</code>.
     *             This method should not be referenced at development time. See
     *             the class comment for more details.
     */
    public IEditorPart openEditor(IFile input) throws PartInitException;

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If this page already has an editor open on the target object that editor
     * is brought to front; otherwise, a new editor is opened. If
     * <code>activate == true</code> the editor will be activated.
     * <p>
     * <p>
     * The editor type is determined by mapping <code>editorId</code> to an
     * editor extension registered with the workbench. An editor id is passed
     * rather than an editor object to prevent the accidental creation of more
     * than one editor for the same input. It also guarantees a consistent
     * lifecycle for editors, regardless of whether they are created by the user
     * or restored from saved data.
     * </p>
     * 
     * @param input
     *            the file to edit
     * @param editorId
     *            the id of the editor extension to use or null
     * @param activate
     *            if <code>true</code> the editor will be activated
     * @return an open and active editor
     * @exception PartInitException
     *                if the editor could not be initialized
     * @deprecated In 3.0 this resource-specific method moved from this
     *             interface to
     *             <code>org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage,IFile,String,boolean)</code>.
     *             This method should not be referenced at development time. See
     *             the class comment for more details.
     */
    public IEditorPart openEditor(IFile input, String editorId, boolean activate)
            throws PartInitException;

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If this page already has an editor open on the target object that editor
     * is activated; otherwise, a new editor is opened.
     * <p>
     * <p>
     * The editor type is determined by mapping <code>editorId</code> to an
     * editor extension registered with the workbench. An editor id is passed
     * rather than an editor object to prevent the accidental creation of more
     * than one editor for the same input. It also guarantees a consistent
     * lifecycle for editors, regardless of whether they are created by the user
     * or restored from saved data.
     * </p>
     * 
     * @param editorId
     *            the id of the editor extension to use
     * @param input
     *            the file to edit
     * @return an open and active editor
     * @exception PartInitException
     *                if the editor could not be initialized
     * @deprecated In 3.0 this resource-specific method moved from this
     *             interface to
     *             <code>org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage,IFile,String)</code>.
     *             This method should not be referenced at development time. See
     *             the class comment for more details.
     */
    public IEditorPart openEditor(IFile input, String editorId)
            throws PartInitException;

    /**
     * Opens an editor on the file resource of the given marker.
     * <p>
     * If this page already has an editor open on the target object that editor
     * is activated; otherwise, a new editor is opened. The cursor and selection
     * state of the editor is then updated from information recorded in the
     * marker.
     * <p>
     * <p>
     * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute the
     * attribute value will be used to determine the editor type to be opened.
     * If not, the registered editor for the marker resource will be used.
     * </p>
     * 
     * @param marker
     *            the marker to open
     * @return an open and active editor, or null if a system editor was opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @deprecated In 3.0 this resource-specific method moved from this
     *             interface to
     *             <code>org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage,IMarker)</code>.
     *             This method should not be referenced at development time. See
     *             the class comment for more details.
     */
    public IEditorPart openEditor(IMarker marker) throws PartInitException;

    /**
     * Opens an editor on the file resource of the given marker.
     * <p>
     * If this page already has an editor open on the target object that editor
     * is brought to front; otherwise, a new editor is opened. If
     * <code>activate == true</code> the editor will be activated. The cursor
     * and selection state of the editor are then updated from information
     * recorded in the marker.
     * <p>
     * <p>
     * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute the
     * attribute value will be used to determine the editor type to be opened.
     * If not, the registered editor for the marker resource will be used.
     * </p>
     * 
     * @param marker
     *            the marker to open
     * @param activate
     *            if <code>true</code> the editor will be activated
     * @return an open editor, or null if a system editor was opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @deprecated In 3.0 this resource-specific method moved from this
     *             interface to
     *             <code>org.eclipse.ui.ide.IDE.openEditor(IWorkbenchPage,IMarker,boolean)</code>.
     *             This method should not be referenced at development time. See
     *             the class comment for more details.
     */
    public IEditorPart openEditor(IMarker marker, boolean activate)
            throws PartInitException;

    /**
     * Opens an operating system editor on a given file. Once open, the
     * workbench has no knowledge of the editor or the state of the file being
     * edited. Users are expected to perform a "Local Refresh" from the
     * workbench user interface.
     * 
     * @param input
     *            the file to edit
     * @exception PartInitException
     *                if the editor could not be opened.
     * @deprecated In 3.0 this resource-specific method was removed. Use
     *             <code>openEditor(new FileEditorInput(file), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)</code>
     *             instead. This method should not be referenced at development
     *             time. See the class comment for more details.
     */
    public void openSystemEditor(IFile input) throws PartInitException;
}
