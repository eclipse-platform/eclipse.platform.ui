package org.eclipse.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
/**
 * An <code>IFolderLayout</code> is used to define the initial pages within a folder.
 * The folder itself is component within an <code>IPageLayout</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPageLayout#createFolder
 */
public interface IFolderLayout {
/**
 * Adds an invisible placeholder for a view with the given id to this folder.
 * A view placeholder is used to define the position of a view before the view
 * appears.  Initially, it is invisible; however, if the user ever opens a view
 * with the same id as a placeholder, the view will replace the placeholder
 * as it is being made visible.
 * The id must name a view contributed to the workbench's view extension point 
 * (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param viewId the view id
 */
public void addPlaceholder(String viewId);
/**
 * Adds a view with the given id to this folder.
 * The id must name a view contributed to the workbench's view extension point 
 * (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param viewId the view id
 */
public void addView(String viewId);
}
