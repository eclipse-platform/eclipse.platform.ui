package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A workbench page consists of an arrangement of views and editors intended to
 * be presented together to the user in a single workbench window. 
 * <p>
 * A page can contain 0 or more views and 0 or more editors. These views and editors
 * are contained wholly within the page and are not shared with other pages. 
 * The layout and visible action set for the page is defined by a perspective.
 * <p>
 * The number of views and editors within a page is restricted to simplify part
 * management for the user.  In particular:
 * <ul>
 * 	<li>Only one instance of a particular view type may exist within a workbench page.</li>
 * 	<li>Only one editor can exist for each editor input within a page.<li>
 * </ul>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPerspectiveDescriptor
 * @see IEditorPart
 * @see IViewPart
 */
public interface IWorkbenchPage extends IPartService, ISelectionService {
	/**
	 * An optional attribute within a workspace marker (<code>IMarker</code>) which
	 * identifies the preferred editor type to be opened when 
	 * <code>openEditor</code> is called.
	 *
	 * @see #openEditor
 	 */	
	public static final String EDITOR_ID_ATTR = "org.eclipse.ui.editorID"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective is reset to its
	 * original state.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_RESET = "reset"; //$NON-NLS-1$

	/**
	 * Change event id when one or more perspective views are shown.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_VIEW_SHOW = "viewShow"; //$NON-NLS-1$

	/**
	 * Change event id when one or more perspective views are hidden.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_VIEW_HIDE = "viewHide"; //$NON-NLS-1$


	/**
	 * Change event id when one or more perspective editors are opened.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_EDITOR_OPEN = "editorOpen"; //$NON-NLS-1$

	/**
	 * Change event id when one or more perspective editors are closed.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_EDITOR_CLOSE = "editorClose"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective editor area is shown.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_EDITOR_AREA_SHOW = "editorAreaShow"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective editor area is hidden.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_EDITOR_AREA_HIDE = "editorAreaHide"; //$NON-NLS-1$

	/**
	 * Change event id when a perspective action set is shown.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_ACTION_SET_SHOW = "actionSetShow"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective action set is hidden.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_ACTION_SET_HIDE = "actionSetHide"; //$NON-NLS-1$
	 
	/**
	 * Change event id when a perspective fast view added.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_FAST_VIEW_ADD = "fastViewAdd"; //$NON-NLS-1$

	/**
	 * Change event id when the perspective fast view removed.
	 *
	 * @see IPerspectiveListener
	 */
	 public static final String CHANGE_FAST_VIEW_REMOVE = "fastViewRemove"; //$NON-NLS-1$
	 
