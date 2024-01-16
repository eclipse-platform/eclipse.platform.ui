/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * A layout that can trigger an problem interaction between
 * {@code org.eclipse.ui.internal.presentations.util.PresentablePartFolder} and
 * {@code org.eclipse.ui.internal.presentations.defaultpresentation.EmptyTabFolder}.
 *
 * @since 3.2
 */
public class PerspectiveViewsBug120934 implements IPerspectiveFactory {
	public static final String PERSP_ID = "org.eclipse.ui.tests.api.PerspectiveViewsBug120934";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView(IPageLayout.ID_BOOKMARKS, IPageLayout.RIGHT, 0.25f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addStandaloneView(IPageLayout.ID_OUTLINE, false,
				IPageLayout.TOP, .3f, IPageLayout.ID_BOOKMARKS);
		layout.setEditorAreaVisible(false);
	}
}
