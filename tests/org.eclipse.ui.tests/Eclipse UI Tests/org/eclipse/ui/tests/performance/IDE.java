/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Hacked in for performance tests.
 */
public final class IDE {
    /**
     * The persistent property key used on IFile resources to contain
     * the preferred editor ID to use.
     * <p>
     * Example of retrieving the persisted editor id:
     * <pre><code>
     * IFile file = ...
     * IEditorDescriptor editorDesc = null;
     * try {
     * 	String editorID = file.getPersistentProperty(EDITOR_KEY);
     * 	if (editorID != null) {
     * 		editorDesc = editorReg.findEditor(editorID);
     * 	}
     * } catch (CoreException e) {
     * 	// handle problem accessing persistent property here
     * }
     * </code></pre>
     * </p><p>
     * Example of persisting the editor id:
     * <pre><code>
     * IFile file = ...
     * try {
     * 	file.setPersistentProperty(EDITOR_KEY, editorDesc.getId());
     * } catch (CoreException e) {
     * 	// handle problem setting persistent property here
     * }
     * </code></pre>
     * </p>
     */
    public static final QualifiedName EDITOR_KEY = new QualifiedName(
            "org.eclipse.ui.internal.registry.ResourceEditorRegistry", "EditorProperty");//$NON-NLS-2$//$NON-NLS-1$

    /**
     * An optional attribute within a workspace marker (<code>IMarker</code>) which
     * identifies the preferred editor type to be opened.
     */
    public static final String EDITOR_ID_ATTR = "org.eclipse.ui.editorID"; //$NON-NLS-1$

    /**
     * The resource based perspective identifier.
     */
    public static final String RESOURCE_PERSPECTIVE_ID = "org.eclipse.ui.resourcePerspective"; //$NON-NLS-1$

    /**
     * Marker help registry mapping markers to help context ids and resolutions;
     * lazily initialized on fist access.
     */
    private static MarkerHelpRegistry markerHelpRegistry = null;

    /**
     * An empty unmodifiable list. Used to avoid garbage creation.
     */
    private static final List emptyUnmodifiableList = Collections
            .unmodifiableList(new ArrayList(0));

    /**
     * Standard shared images defined by the IDE. These are over and above the
     * standard workbench images declared in {@link org.eclipse.ui.ISharedImages
     * ISharedImages}.
     * <p>
     * This interface is not intended to be implemented by clients.
     * </p>
     * 
     * @see org.eclipse.ui.ISharedImages
     */
    public interface SharedImages {
        /**
         * Identifies a project image.
         */
        public final static String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT"; //$NON-NLS-1$

        /**
         * Identifies a closed project image.
         */
        public final static String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED"; //$NON-NLS-1$

        /**
         * Identifies the image used for "open marker".
         */
        public final static String IMG_OPEN_MARKER = "IMG_OPEN_MARKER"; //$NON-NLS-1$

        /**
         * Identifies the default image used to indicate a task.
         */
        public final static String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK"; //$NON-NLS-1$

        /**
         * Identifies the default image used to indicate a bookmark.
         */
        public final static String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK"; //$NON-NLS-1$
    }

    /**
     * Preferences defined by the IDE workbench.
     * <p>
     * This interface is not intended to be implemented by clients.
     * </p>
     */
    public interface Preferences {
        /**
         * A named preference for how a new perspective should be opened
         * when a new project is created.
         * <p>
         * Value is of type <code>String</code>.  The possible values are defined 
         * by the constants <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
         * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
         * </p>
         * 
         * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_WINDOW
         * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_PAGE
         * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_REPLACE
         * @see org.eclipse.ui.IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
         */
        public static final String PROJECT_OPEN_NEW_PERSPECTIVE = "PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$
    }

    /**
     * Block instantiation.
     */
    private IDE() {
        // do nothing
    }

    /**
     * Returns the marker help registry for the workbench.
     * 
     * @return the marker help registry
     */
    public static IMarkerHelpRegistry getMarkerHelpRegistry() {
        if (markerHelpRegistry == null) {
            markerHelpRegistry = new MarkerHelpRegistry();
            new MarkerHelpRegistryReader().addHelp(markerHelpRegistry);
        }
        return markerHelpRegistry;
    }

