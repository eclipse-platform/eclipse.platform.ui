/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPreferencePage;


/**
 * Opens the search preferences dialog
 */
public class OpenSearchPreferencesAction extends Action {
	public OpenSearchPreferencesAction() {
		super(SearchMessages.OpenSearchPreferencesAction_label);
		setToolTipText(SearchMessages.OpenSearchPreferencesAction_tooltip);
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.OPEN_PREFERENCES_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		Shell shell= SearchPlugin.getActiveWorkbenchShell();
		String[] displayedPages= { SearchPreferencePage.PAGE_ID,
				"org.eclipse.ui.editors.preferencePages.Annotations", //$NON-NLS-1$
				"org.eclipse.ui.preferencePages.ColorsAndFonts" //$NON-NLS-1$
		};
		PreferencesUtil.createPreferenceDialogOn(shell, SearchPreferencePage.PAGE_ID, displayedPages, null).open();
	}

}
