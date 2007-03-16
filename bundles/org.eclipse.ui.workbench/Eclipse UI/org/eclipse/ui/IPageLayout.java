/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross <schtoo@schtoo.com> 
 *     - Fix for 99155 - allow standalone view placeholders
 *     Chris Gross chris.gross@us.ibm.com Bug 107443
 *******************************************************************************/
package org.eclipse.ui;

/**
 * A page layout defines the initial layout for a perspective within a page 
 * in a workbench window.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * When a perspective is opened, it creates a new page layout with a single editor area. 
 * This layout is then passed to the perspective factory (implementation of
 * {@link org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)}) where 
 * additional views and other content can be added, using the existing editor area as 
 * the initial point of reference.
 * </p>
 * <p>
 * In some cases, multiple instances of a particular view may need to be added
 * to the same layout.  These are disambiguated using a secondary id.  
 * In layout methods taking a view id, the id can have the compound form: 
 * <strong>primaryId [':' secondaryId]</strong>.
 * If a secondary id is given, the view must allow multiple instances by
 * having specified <code>allowMultiple="true"</code> in its extension.
 * View placeholders may also have a secondary id.
 * </p>
 * <p>
 * Wildcards are permitted in placeholder ids (but not regular view ids).  
 * '*' matches any substring, '?' matches any single character. 
 * Wildcards can be specified for the primary id, the secondary id, or both.  
 * For example, the placeholder "someView:*" will match any occurrence of the view 
 * that has primary id "someView" and that also has some non-null secondary id.
 * Note that this placeholder will not match the view if it has no secondary id,
 * since the compound id in this case is simply "someView".
 * </p>
 * <p>
 * Example of populating a layout with standard workbench views:
 * <pre>
 * IPageLayout layout = ...
 * // Get the editor area.
 * String editorArea = layout.getEditorArea();
 *
 * // Top left: Resource Navigator view and Bookmarks view placeholder
 * IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,
 *    editorArea);
 * topLeft.addView(IPageLayout.ID_RES_NAV);
 * topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 *
 * // Bottom left: Outline view and Property Sheet view
 * IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,
 * 	   "topLeft");
 * bottomLeft.addView(IPageLayout.ID_OUTLINE);
 * bottomLeft.addView(IPageLayout.ID_PROP_SHEET);
 *
 * // Bottom right: Task List view
 * layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, 0.66f, editorArea);
 * </pre>
 * </p>
 */
public interface IPageLayout {

    /**
     * The part id for the workbench's editor area.  This may only be used
     * as a reference part for view addition.
     */
    public static String ID_EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$

    /**
     * The view id for the workbench's Resource Navigator standard component.
     */
    public static String ID_RES_NAV = "org.eclipse.ui.views.ResourceNavigator"; //$NON-NLS-1$

    /**
     * The view id for the workbench's Property Sheet standard component.
     */
    public static String ID_PROP_SHEET = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

    /**
     * The view id for the workbench's Content Outline standard component.
     */
    public static String ID_OUTLINE = "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$

    /**
     * The view id for the workbench's Bookmark Navigator standard component.
     */
    public static String ID_BOOKMARKS = "org.eclipse.ui.views.BookmarkView"; //$NON-NLS-1$

    /**
     * The view id for the workbench's Problems View standard component.
     * @since 3.0
     */
    public static String ID_PROBLEM_VIEW = "org.eclipse.ui.views.ProblemView"; //$NON-NLS-1$
    
    /**
     * The view id for the workbench's Problems View standard component.
     * @since 3.2
     */
    public static String ID_PROGRESS_VIEW = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

    /**
     * The view id for the workbench's Task List standard component.
     */
    public static String ID_TASK_LIST = "org.eclipse.ui.views.TaskList"; //$NON-NLS-1$

    /**
     * Id of the navigate action set. 
     * (value <code>"org.eclipse.ui.NavigateActionSet"</code>)
     * @since 2.1
     */
    public static final String ID_NAVIGATE_ACTION_SET = "org.eclipse.ui.NavigateActionSet"; //$NON-NLS-1$

