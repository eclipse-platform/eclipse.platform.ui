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

import org.eclipse.jface.action.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.internal.ui.*;
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
		UpdatesSearchCategory category = new UpdatesSearchCategory();
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
