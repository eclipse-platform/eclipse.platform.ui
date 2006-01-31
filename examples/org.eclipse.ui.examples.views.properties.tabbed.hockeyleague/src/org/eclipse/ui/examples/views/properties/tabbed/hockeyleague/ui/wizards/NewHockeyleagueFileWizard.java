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
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.wizards;

import java.util.Collections;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueFactory;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * This is the wizard to create a new Hockeyleague model file.
 * 
 * @author Anthony Hunter
 */
public class NewHockeyleagueFileWizard
	extends BasicNewResourceWizard {

	private NewHockeyleagueFileWizardPage mainPage;

	private IStructuredSelection selection;

	private IWorkbench workbench;

	/**
	 * The framework calls this to create the contents of the wizard.
	 */
	public void addPages() {
		// Create a page, set the title, and the initial model file name.
		//
		mainPage = new NewHockeyleagueFileWizardPage("Whatever", selection); //$NON-NLS-1$
		mainPage.setTitle("Hockey League Example File"); //$NON-NLS-1$
		mainPage.setDescription("Create a new Tabbed Properties View Hockey League Example File"); //$NON-NLS-1$
		mainPage.setFileName("example.hockeyleague"); //$NON-NLS-1$
		addPage(mainPage);

		// Try and get the resource selection to determine a current directory
		// for the file dialog.
		//
		if (selection != null && !selection.isEmpty()) {
			// Get the resource...
			//
			Object selectedElement = selection.iterator().next();
			if (selectedElement instanceof IResource) {
				// Get the resource parent, if its a file.
				//
				IResource selectedResource = (IResource) selectedElement;
				if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}

				// This gives us a directory...
				//
				if (selectedResource instanceof IFolder
					|| selectedResource instanceof IProject) {
					// Set this for the container.
					//
					mainPage.setContainerFullPath(selectedResource
						.getFullPath());

					// Make up a unique new name here.
					//
					String defaultFilename = "example"; //$NON-NLS-1$
					String defaultExtension = "hockeyleague"; //$NON-NLS-1$
					String modelFilename = defaultFilename + "." //$NON-NLS-1$
						+ defaultExtension;
					for (int i = 1; ((IContainer) selectedResource)
						.findMember(modelFilename) != null; ++i) {
						modelFilename = defaultFilename + i + "." //$NON-NLS-1$
							+ defaultExtension;
					}
					mainPage.setFileName(modelFilename);
				}
			}
		}
	}

	/**
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench aWorkbench,
			IStructuredSelection currentSelection) {
		super.init(aWorkbench, currentSelection);
		this.workbench = aWorkbench;
		this.selection = currentSelection;
		setWindowTitle("New Hockey League File"); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			// Remember the file.
			//
			final IFile hockeyleagueFile = getHockeyleagueFile();

			// Do the work within an operation.
			//
			WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

				protected void execute(IProgressMonitor progressMonitor) {
					try {
						ResourceSet resourceSet = new ResourceSetImpl();

						URI fileURI = URI
							.createPlatformResourceURI(hockeyleagueFile
								.getFullPath().toString());

						Resource resource = resourceSet.createResource(fileURI);

						// Add the initial model object to the contents.
						//
						EObject rootObject = createInitialModel();
						if (rootObject != null) {
							resource.getContents().add(rootObject);
						}

						// Save the contents of the resource to the file system.
						//
						resource.save(Collections.EMPTY_MAP);
					} catch (Exception exception) {
						exception.printStackTrace();
					} finally {
						progressMonitor.done();
					}
				}
			};

			getContainer().run(false, false, operation);

			// Select the new file resource in the current view.
			//
			IWorkbenchWindow workbenchWindow = workbench
				.getActiveWorkbenchWindow();
			IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(
					hockeyleagueFile);
				getShell().getDisplay().asyncExec(new Runnable() {

					public void run() {
						((ISetSelectionTarget) activePart)
							.selectReveal(targetSelection);
					}
				});
			}

			// Open an editor on the new file.
			//
			try {
				IDE.openEditor(page, hockeyleagueFile, true);
			} catch (PartInitException exception) {
				MessageDialog.openError(workbenchWindow.getShell(),
					"Open Editor", exception.getMessage()); //$NON-NLS-1$
				return false;
			}

			return true;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}

	public IFile getHockeyleagueFile() {
		return mainPage.getHockeyleagueFile();
	}

	private EObject createInitialModel() {
		EClass eClass = HockeyleaguePackage.eINSTANCE.getLeague();
		EObject rootObject = HockeyleagueFactory.eINSTANCE.create(eClass);
		return rootObject;
	}
}