    /**
     * Relationship constant indicating a part should be placed to the left of
     * its relative.
     */
    public static final int LEFT = 1;

    /**
     * Relationship constant indicating a part should be placed to the right of
     * its relative.
     */
    public static final int RIGHT = 2;

    /**
     * Relationship constant indicating a part should be placed above its 
     * relative.
     */
    public static final int TOP = 3;

    /**
     * Relationship constant indicating a part should be placed below its 
     * relative.
     */
    public static final int BOTTOM = 4;

    /**
     * Minimum acceptable ratio value when adding a view
     * @since 2.0
     */
    public static final float RATIO_MIN = 0.05f;

    /**
     * Maximum acceptable ratio value when adding a view
     * @since 2.0
     */
    public static final float RATIO_MAX = 0.95f;

    /**
     * The default fast view ratio width.
     * @since 2.0
     */
    public static final float DEFAULT_FASTVIEW_RATIO = 0.3f;

    /**
     * The default view ratio width for regular (non-fast) views.
     * @since 2.0
     */
    public static final float DEFAULT_VIEW_RATIO = 0.5f;

    /**
     * A variable used to represent invalid  ratios.
     * @since 2.0
     */
    public static final float INVALID_RATIO = -1f;

    /**
     * A variable used to represent a ratio which has not been specified.
     * @since 2.0
     */
    public static final float NULL_RATIO = -2f;

    /**
     * Adds an action set with the given id to this page layout.
     * The id must name an action set contributed to the workbench's extension 
     * point (named <code>"org.eclipse.ui.actionSet"</code>).
     *
     * @param actionSetId the action set id
     */
    public void addActionSet(String actionSetId);

    /**
     * Adds the view with the given compound id to the page layout as a fast view.  
     * See the {@link IPageLayout} type documentation for more details about compound ids.
     * The primary id must name a view contributed to the workbench's view extension
     * point (named <code>"org.eclipse.ui.views"</code>).
     * 
     * @param viewId the compound id of the view to be added
     * @since 2.0
     */
    public void addFastView(String viewId);

    /**
     * Adds the view with the given compound id to the page layout as a fast view
     * with the given width ratio. 
     * See the {@link IPageLayout} type documentation for more details about compound ids.
     * The primary id must name a view contributed to the workbench's view extension 
     * point (named <code>"org.eclipse.ui.views"</code>).
     * 
     * @param viewId the compound id of the view to be added
     * @param ratio the percentage of the workbench the fast view will cover
     * @since 2.0
     */
    public void addFastView(String viewId, float ratio);

    /**
     * Adds a new wizard shortcut to the page layout.
     * These are typically shown in the UI to allow rapid navigation to appropriate new wizards.  
     * For example, in the Eclipse IDE, these appear as items under the File > New menu.
     * The id must name a new wizard extension contributed to the 
     * workbench's new wizards extension point (named <code>"org.eclipse.ui.newWizards"</code>).
     *
     * @param id the wizard id
     */
    public void addNewWizardShortcut(String id);

    /**
     * Adds a perspective shortcut to the page layout.
     * These are typically shown in the UI to allow rapid navigation to appropriate new wizards.  
     * For example, in the Eclipse IDE, these appear as items under the Window > Open Perspective menu.
     * The id must name a perspective extension contributed to the 
     * workbench's perspectives extension point (named <code>"org.eclipse.ui.perspectives"</code>).
     *
     * @param id the perspective id
     */
    public void addPerspectiveShortcut(String id);

