/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 411578
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 485201, 496475
 *     Mickael Istria (Red Hat Inc.) - Bug 90292 (default editor) and family
 *******************************************************************************/
package org.eclipse.ui.ide;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IModelProviderDescriptor;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ModelStatus;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.EditorAssociationOverrideDescriptor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.model.StandardPropertiesAdapterFactory;
import org.eclipse.ui.internal.ide.model.WorkbenchAdapterFactory;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.internal.ide.registry.SystemEditorOrTextEditorStrategy;
import org.eclipse.ui.internal.ide.registry.UnassociatedEditorStrategyRegistry;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Collection of IDE-specific APIs factored out of existing workbench. This
 * class cannot be instantiated; all functionality is provided by static methods
 * and fields.
 *
 * @since 3.0
 */
public final class IDE {
	/**
	 * The persistent property key used on IFile resources to contain the preferred
	 * editor ID to use.
	 * <p>
	 * Example of retrieving the persisted editor id:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 *  IFile file = ...
	 *  IEditorDescriptor editorDesc = null;
	 *  try {
	 *  	String editorID = file.getPersistentProperty(EDITOR_KEY);
	 *  	if (editorID != null) {
	 *  		editorDesc = editorReg.findEditor(editorID);
	 *  	}
	 *  } catch (CoreException e) {
	 *  	// handle problem accessing persistent property here
	 *  }
	 * </code>
	 * </pre>
	 *
	 * <p>
	 * Example of persisting the editor id:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 *  IFile file = ...
	 *  try {
	 *  	file.setPersistentProperty(EDITOR_KEY, editorDesc.getId());
	 *  } catch (CoreException e) {
	 *  	// handle problem setting persistent property here
	 *  }
	 * </code>
	 * </pre>
	 *
	 */
	public static final QualifiedName EDITOR_KEY = new QualifiedName(
			"org.eclipse.ui.internal.registry.ResourceEditorRegistry", "EditorProperty");//$NON-NLS-2$//$NON-NLS-1$

	/**
	 * An optional attribute within a workspace marker (<code>IMarker</code>)
	 * which identifies the preferred editor type to be opened.
	 */
	public static final String EDITOR_ID_ATTR = "org.eclipse.ui.editorID"; //$NON-NLS-1$

	/**
	 * The resource based perspective identifier.
	 */
	public static final String RESOURCE_PERSPECTIVE_ID = "org.eclipse.ui.resourcePerspective"; //$NON-NLS-1$

	/**
	 * A preference key to decide which {@link IUnassociatedEditorStrategy} to use
	 * when trying to open files without associated editors.
	 *
	 * @since 3.12
	 */
	public static final String UNASSOCIATED_EDITOR_STRATEGY_PREFERENCE_KEY = "unassociatedEditorStrategy";//$NON-NLS-1$

	/**
	 * Marker help registry mapping markers to help context ids and resolutions;
	 * lazily initialized on fist access.
	 */
	private static MarkerHelpRegistry markerHelpRegistry = null;

	private static volatile IEditorAssociationOverride[] editorAssociationOverrides;


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
		public static final String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT"; //$NON-NLS-1$

		/**
		 * Identifies a closed project image.
		 */
		public static final String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED"; //$NON-NLS-1$

		/**
		 * Identifies the image used for "open marker".
		 */
		public static final String IMG_OPEN_MARKER = "IMG_OPEN_MARKER"; //$NON-NLS-1$

		/**
		 * Identifies the default image used to indicate a task.
		 */
		public static final String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK"; //$NON-NLS-1$

