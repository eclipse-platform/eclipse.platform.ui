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
package org.eclipse.ui.actions;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.FeatureSelectionDialog;
import org.eclipse.ui.internal.dialogs.WelcomeEditorInput;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * The quick start (Welcome...) action.
 */
public class QuickStartAction
		extends Action 
		implements ActionFactory.IWorkbenchAction {

	private static final String EDITOR_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditor"; //$NON-NLS-1$

	/**
	 * The workbench window; or <code>null</code> if this
	 * action has been <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;
	
	/**
	 * Creates an instance of this action, for use in the given window.
	 */
	public QuickStartAction(IWorkbenchWindow window) {
		super(IDEWorkbenchMessages.getString("QuickStart.text")); //$NON-NLS-1$
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		setToolTipText(IDEWorkbenchMessages.getString("QuickStart.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.QUICK_START_ACTION);
		setActionDefinitionId("org.eclipse.ui.help.quickStartAction"); //$NON-NLS-1$
	}
	
	/**
	 * The user has invoked this action.  Prompts for a feature with a welcome page, 
	 * then opens the corresponding welcome page.
	 */
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		AboutInfo feature = promptForFeature();
		if (feature != null) {
			openWelcomePage(feature);
		}
	}
	
	/**
	 * Prompts the user for a feature that has a welcome page.
	 * 
	 * @return the chosen feature, or <code>null</code> if none was chosen
	 */
	private AboutInfo promptForFeature() {
		// Ask the user to select a feature
		// @issue access to AboutInfo
		AboutInfo[] features = PlatformUI.getWorkbench().getConfigurationInfo().getFeaturesInfo();
		ArrayList welcomeFeatures = new ArrayList();
		for (int i = 0; i < features.length; i++) {
			if (features[i].getWelcomePageURL() != null)
				welcomeFeatures.add(features[i]);
		}

		Shell shell = workbenchWindow.getShell();

		if (welcomeFeatures.size() == 0) {
			MessageDialog.openInformation(
				shell, 
				IDEWorkbenchMessages.getString("QuickStartMessageDialog.title"), //$NON-NLS-1$
				IDEWorkbenchMessages.getString("QuickStartMessageDialog.message")); //$NON-NLS-1$
			return null;
		}

		features = new AboutInfo[welcomeFeatures.size()];
		welcomeFeatures.toArray(features);
		// @issue access to AboutInfo
		AboutInfo primaryFeature = PlatformUI.getWorkbench().getConfigurationInfo().getAboutInfo();

		FeatureSelectionDialog d = new FeatureSelectionDialog(
			shell,
			features,
			primaryFeature,
			"WelcomePageSelectionDialog.title",  //$NON-NLS-1$
			"WelcomePageSelectionDialog.message", //$NON-NLS-1$
			IHelpContextIds.WELCOME_PAGE_SELECTION_DIALOG);
		if (d.open() != Dialog.OK || d.getResult().length != 1)
			return null;
		return (AboutInfo) d.getResult()[0];
	}
	
	/**
	 * Opens the welcome page for the given feature.
	 * 
	 * @param feature the about info for the feature
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */	
	public boolean openWelcomePage(String featureId) {
		AboutInfo feature = findFeature(featureId);
		if (feature == null || feature.getWelcomePageURL() == null) {
			return false;
		}
		return openWelcomePage(feature);
	}

	/**
	 * Returns the about info for the feature with the given id, or <code>null</code>
	 * if there is no such feature.
	 * 
	 * @return the about info for the feature with the given id, or <code>null</code>
	 *   if there is no such feature.
	 */
	private AboutInfo findFeature(String featureId) {
		// @issue access to AboutInfo
		AboutInfo[] features = PlatformUI.getWorkbench().getConfigurationInfo().getFeaturesInfo();
		for (int i = 0; i < features.length; i++) {
			AboutInfo info = features[i];
			if (info.getFeatureId().equals(featureId)) {
				return info;
			}
		}
		return null;
	}
	
	/**
	 * Opens the welcome page for a feature.
	 * 
	 * @param feature the about info for the feature
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */	
	private boolean openWelcomePage(AboutInfo feature) {
		IWorkbenchPage page = null;

		// See if the feature wants a specific perspective
		// @issue access to welcome perspective for feature
		String perspectiveId = feature.getWelcomePerspective();

		if (perspectiveId == null) {
			// Just use the current perspective unless one is not open 
			// in which case use the default
			page = workbenchWindow.getActivePage();

			if (page == null || page.getPerspective() == null) {
				perspectiveId = PlatformUI.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
			}
		}

		if (perspectiveId != null) {
			try {
				page = PlatformUI.getWorkbench().showPerspective(perspectiveId, workbenchWindow);
			} catch (WorkbenchException e) {
				return false;
			}
		}

		page.setEditorAreaVisible(true);

		// create input
		WelcomeEditorInput input = new WelcomeEditorInput(feature);

		// see if we already have a welcome editorz
		IEditorPart editor = page.findEditor(input);
		if (editor != null) {
			page.activate(editor);
			return true;
		}

		try {
			page.openEditor(input, EDITOR_ID);
		} catch (PartInitException e) {
			IStatus status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
			ErrorDialog.openError(
			workbenchWindow.getShell(),
				IDEWorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
				IDEWorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
				status);
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on ActionFactory.IWorkbenchAction.
	 * @since 3.0
	 */
	public void dispose() {
		if (workbenchWindow == null) {
			// action has already been disposed
			return;
		}
		workbenchWindow = null;
	}

}
