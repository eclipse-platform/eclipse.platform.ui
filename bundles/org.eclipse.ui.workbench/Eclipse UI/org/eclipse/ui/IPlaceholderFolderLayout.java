/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

 
/**
 * An <code>IPlaceholderFolderLayout</code> is used to define the initial
 * places for page placeholders within a folder placeholder. The folder itself is a component within
 * an <code>IPageLayout</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPageLayout#createPlaceholderFolder
 * @since 2.0
 */
public interface IPlaceholderFolderLayout {
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
}
