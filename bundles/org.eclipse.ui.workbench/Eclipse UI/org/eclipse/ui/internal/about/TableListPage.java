/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *      font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import java.net.URL;
import java.util.Collection;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.internal.ConfigureColumnsDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.about.IInstallationPageSources;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Displays information about the product plugins.
 * 
 * PRIVATE This class is internal to the workbench and must not be called
 * outside the workbench.
 */
abstract class TableListPage extends ProductInfoPage {

	InstallationDialogSourceProvider sourceProvider;

	public void init(IServiceLocator locator) {
		super.init(locator);
		// cache the source provider and prime the selection variable
		ISourceProviderService sps = (ISourceProviderService) locator
				.getService(ISourceProviderService.class);
		sourceProvider = (InstallationDialogSourceProvider) sps
				.getSourceProvider(IInstallationPageSources.ACTIVE_PAGE_SELECTION);
		selectionChanged();
	}

	public void handleColumnsPressed() {
		ConfigureColumnsDialog d = new ConfigureColumnsDialog(this, getTable());
		d.open();
	}

	protected abstract URL getURL();

	protected abstract Table getTable();

	protected void selectionChanged() {
		Object selection = getSelectionValue();
		if (selection == null)
			selection = IEvaluationContext.UNDEFINED_VARIABLE;
		// This is an ugly hack, but necessary for maintaining different
		// selection variables in different contexts.
		if (getInstallationDialog() instanceof ProductInfoDialog)
			sourceProvider.setProductDialogPageSelection(selection);
		else
			sourceProvider.setPageSelection(selection);
	}

	protected abstract Collection getSelectionValue();
}
