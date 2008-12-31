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

	public void run() {
		fPage.setLayout(fLayout);
	}

	public int getLayout() {
		return fLayout;
	}
}
