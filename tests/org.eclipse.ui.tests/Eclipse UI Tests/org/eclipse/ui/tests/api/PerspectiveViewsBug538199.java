/*******************************************************************************
* Copyright (c) 2018 Myself and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Mykola Zakharchuk <zakharchuk.vn@gmail.com> - Bug 538199
*******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.internal.e4.compatibility.ModeledFolderLayout;

/**
 * This perspective is used for testing visibility of parameterized relative
 * view. It does NOT contain content outline view.
 */
public class PerspectiveViewsBug538199 implements IPerspectiveFactory {

	public static String ID = "org.eclipse.ui.tests.api.PerspectiveViewsBug538199";

	public PerspectiveViewsBug538199() {
		// do nothing
	}

	@Override
	public void createInitialLayout(IPageLayout layout) {
		ModeledFolderLayout folder = (ModeledFolderLayout) layout.createFolder("left", IPageLayout.LEFT, .5f,
				IPageLayout.ID_EDITOR_AREA);
		folder.addView(MockViewPart.ID);
	}
}