    /**
     * Adds a view placeholder to this page layout.
     * A view placeholder is used to define the position of a view before the view
     * appears.  Initially, it is invisible; however, if the user ever opens a view
     * whose compound id matches the placeholder, the view will appear at the same
     * location as the placeholder.  
     * See the {@link IPageLayout} type documentation for more details about compound ids.
     * If the placeholder contains wildcards, it remains in the layout, otherwise 
     * it is replaced by the view.
     * If the primary id of the placeholder has no wildcards, it must refer to a view 
     * contributed to the workbench's view extension point 
     * (named <code>"org.eclipse.ui.views"</code>).
     *
     * @param viewId the compound view id (wildcards allowed)
     * @param relationship the position relative to the reference part;
     *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
     *  or <code>RIGHT</code>
     * @param ratio a ratio specifying how to divide the space currently occupied by the reference part,
     *    in the range <code>0.05f</code> to <code>0.95f</code>.
     *    Values outside this range will be clipped to facilitate direct manipulation.
     *    For a vertical split, the part on top gets the specified ratio of the current space
     *    and the part on bottom gets the rest.
     *    Likewise, for a horizontal split, the part at left gets the specified ratio of the current space
     *    and the part at right gets the rest.
     * @param refId the id of the reference part; either a view id, a folder id,
     *   or the special editor area id returned by <code>getEditorArea</code>
     */
    public void addPlaceholder(String viewId, int relationship, float ratio,
            String refId);

    /**
     * Adds an item to the Show In prompter.
     * The id must name a view contributed to the workbench's view extension point 
     * (named <code>"org.eclipse.ui.views"</code>).
     *
     * @param id the view id
     * 
     * @since 2.1
     */
    public void addShowInPart(String id);

    /**
     * Adds a show view shortcut to the page layout.
     * These are typically shown in the UI to allow rapid navigation to appropriate views.  
     * For example, in the Eclipse IDE, these appear as items under the Window > Show View menu.
     * The id must name a view contributed to the workbench's views extension point 
     * (named <code>"org.eclipse.ui.views"</code>).
     *
     * @param id the view id
     */
    public void addShowViewShortcut(String id);

    /**
     * Adds a view with the given compound id to this page layout.
     * See the {@link IPageLayout} type documentation for more details about compound ids.
     * The primary id must name a view contributed to the workbench's view extension point 
     * (named <code>"org.eclipse.ui.views"</code>).
     *
     * @param viewId the compound view id
     * @param relationship the position relative to the reference part;
     *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
     *  or <code>RIGHT</code>
     * @param ratio a ratio specifying how to divide the space currently occupied by the reference part,
     *    in the range <code>0.05f</code> to <code>0.95f</code>.
     *    Values outside this range will be clipped to facilitate direct manipulation.
     *    For a vertical split, the part on top gets the specified ratio of the current space
     *    and the part on bottom gets the rest.
     *    Likewise, for a horizontal split, the part at left gets the specified ratio of the current space
     *    and the part at right gets the rest.
     * @param refId the id of the reference part; either a view id, a folder id,
     *   or the special editor area id returned by <code>getEditorArea</code>
     */
    public void addView(String viewId, int relationship, float ratio,
            String refId);

    /**
     * Creates and adds a new folder with the given id to this page layout.
     * The position and relative size of the folder is expressed relative to
     * a reference part.
     *
     * @param folderId the id for the new folder.  This must be unique within
     *  the layout to avoid collision with other parts.
     * @param relationship the position relative to the reference part;
     *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
     *  or <code>RIGHT</code>
     * @param ratio a ratio specifying how to divide the space currently occupied by the reference part,
     *    in the range <code>0.05f</code> to <code>0.95f</code>.
     *    Values outside this range will be clipped to facilitate direct manipulation.
     *    For a vertical split, the part on top gets the specified ratio of the current space
     *    and the part on bottom gets the rest.
     *    Likewise, for a horizontal split, the part at left gets the specified ratio of the current space
     *    and the part at right gets the rest.
     * @param refId the id of the reference part; either a view id, a folder id,
     *   or the special editor area id returned by <code>getEditorArea</code>
     * @return the new folder
     */
    public IFolderLayout createFolder(String folderId, int relationship,
            float ratio, String refId);

