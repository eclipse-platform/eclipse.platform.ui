/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

/**
 * This perspective is used for testing api. It defines an initial layout with
 * placeholders for multi-instance views, including wildcards.
 * The placeholders are added in a folder (not a placeholder folder).
 *
 * @since 3.1
 */
public class PerspectiveWithMultiViewPlaceholdersInFolder extends PerspectiveWithMultiViewPlaceholdersInPlaceholderFolder {

	public static String PERSP_ID = "org.eclipse.ui.tests.PerspectiveWithMultiViewPlaceholdersInFolder"; //$NON-NLS-1$

	public PerspectiveWithMultiViewPlaceholdersInFolder() {
		// do nothing
	}

	@Override
	public void createInitialLayout(IPageLayout layout) {
		IFolderLayout folder = layout.createFolder("folder", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		addPlaceholders(folder);
	}
}
