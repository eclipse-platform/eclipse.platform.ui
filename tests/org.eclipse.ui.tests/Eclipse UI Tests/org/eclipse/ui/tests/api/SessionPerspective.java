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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This class tests the persistance of a perspective.
 */
public class SessionPerspective implements IPerspectiveFactory {

	public static String ID = "org.eclipse.ui.tests.api.SessionPerspective";

	/**
	 * @see IPerspectiveFactory#createInitialLayout(IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout
				.addView(SessionView.VIEW_ID, IPageLayout.LEFT, 0.33f,
						editorArea);
		layout.addPlaceholder(MockViewPart.ID4, IPageLayout.RIGHT, .033f,
				editorArea);
	}

}

