/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

/**
 * An <code>IFolderLayout</code> is used to define the initial views within a
 * folder. The folder itself is contained within an <code>IPageLayout</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPageLayout#createFolder
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFolderLayout extends IPlaceholderFolderLayout {
	/**
	 * Adds a view with the given compound id to this folder. See the
	 * {@link IPageLayout} type documentation for more details about compound ids.
	 * The primary id must name a view contributed to the workbench's view extension
	 * point (named <code>"org.eclipse.ui.views"</code>).
	 *
	 * @param viewId the view id
	 */
	void addView(String viewId);
}
