/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class RuntimeSpyPerspective implements IPerspectiveFactory {

	public RuntimeSpyPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addView(ActivePluginsView.VIEW_ID, IPageLayout.LEFT, 0.75f, layout.getEditorArea());
		layout.addView(PluginDataSheetView.VIEW_ID, IPageLayout.RIGHT, 0.75f, ActivePluginsView.VIEW_ID);
		layout.addView(LoadedClassesView.VIEW_ID, IPageLayout.BOTTOM, 0.50f, ActivePluginsView.VIEW_ID);
		layout.addView(StackTraceView.VIEW_ID, IPageLayout.BOTTOM, 0.25f, PluginDataSheetView.VIEW_ID);
	}
}