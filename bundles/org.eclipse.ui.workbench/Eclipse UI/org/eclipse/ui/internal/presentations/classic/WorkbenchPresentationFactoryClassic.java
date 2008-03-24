/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.presentations.classic;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultSimpleTabListener;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultThemeListener;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * The classic presentation factory for the Workbench (the 3.0 look).
 * 
 * @since 3.4
 *
 */
public class WorkbenchPresentationFactoryClassic extends
		WorkbenchPresentationFactory {

	private static int viewTabPosition = WorkbenchPlugin.getDefault()
		.getPreferenceStore()
		.getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.WorkbenchPresentationFactory#createViewPresentation(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.presentations.IStackPresentationSite)
	 */
	public StackPresentation createViewPresentation(Composite parent,
			IStackPresentationSite site) {

		DefaultTabFolder folder = new DefaultTabFolder(parent, viewTabPosition
				| SWT.BORDER, site
				.supportsState(IStackPresentationSite.STATE_MINIMIZED), site
				.supportsState(IStackPresentationSite.STATE_MAXIMIZED));

		final IPreferenceStore store = PlatformUI.getPreferenceStore();
		final int minimumCharacters = store
				.getInt(IWorkbenchPreferenceConstants.VIEW_MINIMUM_CHARACTERS);
		if (minimumCharacters >= 0) {
			folder.setMinimumCharacters(minimumCharacters);
		}

		PresentablePartFolder partFolder = new PresentablePartFolder(folder);

		folder.setUnselectedCloseVisible(false);
		folder.setUnselectedImageVisible(false);

		TabbedStackPresentation result = new TabbedStackPresentation(site,
				partFolder, new StandardViewSystemMenu(site));

		DefaultThemeListener themeListener = new DefaultThemeListener(folder,
				result.getTheme());
		result.getTheme().addListener(themeListener);

		new DefaultSimpleTabListener(result.getApiPreferences(),
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
				folder);

		return result;
	}
}
