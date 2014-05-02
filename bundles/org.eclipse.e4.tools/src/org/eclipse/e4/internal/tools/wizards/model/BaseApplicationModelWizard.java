/*******************************************************************************
 * Copyright (c) 2010 - 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Dmitry Spiridenok <d.spiridenok@gmail.com> - Bug 408712
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

public abstract class BaseApplicationModelWizard extends Wizard implements INewWizard {
	private NewModelFilePage page;
	private ISelection selection;

	protected IWorkbench workbench;

	/**
	 * Constructor for NewApplicationModelWizard.
	 */
	public BaseApplicationModelWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages() {
		page = createWizardPage(selection);
		addPage(page);
	}

	protected abstract NewModelFilePage createWizardPage(ISelection selection);

	public abstract String getDefaultFileName();

	@Override
	public boolean performFinish() {
		try {
			// Remember the file.
			//
			final IFile modelFile = getModelFile();

			if( modelFile.exists() ) {
				if( ! MessageDialog.openQuestion(getShell(), "File exists", "The file already exists. "
						+ "Would you like to add the extracted node to this file?")) {
					return false;
				}
			}

			// Do the work within an operation.
			//
			WorkspaceModifyOperation operation =
				new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor progressMonitor) {
						try {
							// Create a resource set
							//
							ResourceSet resourceSet = new ResourceSetImpl();

							// Get the URI of the model file.
							//
							URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);

							// Create a resource for this file.
							//
							Resource resource = resourceSet.createResource(fileURI);

							// If target file already exists, load its content
							//
							if (modelFile.exists()) {
								resource.load(null);
							}

							// Add the initial model object to the contents.
							//
							EObject rootObject = createInitialModel();
							if (rootObject != null) {
								if (resource.getContents().size() == 0) {
									//If target model is empty (file just created) => add as is
									resource.getContents().add(rootObject);
								} else {
									//Otherwise (file already exists) => take the roots of source and target models
									//and copy multiple attributes 'imports' and 'fragments' objects from source to target
									MModelFragments sourceFragments = (MModelFragments) rootObject;
									MModelFragments targetFragments = (MModelFragments) resource.getContents().get(0);


									List<MCommand> listOfAllImportsFromElements=new ArrayList<MCommand>();
									for (MModelFragment fragment : sourceFragments.getFragments()) {
										List<MCommand> commandsToImport=new ArrayList<MCommand>();
										EObject eObject=(EObject) fragment;
										TreeIterator<EObject> eAllContents = eObject.eAllContents();
										while(eAllContents.hasNext()){
											EObject next = eAllContents.next();
											MApplicationElement mApplicationElement=(MApplicationElement) next;
											if(mApplicationElement instanceof MHandler){
												MHandler mHandler=(MHandler)mApplicationElement;
												MCommand command = mHandler.getCommand();
												commandsToImport.add(command);
												MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject)command);
												targetFragments.getImports().add(copy);
												mHandler.setCommand((MCommand) copy);
											}
											else if(mApplicationElement instanceof MHandledItem){
												MHandledItem mHandledItem=(MHandledItem)mApplicationElement;
												MCommand command = mHandledItem.getCommand();
												commandsToImport.add(command);
												MApplicationElement copy = (MApplicationElement) EcoreUtil.copy((EObject)command);
												targetFragments.getImports().add(copy);
												mHandledItem.setCommand((MCommand) copy);
											}

										}
										listOfAllImportsFromElements.addAll(commandsToImport);
										targetFragments.getFragments().add((MModelFragment) EcoreUtil.copy((EObject) fragment));

									}
									for (MApplicationElement element : sourceFragments.getImports()) {
										boolean isAlreadyImport=true;
					  					for (MCommand mCommand : listOfAllImportsFromElements) {

											if(!mCommand.getElementId().equals(element.getElementId())){

											    isAlreadyImport=false;
												break;
											}
											if(!isAlreadyImport){

												targetFragments.getImports().add((MApplicationElement) EcoreUtil.copy((EObject)element));
											}

										}

									}

								}
							}

							// Save the contents of the resource to the file system.
							//
							Map<Object, Object> options = new HashMap<Object, Object>();
							resource.save(options);
							adjustBuildPropertiesFile( modelFile );
						}
						catch (Exception exception) {
							throw new RuntimeException(exception);
						}
						finally {
							progressMonitor.done();
						}
					}
				};

			getContainer().run(false, false, operation);

			// Select the new file resource in the current view.
			//
			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(modelFile);
				getShell().getDisplay().asyncExec
					(new Runnable() {
						 public void run() {
							 ((ISetSelectionTarget)activePart).selectReveal(targetSelection);
						 }
					 });
			}

			// Open an editor on the new file.
			//
			try {
				page.openEditor
					(new FileEditorInput(modelFile),
					 workbench.getEditorRegistry().getDefaultEditor(modelFile.getFullPath().toString()).getId());
			}
			catch (PartInitException exception) {
				MessageDialog.openError(workbenchWindow.getShell(), "Could not init editor", exception.getMessage()); //$NON-NLS-1$
				return false;
			}

			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			MessageDialog.openError(getShell(), "Error", exception.getMessage());
			return false;
		}
	}

	protected abstract EObject createInitialModel();

	protected IFile getModelFile() throws CoreException {
		String containerName = page.getContainerName();
		String fileName = page.getFileName();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		return container.getFile(new Path(fileName));
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,
				"org.eclipse.e4.tools.emf.editor3x", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 *
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}
	/**
	 * Adds other file to the build.properties file.
	 */
	private void adjustBuildPropertiesFile(IFile file)
			throws CoreException {
		IProject project = file.getProject();
		IFile buildPropertiesFile = PDEProject.getBuildProperties(project);
		if (buildPropertiesFile.exists()) {
			WorkspaceBuildModel model = new WorkspaceBuildModel(buildPropertiesFile);
			IBuildEntry entry = model.getBuild().getEntry(IBuildEntry.BIN_INCLUDES);
			String token = file.getProjectRelativePath().toString();
			if(!entry.contains(token)) entry.addToken(token);
			model.save();
		}
	}
}