    /**
     * Creates and adds a placeholder for a new folder with the given id to this page layout.
     * The position and relative size of the folder is expressed relative to
     * a reference part.
     * 
     * @param folderId the id for the new folder.  This must be unique within
     *  the layout to avoid collision with other parts.
     * @param relationship the position relative to the reference part;
     *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
     *  or <code>RIGHT</code>
     * @param ratio a ratio specifying how to divide the space currently occupied by the reference part,
     *    in the range <code>0.05f</code> to <code>0.95f</code>.
     *    Values outside this range will be clipped to facilitate direct manipulation.
     *    For a vertical split, the part on top gets the specified ratio of the current space
     *    and the part on bottom gets the rest.
     *    Likewise, for a horizontal split, the part at left gets the specified ratio of the current space
     *    and the part at right gets the rest.
     * @param refId the id of the reference part; either a view id, a folder id,
     *   or the special editor area id returned by <code>getEditorArea</code>
     * @return a placeholder for the new folder
     * @since 2.0
     */
    public IPlaceholderFolderLayout createPlaceholderFolder(String folderId,
            int relationship, float ratio, String refId);

    /**
     * Returns the special identifier for the editor area in this page 
     * layout.  The identifier for the editor area is also stored in
     * <code>ID_EDITOR_AREA</code>.
     * <p>
     * The editor area is automatically added to each layout before anything else.
     * It should be used as the point of reference when adding views to a layout.
     * </p>
     *
     * @return the special id of the editor area
     */
    public String getEditorArea();

    /**
     * Returns whether the page's layout will show
     * the editor area.
     *
     * @return <code>true</code> when editor area visible, <code>false</code> otherwise
     */
    public boolean isEditorAreaVisible();

    /**
     * Show or hide the editor area for the page's layout.
     *
     * @param showEditorArea <code>true</code> to show the editor area, <code>false</code> to hide the editor area
     */
    public void setEditorAreaVisible(boolean showEditorArea);

    /**
     * Returns the number of open editors before reusing editors or -1 if the 
     * preference settings should be used instead.
     *
     * @return the number of open editors before reusing editors or -1 if the 
     * preference settings should be used instead.
     * 
     * @deprecated this always returns -1 as of Eclipse 2.1
     */
    public int getEditorReuseThreshold();

    /**
     * Sets the number of open editors before reusing editors.
     * If < 0 the user preference settings will be used.
     * 
     * @param openEditors the number of open editors
     * 
     * @deprecated this method has no effect, as of Eclipse 2.1
     */
    public void setEditorReuseThreshold(int openEditors);

    /**
     * Sets whether this layout is fixed.
     * In a fixed layout, layout parts cannot be moved or zoomed, and the initial
     * set of views cannot be closed.
     *
     * @param isFixed <code>true</code> if this layout is fixed, <code>false</code> if not
     * @since 3.0
     */
    public void setFixed(boolean isFixed);

    /**
     * Returns <code>true</code> if this layout is fixed, <code>false</code> if not.
     * In a fixed layout, layout parts cannot be moved or zoomed, and the initial
     * set of views cannot be closed.
     * The default is <code>false</code>.
     * 
     * @return <code>true</code> if this layout is fixed, <code>false</code> if not.
     * @since 3.0
     */
    public boolean isFixed();

    /**
     * Returns the layout for the view or placeholder with the given compound id in
     * this page layout.
     * See the {@link IPageLayout} type documentation for more details about compound ids.
     * Returns <code>null</code> if the specified view or placeholder is unknown to the layout.
     * 
     * @param id the compound view id or placeholder
     * @return the view layout, or <code>null</code>
     * @since 3.0
     */
    public IViewLayout getViewLayout(String id);