	/**
	 * Change event id when the page working set was replaced
	 *
	 * @see IPropertyChangeListener
	 */
	 public static final String CHANGE_WORKING_SET_REPLACE = "workingSetReplace";	//$NON-NLS-1$	 
	 
/**
 * Activates the given part. The part will be brought to the front and given
 * focus. The part must belong to this page.
 *
 * @param part the part to activate
 */
public void activate(IWorkbenchPart part);
/**
 * Adds a property change listener.
 * 
 * @param listener the property change listener to add
 * @since 2.0
 * @deprecated individual views should store a working set if needed and
 * 	register a property change listener directly with the working set manager
 * 	to receive notification when the view working set is removed.
 */
public void addPropertyChangeListener(IPropertyChangeListener listener);
/**
 * Moves the given part forward in the Z order of this page so as to make
 * it visible, without changing which part has focus.
 * The part must belong to this page.
 *
 * @param part the part to bring forward
 */
public void bringToTop(IWorkbenchPart part);
/**
 * Closes this workbench page. If this page is the active one, this honor is
 * passed along to one of the window's other pages if possible.
 * <p>
 * If the page has an open editor with unsaved content, the user will be
 * given the opportunity to save it.
 * </p>
 *
 * @return <code>true</code> if the page was successfully closed,
 *   and <code>false</code> if it is still open
 */
public boolean close();
/**
 * Closes all of the editors belonging to this workbench page.
 * <p>
 * If the page has open editors with unsaved content and <code>save</code>
 * is <code>true</code>, the user will be given the opportunity to save them.
 * </p>
 *
 * @return <code>true</code> if all editors were successfully closed,
 *   and <code>false</code> if at least one is still open
 */
public boolean closeAllEditors(boolean save);
/**
 * Closes the given editor. The editor must belong to this workbench page.
 * <p>
 * If the editor has unsaved content and <code>save</code> is <code>true</code>,
 * the user will be given the opportunity to save it.
 * </p>
 *
 * @param editor the editor to close
 * @param save <code>true</code> to save the editor contents if required
 *  (recommended), and <code>false</code> to discard any unsaved changes
 * @return <code>true</code> if the editor was successfully closed,
 *   and <code>false</code> if the editor is still open
 */
public boolean closeEditor(IEditorPart editor, boolean save);
/**
 * Returns the view in this page with the specified id.  There is at most one
 * view in the page with the specified id.
 *
 * @param viewId the id of the view extension to use
 * @return the view, or <code>null</code> if none is found
 */
public IViewPart findView(String viewId);
/**
 * Returns the active editor open in this page.
 * <p>
 * This is the visible editor on the page, or, if there is more than one
 * visible editor, this is the one most recently brought to top.
 * </p>
 *
 * @return the active editor, or <code>null</code> if no editor is active
 */
public IEditorPart getActiveEditor();
/**
 * Returns the editor with the specified input. Returns null if there
 * is no opened editor with that input.
 *
 * @return an editor with input equals to <code>input</code>
 */
public IEditorPart findEditor(IEditorInput input);
/**
 * Returns a list of the editors open in this page.
 * <p>
 * Note that each page has its own editors; editors are never shared between
 * pages.
 * </p>
 *
 * @return a list of open editors
 * 
 * @deprecated use getEditorReferences() instead
 */
public IEditorPart[] getEditors();
/**
 * Returns a array of references to open editors in this page.
 * <p>
 * Note that each page has its own editors; editors are never shared between
 * pages.
 * </p>
 *
 * @return a list of open editors
 */
public IEditorReference[] getEditorReferences();
/**
 * Returns a list of dirty editors in this page.
 *
 * @return a list of dirty editors
 */
public IEditorPart[] getDirtyEditors();
/**
 * Returns the input for this page.
 *
 * @return the input for this page, or <code>null</code> if none 
 */
public IAdaptable getInput();
/**
 * Returns the page label.  This will be a unique identifier within the
 * containing workbench window.
 *
 * @return the page label
 */
public String getLabel();
/**
 * Returns the current perspective descriptor for this page.
 *
 * @return the current perspective descriptor
 * @see #setPerspective
 * @see #savePerspective
 */
public IPerspectiveDescriptor getPerspective();
/**
 * Returns a list of the reference to views visible on this page.
 * <p>
 * Note that each page has its own views; views are never shared between pages.
 * </p>
 *
 * @return a list of references to visible views
 */
public IViewReference[] getViewReferences();
/**
 * Returns a list of the views visible on this page.
 * <p>
 * Note that each page has its own views; views are never shared between pages.
 * </p>
 *
 * @return a list of visible views
 * 
 * @deprecated use getViewReferences() instead.
 */
public IViewPart[] getViews();
/**
 * Returns the workbench window of this page.
 *
 * @return the workbench window
 */
public IWorkbenchWindow getWorkbenchWindow();
/**
 * Returns the working set of this page.
 * 
 * @return the working set of this page.
 * @since 2.0
 * @deprecated individual views should store a working set if needed
 */
public IWorkingSet getWorkingSet();
/**
 * Hides an action set in this page.
 * <p>
 * In most cases where this method is used the caller is tightly coupled to
 * a particular action set.  They define it in the registry and may make it
 * visible in certain scenarios by calling <code>showActionSet</code>.
 * A static variable is often used to identify the action set id in caller
 * code.
 * </p>
 */
public void hideActionSet(String actionSetID);
/**
 * Hides the given view. The view must belong to this page.
 *
 * @param view the view to hide
 */
public void hideView(IViewPart view);
/**
 * Returns whether the specified part is visible.
 *
 * @return boolean true if part is visible
 */
public boolean isPartVisible(IWorkbenchPart part);
/**
 * Returns whether the page's current perspective is showing
 * the editor area.
 *
 * @return <code>true</code> when editor area visible, <code>false</code> otherwise
 */
public boolean isEditorAreaVisible();
/**
 * Reuses the specified editor by setting its new input.
 * 
 * @param editor the editor to be reused
 * @param input the new input for the reusable editor
 */
public void reuseEditor(IReusableEditor editor,IEditorInput input);
/**
 * Opens an editor on the given file resource. 
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * activated; otherwise, a new editor is opened. 
 * <p><p>
 * An appropriate editor for the input is determined using a multistep process.
 * </p>
 * <ol>
 *   <li>The workbench editor registry is consulted to determine if an editor 
 *			extension has been registered for the file type.  If so, an 
 *			instance of the editor extension is opened on the file.  
 *			See <code>IEditorRegistry.getDefaultEditor(IFile)</code>.
 *   <li>Next, the native operating system will be consulted to determine if a native
 *			editor exists for the file type.  If so, a new process is started
 *			and the native editor is opened on the file.
 *   <li>If all else fails the file will be opened in a default text editor.</li>
 * </ol>
 * </p>
 *
 * @param input the file to edit
 * @return an open and active editor, or null if a system editor was opened
 * @exception PartInitException if the editor could not be initialized
 */
public IEditorPart openEditor(IFile input) throws PartInitException;
/**
 * Opens an editor on the given file resource.  
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * brought to front; otherwise, a new editor is opened. If 
 * <code>activate == true</code> the editor will be activated. 
 * <p><p>
 * The editor type is determined by mapping <code>editorId</code> to an editor
 * extension registered with the workbench.  An editor id is passed rather than
 * an editor object to prevent the accidental creation of more than one editor
 * for the same input. It also guarantees a consistent lifecycle for editors,
 * regardless of whether they are created by the user or restored from saved 
 * data.
 * </p>
 *
 * @param input the file to edit
 * @param editorId the id of the editor extension to use or null
 * @param activate if <code>true</code> the editor will be activated
 * @return an open and active editor
 * @exception PartInitException if the editor could not be initialized
 */
public IEditorPart openEditor(IFile input, String editorID, boolean activate)
	throws PartInitException;
/**
 * Opens an editor on the given file resource.  
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * activated; otherwise, a new editor is opened. 
 * <p><p>
 * The editor type is determined by mapping <code>editorId</code> to an editor
 * extension registered with the workbench.  An editor id is passed rather than
 * an editor object to prevent the accidental creation of more than one editor
 * for the same input. It also guarantees a consistent lifecycle for editors,
 * regardless of whether they are created by the user or restored from saved 
 * data.
 * </p>
 *
 * @param editorId the id of the editor extension to use
 * @param input the file to edit
 * @return an open and active editor
 * @exception PartInitException if the editor could not be initialized
 */
public IEditorPart openEditor(IFile input, String editorID)
	throws PartInitException;
/**
 * Opens an editor on the file resource of the given marker. 
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * activated; otherwise, a new editor is opened. The cursor and selection state 
 * of the editor is then updated from information recorded in the marker.
 * <p><p>
 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute 
 * the attribute value will be used to determine the editor type to be opened. 
 * If not, the registered editor for the marker resource will be used. 
 * </p>
 *
 * @param marker the marker to open
 * @return an open and active editor, or null if a system editor was opened
 * @exception PartInitException if the editor could not be initialized
 * @see IEditorPart#gotoMarker
 */
public IEditorPart openEditor(IMarker marker) throws PartInitException;
/**
 * Opens an editor on the file resource of the given marker. 
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * brought to front; otherwise, a new editor is opened. If 
 * <code>activate == true</code> the editor will be activated.  The cursor and 
 * selection state of the editor are then updated from information recorded in 
 * the marker.
 * <p><p>
 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute 
 * the attribute value will be used to determine the editor type to be opened. 
 * If not, the registered editor for the marker resource will be used. 
 * </p>
 *
 * @param marker the marker to open
 * @param activate if <code>true</code> the editor will be activated
 * @return an open editor, or null if a system editor was opened
 * @exception PartInitException if the editor could not be initialized
 * @see IEditorPart#gotoMarker
 */
public IEditorPart openEditor(IMarker marker, boolean activate) 
	throws PartInitException;
/**
 * Opens an editor on the given object.  
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * activated; otherwise, a new editor is opened. 
 * <p><p>
 * The editor type is determined by mapping <code>editorId</code> to an editor
 * extension registered with the workbench.  An editor id is passed rather than
 * an editor object to prevent the accidental creation of more than one editor
 * for the same input. It also guarantees a consistent lifecycle for editors,
 * regardless of whether they are created by the user or restored from saved 
 * data.
 * </p>
 *
 * @param input the editor input
 * @param editorId the id of the editor extension to use
 * @return an open and active editor
 * @exception PartInitException if the editor could not be initialized
 */
public IEditorPart openEditor(IEditorInput input, String editorId)
	throws PartInitException;
/**
 * Opens an editor on the given object.  
 * <p>
 * If this page already has an editor open on the target object that editor is 
 * brought to the front; otherwise, a new editor is opened.  If 
 * <code>activate == true</code> the editor will be activated.  
 * <p><p>
 * The editor type is determined by mapping <code>editorId</code> to an editor
 * extension registered with the workbench.  An editor id is passed rather than
 * an editor object to prevent the accidental creation of more than one editor
 * for the same input. It also guarantees a consistent lifecycle for editors,
 * regardless of whether they are created by the user or restored from saved 
 * data.
 * </p>
 *
 * @param input the editor input
 * @param editorId the id of the editor extension to use
 * @param activate if <code>true</code> the editor will be activated
 * @return an open editor
 * @exception PartInitException if the editor could not be initialized
 */
public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate)
	throws PartInitException;
