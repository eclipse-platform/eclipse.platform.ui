/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Schmidt - bug 384460
 *******************************************************************************/

package org.eclipse.debug.internal.ui.importexport.breakpoints;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <p>
 * Wizard for Importing breakpoints.
 * It serves a dual purpose, in that it is used by the platform import/export wizard,
 * but it can also be used as a standalone wizard.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * IWizard wiz = new WizardImportBreakpoints();
 * wiz.init(workbench, selection);
 * WizardDialog wizdialog = new WizardDialog(shell, wiz);
 * wizdialog.open();
 * </pre>
 *
 * This class uses <code>WizardImportBreakpointsPage</code> and
 * <code>WizardImportBreakpointsSelectionPage</code>.
 *
 * @since 3.2
 *
 */
public class WizardImportBreakpoints extends Wizard implements IImportWizard {

	/*
	 * The main page
	 */
	private WizardImportBreakpointsPage fMainPage;

	private WizardImportBreakpointsSelectionPage fSelectionPage;

	/**
	 * Identifier for dialog settings section for the import wizard.
	 */
	private static final String IMPORT_DIALOG_SETTINGS = "BreakpointImportSettings"; //$NON-NLS-1$

	/**
	 * This is the default constructor
	 */
	public WizardImportBreakpoints() {
		super();
		DebugUIPlugin plugin = DebugUIPlugin.getDefault();
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection(IMPORT_DIALOG_SETTINGS);
		if (section == null) {
			section = workbenchSettings.addNewSection(IMPORT_DIALOG_SETTINGS);
		}
		setDialogSettings(section);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		fMainPage = new WizardImportBreakpointsPage(ImportExportMessages.WizardImportBreakpoints_0);
		addPage(fMainPage);
		fSelectionPage = new WizardImportBreakpointsSelectionPage(ImportExportMessages.WizardImportBreakpointsSelectionPage_0);
		addPage(fSelectionPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		fMainPage = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		List<IMarker> selectedBreakpoints = fSelectionPage.getSelectedMarkers();
		return fMainPage.finish(selectedBreakpoints);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(ImportExportMessages.WizardImportBreakpoints_0);
        setNeedsProgressMonitor(true);
	}

	@Override
	public boolean needsProgressMonitor() {
		return true;
	}
}