    /**
     * Adds a standalone view with the given compound id to this page layout.
     * See the {@link IPageLayout} type documentation for more details about compound ids.
     * A standalone view cannot be docked together with other views.
     * A standalone view's title can optionally be hidden.  If hidden,
     * then any controls typically shown with the title (such as the close button) 
     * are also hidden.  Any contributions or other content from the view itself
     * are always shown (e.g. toolbar or view menu contributions, content description).
     * <p>
     * The id must name a view contributed to the workbench's view extension point 
     * (named <code>"org.eclipse.ui.views"</code>).
     * </p>
     *
     * @param viewId the compound view id
     * @param showTitle <code>true</code> to show the title and related controls,
     *  <code>false</code> to hide them
     * @param relationship the position relative to the reference part;
     *  one of <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
     *  or <code>RIGHT</code>
     * @param ratio a ratio specifying how to divide the space currently occupied by the reference part,
     *    in the range <code>0.05f</code> to <code>0.95f</code>.
     *    Values outside this range will be clipped to facilitate direct manipulation.
     *    For a vertical split, the part on top gets the specified ratio of the current space
     *    and the part on bottom gets the rest.
     *    Likewise, for a horizontal split, the part at left gets the specified ratio of the current space
     *    and the part at right gets the rest.
     * @param refId the id of the reference part; either a view id, a folder id,
     *   or the special editor area id returned by <code>getEditorArea</code>
     * 
     * @since 3.0
     */
    public void addStandaloneView(String viewId, boolean showTitle,
            int relationship, float ratio, String refId);
    
    /**
	 * Adds a standalone view placeholder to this page layout. A view
	 * placeholder is used to define the position of a view before the view
	 * appears. Initially, it is invisible; however, if the user ever opens a
	 * view whose compound id matches the placeholder, the view will appear at
	 * the same location as the placeholder. See the {@link IPageLayout} type
	 * documentation for more details about compound ids. If the placeholder
	 * contains wildcards, it remains in the layout, otherwise it is replaced by
	 * the view. If the primary id of the placeholder has no wildcards, it must
	 * refer to a view contributed to the workbench's view extension point
	 * (named <code>"org.eclipse.ui.views"</code>).
	 * 
	 * @param viewId
	 *            the compound view id (wildcards allowed)
	 * @param relationship
	 *            the position relative to the reference part; one of
	 *            <code>TOP</code>, <code>BOTTOM</code>, <code>LEFT</code>,
	 *            or <code>RIGHT</code>
	 * @param ratio
	 *            a ratio specifying how to divide the space currently occupied
	 *            by the reference part, in the range <code>0.05f</code> to
	 *            <code>0.95f</code>. Values outside this range will be
	 *            clipped to facilitate direct manipulation. For a vertical
	 *            split, the part on top gets the specified ratio of the current
	 *            space and the part on bottom gets the rest. Likewise, for a
	 *            horizontal split, the part at left gets the specified ratio of
	 *            the current space and the part at right gets the rest.
	 * @param refId
	 *            the id of the reference part; either a view id, a folder id,
	 *            or the special editor area id returned by
	 *            <code>getEditorArea</code>
	 * @param showTitle
	 *            true to show the view's title, false if not
	 *            
	 * @since 3.2
	 */
    public void addStandaloneViewPlaceholder(String viewId, int relationship,
			float ratio, String refId, boolean showTitle);


    /**
	 * Returns the perspective descriptor for the perspective being layed out.
	 * 
	 * @return the perspective descriptor for the perspective being layed out
	 * @since 3.2
	 */
    public IPerspectiveDescriptor getDescriptor();
    
    /**
	 * Returns the folder layout for the view or placeholder with the given
	 * compound id in this page layout. See the {@link IPageLayout} type
	 * documentation for more details about compound ids. Returns
	 * <code>null</code> if the specified view or placeholder is unknown to
	 * the layout, or the placeholder was not in a folder.
	 * 
	 * @param id
	 *            the compound view id or placeholder. Must not be
	 *            <code>null</code>.
	 * @return the folder layout, or <code>null</code>
	 * @since 3.3
	 */
    public IPlaceholderFolderLayout getFolderForView(String id);
}