/**
 * Opens an operating system editor on a given file. Once open, the
 * workbench has no knowledge of the editor or the state of the file
 * being edited.  Users are expected to perform a "Local Refresh" from
 * the workbench user interface.
 *
 * @param input the file to edit
 * @exception PartInitException if the editor could not be opened.
 */
public void openSystemEditor(IFile input) throws PartInitException;
/**
 * Removes the property change listener.
 * 
 * @param listener the property change listener to remove
 * @since 2.0
 * @deprecated individual views should store a working set if needed and
 * 	register a property change listener directly with the working set manager
 * 	to receive notification when the view working set is removed.
 */
public void removePropertyChangeListener(IPropertyChangeListener listener);
/**
 * Changes the visible views, their layout, and the visible action sets 
 * within the page to match the current perspective descriptor.  This is a 
 * rearrangement of components and not a replacement.  The contents of the 
 * current perspective descriptor are unaffected.
 * <p>
 * For more information on perspective change see<code>setPerspective()</code>.
 * </p>
 */
public void resetPerspective();
/**
 * Saves the contents of all dirty editors belonging to this workbench 
 * page.  If there are no dirty editors this method returns without
 * effect.
 * <p>
 * If <code>confirm</code> is <code>true</code> the user is prompted to
 * confirm the command.
 * </p>
 *
 * @param confirm <code>true</code> to ask the user before saving unsaved
 *  changes (recommended), and <code>false</code> to save unsaved changes
 *  without asking
 * @return <code>true</code> if the command succeeded, and <code>false</code>
 *   if at least one editor with unsaved changes was not saved
 */
