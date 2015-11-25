/*******************************************************************************
 * Copyright (c) 2014-2015 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

public class OpenFolderCommand extends AbstractHandler {

	private Shell shell;

	public OpenFolderCommand() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		this.shell = workbench.getActiveWorkbenchWindow().getShell();
		DirectoryDialog directoryDialog = new DirectoryDialog(shell);
		directoryDialog.setText(DataTransferMessages.SmartImportWizardPage_selectFolderOrArchiveToImport);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		directoryDialog.setFilterPath(workspaceRoot.getLocation().toFile().toString());
		ISelection sel = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
		IStructuredSelection structuredSel = null;
		if (sel != null && sel instanceof IStructuredSelection) {
			structuredSel = (IStructuredSelection)sel;
			if (!structuredSel.isEmpty()) {
				File selectedFile = SmartImportWizard.toFile(structuredSel.getFirstElement());
				if (selectedFile != null) {
					directoryDialog.setFilterPath(selectedFile.getAbsolutePath());
				}
			}
		}
		String res = directoryDialog.open();
		if (res == null) {
			return null;
		}
		SmartImportWizard wizard = new SmartImportWizard();
		final File directory = new File(res);
		wizard.setInitialImportSource(directory);
		// inherit workingSets
		final Path asPath = new Path(directory.getAbsolutePath());
		IProject parentProject = null;
		for (IProject project : workspaceRoot.getProjects()) {
			if (project.getLocation().isPrefixOf(asPath) && (parentProject == null || parentProject.getLocation().isPrefixOf(project.getLocation())) ) {
				parentProject = project;
			}
		}
		Set<IWorkingSet> initialWorkingSets = new HashSet<>();
		if (parentProject != null) {
			for (IWorkingSet workingSet : workbench.getWorkingSetManager().getAllWorkingSets()) {
				for (IAdaptable element : workingSet.getElements()) {
					if (element.equals(parentProject)) {
						initialWorkingSets.add(workingSet);
					}
				}
			}
		}
		if (initialWorkingSets.isEmpty()) {
			wizard.init(workbench, structuredSel);
		} else {
			wizard.setInitialWorkingSets(initialWorkingSets);
		}
		new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard).open();
		return null;
	}

}