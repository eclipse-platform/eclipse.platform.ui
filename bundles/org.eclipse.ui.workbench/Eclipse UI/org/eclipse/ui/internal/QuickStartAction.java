/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.WelcomeEditorInput;

/**
 * Launch the quick start action.
 */
public class QuickStartAction extends PartEventAction {
	private static final String EDITOR_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditor"; //$NON-NLS-1$

	private IWorkbenchWindow window;

	/**
	 *	Create an instance of this class.
	 *  <p>
	 * 	This consructor added to support calling the action from the welcome page
	 *  </p>
	 */
	public QuickStartAction() {
		this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}
	/**
	 *	Create an instance of this class
	 */
	public QuickStartAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("QuickStart.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("QuickStart.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.QUICK_START_ACTION);
		this.window = window;
	}
	/**
	 *	The user has invoked this action
	 */
	public void run() {
		// Ask the user to select a feature
		AboutInfo[] features = ((Workbench) window.getWorkbench()).getConfigurationInfo().getFeaturesInfo();
		ArrayList welcomeFeatures = new ArrayList();
		for (int i = 0; i < features.length; i++) {
			if (features[i].getWelcomePageURL() != null)
				welcomeFeatures.add(features[i]);
		}

		if (window == null)
			return;

		Shell shell = window.getShell();

		if (welcomeFeatures.size() == 0) {
			MessageDialog.openInformation(
				shell, 
				WorkbenchMessages.getString("QuickStartMessageDialog.title"), //$NON-NLS-1$
				WorkbenchMessages.getString("QuickStartMessageDialog.message")); //$NON-NLS-1$
			return;
		}

		features = new AboutInfo[welcomeFeatures.size()];
		welcomeFeatures.toArray(features);
		AboutInfo primaryFeature = ((Workbench) window.getWorkbench()).getConfigurationInfo().getAboutInfo();

		FeatureSelectionDialog d = new FeatureSelectionDialog(
			shell,
			features,
			primaryFeature,
			"WelcomePageSelectionDialog.title",  //$NON-NLS-1$
			"WelcomePageSelectionDialog.message", //$NON-NLS-1$
			IHelpContextIds.WELCOME_PAGE_SELECTION_DIALOG);
		if (d.open() != Dialog.OK || d.getResult().length != 1)
			return;

		AboutInfo feature = (AboutInfo) d.getResult()[0];

		IWorkbenchPage page = null;

		// See if the feature wants a specific perspective
		String perspectiveId = feature.getWelcomePerspective();

		if (perspectiveId == null) {
			// Just use the current perspective unless one is not open 
			// in which case use the default
			page = window.getActivePage();

			if (page == null || page.getPerspective() == null) {
				perspectiveId = WorkbenchPlugin.getDefault().getPerspectiveRegistry().getDefaultPerspective();
			}
		}

		if (perspectiveId != null) {
			try {
				page = (WorkbenchPage) window.getWorkbench().showPerspective(perspectiveId, window);
			} catch (WorkbenchException e) {
				return;
			}
		}

		page.setEditorAreaVisible(true);

		// create input
		WelcomeEditorInput input = new WelcomeEditorInput(feature);

		// see if we already have a welcome editor
		IEditorPart editor = page.findEditor(input);
		if (editor != null) {
			page.activate(editor);
			return;
		}

		try {
			page.openEditor(input, EDITOR_ID);
		} catch (PartInitException e) {
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
			ErrorDialog.openError(
				window.getShell(),
				WorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
				WorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
				status);
		}
	}
}