public boolean saveAllEditors(boolean confirm);
/**
 * Saves the contents of the given editor if dirty.  If not, this
 * method returns without effect.
 * <p>
 * If <code>confirm</code> is <code>true</code> the user is prompted to 
 * confirm the command. Otherwise, the save happens without prompt.
 * </p><p>
 * The editor must belong to this workbench page.
 * </p>
 *
 * @param editor the editor to close
 * @param confirm <code>true</code> to ask the user before saving unsaved
 *  changes (recommended), and <code>false</code> to save unsaved changes
 *  without asking
 * @return <code>true</code> if the command succeeded, and <code>false</code>
 *   if the editor was not saved
 */
public boolean saveEditor(IEditorPart editor, boolean confirm);
/**
 * Saves the visible views, their layout, and the visible action sets for
 * this page to the current perspective descriptor. The contents of the current 
 * perspective descriptor are overwritten. 
 */
public void savePerspective();
/**
 * Saves the visible views, their layout, and the visible action sets for
 * this page to the given perspective descriptor. The contents of the given 
 * perspective descriptor are overwritten and it is made the current one 
 * for this page.
 *
 * @param perspective the perspective descriptor to save to
 */
public void savePerspectiveAs(IPerspectiveDescriptor perspective);
/**
 * Show or hide the editor area for the page's active perspective.
 *
 * @param showEditorArea <code>true</code> to show the editor area, <code>false</code> to hide the editor area
 */
