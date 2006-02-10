/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ui.history;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.ui.history.HistoryPage;

public class GenericHistoryViewDefaultPage extends HistoryPage {

	private Composite pgComp;

	public void createControl(Composite parent) {
		pgComp = new Composite(parent, SWT.NULL);
		pgComp.setLayout(new FillLayout());
		pgComp.setBackground(JFaceColors.getBannerBackground(pgComp.getDisplay()));
	}

	public Control getControl() {
		return pgComp;
	}

	public void setFocus() {
		pgComp.setFocus();
	}

	public boolean isValidInput(Object object) {
		return false;
	}

	public void refresh() {
		//nothing to refresh
	}

	public String getName() {
		return ""; //$NON-NLS-1$
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public Object getInput() {
		return null;
	}

	public boolean inputSet() {
		//no history to show
		return false;
	}

}
