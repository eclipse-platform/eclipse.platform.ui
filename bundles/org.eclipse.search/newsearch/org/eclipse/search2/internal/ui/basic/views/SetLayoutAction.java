/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

public class SetLayoutAction extends Action {

	private AbstractTextSearchViewPage fPage;
	private int fLayout;

	public SetLayoutAction(AbstractTextSearchViewPage page, String label, String tooltip, int layout) {
		super(label,  IAction.AS_RADIO_BUTTON);
		fPage= page;
		setToolTipText(tooltip);
		fLayout= layout;
	}

	@Override
	public void run() {
		fPage.setLayout(fLayout);
	}

	public int getLayout() {
		return fLayout;
	}
}