public void setEditorAreaVisible(boolean showEditorArea);
/**
 * Changes the visible views, their layout, and the visible action sets 
 * within the page to match the given perspective descriptor.  This is a 
 * rearrangement of components and not a replacement.  The contents of the 
 * old perspective descriptor are unaffected.
 * <p>
 * When a perspective change occurs the old perspective is deactivated (hidden) 
 * and cached for future reference.  Then the new perspective is activated (shown).  
 * The views within the page are shared by all existing perspectives to make 
 * it easy for the user to switch between one perspective and another quickly 
 * without loss of context. 
 * </p><p>
 * During activation the action sets are modified.  If an action set is 
 * specified in the new perspective which is not visible in the old one it 
 * will be created.  If an old action set is not specified in the new 
 * perspective it will be disposed.
 * </p><p>
 * The visible views and their layout within the page also change.  If 
 * a view is specified in the new perspective which is not visible in the old
 * one a new instance of the view will be created.  If an old view is not specified
 * in the new perspective it will be hidden.  This view may reappear if the user 
 * selects it from the View menu or if they switch to a perspective (which may be 
 * the old one) where the view is visible.
 * </p><p>
 * The open editors are not modified by this method.
 * </p>
 *
 * @param perspective the perspective descriptor
 */
public void setPerspective(IPerspectiveDescriptor perspective);
/**
 * Shows an action set in this page. 
 * <p>
 * In most cases where this method is used the caller is tightly coupled to
 * a particular action set.  They define it in the registry and may make it
 * visible in certain scenarios by calling <code>showActionSet</code>.
 * A static variable is often used to identify the action set id in caller
 * code.
 * </p>
 */
public void showActionSet(String actionSetID);
/**
 * Shows a view in this page and give it focus. If the view is already visible,
 * it is given focus.
 * <p>
 * The view type is determined by mapping <code>viewId</code> to a view extension
 * registered with the workbench.  A view id is passed rather than a view object
 * to prevent the accidental creation of more than one view of a particular type.
 * It also guarantees a consistent lifecycle for views, regardless of whether 
 * they are created by the user or restored from saved data.
 * </p>
 *
 * @param viewId the id of the view extension to use
 * @return a view
 * @exception PartInitException if the view could not be initialized
 */
public IViewPart showView(String viewId) throws PartInitException;
/**
 * Returns true if the editor is pinned and should not be
 * reused.
 * 
 * @return boolean
 */
public boolean isEditorPinned(IEditorPart editor);
/**
 * Returns the number of open editors before reusing editors.
 *
 * @return a int
 * 
 * Note: For EXPERIMENTAL use only. IT MAY CHANGE IN NEAR FUTURE.
 * 
 * @deprecated
 */
public int getEditorReuseThreshold();
/**
 * Set the number of open editors before reusing editors.
 * If < 0 the user preference settings will be used.
 * 
 * Note: For EXPERIMENTAL use only. IT MAY CHANGE IN NEAR FUTURE.
 * 
 * @deprecated use IPageLayout.setEditorReuseThreshold(int openEditors) instead.
 */
public void setEditorReuseThreshold(int openEditors);
/**
 * Returns the navigation history which manages a list of entries keeping the history
 * of places (positions, selection and editors) the user visited making it easier to the
 * user to move back and forward without losing context.
 * 
 * @since 2.1
 */
public INavigationHistory getNavigationHistory();
}
