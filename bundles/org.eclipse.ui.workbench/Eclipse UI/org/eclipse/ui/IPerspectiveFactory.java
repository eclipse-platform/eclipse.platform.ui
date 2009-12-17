/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * A perspective factory generates the initial page layout and visible
 * action set for a page.
 * <p>
 * When a new page is created in the workbench a perspective is used to define
 * the initial page layout.  If this is a predefined perspective (based on an extension to 
 * the workbench's perspective extension point) an <code>IPerspectiveFactory</code> 
 * is used to define the initial page layout.
 * </p><p>
 * The factory for the perspective is created and passed an <code>IPageLayout</code> 
 * where views can be added.  The default layout consists of the editor area with no 
 * additional views.  Additional views are added to the layout using 
 * the editor area as the initial point of reference.  The factory is used only briefly 
 * while a new page is created; then discarded.
 * </p><p>
 * To define a perspective clients should implement this interface and 
 * include the name of their class in an extension to the workbench's perspective 
 * extension point (named <code>"org.eclipse.ui.perspectives"</code>).  For example, 
 * the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.perspectives"&GT;
 *   &LT;perspective
 *       id="com.example.javaplugin.perspective"
 *       name="Java"
 *       class="com.example.javaplugin.JavaPerspective"&GT;
 *   &LT;/perspective&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p><p>
 * Example of populating a page with standard workbench views:
 * <pre>
 * public void createInitialLayout(IPageLayout layout) {
 *		// Get the editor area.
 *		String editorArea = layout.getEditorArea();
 *
 *		// Top left: Resource Navigator view and Bookmarks view placeholder
 *		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,
 *			editorArea);
 *		topLeft.addView(IPageLayout.ID_RES_NAV);
 *		topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 *
 *		// Bottom left: Outline view and Property Sheet view
 *		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,
 *			"topLeft");
 *		bottomLeft.addView(IPageLayout.ID_OUTLINE);
 *		bottomLeft.addView(IPageLayout.ID_PROP_SHEET);
 *
 *		// Bottom right: Task List view
 *		layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, 0.66f, editorArea);
 *	}
 * </pre>
 * </p><p>
 * Within the workbench a user may override the visible views, layout and
 * action sets of a predefined perspective to create a custom perspective.  In such cases 
 * the layout is persisted by the workbench and the factory is not used.
 * </p>
 */
public interface IPerspectiveFactory {
    /**
     * Creates the initial layout for a page.
     * <p>
     * Implementors of this method may add additional views to a
     * perspective.  The perspective already contains an editor folder
     * identified by the result of <code>IPageLayout.getEditorArea()</code>.  
     * Additional views should be added to the layout using this value as 
     * the initial point of reference.  
     * </p>
     *
     * @param layout the page layout
     */
    public void createInitialLayout(IPageLayout layout);
}