    /**
     * Opens an editor on the given object.
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened.
     * <p>
     * 
     * @param page
     *            the page in which the editor will be opened
     * @param input
     *            the editor input
     * @param editorId
     *            the id of the editor extension to use
     * @return an open editor or <code>null</code> if an external editor was
     *         opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
     */
    public static IEditorPart openEditor(IWorkbenchPage page,
            IEditorInput input, String editorId) throws PartInitException {
        //sanity checks
        if (page == null)
            throw new IllegalArgumentException();

        // open the editor on the file
        return page.openEditor(input, editorId);
    }

    /**
     * Opens an editor on the given object.
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened. If
     * <code>activate == true</code> the editor will be activated.
     * <p>
     * @param page
     *            the page in which the editor will be opened
     * @param input
     *            the editor input
     * @param editorId
     *            the id of the editor extension to use
     * @param activate
     * 			  if <code>true</code> the editor will be activated
     * @return an open editor or <code>null</code> if an external editor was
     *         opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String, boolean)
     */
    public static IEditorPart openEditor(IWorkbenchPage page,
            IEditorInput input, String editorId, boolean activate)
            throws PartInitException {
        //sanity checks
        if (page == null)
            throw new IllegalArgumentException();

        // open the editor on the file
        return page.openEditor(input, editorId, activate);
    }

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened. If
     * <code>activate == true</code> the editor will be activated.
     * <p>
     * @param page
     *            the page in which the editor will be opened
     * @param input
     *            the editor input
     * @param activate
     * 			  if <code>true</code> the editor will be activated
     * @return an open editor or <code>null</code> if an external editor was
     *         opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput,
     *      String, boolean)
     */
    public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
            boolean activate) throws PartInitException {
        //sanity checks
        if (page == null)
            throw new IllegalArgumentException();

        // open the editor on the file
        IEditorDescriptor editorDesc = getEditorDescriptor(input);
        return page.openEditor(new FileEditorInput(input), editorDesc.getId(),
                activate);
    }

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened.
     * <p>
     * @param page
     *            the page in which the editor will be opened
     * @param input
     *            the editor input
     * @return an open editor or <code>null</code> if an external editor was
     *         opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
     */
    public static IEditorPart openEditor(IWorkbenchPage page, IFile input)
            throws PartInitException {
        //sanity checks
        if (page == null)
            throw new IllegalArgumentException();

        // open the editor on the file
        IEditorDescriptor editorDesc = getEditorDescriptor(input);
        return page.openEditor(new FileEditorInput(input), editorDesc.getId());
    }

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened.
     * <p>
     * @param page
     *            the page in which the editor will be opened
     * @param input
     *            the editor input
     * @param editorId
     *            the id of the editor extension to use
     * @return an open editor or <code>null</code> if an external editor was
     *         opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
     */
    public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
            String editorId) throws PartInitException {
        //sanity checks
        if (page == null)
            throw new IllegalArgumentException();

        // open the editor on the file
        return page.openEditor(new FileEditorInput(input), editorId);
    }

    /**
     * Opens an editor on the given file resource.
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened. If
     * <code>activate == true</code> the editor will be activated.
     * <p>
     * @param page
     *            the page in which the editor will be opened
     * @param input
     *            the editor input
     * @param editorId
     *            the id of the editor extension to use
     * @param activate
     * 			  if <code>true</code> the editor will be activated
     * @return an open editor or <code>null</code> if an external editor was
     *         opened
     * @exception PartInitException
     *                if the editor could not be initialized
     * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String, boolean)
     */
    public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
            String editorId, boolean activate) throws PartInitException {
        //sanity checks
        if (page == null)
            throw new IllegalArgumentException();

        // open the editor on the file
        return page.openEditor(new FileEditorInput(input), editorId, activate);
    }

    /**
     * Returns an editor descriptor appropriate for opening the given file resource.
     * <p>
     * The editor descriptor is determined using a multistep process.
     * </p>
     * <ol>
     *   <li>The file is consulted for a persistent property named
     *       <code>IDE.EDITOR_KEY</code> containing the preferred editor id
     *       to be used.</li>
     *   <li>The workbench editor registry is consulted to determine if an editor 
     *			extension has been registered for the file type.  If so, an 
     *			instance of the editor extension is opened on the file.  
     *			See <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
     *   <li>The operating system is consulted to determine if an in-place
     *       component editor is available (e.g. OLE editor on Win32 platforms).</li>
     *   <li>The operating system is consulted to determine if an external
     * 		editor is available.</li>
     * </ol>
     * </p>
     * @param file the file
     * @return an editor descriptor, appropriate for opening the file
     * @throws PartInitException if no editor can be found
     */
    public static IEditorDescriptor getEditorDescriptor(IFile file)
            throws PartInitException {

		if (file == null) {
			throw new IllegalArgumentException();
		}

		return getEditorDescriptor(file.getName(), PlatformUI.getWorkbench()
				.getEditorRegistry(), getDefaultEditor(file));
	}

	/**
	 * Returns an editor descriptor appropriate for opening a file resource with
	 * the given name.
	 * <p>
	 * The editor descriptor is determined using a multistep process.
	 * </p>
	 * <ol>
	 * <li>The file is consulted for a persistent property named
	 * <code>IDE.EDITOR_KEY</code> containing the preferred editor id to be
	 * used.</li>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param name
	 *            the file name
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 */
	public static IEditorDescriptor getEditorDescriptor(String name)
			throws PartInitException {

		if (name == null) {
			throw new IllegalArgumentException();
		}

		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();

		return getEditorDescriptor(name, editorReg, editorReg
				.getDefaultEditor(name));
	}

	/**
	 * Get the editor descriptor for a given name using the editorDescriptor
	 * passed in as a default as a starting point.
	 * 
	 * @param name
	 *            The name of the element to open.
	 * @param editorReg
	 *            The editor registry to do the lookups from.
	 * @param defaultDescriptor
	 *            IEditorDescriptor or <code>null</code>
	 * @return IEditorDescriptor
	 * @throws PartInitException
	 *             if no valid editor can be found
	 */
	private static IEditorDescriptor getEditorDescriptor(String name,
			IEditorRegistry editorReg, IEditorDescriptor defaultDescriptor)
			throws PartInitException {

		if (defaultDescriptor != null)
			return defaultDescriptor;

		IEditorDescriptor editorDesc = defaultDescriptor;

		// if no valid editor found, bail out
		if (editorDesc == null)
			throw new PartInitException("No editor found"); //$NON-NLS-1$

		return editorDesc;
	}

    /**
     * Returns the default editor for a given file.
     * <p>
     * A default editor id may be registered for a specific file using
     * <code>setDefaultEditor</code>.  If the given file has a registered
     * default editor id the default editor will derived from it.  If not, 
     * the default editor is determined by taking the file name for the 
     * file and obtaining the default editor for that name.
     * </p>
     *
     * @param file the file
     * @return the descriptor of the default editor, or <code>null</code> if not
     *   found
     */
    public static IEditorDescriptor getDefaultEditor(IFile file) {
        // Try file specific editor.
        IEditorRegistry editorReg = PlatformUI.getWorkbench()
                .getEditorRegistry();
        try {
            String editorID = file.getPersistentProperty(EDITOR_KEY);
            if (editorID != null) {
                IEditorDescriptor desc = editorReg.findEditor(editorID);
                if (desc != null)
                    return desc;
            }
        } catch (CoreException e) {
            // do nothing
        }

        // Try lookup with filename
        return editorReg.getDefaultEditor(file.getName());
    }

    /**
     * Extracts and returns the <code>IResource</code>s in the given
     * selection or the resource objects they adapts to.
     * 
     * @param originalSelection the original selection, possibly empty
     * @return list of resources (element type: <code>IResource</code>), 
     *    possibly empty
     */
    public static List computeSelectedResources(
            IStructuredSelection originalSelection) {
        List resources = null;
        for (Iterator e = originalSelection.iterator(); e.hasNext();) {
            Object next = e.next();
            Object resource = null;
            if (next instanceof IResource) {
                resource = next;
            } else if (next instanceof IAdaptable) {
                resource = ((IAdaptable) next).getAdapter(IResource.class);
            }
            if (resource != null) {
                if (resources == null) {
                    // lazy init to avoid creating empty lists
                    // assume selection contains mostly resources most times
                    resources = new ArrayList(originalSelection.size());
                }
                resources.add(resource);
            }
        }
        if (resources == null) {
            return emptyUnmodifiableList;
        }
        return resources;
        
    }
}