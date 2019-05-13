/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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

package org.eclipse.ui.tests.views.properties.tabbed.decorations.views;

import org.eclipse.ui.tests.views.properties.tabbed.decorations.TabbedPropertySheetPageWithDecorations;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsView;
import org.eclipse.ui.views.properties.IPropertySheetPage;

public class DecorationTestsView extends TestsView {

	public static final String DECORATION_TESTS_VIEW_ID = "org.eclipse.ui.tests.views.properties.tabbed.decorations.views.DecorationTestsView"; //$NON-NLS-1$

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class) {
			if (tabbedPropertySheetPage == null) {
				tabbedPropertySheetPage = new TabbedPropertySheetPageWithDecorations(this);
			}
			return tabbedPropertySheetPage;
		}
		return super.getAdapter(adapter);
	}

}
