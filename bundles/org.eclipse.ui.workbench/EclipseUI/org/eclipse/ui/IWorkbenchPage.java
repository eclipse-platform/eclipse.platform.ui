/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A workbench page consists of an arrangement of views and editors intended to
 * be presented together to the user in a single workbench window.
 * <p>
 * A page can contain 0 or more views and 0 or more editors. These views and
 * editors are contained wholly within the page and are not shared with other
 * pages. The layout and visible action set for the page is defined by a
 * perspective.
 * <p>
 * The number of views and editors within a page is restricted to simplify part
 * management for the user. In particular:
 * </p>
 * <ul>
 * <li>Unless a view explicitly allows for multiple instances in its plug-in
 * declaration there will be only one instance in a given workbench page.</li>
 * <li>Only one editor can exist for each editor input within a page.</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPerspectiveDescriptor
 * @see IEditorPart
 * @see IViewPart
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchPage extends IPartService, ISelectionService {
	/**
	 * An optional attribute within a workspace marker (<code>IMarker</code>) which
	 * identifies the preferred editor type to be opened when
	 * <code>openEditor</code> is called.
	 *
	 * @see #openEditor(IEditorInput, String)
	 * @see #openEditor(IEditorInput, String, boolean)
	 * @deprecated in 3.0 since the notion of markers this is not generally
	 *             applicable. Use the IDE-specific constant
	 *             <code>IDE.EDITOR_ID_ATTR</code>.
	 */
	@Deprecated
	String EDITOR_ID_ATTR = "org.eclipse.ui.editorID"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective is reset to its original state.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_RESET = "reset"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective has completed a reset to its original
	 * state.
	 *
	 * @since 3.0
	 * @see IPerspectiveListener
	 */
	String CHANGE_RESET_COMPLETE = "resetComplete"; //$NON-NLS-1$

	/**
	 * Change event id when one or more views are shown in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_VIEW_SHOW = "viewShow"; //$NON-NLS-1$

	/**
	 * Change event id when one or more views are hidden in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_VIEW_HIDE = "viewHide"; //$NON-NLS-1$

	/**
	 * Change event id when one or more editors are opened in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_EDITOR_OPEN = "editorOpen"; //$NON-NLS-1$

	/**
	 * Change event id when one or more editors are closed in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_EDITOR_CLOSE = "editorClose"; //$NON-NLS-1$

	/**
	 * Change event id when the editor area is shown in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_EDITOR_AREA_SHOW = "editorAreaShow"; //$NON-NLS-1$

	/**
	 * Change event id when the editor area is hidden in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_EDITOR_AREA_HIDE = "editorAreaHide"; //$NON-NLS-1$

	/**
	 * Change event id when an action set is shown in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_ACTION_SET_SHOW = "actionSetShow"; //$NON-NLS-1$

	/**
	 * Change event id when an action set is hidden in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_ACTION_SET_HIDE = "actionSetHide"; //$NON-NLS-1$

	/**
	 * Change event id when a fast view is added in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_FAST_VIEW_ADD = "fastViewAdd"; //$NON-NLS-1$

	/**
	 * Change event id when a fast view is removed in a perspective.
	 *
	 * @see IPerspectiveListener
	 */
	String CHANGE_FAST_VIEW_REMOVE = "fastViewRemove"; //$NON-NLS-1$

	/**
	 * Change event id when the page working set was replaced
	 *
	 * @see IPropertyChangeListener
	 */
	String CHANGE_WORKING_SET_REPLACE = "workingSetReplace"; //$NON-NLS-1$

	/**
	 * Change event id when the page working set list was replaced
	 *
	 * @see IPropertyChangeListener
	 * @since 3.2
	 */
	String CHANGE_WORKING_SETS_REPLACE = "workingSetsReplace"; //$NON-NLS-1$

	/**
	 * Show view mode that indicates the view should be made visible and activated.
	 * Use of this mode has the same effect as calling {@link #showView(String)}.
	 *
	 * @since 3.0
	 */
	int VIEW_ACTIVATE = 1;

	/**
	 * Show view mode that indicates the view should be made visible. If the view is
	 * opened in the container that contains the active view then this has the same
	 * effect as <code>VIEW_CREATE</code>.
	 *
	 * @since 3.0
	 */
	int VIEW_VISIBLE = 2;

	/**
	 * Show view mode that indicates the view should be made created but not
	 * necessarily be made visible. It will only be made visible in the event that
	 * it is opened in its own container. In other words, only if it is not stacked
	 * with another view.
	 *
	 * @since 3.0
	 */
	int VIEW_CREATE = 3;

	/**
	 * Editor opening match mode specifying that no matching against existing
	 * editors should be done.
	 *
	 * @since 3.2
	 */
	int MATCH_NONE = 0;

	/**
	 * Editor opening match mode specifying that the editor input should be
	 * considered when matching against existing editors.
	 *
	 * @since 3.2
	 */
	int MATCH_INPUT = 1;

	/**
	 * Editor opening match mode specifying that the editor id should be considered
	 * when matching against existing editors.
	 *
	 * @since 3.2
	 */
	int MATCH_ID = 2;

	/**
	 * Editor opening match mode specifying that the editor selection strategy
	 * should ignore file size
	 *
	 * @since 3.125
	 */
	int MATCH_IGNORE_SIZE = 4;

	/**
	 * State of a view in a given page when the view stack is minimized.
	 *
	 * @since 3.2
	 */
	int STATE_MINIMIZED = 0;

	/**
	 * State of a view in a given page when the page is zoomed in on the view stack.
	 *
	 * @since 3.2
	 */
	int STATE_MAXIMIZED = 1;

	/**
	 * State of a view in a given page when the view stack is in it's normal state.
	 *
	 * @since 3.2
	 */
	int STATE_RESTORED = 2;

	/**
	 * Activates the given part. The part will be brought to the front and given
	 * focus. The part must belong to this page.
	 *
	 * @param part the part to activate
	 */
	void activate(IWorkbenchPart part);

	/**
	 * Adds a property change listener.
	 *
	 * @param listener the property change listener to add
	 * @since 2.0
	 * @deprecated client should register listeners on the instance of
	 *             {@link org.eclipse.ui.IWorkingSetManager} returned by
	 *             {@link org.eclipse.ui.IWorkbench#getWorkingSetManager()} instead.
	 */
	@Deprecated
	void addPropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Moves the given part forward in the Z order of this page so as to make it
	 * visible, without changing which part has focus. The part must belong to this
	 * page.
	 *
	 * @param part the part to bring forward
	 */
	void bringToTop(IWorkbenchPart part);

	/**
	 * Closes this workbench page. If this page is the active one, this honor is
	 * passed along to one of the window's other pages if possible.
	 * <p>
	 * If the page has an open editor with unsaved content, the user will be given
	 * the opportunity to save it.
	 * </p>
	 *
	 * @return <code>true</code> if the page was successfully closed, and
	 *         <code>false</code> if it is still open
	 */
	boolean close();

	/**
	 * Closes all of the editors belonging to this workbench page.
	 * <p>
	 * If the page has open editors with unsaved content and <code>save</code> is
	 * <code>true</code>, the user will be given the opportunity to save them.
	 * </p>
	 *
	 * @param save <code>true</code> to save the editor contents if required
	 *             (recommended), and <code>false</code> to discard any unsaved
	 *             changes
	 *
	 * @return <code>true</code> if all editors were successfully closed, and
	 *         <code>false</code> if at least one is still open
	 */
	boolean closeAllEditors(boolean save);

	/**
	 * Closes the given <code>Array</code> of editor references. The editors must
	 * belong to this workbench page.
	 * <p>
	 * If any of the editors have unsaved content and <code>save</code> is
	 * <code>true</code>, the user will be given the opportunity to save them.
	 * </p>
	 *
	 * @param editorRefs the editors to close
	 * @param save       <code>true</code> to save the editor contents if required
	 *                   (recommended), and <code>false</code> to discard any
	 *                   unsaved changes
	 * @return <code>true</code> if the editors were successfully closed, and
	 *         <code>false</code> if the editors are still open
	 * @since 3.0
	 */
	boolean closeEditors(IEditorReference[] editorRefs, boolean save);

	/**
	 * Closes the given editor. The editor must belong to this workbench page.
	 * <p>
	 * If the editor has unsaved content and <code>save</code> is <code>true</code>,
	 * the user will be given the opportunity to save it.
	 * </p>
	 *
	 * @param editor the editor to close
	 * @param save   <code>true</code> to save the editor contents if required
	 *               (recommended), and <code>false</code> to discard any unsaved
	 *               changes
	 * @return <code>true</code> if the editor was successfully closed, and
	 *         <code>false</code> if the editor is still open
	 */
	boolean closeEditor(IEditorPart editor, boolean save);

	/**
	 * Returns the view in this page with the specified id. There is at most one
	 * view in the page with the specified id.
	 *
	 * @param viewId the id of the view extension to use
	 * @return the view, or <code>null</code> if none is found
	 */
	IViewPart findView(String viewId);

	/**
	 * Returns the view reference with the specified id.
	 *
	 * @param viewId the id of the view extension to use
	 * @return the view reference, or <code>null</code> if none is found
	 * @since 3.0
	 */
	IViewReference findViewReference(String viewId);

	/**
	 * Returns the view reference with the specified id and secondary id.
	 *
	 * @param viewId      the id of the view extension to use
	 * @param secondaryId the secondary id to use, or <code>null</code> for no
	 *                    secondary id
	 * @return the view reference, or <code>null</code> if none is found
	 * @since 3.0
	 */
	IViewReference findViewReference(String viewId, String secondaryId);

	/**
	 * Returns the active editor open in this page.
	 * <p>
	 * This is the visible editor on the page, or, if there is more than one visible
	 * editor, this is the one most recently brought to top.
	 * </p>
	 *
	 * @return the active editor, or <code>null</code> if no editor is active
	 */
	IEditorPart getActiveEditor();

	/**
	 * Returns the editor with the specified input. Returns null if there is no
	 * opened editor with that input.
	 *
	 * @param input the editor input
	 * @return an editor with input equals to <code>input</code>
	 */
	IEditorPart findEditor(IEditorInput input);

	/**
	 * Returns an array of editor references that match the given input and/or
	 * editor id, as specified by the given match flags. Returns an empty array if
	 * there are no matching editors, or if matchFlags is MATCH_NONE.
	 *
	 * @param input      the editor input, or <code>null</code> if MATCH_INPUT is
	 *                   not specified in matchFlags
	 * @param editorId   the editor id, or <code>null</code> if MATCH_ID is not
	 *                   specified in matchFlags
	 * @param matchFlags a bit mask consisting of zero or more of the MATCH_*
	 *                   constants OR-ed together
	 * @return the references for the matching editors
	 *
	 * @see #MATCH_NONE
	 * @see #MATCH_INPUT
	 * @see #MATCH_ID
	 * @since 3.2
	 */
	IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags);

	/**
	 * Returns a list of the editors open in this page.
	 * <p>
	 * Note that each page has its own editors; editors are never shared between
	 * pages.
	 * </p>
	 *
	 * @return a list of open editors
	 *
	 * @deprecated Clients are encouraged to use {@link #getEditorReferences()}
	 *             instead. Calling this method has the side effect of restoring all
	 *             the editors in the page which can cause plug-in activation.
	 */
	@Deprecated
	IEditorPart[] getEditors();

	/**
	 * Returns an array of references to open editors in this page.
	 * <p>
	 * Note that each page has its own editors; editors are never shared between
	 * pages.
	 * </p>
	 *
	 * @return a list of open editors
	 */
	IEditorReference[] getEditorReferences();

	/**
	 * Returns a list of dirty editors in this page.
	 *
	 * @return a list of dirty editors
	 */
	IEditorPart[] getDirtyEditors();

	/**
	 * Returns the input for this page.
	 *
	 * @return the input for this page, or <code>null</code> if none
	 */
	IAdaptable getInput();

	/**
	 * Returns the page label. This will be a unique identifier within the
	 * containing workbench window.
	 *
	 * @return the page label
	 */
	String getLabel();

	/**
	 * Returns the current perspective descriptor for this page, or
	 * <code>null</code> if there is no current perspective.
	 *
	 * @return the current perspective descriptor or <code>null</code>
	 * @see #setPerspective
	 * @see #savePerspective
	 */
	IPerspectiveDescriptor getPerspective();

	/**
	 * Returns a list of the reference to views visible on this page.
	 * <p>
	 * Note that each page has its own views; views are never shared between pages.
	 * </p>
	 *
	 * @return a list of references to visible views
	 */
	IViewReference[] getViewReferences();

	/**
	 * Returns a list of the views visible on this page.
	 * <p>
	 * Note that each page has its own views; views are never shared between pages.
	 * </p>
	 *
	 * @return a list of visible views
	 *
	 * @deprecated Clients are encouraged to use {@link #getViewReferences()}
	 *             instead. Calling this method has the side effect of restoring all
	 *             the views in the page which can cause plug-in activation.
	 */
	@Deprecated
	IViewPart[] getViews();

	/**
	 * Returns the workbench window of this page.
	 *
	 * @return the workbench window
	 */
	IWorkbenchWindow getWorkbenchWindow();

	/**
	 * Returns the working set of this page.
	 *
	 * @return the working set of this page.
	 * @since 2.0
	 * @deprecated individual views should store a working set if needed
	 */
	@Deprecated
	IWorkingSet getWorkingSet();

	/**
	 * Hides an action set in this page.
	 * <p>
	 * In most cases where this method is used the caller is tightly coupled to a
	 * particular action set. They define it in the registry and may make it visible
	 * in certain scenarios by calling <code>showActionSet</code>. A static variable
	 * is often used to identify the action set id in caller code.
	 * </p>
	 *
	 * @param actionSetID the action set to hide
	 */
	void hideActionSet(String actionSetID);

	/**
	 * Hides the given view. The view must belong to this page.
	 *
	 * @param view the view to hide
	 */
	void hideView(IViewPart view);

	/**
	 * Hides the given view that belongs to the reference, if any.
	 *
	 * @param view the references whos view is to be hidden
	 * @since 3.0
	 */
	void hideView(IViewReference view);

	/**
	 * Returns whether the specified part is visible.
	 *
	 * @param part the part to test
	 * @return boolean <code>true</code> if part is visible
	 */
	boolean isPartVisible(IWorkbenchPart part);

	/**
	 * Returns whether the page's current perspective is showing the editor area.
	 *
	 * @return <code>true</code> when editor area visible, <code>false</code>
	 *         otherwise
	 */
	boolean isEditorAreaVisible();

	/**
	 * Reuses the specified editor by setting its new input.
	 *
	 * @param editor the editor to be reused
	 * @param input  the new input for the reusable editor
	 */
	void reuseEditor(IReusableEditor editor, IEditorInput input);

	/**
	 * Opens an editor on the given input.
	 * <p>
	 * If this page already has an editor open on the target input that editor is
	 * activated; otherwise, a new editor is opened. Two editor inputs, input1 and
	 * input2, are considered the same if
	 * </p>
	 *
	 * <pre>
	 * input1.equals(input2) == true
	 * </pre>
	 * <p>
	 * The editor type is determined by mapping <code>editorId</code> to an editor
	 * extension registered with the workbench. An editor id is passed rather than
	 * an editor object to prevent the accidental creation of more than one editor
	 * for the same input. It also guarantees a consistent lifecycle for editors,
	 * regardless of whether they are created by the user or restored from saved
	 * data.
	 * </p>
	 *
	 * @param input    the editor input
	 * @param editorId the id of the editor extension to use
	 * @return an open and active editor, or <code>null</code> if an external editor
	 *         was opened
	 * @exception PartInitException if the editor could not be created or
	 *                              initialized
	 */
	IEditorPart openEditor(IEditorInput input, String editorId) throws PartInitException;

	/**
	 * Opens an editor on the given input.
	 * <p>
	 * If this page already has an editor open on the target input that editor is
	 * brought to the front; otherwise, a new editor is opened. Two editor inputs
	 * are considered the same if they equal. See <code>Object.equals(Object)</code>
	 * and <code>IEditorInput</code>. If <code>activate == true</code> the editor
	 * will be activated.
	 * </p>
	 * <p>
	 * The editor type is determined by mapping <code>editorId</code> to an editor
	 * extension registered with the workbench. An editor id is passed rather than
	 * an editor object to prevent the accidental creation of more than one editor
	 * for the same input. It also guarantees a consistent lifecycle for editors,
	 * regardless of whether they are created by the user or restored from saved
	 * data.
	 * </p>
	 *
	 * @param input    the editor input
	 * @param editorId the id of the editor extension to use
	 * @param activate if <code>true</code> the editor will be activated
	 * @return an open editor, or <code>null</code> if an external editor was opened
	 * @exception PartInitException if the editor could not be created or
	 *                              initialized
	 */
	IEditorPart openEditor(IEditorInput input, String editorId, boolean activate) throws PartInitException;

	/**
	 * Opens an editor on the given input.
	 * <p>
	 * If this page already has an editor open that matches the given input and/or
	 * editor id (as specified by the matchFlags argument), that editor is brought
	 * to the front; otherwise, a new editor is opened. Two editor inputs are
	 * considered the same if they equal. See <code>Object.equals(Object)</code> and
	 * <code>IEditorInput</code>. If <code>activate == true</code> the editor will
	 * be activated.
	 * </p>
	 * <p>
	 * The editor type is determined by mapping <code>editorId</code> to an editor
	 * extension registered with the workbench. An editor id is passed rather than
	 * an editor object to prevent the accidental creation of more than one editor
	 * for the same input. It also guarantees a consistent lifecycle for editors,
	 * regardless of whether they are created by the user or restored from saved
	 * data.
	 * </p>
	 *
	 * @param input      the editor input
	 * @param editorId   the id of the editor extension to use
	 * @param activate   if <code>true</code> the editor will be activated
	 * @param matchFlags a bit mask consisting of zero or more of the MATCH_*
	 *                   constants OR-ed together
	 * @return an open editor, or <code>null</code> if an external editor was opened
	 * @exception PartInitException if the editor could not be created or
	 *                              initialized
	 *
	 * @see #MATCH_NONE
	 * @see #MATCH_INPUT
	 * @see #MATCH_ID
	 * @since 3.2
	 */
	IEditorPart openEditor(final IEditorInput input, final String editorId, final boolean activate,
			final int matchFlags) throws PartInitException;

	/**
	 * Removes the property change listener.
	 *
	 * @param listener the property change listener to remove
	 * @since 2.0
	 */
	void removePropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Changes the visible views, their layout, and the visible action sets within
	 * the page to match the current perspective descriptor. This is a rearrangement
	 * of components and not a replacement. The contents of the current perspective
	 * descriptor are unaffected.
	 * <p>
	 * For more information on perspective change see <code>setPerspective()</code>.
	 * </p>
	 */
	void resetPerspective();

	/**
	 * Saves the contents of all dirty editors belonging to this workbench page. If
	 * there are no dirty editors this method returns without effect.
	 * <p>
	 * If <code>confirm</code> is <code>true</code> the user is prompted to confirm
	 * the command.
	 * </p>
	 * <p>
	 * Note that as of 3.2, this method also saves views that implement
	 * ISaveablePart and are dirty.
	 * </p>
	 *
	 * @param confirm <code>true</code> to ask the user before saving unsaved
	 *                changes (recommended), and <code>false</code> to save unsaved
	 *                changes without asking
	 * @return <code>true</code> if the command succeeded, and <code>false</code> if
	 *         the operation was canceled by the user or an error occurred while
	 *         saving
	 */
	boolean saveAllEditors(boolean confirm);

	/**
	 * Saves the contents of the given editor if dirty. If not, this method returns
	 * without effect.
	 * <p>
	 * If <code>confirm</code> is <code>true</code> the user is prompted to confirm
	 * the command. Otherwise, the save happens without prompt.
	 * </p>
	 * <p>
	 * The editor must belong to this workbench page.
	 * </p>
	 *
	 * @param editor  the editor to close
	 * @param confirm <code>true</code> to ask the user before saving unsaved
	 *                changes (recommended), and <code>false</code> to save unsaved
	 *                changes without asking
	 * @return <code>true</code> if the command succeeded, and <code>false</code> if
	 *         the editor was not saved
	 */
	boolean saveEditor(IEditorPart editor, boolean confirm);

	/**
	 * Saves the visible views, their layout, and the visible action sets for this
	 * page to the current perspective descriptor. The contents of the current
	 * perspective descriptor are overwritten.
	 */
	void savePerspective();

	/**
	 * Saves the visible views, their layout, and the visible action sets for this
	 * page to the given perspective descriptor. The contents of the given
	 * perspective descriptor are overwritten and it is made the current one for
	 * this page.
	 *
	 * @param perspective the perspective descriptor to save to
	 */
	void savePerspectiveAs(IPerspectiveDescriptor perspective);

	/**
	 * Show or hide the editor area for the page's active perspective.
	 *
	 * @param showEditorArea <code>true</code> to show the editor area,
	 *                       <code>false</code> to hide the editor area
	 */
	void setEditorAreaVisible(boolean showEditorArea);

	/**
	 * Changes the visible views, their layout, and the visible action sets within
	 * the page to match the given perspective descriptor. This is a rearrangement
	 * of components and not a replacement. The contents of the old perspective
	 * descriptor are unaffected.
	 * <p>
	 * When a perspective change occurs the old perspective is deactivated (hidden)
	 * and cached for future reference. Then the new perspective is activated
	 * (shown). The views within the page are shared by all existing perspectives to
	 * make it easy for the user to switch between one perspective and another
	 * quickly without loss of context.
	 * </p>
	 * <p>
	 * During activation the action sets are modified. If an action set is specified
	 * in the new perspective which is not visible in the old one it will be
	 * created. If an old action set is not specified in the new perspective it will
	 * be disposed.
	 * </p>
	 * <p>
	 * The visible views and their layout within the page also change. If a view is
	 * specified in the new perspective which is not visible in the old one a new
	 * instance of the view will be created. If an old view is not specified in the
	 * new perspective it will be hidden. This view may reappear if the user selects
	 * it from the View menu or if they switch to a perspective (which may be the
	 * old one) where the view is visible.
	 * </p>
	 * <p>
	 * The open editors are not modified by this method.
	 * </p>
	 *
	 * @param perspective the perspective descriptor
	 */
	void setPerspective(IPerspectiveDescriptor perspective);

	/**
	 * Shows an action set in this page.
	 * <p>
	 * In most cases where this method is used the caller is tightly coupled to a
	 * particular action set. They define it in the registry and may make it visible
	 * in certain scenarios by calling <code>showActionSet</code>. A static variable
	 * is often used to identify the action set id in caller code.
	 * </p>
	 *
	 * @param actionSetID the action set to show
	 */
	void showActionSet(String actionSetID);

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is given focus.
	 *
	 * @param viewId the id of the view extension to use
	 * @return the shown view
	 * @exception PartInitException if the view could not be initialized
	 */
	IViewPart showView(String viewId) throws PartInitException;

	/**
	 * Shows a view in this page with the given id and secondary id. The behaviour
	 * of this method varies based on the supplied mode. If
	 * <code>VIEW_ACTIVATE</code> is supplied, the view is given focus. If
	 * <code>VIEW_VISIBLE</code> is supplied, then it is made visible but not given
	 * focus. Finally, if <code>VIEW_CREATE</code> is supplied the view is created
	 * and will only be made visible if it is not created in a folder that already
	 * contains visible views.
	 * <p>
	 * This allows multiple instances of a particular view to be created. They are
	 * disambiguated using the secondary id. If a secondary id is given, the view
	 * must allow multiple instances by having specified allowMultiple="true" in its
	 * extension.
	 * </p>
	 *
	 * @param viewId      the id of the view extension to use
	 * @param secondaryId the secondary id to use, or <code>null</code> for no
	 *                    secondary id
	 * @param mode        the activation mode. Must be {@link #VIEW_ACTIVATE},
	 *                    {@link #VIEW_VISIBLE} or {@link #VIEW_CREATE}
	 * @return a view
	 * @exception PartInitException        if the view could not be initialized
	 * @exception IllegalArgumentException if the supplied mode is not valid
	 * @since 3.0
	 */
	IViewPart showView(String viewId, String secondaryId, int mode) throws PartInitException;

	/**
	 * Returns <code>true</code> if the editor is pinned and should not be reused.
	 *
	 * @param editor the editor to test
	 * @return boolean whether the editor is pinned
	 */
	boolean isEditorPinned(IEditorPart editor);

	/**
	 * Returns the number of open editors before reusing editors.
	 *
	 * @return a int
	 *
	 * @deprecated
	 */
	@Deprecated
	int getEditorReuseThreshold();

	/**
	 * Set the number of open editors before reusing editors. If &lt; 0 the user
	 * preference settings will be used.
	 *
	 * @param openEditors the threshold
	 * @deprecated use IPageLayout.setEditorReuseThreshold(int openEditors) instead.
	 */
	@Deprecated
	void setEditorReuseThreshold(int openEditors);

	/**
	 * Returns the navigation history which manages a list of entries keeping the
	 * history of places (positions, selection and editors) the user visited making
	 * it easier to the user to move back and forward without losing context.
	 *
	 * @return the navigation history
	 * @since 2.1
	 */
	INavigationHistory getNavigationHistory();

	/**
	 * Returns an array of IViewParts that are stacked with the given part in the
	 * currently active perspective.
	 *
	 * @param part the part to test
	 * @return the parts that are stacked with this part, including the part in
	 *         question. <code>null</code> is returned if the part does not belong
	 *         to this page or the part is not contained in the active perspective.
	 *         The parts are in LRU order.
	 * @since 3.0
	 */
	IViewPart[] getViewStack(IViewPart part);

	/**
	 * Returns the new wizard shortcuts associated with the current perspective.
	 * Returns an empty array if there is no current perspective.
	 *
	 * @see IPageLayout#addNewWizardShortcut(String)
	 * @return an array of wizard identifiers
	 * @since 3.1
	 */
	String[] getNewWizardShortcuts();

	/**
	 * Returns the perspective shortcuts associated with the current perspective.
	 * Returns an empty array if there is no current perspective.
	 *
	 * @see IPageLayout#addPerspectiveShortcut(String)
	 * @return an array of perspective identifiers
	 * @since 3.1
	 */
	String[] getPerspectiveShortcuts();

	/**
	 * Returns the show view shortcuts associated with the current perspective.
	 * Returns an empty array if there is no current perspective.
	 *
	 * @see IPageLayout#addShowViewShortcut(String)
	 * @return an array of view identifiers
	 * @since 3.1
	 */
	String[] getShowViewShortcuts();

	/**
	 * Returns the descriptors for the perspectives that are open in this page, in
	 * the order in which they were opened.
	 *
	 * @return the open perspective descriptors, in order of opening
	 * @since 3.1
	 */
	IPerspectiveDescriptor[] getOpenPerspectives();

	/**
	 * Returns the descriptors for the perspectives that are open in this page, in
	 * the order in which they were activated (oldest first).
	 *
	 * @return the open perspective descriptors, in order of activation
	 * @since 3.1
	 */
	IPerspectiveDescriptor[] getSortedPerspectives();

	/**
	 * Closes the specified perspective in this page. If the last perspective in
	 * this page is closed, then all editors are closed. Views that are not shown in
	 * other perspectives are closed as well. If <code>saveParts</code> is
	 * <code>true</code>, the user will be prompted to save any unsaved changes for
	 * parts that are being closed. The page itself is closed if
	 * <code>closePage</code> is <code>true</code>.
	 *
	 * @param desc      the descriptor of the perspective to be closed
	 * @param saveParts whether the page's parts should be saved if closed
	 * @param closePage whether the page itself should be closed if last perspective
	 * @since 3.1
	 */
	void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage);

	/**
	 * Closes all perspectives in this page. All editors are closed, prompting to
	 * save any unsaved changes if <code>saveEditors</code> is <code>true</code>.
	 * The page itself is closed if <code>closePage</code> is <code>true</code>.
	 *
	 * @param saveEditors whether the page's editors should be saved
	 * @param closePage   whether the page itself should be closed
	 * @since 3.1
	 */
	void closeAllPerspectives(boolean saveEditors, boolean closePage);

	/**
	 * <p>
	 * Return the extension tracker for the workbench. This tracker may be used by
	 * plug-ins to ensure responsiveness to changes to the plug-in registry.
	 * </p>
	 * <p>
	 * The tracker at this level of the workbench is typically used to track
	 * elements that only exist over the lifespan of a page. For example,
	 * <code>ViewPart</code> objects fall into this category.
	 * </p>
	 *
	 * @return the extension tracker
	 * @see IWorkbench#getExtensionTracker()
	 * @see IWorkbenchWindow#getExtensionTracker()
	 * @since 3.1
	 */
	IExtensionTracker getExtensionTracker();

	/**
	 * Return the visible working sets for this page. Please note that this array is
	 * not filtered by activities. Clients should attempt to ensure that any use of
	 * this method is consistant with the currently enabled activity set.
	 *
	 * @return the visible working sets for this page
	 * @see IWorkbench#getActivitySupport()
	 * @since 3.2
	 */
	IWorkingSet[] getWorkingSets();

	/**
	 * Set the working sets for this page. Any duplicate entries will be removed
	 * from the array by this method.
	 *
	 * @param sets the new working sets for this page. The array may be empty, but
	 *             no element in the array may be <code>null</code>.
	 * @since 3.2
	 */
	void setWorkingSets(IWorkingSet[] sets);

	/**
	 * Return a working set that contains all of the elements contained in the array
	 * of working sets provided by {@link #getWorkingSets()}. Should this array or
	 * the underlying elements in any of the working sets change this set will be
	 * updated.
	 *
	 * <p>
	 * This working set is never <code>null</code>, even if there are no working
	 * sets assigned to this page via {@link #setWorkingSets(IWorkingSet[])}. It is
	 * recommended that any client that uses this API be aware of this and act
	 * accordingly. Specifically, it is recommended that any client utilizing this
	 * or any other IWorkingSet whose {@link IWorkingSet#isAggregateWorkingSet()}
	 * returns <code>true</code> act as if they are not using any working set if the
	 * set is empty. These clients should also maintain an awareness of the contents
	 * of aggregate working sets and toggle this behavior should the contents of the
	 * aggregate either become empty or non-empty.
	 * </p>
	 * <p>
	 * Example pseudocode showing how some workingset utilizing component could
	 * react to changes in aggregate working sets:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * private IWorkingSet myWorkingSet;
	 *
	 * IPropertyChangeListener workingSetListener = new IPropertyChangeListener() {
	 *	void propertyChange(PropertyChangeEvent event) {
	 * 		if (isMyCurrentWorkingSet(event)) {
	 * 			if (isEmptyAggregate(myWorkingSet)) {
	 * 				showNoSet();
	 * 			}
	 * 			else {
	 * 				showSet();
	 * 			}
	 *		}
	 *	}
	 * };
	 *
	 * void setWorkingSet(IWorkingSet newSet) {
	 * 		myWorkingSet = newSet;
	 * 		if (myWorkingSet == null || isEmptyAggregate(myWorkingSet)){
	 * 			showNoSet();
	 * 		}
	 * 		else {
	 * 			showSet();
	 * 		}
	 * }
	 * </code>
	 * </pre>
	 *
	 * @return the aggregate working set for this page, this implements
	 *         {@link IAggregateWorkingSet}
	 * @see IAggregateWorkingSet
	 * @since 3.2
	 */
	IWorkingSet getAggregateWorkingSet();

	/**
	 * Returns the page "zoomed" state.
	 *
	 * @return <code>true</code> if the page is zoomed in the workbench window,
	 *         <code>false</code> otherwise.
	 * @since 3.2
	 */
	boolean isPageZoomed();

	/**
	 * Zooms out the zoomed-in part. If the page does not have a zoomed part, it
	 * does nothing.
	 *
	 * @since 3.2
	 */
	void zoomOut();

	/**
	 * Zoom the page in on a part. If the part is already in zoom then zoom out.
	 *
	 * @param ref the workbench part to zoom in on. Must not be <code>null</code>.
	 * @since 3.2
	 */
	void toggleZoom(IWorkbenchPartReference ref);

	/**
	 * Returns the maximized/minimized/restored state of the given part reference.
	 *
	 * @param ref the workbench part to query. Must not be <code>null</code>.
	 * @return one of the STATE_* contants.
	 * @since 3.2
	 */
	int getPartState(IWorkbenchPartReference ref);

	/**
	 * Set the state of the given part reference. Setting the state of one part can
	 * effect the state of other parts.
	 *
	 * @param ref   the workbench part reference. Must not be <code>null</code>.
	 * @param state one of the STATE_* constants.
	 * @since 3.2
	 */
	void setPartState(IWorkbenchPartReference ref, int state);

	/**
	 * Find the part reference for the given part. A convenience method to quickly
	 * go from part to part reference.
	 *
	 * @param part The part to search for. It can be <code>null</code>.
	 * @return The reference for the given part, or <code>null</code> if no
	 *         reference can be found.
	 * @since 3.2
	 */
	IWorkbenchPartReference getReference(IWorkbenchPart part);

	/**
	 * Add back an open but non-participating editor
	 *
	 * @param ref the editor to re-add. Must be an editor removed using
	 *            #hideEditor(IEditorReference), must not have been closed, and must
	 *            not be <code>null</code>.
	 * @since 3.5
	 * @see #hideEditor(IEditorReference)
	 */
	void showEditor(IEditorReference ref);

	/**
	 * Remove an open editor, turn it into a non-participating editor.
	 * <p>
	 * A non-participating editor will not be returned in the list of open editors
	 * ({@link #getEditorReferences()}) and will not be visible in the editor area.
	 * However, it will continue to participate in the save lifecycle and may still
	 * be closed by some workbench close events.
	 * </p>
	 * <p>
	 * Behaviour for hiding and showing editors from multiple stacks is not defined
	 * (and unsupported) at this time.
	 * </p>
	 *
	 * @param ref the editor reference to remove. It must be a current open editor
	 *            belonging to this page, and must not be <code>null</code>.
	 * @since 3.5
	 * @see #showEditor(IEditorReference)
	 */
	void hideEditor(IEditorReference ref);

	/**
	 * Opens editors for the given inputs. Only the editor constructed for the first
	 * input gets activated.
	 * <p>
	 * The editor type is determined by mapping <code>editorIDs</code> to an editor
	 * extension registered with the workbench. An editor id is passed rather than
	 * an editor object to prevent the accidental creation of more than one editor
	 * for the same input. It also guarantees a consistent lifecycle for editors,
	 * regardless of whether they are created by the user or restored from saved
	 * data.
	 * </p>
	 * <p>
	 * The length of the input array and editor ID arrays must be the same. The
	 * editors are opened using pairs of { input[i], editorIDs[i] }.
	 * </p>
	 *
	 * @param inputs     the editor inputs
	 * @param editorIDs  the IDs of the editor extensions to use, in the order of
	 *                   inputs
	 * @param matchFlags a bit mask consisting of zero or more of the MATCH_*
	 *                   constants OR-ed together
	 * @return references to the editors constructed for the inputs. The editors
	 *         corresponding to those reference might not be materialized.
	 * @exception MultiPartInitException if at least one editor could not be created
	 *                                   or initialized
	 * @see #MATCH_NONE
	 * @see #MATCH_INPUT
	 * @see #MATCH_ID
	 * @since 3.5
	 */
	IEditorReference[] openEditors(final IEditorInput[] inputs, final String[] editorIDs, final int matchFlags)
			throws MultiPartInitException;

	/**
	 * Opens editors for the given inputs. Only the editor constructed for the given
	 * index will be activated.
	 * <p>
	 * There are effectively two different ways to use this method based on what
	 * information the supplied mementos contain @see org.eclipse.ui.IWorkbenchPage
	 * #getEditorState(org.eclipse.ui.IEditorReference []):
	 * </p>
	 * <ol>
	 * <li>If the mementos contain the 'input' information then only the memento
	 * itself is required since it can be used to re-create the editor input and its
	 * editorID. If all the mementos are of this type then the inputs and editorIDs
	 * arrays may be null.</li>
	 * <li>If the supplied memento only contains the editor state then both the
	 * input and editorID must be non-null.</li>
	 * </ol>
	 * <p>
	 * The editor type is determined by mapping <code>editorIDs</code> to an editor
	 * extension registered with the workbench. An editor id is passed rather than
	 * an editor object to prevent the accidental creation of more than one editor
	 * for the same input. It also guarantees a consistent lifecycle for editors,
	 * regardless of whether they are created by the user or restored from saved
	 * data.
	 * </p>
	 * <p>
	 * The length of the input array and editor ID arrays must be the same. The
	 * editors are opened using pairs of { input[i], editorIDs[i] }.
	 * </p>
	 * <p>
	 * The mementos array mat be null but if not must match the input array in
	 * length. Entries in the mementos array may also be null if no state is desired
	 * for that particular editor.
	 * </p>
	 *
	 * @param inputs        the editor inputs
	 * @param editorIDs     the IDs of the editor extensions to use, in the order of
	 *                      inputs
	 * @param mementos      the mementos representing the state to open the editor
	 *                      with. If the supplied memento contains the input's state
	 *                      as well as the editor's state then the corresponding
	 *                      entries in the 'inputs' and 'ids' arrays may be
	 *                      <code>null</code> (they will be created from the
	 *                      supplied memento).
	 *
	 * @param matchFlags    a bit mask consisting of zero or more of the MATCH_*
	 *                      constants OR-ed together
	 * @param activateIndex the index of the editor to make active or -1 if no
	 *                      activation is desired.
	 * @return references to the editors constructed for the inputs. The editors
	 *         corresponding to those reference might not be materialized.
	 * @exception MultiPartInitException if at least one editor could not be created
	 *                                   or initialized
	 * @see #MATCH_NONE
	 * @see #MATCH_INPUT
	 * @see #MATCH_ID
	 * @since 3.8.2
	 */
	IEditorReference[] openEditors(final IEditorInput[] inputs, final String[] editorIDs, final IMemento[] mementos,
			final int matchFlags, final int activateIndex) throws MultiPartInitException;

	/**
	 * Return an IMemento containing the current state of the editor for each of the
	 * given references. These mementos may be used to determine the initial state
	 * of an editor on a subsequent open.
	 *
	 * @param editorRefs        The array of editor references to get the state for
	 * @param includeInputState If <code>true</code> then the resulting memento will
	 *                          be contain the editor input's state as well as the
	 *                          editor's state.
	 * @return The array of mementos. The length of the array will match that of the
	 *         refs array.
	 * @since 3.8.2
	 */
	IMemento[] getEditorState(IEditorReference[] editorRefs, boolean includeInputState);
}
