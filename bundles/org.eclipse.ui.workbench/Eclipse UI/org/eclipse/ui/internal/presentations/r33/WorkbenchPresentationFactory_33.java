/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.presentations.r33;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.presentations.util.PresentablePartFolder;
import org.eclipse.ui.internal.presentations.util.StandardViewSystemMenu;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * The is a stub implementation which allows clients to choose the new 3.3
 * 'look'. Currently this includes the new min/max behaviour as well as an image
 * based animation feedback mechanism.
 * 
 * @since 3.3
 * 
 */
public class WorkbenchPresentationFactory_33 extends
		WorkbenchPresentationFactory {

	private static int viewTabPosition = WorkbenchPlugin.getDefault()
			.getPreferenceStore()
			.getInt(IPreferenceConstants.VIEW_TAB_POSITION);

	/**
	 * Default to the superclass
	 */
	public WorkbenchPresentationFactory_33() {
		super();
	}

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
		folder.setUnselectedImageVisible(true);

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
