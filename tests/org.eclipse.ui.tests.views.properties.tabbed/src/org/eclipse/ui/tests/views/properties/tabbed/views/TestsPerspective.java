/*******************************************************************************
 * Copyright (c) 2007, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.views.properties.tabbed.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.text.TextTestsView;

/**
 * Perspective used by the tabbed properties view test JUnit tests.
 *
 * @since 3.3
 */
public class TestsPerspective implements IPerspectiveFactory {

	public static final String TESTS_PERSPECTIVE_ID = "org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout topLeft = layout.createFolder(
				"topLeft", IPageLayout.LEFT, (float) 0.50, editorArea);//$NON-NLS-1$
		topLeft.addView(TestsView.TESTS_VIEW_ID);
		IFolderLayout middleLeft = layout.createFolder(
				"middleLeft", IPageLayout.BOTTOM, (float) 0.33, "topLeft");//$NON-NLS-1$
		middleLeft.addView(OverrideTestsView.OVERRIDE_TESTS_VIEW_ID);
		IFolderLayout bottomLeft = layout.createFolder(
				"bottomLeft", IPageLayout.BOTTOM, (float) 0.40, "middleLeft");//$NON-NLS-1$//$NON-NLS-2$
		bottomLeft.addView(DynamicTestsView.DYNAMIC_TESTS_VIEW_ID);
		IFolderLayout top = layout.createFolder(
				"top", IPageLayout.TOP, (float) 0.25, editorArea);//$NON-NLS-1$
		top.addView(TextTestsView.TEXT_TESTS_VIEW_ID);
		IFolderLayout bottom = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, (float) 0.25,//$NON-NLS-1$
				editorArea);
		bottom.addView(IPageLayout.ID_PROP_SHEET);
	}

}
