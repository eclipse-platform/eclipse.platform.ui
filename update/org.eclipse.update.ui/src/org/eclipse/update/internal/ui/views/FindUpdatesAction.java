/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.search.UnifiedUpdatesSearchCategory;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.search.*;

public class FindUpdatesAction extends Action {

	private IFeature feature;
	private Shell shell;

	public FindUpdatesAction(Shell shell, String text) {
		super(text);
		this.shell = shell;
	}
	
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	public void run() {
		UpdateSearchScope scope = new UpdateSearchScope();
		scope.setUpdateMapURL(UpdateUtils.getUpdateMapURL());
		UnifiedUpdatesSearchCategory category = new UnifiedUpdatesSearchCategory();
		final UpdateSearchRequest searchRequest = new UpdateSearchRequest(category, scope);
		searchRequest.addFilter(new EnvironmentFilter());
		if (feature!=null)
			category.setFeatures(new IFeature[] { feature });
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				InstallWizard wizard = new InstallWizard(searchRequest);
				WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
				dialog.create();
				dialog.getShell().setText("Updates");
				dialog.getShell().setSize(600, 500);
				dialog.open();
				if (wizard.isSuccessfulInstall())
					UpdateUI.requestRestart();				
			}
		});
	}
}
