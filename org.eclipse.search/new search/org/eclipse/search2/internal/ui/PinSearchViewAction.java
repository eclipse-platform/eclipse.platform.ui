/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.texteditor.IUpdate;

import org.eclipse.search.internal.ui.SearchPluginImages;

/**
 * Pins the currently visible search view
 */
public class PinSearchViewAction extends Action implements IUpdate {

	private SearchView fView = null;

	/**
	 * Constructs a 'pin console' action
	 * @param view the search view
	 */
	public PinSearchViewAction(SearchView view) {
		super(SearchMessages.PinSearchViewAction_label, IAction.AS_CHECK_BOX);
		setToolTipText(SearchMessages.PinSearchViewAction_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_PIN_VIEW);
		fView = view;
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fView.setPinned(isChecked());
		fView.updatePartName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		//setEnabled(fView.getConsole() != null);
		setChecked(fView.isPinned());
	}
}
