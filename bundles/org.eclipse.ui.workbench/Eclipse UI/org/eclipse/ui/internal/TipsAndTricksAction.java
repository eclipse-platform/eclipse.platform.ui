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

package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.IHelp;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Launch the tips and tricks action.
 */
public class TipsAndTricksAction extends PartEventAction {
	private IWorkbenchWindow window;

	/**
	 *	Create an instance of this class
	 */
	public TipsAndTricksAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("TipsAndTricks.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("TipsAndTricks.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.TIPS_AND_TRICKS_ACTION);
		this.window = window;
	}

	/**
	 *	The user has invoked this action
	 */
	public void run() {
		// Ask the user to select a feature
		AboutInfo[] features = ((Workbench) window.getWorkbench()).getConfigurationInfo().getFeaturesInfo();
		ArrayList tipsAndTricksFeatures = new ArrayList();
		for (int i = 0; i < features.length; i++) {
			if (features[i].getTipsAndTricksHref() != null)
				tipsAndTricksFeatures.add(features[i]);
		}

		if (window == null)
			return;
		Shell shell = window.getShell();

		if (tipsAndTricksFeatures.size() == 0) {
			MessageDialog.openInformation(
				shell, 
				WorkbenchMessages.getString("TipsAndTricksMessageDialog.title"), //$NON-NLS-1$
				WorkbenchMessages.getString("TipsAndTricksMessageDialog.message")); //$NON-NLS-1$
			return;
		}

		features = new AboutInfo[tipsAndTricksFeatures.size()];
		tipsAndTricksFeatures.toArray(features);
		AboutInfo primaryFeature = ((Workbench) window.getWorkbench()).getConfigurationInfo().getAboutInfo();

		FeatureSelectionDialog d = new FeatureSelectionDialog(
			shell, 
			features, 
			primaryFeature, 
			"TipsAndTricksPageSelectionDialog.title", //$NON-NLS-1$
			"TipsAndTricksPageSelectionDialog.message", //$NON-NLS-1$
			IHelpContextIds.TIPS_AND_TRICKS_PAGE_SELECTION_DIALOG);

		if (d.open() != Dialog.OK || d.getResult().length != 1)
			return;

		AboutInfo feature = (AboutInfo) d.getResult()[0];

		/**
		 * Open the tips and trick help topic
		 */
		if (feature != null) {
			final String href = feature.getTipsAndTricksHref();
			if (href != null) {
				BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
					public void run() {
						final IHelp helpSupport = WorkbenchHelp.getHelpSupport();
						if (helpSupport != null) {
							helpSupport.displayHelpResource(href);
						}
					}
				});
			} else {
				IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("TipsAndTricksAction.hrefNotDefined"), null); //$NON-NLS-1$
				ErrorDialog.openError(
					shell, 
					WorkbenchMessages.getString("TipsAndTricksErrorDialog.title"), //$NON-NLS-1$
					WorkbenchMessages.getString("TipsAndTricksErrorDialog.noHref"), //$NON-NLS-1$
					status);
			}
		} else {
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("TipsAndTricksAction.hrefNotDefined"), null); //$NON-NLS-1$
			ErrorDialog.openError(
				shell, 
				WorkbenchMessages.getString("TipsAndTricksErrorDialog.title"), //$NON-NLS-1$
				WorkbenchMessages.getString("TipsAndTricksErrorDialog.noFeatures"), //$NON-NLS-1$
				status);
		}
	}
}