		/**
		 * Identifies the default image used to indicate a bookmark.
		 */
		public static final String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK"; //$NON-NLS-1$
	}

	/**
	 * Preferences defined by the IDE workbench.
	 * <p>
	 * This interface is not intended to be implemented by clients.
	 * </p>
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public interface Preferences {


		/**
		 * A named preference for how a new perspective should be opened when a
		 * new project is created.
		 * <p>
		 * Value is of type <code>String</code>. The possible values are
		 * defined by the constants
		 * <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE,
		 * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
		 * </p>
		 *
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_WINDOW
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_PAGE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_REPLACE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
		 */
		public static final String PROJECT_OPEN_NEW_PERSPECTIVE = "PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

		/**
		 * <p>
		 * Specifies whether or not the workspace selection dialog should be
		 * shown on startup.
		 * </p>
		 * <p>
		 * The default value for this preference is <code>true</code>.
		 * </p>
		 *
		 * @since 3.1
		 */
		public static final String SHOW_WORKSPACE_SELECTION_DIALOG = "SHOW_WORKSPACE_SELECTION_DIALOG"; //$NON-NLS-1$

		/**
		 * Specifies whether the "Recent Workspaces" should be shown
		 *
		 * @since 3.12
		 */
		public static final String SHOW_RECENT_WORKSPACES = "SHOW_RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores the maximum number of workspaces that should be displayed in
		 * the ChooseWorkspaceDialog.
		 * </p>
		 *
		 * @since 3.1
		 */
		public static final String MAX_RECENT_WORKSPACES = "MAX_RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores a comma separated list of the recently used workspace paths.
		 * </p>
		 *
		 * @since 3.1
		 */
		public static final String RECENT_WORKSPACES = "RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores the version of the protocol used to decode/encode the list of
		 * recent workspaces.
		 * </p>
		 *
		 * @since 3.1
		 */
		public static final String RECENT_WORKSPACES_PROTOCOL = "RECENT_WORKSPACES_PROTOCOL"; //$NON-NLS-1$

		/**
		 * Workspace name, will be displayed in the window title. This
		 * preference must only be changed on the UI thread.
		 * @since 3.10
		 */
		public static final String WORKSPACE_NAME = "WORKSPACE_NAME"; //$NON-NLS-1$
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
	 * Sets the cursor and selection state for the given editor to reveal the
	 * position of the given marker. This is done on a best effort basis. If the
	 * editor does not provide an <code>IGotoMarker</code> interface (either
	 * directly or via <code>IAdaptable.getAdapter</code>), this has no
	 * effect.
	 *
	 * @param editor
	 *            the editor
	 * @param marker
	 *            the marker
	 */
	public static void gotoMarker(IEditorPart editor, IMarker marker) {
		IGotoMarker gotoMarker = Adapters.adapt(editor, IGotoMarker.class);
		if (gotoMarker != null) {
			gotoMarker.gotoMarker(marker);
		}
	}

	/**
	 * Opens an editor on the given object.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened.
	 * </p>
	 *
	 * @param page     the page in which the editor will be opened
	 * @param input    the editor input
	 * @param editorId the id of the editor extension to use
	 * @return an open editor or <code>null</code> if an external editor was opened
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page,
			IEditorInput input, String editorId) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(input, editorId);
	}

	/**
	 * Opens an editor on the given IFileStore object.
	 * <p>
	 * Unlike the other <code>openEditor</code> methods, this one can be used
	 * to open files that reside outside the workspace resource set.
	 * </p>
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * </p>
	 *
	 * @param page
	 *            the page in which the editor will be opened
	 * @param uri
	 *            the URI of the file store representing the file to open
	 * @param editorId
	 *            the id of the editor extension to use
	 * @param activate
	 *            if <code>true</code> the editor will be activated opened
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 *
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 * @see EFS#getStore(URI)
	 *
	 * @since 3.3
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, URI uri,
			String editorId, boolean activate) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		IFileStore fileStore;
		try {
			fileStore = EFS.getStore(uri);
		} catch (CoreException e) {
			throw new PartInitException(
					IDEWorkbenchMessages.IDE_coreExceptionFileStore, e);
		}

		IEditorInput input = getEditorInput(fileStore);

		// open the editor on the file
		return page.openEditor(input, editorId, activate);
	}

	/**
	 * Create the Editor Input appropriate for the given <code>IFileStore</code>.
	 * The result is a normal file editor input if the file exists in the
	 * workspace and, if not, we create a wrapper capable of managing an
	 * 'external' file using its <code>IFileStore</code>.
	 *
	 * @param fileStore
	 *            The file store to provide the editor input for
	 * @return The editor input associated with the given file store
	 * @since 3.3
	 */
	private static IEditorInput getEditorInput(IFileStore fileStore) {
		IFile workspaceFile = getWorkspaceFile(fileStore);
		if (workspaceFile != null)
			return new FileEditorInput(workspaceFile);
		return new FileStoreEditorInput(fileStore);
	}

	/**
	 * Determine whether or not the <code>IFileStore</code> represents a file
	 * currently in the workspace.
	 *
	 * @param fileStore
	 *            The <code>IFileStore</code> to test
	 * @return The workspace's <code>IFile</code> if it exists or
	 *         <code>null</code> if not
	 */
	private static IFile getWorkspaceFile(IFileStore fileStore) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = root.findFilesForLocationURI(fileStore.toURI());
		files = filterNonExistentFiles(files);
		if (files == null || files.length == 0)
			return null;

		// for now only return the first file
		return files[0];
	}

	/**
	 * Filter the incoming array of <code>IFile</code> elements by removing
	 * any that do not currently exist in the workspace.
	 *
	 * @param files
	 *            The array of <code>IFile</code> elements
	 * @return The filtered array
	 */
	private static IFile[] filterNonExistentFiles(IFile[] files) {
		if (files == null)
			return null;

		int length = files.length;
		ArrayList<IFile> existentFiles = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			if (files[i].exists())
				existentFiles.add(files[i]);
		}
		return existentFiles.toArray(new IFile[existentFiles.size()]);
	}

	/**
	 * Opens an editor on the given object.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * </p>
	 *
	 * @param page     the page in which the editor will be opened
	 * @param input    the editor input
	 * @param editorId the id of the editor extension to use
	 * @param activate if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if an external editor was opened
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String, boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page,
			IEditorInput input, String editorId, boolean activate)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(input, editorId, activate);
	}

	/**
	 * Opens an editor on the given file resource. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * </p>
	 *
	 * @param page     the page in which the editor will be opened
	 * @param input    the editor input
	 * @param activate if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if an external editor was opened
	 *         or if opening was canceled
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput,
	 *      String, boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			boolean activate) throws PartInitException {
		return openEditor(page, input, activate, true);
	}

	/**
	 * Opens an editor on the given file resource. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings if <code>determineContentType</code> is
	 * <code>true</code>.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * </p>
	 *
	 * @param page                 the page in which the editor will be opened
	 * @param input                the editor input
	 * @param activate             if <code>true</code> the editor will be activated
	 * @param determineContentType attempt to resolve the content type for this file
	 * @return an open editor or <code>null</code> if an external editor was opened
	 *         or if opening was canceled
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput,
	 *      String, boolean)
	 * @since 3.1
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			boolean activate, boolean determineContentType)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		IEditorDescriptor editorDesc;
		try {
			editorDesc = getEditorDescriptor(input, determineContentType, true);
		} catch (OperationCanceledException ex) {
			return null;
		}
		return page.openEditor(new FileEditorInput(input), editorDesc.getId(),
				activate);
	}

	/**
	 * Opens an editor on the given file resource. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened.
	 * </p>
	 *
	 * @param page  the page in which the editor will be opened
	 * @param input the editor input
	 * @return an open editor or <code>null</code> if an external editor was opened
	 *         or if opening was canceled
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		IEditorDescriptor editorDesc;
		try {
			editorDesc = getEditorDescriptor(input, true, true);
		} catch (OperationCanceledException ex) {
			return null;
		}
		return page.openEditor(new FileEditorInput(input), editorDesc.getId());
	}

	/**
	 * Opens an editor on the given file resource.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened.
	 * </p>
	 *
	 * @param page     the page in which the editor will be opened
	 * @param input    the editor input
	 * @param editorId the id of the editor extension to use
	 * @return an open editor or <code>null</code> if an external editor was opened
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			String editorId) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(new FileEditorInput(input), editorId);
	}

	/**
	 * Opens an editor on the given file resource.
	 * <p>
	 * If the page already has an editor open on the target object then that editor
	 * is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * </p>
	 *
	 * @param page     the page in which the editor will be opened
	 * @param input    the editor input
	 * @param editorId the id of the editor extension to use
	 * @param activate if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if an external editor was opened
	 * @exception PartInitException if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String, boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			String editorId, boolean activate) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(new FileEditorInput(input), editorId, activate);
	}

	/**
	 * Returns an editor descriptor appropriate for opening the given file
	 * resource.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This
	 * method will attempt to resolve the editor based on content-type bindings
	 * as well as traditional name/extension bindings.
	 * </p>
	 * <ol>
	 * <li>The <code>IResource</code> is consulted for a persistent property named
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
	 * <li>The workbench editor registry is consulted to determine if the
	 * default text editor is available.</li>
	 * </ol>
	 *
	 * @param file
	 *            the file
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @deprecated Since 3.12, use
	 *             {@link IDE#getEditorDescriptor(IFile, boolean, boolean)}
	 */
	@Deprecated
	public static IEditorDescriptor getEditorDescriptor(IFile file) throws PartInitException {
		return getEditorDescriptor(file, true);
	}

	/**
	 * Returns an editor descriptor appropriate for opening the given file
	 * resource.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This
	 * method will attempt to resolve the editor based on content-type bindings
	 * as well as traditional name/extension bindings if
	 * <code>determineContentType</code>is <code>true</code>.
	 * </p>
	 * <ol>
	 * <li>The <code>IResource</code> is consulted for a persistent property named
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
	 * <li>The workbench editor registry is consulted to determine if the
	 * default text editor is available.</li>
	 * </ol>
	 *
	 * @param file
	 *            the file
	 * @param determineContentType
	 *            query the content type system for the content type of the file
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @since 3.1
	 * @deprecated Since 3.12, use
	 *             {@link IDE#getEditorDescriptor(IFile, boolean, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static IEditorDescriptor getEditorDescriptor(IFile file, boolean determineContentType) throws PartInitException {

		if (file == null) {
			throw new IllegalArgumentException();
		}

		return getEditorDescriptor(file.getName(), PlatformUI.getWorkbench()
				.getEditorRegistry(), getDefaultEditor(file,
				determineContentType));
	}

	/**
	 * Returns an editor descriptor appropriate for opening the given file resource.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This method
	 * will attempt to resolve the editor based on content-type bindings as well as
	 * traditional name/extension bindings if <code>determineContentType</code>is
	 * <code>true</code>.
	 * </p>
	 * <ol>
	 * <li>The <code>IResource</code> is consulted for a persistent property named
	 * <code>IDE.EDITOR_KEY</code> containing the preferred editor id to be
	 * used.</li>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of the
	 * editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The preferred {@link IUnassociatedEditorStrategy} is consulted.</li>
	 * <li>The {@link SystemEditorOrTextEditorStrategy} is consulted, whose behavior
	 * is
	 * <ol>
	 * <li>The operating system is consulted to determine if an in-place component
	 * editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor is
	 * available.</li>
	 * <li>The workbench editor registry is consulted to determine if the default
	 * text editor is available.</li>
	 * </ol>
	 * </li>
	 * </ol>
	 *
	 * @param file                 the file
	 * @param determineContentType query the content type system for the content
	 *                             type of the file
	 * @param allowInteractive     whether we allow user interactions
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException          if no editor can be found
	 * @throws OperationCanceledException in case descriptor lookup was canceled by
	 *                                    the user
	 * @since 3.12
	 */
	public static IEditorDescriptor getEditorDescriptor(IFile file, boolean determineContentType, boolean allowInteractive)
			throws PartInitException, OperationCanceledException {

		if (file == null) {
			throw new IllegalArgumentException();
		}

		return getEditorDescriptor(file.getName(), PlatformUI.getWorkbench().getEditorRegistry(),
				getDefaultEditor(file, determineContentType), allowInteractive);
	}

	/**
	 * Returns an editor descriptor appropriate for opening the given file store.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This method
	 * will attempt to resolve the editor based on content-type bindings as well as
	 * traditional name/extension bindings.
	 * </p>
	 * <ol>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of the
	 * editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The preferred {@link IUnassociatedEditorStrategy} is consulted.</li>
	 * <li>The {@link SystemEditorOrTextEditorStrategy} is consulted, whose behavior
	 * is
	 * <ol>
	 * <li>The operating system is consulted to determine if an in-place component
	 * editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor is
	 * available.</li>
	 * <li>The workbench editor registry is consulted to determine if the default
	 * text editor is available.</li>
	 * </ol>
	 * </li>
	 * </ol>
	 *
	 * @param fileStore        the file store
	 * @param allowInteractive Whether user interactions are allowed
	 * @return editor descriptor of an editor, appropriate for opening the file
	 * @throws PartInitException if no editor can be found
	 * @since 3.16
	 */
	public static IEditorDescriptor getEditorDescriptorForFileStore(IFileStore fileStore, boolean allowInteractive)
			throws PartInitException {
		String name = fileStore.fetchInfo().getName();
		if (name == null) {
			throw new IllegalArgumentException();
		}

		IContentType contentType = null;
		try (InputStream is = fileStore.openInputStream(EFS.NONE, null)) {
			contentType = Platform.getContentTypeManager().findContentTypeFor(is, name);
		} catch (CoreException | IOException ex) {
			// continue without content type
		}

		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();

		IEditorDescriptor defaultEditor = editorReg.getDefaultEditor(name, contentType);
		defaultEditor = overrideDefaultEditorAssociation(new FileStoreEditorInput(fileStore), contentType,
				defaultEditor);
		return getEditorDescriptor(name, editorReg, defaultEditor, allowInteractive);
	}

	/**
	 * Applies the <code>org.eclipse.ui.ide.editorAssociationOverride</code> extensions to the given
	 * input.
	 * <p>
	 * <strong>Note:</strong> It is recommended to get the descriptor for the default editor by
	 * calling {@link #getDefaultEditor(IFile, boolean)}. This method here should only be used if
	 * this is not possible for whatever reason.
	 * </p>
	 *
	 * @param editorInput the editor input for the editor
	 * @param contentType the content type of the input or <code>null</code> if not available
	 * @param editorDescriptor the current association for the given input or <code>null</code> if
	 *            none
	 * @return the editor descriptor to be used for the given input or <code>null</code> if none.
	 *         Can be <code>editorDescriptor</code>.
	 * @see IEditorAssociationOverride#overrideDefaultEditor(IEditorInput, IContentType,
	 *      IEditorDescriptor)
	 * @since 3.8
	 */
	public static IEditorDescriptor overrideDefaultEditorAssociation(IEditorInput editorInput, IContentType contentType, IEditorDescriptor editorDescriptor) {
		for (IEditorAssociationOverride override : getEditorAssociationOverrides()) {
			editorDescriptor = override.overrideDefaultEditor(editorInput, contentType, editorDescriptor);
		}
		return editorDescriptor;
	}

	/**
	 * Applies the <code>org.eclipse.ui.ide.editorAssociationOverride</code> extensions to the given
	 * input.
	 *
	 * @param fileName the name of the file for which to choose the editor
	 * @param contentType the content type of the input or <code>null</code> if not available
	 * @param editorDescriptor the current association for the given input or <code>null</code> if
	 *            none
	 * @return the editor descriptor to be used for the given input or <code>null</code> if none.
	 *         Can be <code>editorDescriptor</code>.
	 * @see IEditorAssociationOverride#overrideDefaultEditor(String, IContentType,
	 *      IEditorDescriptor)
	 * @since 3.8
	 */
	private static IEditorDescriptor overrideDefaultEditorAssociation(String fileName, IContentType contentType, IEditorDescriptor editorDescriptor) {
		for (IEditorAssociationOverride override : getEditorAssociationOverrides()) {
			editorDescriptor = override.overrideDefaultEditor(fileName, contentType, editorDescriptor);
		}
		return editorDescriptor;
	}

	/**
	 * Applies the <code>org.eclipse.ui.ide.editorAssociationOverride</code> extensions to the given
	 * input.
	 *
	 * @param editorInput the editor input for the editor
	 * @param contentType the content type of the input or <code>null</code> if not available
	 * @param editorDescriptors the current association for the given input
	 * @return the editor descriptors to be used for the given input - can be
	 *         <code>editorDescriptors</code>. The order is not relevant.
	 * @see IEditorAssociationOverride#overrideEditors(IEditorInput, IContentType,
	 *      IEditorDescriptor[])
	 * @since 3.8
	 */
	public static IEditorDescriptor[] overrideEditorAssociations(IEditorInput editorInput, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		for (IEditorAssociationOverride override : getEditorAssociationOverrides()) {
			editorDescriptors = override.overrideEditors(editorInput, contentType, editorDescriptors);
		}
		return removeNullEntries(editorDescriptors);
	}

	/**
	 * Applies the <code>org.eclipse.ui.ide.editorAssociationOverride</code> extensions to the given
	 * input.
	 *
	 * @param fileName the name of the file for which to choose the editor
	 * @param contentType the content type of the input or <code>null</code> if not available
	 * @param editorDescriptors the current association for the given input
	 * @return the editor descriptors to be used for the given input - can be
	 *         <code>editorDescriptors</code>. The order is not relevant.
	 * @see IEditorAssociationOverride#overrideEditors(IEditorInput, IContentType,
	 *      IEditorDescriptor[])
	 * @since 3.8
	 */
	public static IEditorDescriptor[] overrideEditorAssociations(String fileName, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		for (IEditorAssociationOverride override : getEditorAssociationOverrides()) {
			editorDescriptors = override.overrideEditors(fileName, contentType, editorDescriptors);
		}
		return removeNullEntries(editorDescriptors);
	}

	private static IEditorDescriptor[] removeNullEntries(IEditorDescriptor[] editorDescriptors) {
		boolean nullDescriptorFound = false;
		for (IEditorDescriptor editorDesc : editorDescriptors) {
			if (editorDesc == null) {
				nullDescriptorFound = true;
				break;
			}
		}
		if (!nullDescriptorFound) {
			return editorDescriptors;
		}
		List<IEditorDescriptor> nonNullDescriptors = new ArrayList<>(editorDescriptors.length);
		for (IEditorDescriptor editorDesc : editorDescriptors) {
			if (editorDesc != null) {
				nonNullDescriptors.add(editorDesc);
			}
		}
		return nonNullDescriptors.toArray(new IEditorDescriptor[nonNullDescriptors.size()]);
	}

	/**
	 * Returns an editor descriptor appropriate for opening a file resource with
	 * the given name.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This
	 * method will attempt to infer content type from the file name.
	 * </p>
	 * <ol>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * <li>The workbench editor registry is consulted to determine if the
	 * default text editor is available.</li>
	 * </ol>
	 *
	 * @param name
	 *            the file name
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @since 3.1
	 * @deprecated Since 3.12, use
	 *             {@link IDE#getEditorDescriptor(String, boolean, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static IEditorDescriptor getEditorDescriptor(String name)
			throws PartInitException {
		return getEditorDescriptor(name, true);
	}

	/**
	 * Returns an editor descriptor appropriate for opening a file resource with
	 * the given name.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This
	 * method will attempt to infer the content type of the file if
	 * <code>inferContentType</code> is <code>true</code>.
	 * </p>
	 * <ol>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * <li>The workbench editor registry is consulted to determine if the
	 * default text editor is available.</li>
	 * </ol>
	 *
	 * @param name
	 *            the file name
	 * @param inferContentType
	 *            attempt to infer the content type from the file name if this
	 *            is <code>true</code>
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @since 3.1
	 * @deprecated Since 3.12, use
	 *             {@link IDE#getEditorDescriptor(String, boolean, boolean)}
	 *             instead.
	 */
	@Deprecated
	public static IEditorDescriptor getEditorDescriptor(String name, boolean inferContentType)
			throws PartInitException {

		if (name == null) {
			throw new IllegalArgumentException();
		}

		IContentType contentType = inferContentType ? Platform
				.getContentTypeManager().findContentTypeFor(name) : null;
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();

		IEditorDescriptor defaultEditor = editorReg.getDefaultEditor(name, contentType);
		defaultEditor = getEditorDescriptor(name, editorReg, defaultEditor);
		return overrideDefaultEditorAssociation(name, contentType, defaultEditor);
	}

	/**
	 * Returns an editor descriptor appropriate for opening a file resource with the
	 * given name.
	 * <p>
	 * The editor descriptor is determined using a multi-step process. This method
	 * will attempt to infer the content type of the file if
	 * <code>inferContentType</code> is <code>true</code>.
	 * </p>
	 * <ol>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of the
	 * editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The preferred {@link IUnassociatedEditorStrategy} is consulted.</li>
	 * <li>The {@link SystemEditorOrTextEditorStrategy} is consulted, whose behavior
	 * is
	 * <ol>
	 * <li>The operating system is consulted to determine if an in-place component
	 * editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor is
	 * available.</li>
	 * <li>The workbench editor registry is consulted to determine if the default
	 * text editor is available.</li>
	 * </ol>
	 * </li>
	 * </ol>
	 *
	 * @param name             the file name
	 * @param inferContentType attempt to infer the content type from the file name
	 *                         if this is <code>true</code>
	 * @param allowInteractive whether we allow user interactions.
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException          if no editor can be found
	 * @throws OperationCanceledException in case descriptor lookup was canceled by
	 *                                    the user
	 * @since 3.12
	 */
	public static IEditorDescriptor getEditorDescriptor(String name, boolean inferContentType, boolean allowInteractive)
			throws PartInitException, OperationCanceledException {

		if (name == null) {
			throw new IllegalArgumentException();
		}

		IContentType contentType = inferContentType ? Platform
				.getContentTypeManager().findContentTypeFor(name) : null;
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();

		IEditorDescriptor defaultEditor = editorReg.getDefaultEditor(name, contentType);
		defaultEditor = getEditorDescriptor(name, editorReg, defaultEditor, allowInteractive);
		return overrideDefaultEditorAssociation(name, contentType, defaultEditor);
	}

	/**
	 * Get the editor descriptor for a given name using the editorDescriptor
	 * passed in as a default as a starting point. It may delegate computation
	 * to the active {@link IUnassociatedEditorStrategy}.
	 *
	 * @param name
	 *            The name of the element to open.
	 * @param editorReg
	 *            The editor registry to do the lookups from.
	 * @param defaultDescriptor
	 *            IEditorDescriptor or <code>null</code>
	 * @param allowInteractive
	 *            Whether we ask selected {@link IUnassociatedEditorStrategy}, that
	 *            can be interactive.
	 * @return IEditorDescriptor
	 * @throws PartInitException
	 *             if no valid editor can be found
	 * @throws OperationCanceledException
	 *             in case descriptor lookup was canceled by the user
	 *
	 * @since 3.12
	 */
	private static IEditorDescriptor getEditorDescriptor(String name, IEditorRegistry editorReg,
			IEditorDescriptor defaultDescriptor, boolean allowInteractive)
					throws PartInitException, OperationCanceledException {

		if (defaultDescriptor != null) {
			return defaultDescriptor;
		}

		IUnassociatedEditorStrategy strategy = getUnassociatedEditorStrategy(allowInteractive);
		IEditorDescriptor editorDesc;
		try {
			editorDesc = strategy.getEditorDescriptor(name, editorReg);
		} catch (CoreException e) {
			throw new PartInitException(IDEWorkbenchMessages.IDE_noFileEditorFound, e);
		}

		// if no valid editor found, bail out
		if (editorDesc == null) {
			throw new PartInitException(
					IDEWorkbenchMessages.IDE_noFileEditorFound);
		}

		return editorDesc;
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
	 *
	 * @since 3.1
	 * @deprecated Since 3.12, use {@link IDE#getEditorDescriptor(String, boolean, boolean)} instead
	 */
	@Deprecated
	private static IEditorDescriptor getEditorDescriptor(String name,
			IEditorRegistry editorReg, IEditorDescriptor defaultDescriptor)
			throws PartInitException {

		if (defaultDescriptor != null) {
			return defaultDescriptor;
		}

		IEditorDescriptor editorDesc = defaultDescriptor;

		// next check the OS for in-place editor (OLE on Win32)
		if (editorReg.isSystemInPlaceEditorAvailable(name)) {
			editorDesc = editorReg
					.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		}

		// next check with the OS for an external editor
		if (editorDesc == null
				&& editorReg.isSystemExternalEditorAvailable(name)) {
			editorDesc = editorReg
					.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}

		// next lookup the default text editor
		if (editorDesc == null) {
			editorDesc = editorReg
					.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		}

		// if no valid editor found, bail out
		if (editorDesc == null) {
			throw new PartInitException(
					IDEWorkbenchMessages.IDE_noFileEditorFound);
		}

		return editorDesc;
	}

	/**
	 * @param allowInteractive
	 *            Whether interactive strategies are considered
	 * @return The strategy to use in order to open unknown file. Either as set
	 *         by preference, or a {@link SystemEditorOrTextEditorStrategy} if
	 *         none is explicitly configured. Never returns {@code null}.
	 */
	private static IUnassociatedEditorStrategy getUnassociatedEditorStrategy(boolean allowInteractive) {
		String preferedStrategy = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(UNASSOCIATED_EDITOR_STRATEGY_PREFERENCE_KEY);
		IUnassociatedEditorStrategy res = null;
		UnassociatedEditorStrategyRegistry registry = IDEWorkbenchPlugin.getDefault()
				.getUnassociatedEditorStrategyRegistry();
		if (allowInteractive || !registry.isInteractive(preferedStrategy)) {
			res = registry.getStrategy(preferedStrategy);
		}
		if (res == null) {
			res = new SystemEditorOrTextEditorStrategy();
		}
		return res;
	}

	/**
	 * Opens an editor on the file resource of the given marker.
	 * <p>
	 * If this page already has an editor open on the marker resource file that
	 * editor is brought to front; otherwise, a new editor is opened.The cursor
	 * and selection state of the editor are then updated from information
	 * recorded in the marker.
	 * </p>
	 * <p>
	 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute the
	 * attribute value will be used to determine the editor type to be opened.
	 * If not, the registered editor for the marker resource file will be used.
	 * </p>
	 *
	 * @param page
	 *            the workbench page to open the editor in
	 * @param marker
	 *            the marker to open
	 * @return an open editor or <code>null</code> not possible
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see #openEditor(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.core.resources.IMarker, boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IMarker marker)
			throws PartInitException {
		return openEditor(page, marker, true);
	}

	/**
	 * Opens an editor on the file resource of the given marker.
	 * <p>
	 * If this page already has an editor open on the marker resource file that
	 * editor is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated. The cursor
	 * and selection state of the editor are then updated from information
	 * recorded in the marker.
	 * </p>
	 * <p>
	 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute the
	 * attribute value will be used to determine the editor type to be opened.
	 * If not, the registered editor for the marker resource file will be used.
	 * </p>
	 *
	 * @param page
	 *            the workbench page to open the editor in
	 * @param marker
	 *            the marker to open
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if not possible or if opening
	 *         was canceled
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IMarker marker,
			boolean activate) throws PartInitException {
		// sanity checks
		if (page == null || marker == null) {
			throw new IllegalArgumentException();
		}

		// get the marker resource file
		if (!(marker.getResource() instanceof IFile)) {
			IDEWorkbenchPlugin
					.log("Open editor on marker failed; marker resource not an IFile"); //$NON-NLS-1$
			return null;
		}
		IFile file = (IFile) marker.getResource();

		// get the preferred editor id from the marker
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();
		IEditorDescriptor editorDesc = null;
		try {
			String editorID = (String) marker.getAttribute(EDITOR_ID_ATTR);
			if (editorID != null) {
				editorDesc = editorReg.findEditor(editorID);
			}
		} catch (CoreException e) {
			// ignore this
		}

		// open the editor on the marker resource file
		IEditorPart editor = null;
		if (editorDesc == null) {
			editor = openEditor(page, file, activate);
		} else {
			editor = page.openEditor(new FileEditorInput(file), editorDesc
					.getId(), activate, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
		}

		// get the editor to update its position based on the marker
		if (editor != null) {
			gotoMarker(editor, marker);
		}

		return editor;
	}

	/**
	 * Opens an editor on the given IFileStore object.
	 * <p>
	 * Unlike the other <code>openEditor</code> methods, this one
	 * can be used to open files that reside outside the workspace
	 * resource set.
	 * </p>
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * </p>
	 *
	 * @param page
	 *            the page in which the editor will be opened
	 * @param fileStore
	 *            the IFileStore representing the file to open
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened or if opening was canceled
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 * @since 3.3
	 */
	public static IEditorPart openEditorOnFileStore(IWorkbenchPage page, IFileStore fileStore) throws PartInitException {
		//sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		IEditorInput input = getEditorInput(fileStore);
		String editorId;
		try {
			editorId = getEditorDescriptorForFileStore(fileStore, true).getId();
		} catch (OperationCanceledException ex) {
			return null;
		}

		// open the editor on the file
		return page.openEditor(input, editorId);
	}

	/**
	 * Opens an internal editor on the given IFileStore object.
	 * <p>
	 * Unlike the other <code>openEditor</code> methods, this one can be used to
	 * open files that reside outside the workspace resource set.
	 * </p>
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * </p>
	 *
	 * @param page
	 *            the page in which the editor will be opened
	 * @param fileStore
	 *            the IFileStore representing the file to open
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if no internal editor can be found or if the editor could
	 *                not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 * @since 3.6
	 */
	public static IEditorPart openInternalEditorOnFileStore(IWorkbenchPage page, IFileStore fileStore) throws PartInitException {
		if (page == null)
			throw new IllegalArgumentException();
		if (fileStore == null)
			throw new IllegalArgumentException();

		IEditorInput input = getEditorInput(fileStore);
		String name = fileStore.fetchInfo().getName();
		if (name == null)
			throw new IllegalArgumentException();

		IContentType[] contentTypes = null;
		InputStream is = null;
		try {
			is = fileStore.openInputStream(EFS.NONE, null);
			contentTypes = Platform.getContentTypeManager().findContentTypesFor(is, name);
		} catch (CoreException | IOException ex) {
			// it's OK, ignore
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// nothing good can be done here, ignore
				}
			}
		}

		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		if (contentTypes != null) {
			for (IContentType contentType : contentTypes) {
				IEditorDescriptor editorDesc = editorReg.getDefaultEditor(name, contentType);
				editorDesc = overrideDefaultEditorAssociation(input, contentType, editorDesc);
				if ((editorDesc != null) && (editorDesc.isInternal()))
					return page.openEditor(input, editorDesc.getId());
			}
		}

		// no content types are available, use file name associations
		IEditorDescriptor[] editors = editorReg.getEditors(name);
		if (editors != null) {
			editors = overrideEditorAssociations(input, null, editors);
			for (IEditorDescriptor editor : editors) {
				if ((editor != null) && (editor.isInternal()))
					return page.openEditor(input, editor.getId());
			}
		}

		// fallback to the default text editor
		IEditorDescriptor textEditor = editorReg.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		if (textEditor == null)
			throw new PartInitException(IDEWorkbenchMessages.IDE_noFileEditorFound);
		return page.openEditor(input, textEditor.getId());
	}

	/**
	 * Save all dirty editors in the workbench whose editor input is a child
	 * resource of one of the <code>IResource</code>'s provided. Opens a
	 * dialog to prompt the user if <code>confirm</code> is true. Return true
	 * if successful. Return false if the user has canceled the command.
	 *
	 * @since 3.0
	 *
	 * @param resourceRoots the resource roots under which editor input should
	 *            be saved, other will be left dirty
	 * @param confirm <code>true</code> to ask the user before saving unsaved
	 *            changes (recommended), and <code>false</code> to save
	 *            unsaved changes without asking
	 * @return <code>true</code> if the command succeeded, and
	 *         <code>false</code> if the operation was canceled by the user or
	 *         an error occurred while saving
	 */
	public static boolean saveAllEditors(final IResource[] resourceRoots,
			final boolean confirm) {

		if (resourceRoots.length == 0) {
			return true;
		}

		final boolean[] result = new boolean[] { true };
		SafeRunner.run(new SafeRunnable(IDEWorkbenchMessages.ErrorOnSaveAll) {
			@Override
			public void run() {
				IWorkbenchWindow w = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				if (w == null) {
					IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
							.getWorkbenchWindows();
					if (windows.length > 0)
						w = windows[0];
				}
				if (w != null) {
					result[0] = PlatformUI.getWorkbench().saveAll(w, w,
							new ResourceSaveableFilter(resourceRoots), confirm);
				}
			}
		});
		return result[0];
	}

	/**
	 * Sets the default editor id for a given file. This value will be used to
	 * determine the default editor descriptor for the file in future calls to
	 * <code>getDefaultEditor(IFile)</code>.
	 *
	 * @param file
	 *            the file
	 * @param editorID
	 *            the editor id
	 */
	public static void setDefaultEditor(IFile file, String editorID) {
		try {
			file.setPersistentProperty(EDITOR_KEY, editorID);
		} catch (CoreException e) {
			// do nothing
		}
	}

	/**
	 * Returns the default editor for a given file. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings.
	 * <p>
	 * A default editor id may be registered for a specific file using
	 * <code>setDefaultEditor</code>. If the given file has a registered
	 * default editor id the default editor will derived from it. If not, the
	 * default editor is determined by taking the file name for the file and
	 * obtaining the default editor for that name.
	 * </p>
	 *
	 * @param file
	 *            the file
	 * @return the descriptor of the default editor, or <code>null</code> if
	 *         not found
	 */
	public static IEditorDescriptor getDefaultEditor(IFile file) {
		return getDefaultEditor(file, true);
	}

	/**
	 * Returns the default editor for a given file. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings if <code>determineContentType</code> is
	 * <code>true</code>.
	 * <p>
	 * A default editor id may be registered for a specific file using
	 * <code>setDefaultEditor</code>. If the given file has a registered
	 * default editor id the default editor will derived from it. If not, the
	 * default editor is determined by taking the file name for the file and
	 * obtaining the default editor for that name.
	 * </p>
	 *
	 * @param file
	 *            the file
	 * @param determineContentType
	 *            determine the content type for the given file
	 * @return the descriptor of the default editor, or <code>null</code> if
	 *         not found
	 * @since 3.1
	 */
	public static IEditorDescriptor getDefaultEditor(IFile file,
			boolean determineContentType) {
		// Try file specific editor.
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();

		IContentType contentType = null;
		if (determineContentType) {
			contentType = getContentType(file);
		}

		try {
			String editorID = file.getPersistentProperty(EDITOR_KEY);
			if (editorID != null) {
				IEditorDescriptor desc = editorReg.findEditor(editorID);
				if (desc != null) {
					return overrideDefaultEditorAssociation(new FileEditorInput(file), contentType, desc);
				}
			}
		} catch (CoreException e) {
			// do nothing
		}

		// Try lookup with filename
		IEditorDescriptor desc = editorReg.getDefaultEditor(file.getName(), contentType);
		return overrideDefaultEditorAssociation(new FileEditorInput(file), contentType, desc);
	}

	/**
	 * Extracts and returns the <code>IResource</code>s in the given
	 * selection or the resource objects they adapts to.
	 *
	 * @param originalSelection
	 *            the original selection, possibly empty
	 * @return list of resources (element type: <code>IResource</code>),
	 *         possibly empty
	 */
	public static List<IResource> computeSelectedResources(IStructuredSelection originalSelection) {
		List<IResource> resources = null;
		for (Object next : originalSelection) {
			IResource resource = Adapters.adapt(next, IResource.class);
			if (resource != null) {
				if (resources == null) {
					// lazy init to avoid creating empty lists
					// assume selection contains mostly resources most times
					resources = new ArrayList<>(originalSelection.size());
				}
				resources.add(resource);
			}
		}
		if (resources == null) {
			return Collections.emptyList();
		}
		return resources;

	}

	/**
	 * Return the content type for the given file.
	 *
	 * @param file
	 *            the file to test
	 * @return the content type, or <code>null</code> if it cannot be
	 *         determined.
	 * @since 3.1
	 */
	public static IContentType getContentType(IFile file) {
		try {
			UIStats.start(UIStats.CONTENT_TYPE_LOOKUP, file.getName());
			IContentDescription contentDescription = file
					.getContentDescription();
			if (contentDescription == null) {
				return null;
			}
			return contentDescription.getContentType();
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
				// Determine the content type from the file name.
				return Platform.getContentTypeManager()
							.findContentTypeFor(file.getName());
			}
			return null;
		} finally {
			UIStats.end(UIStats.CONTENT_TYPE_LOOKUP, file, file.getName());
		}
	}

	/**
	 * Guess at the content type of the given file based on the filename.
	 *
	 * @param file
	 *            the file to test
	 * @return the content type, or <code>null</code> if it cannot be
	 *         determined.
	 * @since 3.2
	 */
	public static IContentType guessContentType(IFile file) {
		String fileName = file.getName();
		try {
			UIStats.start(UIStats.CONTENT_TYPE_LOOKUP, fileName);
			IContentTypeMatcher matcher = file.getProject()
					.getContentTypeMatcher();
			return matcher.findContentTypeFor(fileName);
		} catch (CoreException e) {
			return null;
		} finally {
			UIStats.end(UIStats.CONTENT_TYPE_LOOKUP, file, fileName);
		}
	}

	/**
	 * Prompt the user to inform them of the possible side effects of an
	 * operation on resources. Do not prompt for side effects from ignored model
	 * providers. A model provider can be ignored if it is the client calling
	 * this API. Any message from the provided model provider id or any model
	 * providers it extends will be ignored.
	 *
	 * @param shell
	 *            the shell to parent the prompt dialog
	 * @param title
	 *            the title of the dialog
	 * @param message
	 *            the message for the dialog
	 * @param delta
	 *            a delta built using an
	 *            {@link IResourceChangeDescriptionFactory}
	 * @param ignoreModelProviderIds
	 *            model providers to be ignored
	 * @param syncExec
	 *            prompt in a sync exec (required when called from a non-UI
	 *            thread)
	 * @return whether the user chose to continue
	 * @since 3.2
	 */
	public static boolean promptToConfirm(final Shell shell,
			final String title, String message, IResourceDelta delta,
			String[] ignoreModelProviderIds, boolean syncExec) {
		IStatus status = ResourceChangeValidator.getValidator().validateChange(
				delta, null);
		if (status.isOK()) {
			return true;
		}
		final IStatus displayStatus;
		if (status.isMultiStatus()) {
			List<IStatus> result = new ArrayList<>();
			IStatus[] children = status.getChildren();
			for (IStatus child : children) {
				if (!isIgnoredStatus(child, ignoreModelProviderIds)) {
					result.add(child);
				}
			}
			if (result.isEmpty()) {
				return true;
			}
			if (result.size() == 1) {
				displayStatus = result.get(0);
			} else {
				displayStatus = new MultiStatus(status.getPlugin(), status.getCode(),
						result.toArray(new IStatus[result.size()]), status.getMessage(), status.getException());
			}
		} else {
			if (isIgnoredStatus(status, ignoreModelProviderIds)) {
				return true;
			}
			displayStatus = status;
		}

		if (message == null) {
			message = IDEWorkbenchMessages.IDE_sideEffectWarning;
		}
		final String dialogMessage = NLS.bind(
				IDEWorkbenchMessages.IDE_areYouSure, message);

		final boolean[] result = new boolean[] { false };
		Runnable runnable = () -> {
			ErrorDialog dialog = new ErrorDialog(shell, title,
					dialogMessage, displayStatus, IStatus.ERROR
							| IStatus.WARNING | IStatus.INFO) {
				@Override
				protected void createButtonsForButtonBar(Composite parent) {
					createButton(parent, IDialogConstants.YES_ID,
							IDialogConstants.YES_LABEL, false);
					createButton(parent, IDialogConstants.NO_ID,
							IDialogConstants.NO_LABEL, true);
					createDetailsButton(parent);
				}

				@Override
				protected void buttonPressed(int id) {
					if (id == IDialogConstants.YES_ID) {
						super.buttonPressed(IDialogConstants.OK_ID);
					} else if (id == IDialogConstants.NO_ID) {
						super.buttonPressed(IDialogConstants.CANCEL_ID);
					}
					super.buttonPressed(id);
				}
				@Override
				protected int getShellStyle() {
					return super.getShellStyle() | SWT.SHEET;
				}
			};
			int code = dialog.open();
			result[0] = code == 0;
		};
		if (syncExec) {
			shell.getDisplay().syncExec(runnable);
		} else {
			runnable.run();
		}
		return result[0];
	}

	/**
	 * Register workbench adapters programmatically. This is necessary to enable
	 * certain types of content in the explorers.
	 * <p>
	 * <b>Note:</b> this method should only be called once, in your
	 * application's WorkbenchAdvisor#initialize(IWorkbenchConfigurer) method.
	 * </p>
	 *
	 * @since 3.5
	 */
	public static void registerAdapters() {
		IAdapterManager manager = Platform.getAdapterManager();
		IAdapterFactory factory = new WorkbenchAdapterFactory();
		manager.registerAdapters(factory, IWorkspace.class);
		manager.registerAdapters(factory, IWorkspaceRoot.class);
		manager.registerAdapters(factory, IProject.class);
		manager.registerAdapters(factory, IFolder.class);
		manager.registerAdapters(factory, IFile.class);
		manager.registerAdapters(factory, IMarker.class);

		// properties adapters
		IAdapterFactory paFactory = new StandardPropertiesAdapterFactory();
		manager.registerAdapters(paFactory, IWorkspace.class);
		manager.registerAdapters(paFactory, IWorkspaceRoot.class);
		manager.registerAdapters(paFactory, IProject.class);
		manager.registerAdapters(paFactory, IFolder.class);
		manager.registerAdapters(paFactory, IFile.class);
		manager.registerAdapters(paFactory, IMarker.class);
		manager.registerAdapters(paFactory, IEditorPart.class);
	}

	private static boolean isIgnoredStatus(IStatus status,
			String[] ignoreModelProviderIds) {
		if (ignoreModelProviderIds == null) {
			return false;
		}
		if (status instanceof ModelStatus) {
			ModelStatus ms = (ModelStatus) status;
			for (String id : ignoreModelProviderIds) {
				if (ms.getModelProviderId().equals(id)) {
					return true;
				}
				IModelProviderDescriptor desc = ModelProvider
						.getModelProviderDescriptor(id);
				String[] extended = desc.getExtendedModels();
				if (isIgnoredStatus(status, extended)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Opens editors on given file resources.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened. The editor created
	 * for the first input will be activated.
	 * </p>
	 * @param page the page in which the editor will be opened
	 * @param inputs the inputs for the editors
	 * @return references to the editors opened; the corresponding editors might not be materialized
	 * @exception MultiPartInitException if at least one of the editors could not be initialized
	 * @since 3.5
	 */
	public static IEditorReference[] openEditors(IWorkbenchPage page, IFile[] inputs) throws MultiPartInitException {
		if ((page == null) || (inputs == null))
			throw new IllegalArgumentException();

		String[] editorDescriptions = new String[inputs.length];
		IEditorInput[] editorInputs = new IEditorInput[inputs.length];
		for(int i = 0 ; i < inputs.length; i++) {
			editorInputs[i] = new FileEditorInput(inputs[i]);
			try {
				editorDescriptions[i] = getEditorDescriptor(inputs[i], true, true).getId();
			} catch (PartInitException e) {
				PartInitException[] exceptions = new PartInitException[inputs.length];
				exceptions[i] = e;
				throw new MultiPartInitException(new IWorkbenchPartReference[inputs.length], exceptions);
			}
		}
		return page.openEditors(editorInputs, editorDescriptions, IWorkbenchPage.MATCH_INPUT);
	}

	private static IEditorAssociationOverride[] getEditorAssociationOverrides() {
		if (editorAssociationOverrides == null) {
			EditorAssociationOverrideDescriptor[] descriptors = EditorAssociationOverrideDescriptor.getContributedEditorAssociationOverrides();
			List<IEditorAssociationOverride> overrides = new ArrayList<>(descriptors.length);
			for (EditorAssociationOverrideDescriptor descriptor : descriptors) {
				try {
					IEditorAssociationOverride override = descriptor.createOverride();
					overrides.add(override);
				} catch (CoreException e) {
					IDEWorkbenchPlugin
							.log("Error while creating IEditorAssociationOverride from: " + descriptor.getId(), e); //$NON-NLS-1$
				}
			}
			editorAssociationOverrides = overrides.toArray(new IEditorAssociationOverride[overrides.size()]);
		}
		return editorAssociationOverrides;
	}
}
