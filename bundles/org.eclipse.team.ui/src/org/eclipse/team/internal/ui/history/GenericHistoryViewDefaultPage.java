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
import org.eclipse.team.ui.history.IHistoryPageSite;

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

	public boolean showHistory(Object object, boolean refetch) {
		//no history to show
		return false;
	}

	public boolean canShowHistoryFor(Object object) {
		return false;
	}

	public void refresh() {
		//nothing to refresh
	}

	public String getName() {
		return ""; //$NON-NLS-1$
	}

	public void setSite(IHistoryPageSite  viewSite) {
		//nothing to set
	}

}
