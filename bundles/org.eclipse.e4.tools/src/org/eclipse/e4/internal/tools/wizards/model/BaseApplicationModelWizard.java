/*******************************************************************************
 * Copyright (c) 2010 - 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Dmitry Spiridenok <d.spiridenok@gmail.com> - Bug 408712
 * Marco Descher <marco@descher.at> - Bug 434371
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
import org.eclipse.e4.internal.tools.Messages;
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
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
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

@SuppressWarnings("restriction")
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

			if (modelFile.exists()) {
				if (!MessageDialog.openQuestion(getShell(), Messages.BaseApplicationModelWizard_FileExists, Messages.BaseApplicationModelWizard_TheFileAlreadyExists
					+ Messages.BaseApplicationModelWizard_AddExtractedNode)) {
					return false;
				}
			}

			// Do the work within an operation.
			//
			final WorkspaceModifyOperation operation =
				new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor progressMonitor) {
						try {
							// Create a resource set
							//
							final ResourceSet resourceSet = new ResourceSetImpl();

							// Get the URI of the model file.
							//
							final URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);

							// Create a resource for this file.
							//
							final Resource resource = resourceSet.createResource(fileURI);

							// If target file already exists, load its content
							//
							if (modelFile.exists()) {
								resource.load(null);
							}

							// Add the initial model object to the contents.
							//
							final EObject rootObject = createInitialModel();
							if (rootObject != null) {
								if (resource.getContents().size() == 0) {
									// If target model is empty (file just created) => add as is
									resource.getContents().add(rootObject);
								} else {
									// Otherwise (file already exists) => take the roots of source and target models
									// and copy multiple attributes 'imports' and 'fragments' objects from source to
								// target
									final MModelFragments sourceFragments = (MModelFragments) rootObject;
									final MModelFragments targetFragments = (MModelFragments) resource.getContents()
									.get(0);

								final List<MCommand> listOfAllImportsFromElements = new ArrayList<MCommand>();
									for (final MModelFragment fragment : sourceFragments.getFragments()) {
										final List<MCommand> commandsToImport = new ArrayList<MCommand>();
										final EObject eObject = (EObject) fragment;
										final TreeIterator<EObject> eAllContents = eObject.eAllContents();
										while (eAllContents.hasNext()) {
											final EObject next = eAllContents.next();
											final MApplicationElement mApplicationElement = (MApplicationElement) next;
											if (mApplicationElement instanceof MHandler) {
												final MHandler mHandler = (MHandler) mApplicationElement;
												final MCommand command = mHandler.getCommand();
												commandsToImport.add(command);
												final MApplicationElement copy = (MApplicationElement) EcoreUtil
												.copy((EObject) command);
												targetFragments.getImports().add(copy);
												mHandler.setCommand((MCommand) copy);
											}
											else if (mApplicationElement instanceof MHandledItem) {
												final MHandledItem mHandledItem = (MHandledItem) mApplicationElement;
												final MCommand command = mHandledItem.getCommand();
												commandsToImport.add(command);
												final MApplicationElement copy = (MApplicationElement) EcoreUtil
												.copy((EObject) command);
												targetFragments.getImports().add(copy);
												mHandledItem.setCommand((MCommand) copy);
											}

										}
										listOfAllImportsFromElements.addAll(commandsToImport);
										targetFragments.getFragments().add(
										(MModelFragment) EcoreUtil.copy((EObject) fragment));

									}
									for (final MApplicationElement element : sourceFragments.getImports()) {
										boolean isAlreadyImport = true;
									for (final MCommand mCommand : listOfAllImportsFromElements) {

											if (!mCommand.getElementId().equals(element.getElementId())) {

											isAlreadyImport = false;
												break;
											}
											if (!isAlreadyImport) {

												targetFragments.getImports().add(
												(MApplicationElement) EcoreUtil.copy((EObject) element));
											}

										}

									}

								}
							}

							// Save the contents of the resource to the file system.
							//
							final Map<Object, Object> options = new HashMap<Object, Object>();
							resource.save(options);
							adjustBuildPropertiesFile(modelFile);
							adjustDependencies(modelFile);
						}
						catch (final Exception exception) {
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
			final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			final IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(modelFile);
				getShell().getDisplay().asyncExec
					(new Runnable() {
					@Override
						public void run() {
						((ISetSelectionTarget) activePart).selectReveal(targetSelection);
					}
				});
			}

			// Open an editor on the new file.
			//
			try {
				page.openEditor
					(new FileEditorInput(modelFile),
					workbench.getEditorRegistry().getDefaultEditor(modelFile.getFullPath().toString()).getId());
			} catch (final PartInitException exception) {
				MessageDialog.openError(workbenchWindow.getShell(), "Could not init editor", exception.getMessage()); //$NON-NLS-1$
				return false;
			}

			return true;
		} catch (final Exception exception) {
			exception.printStackTrace();
			MessageDialog.openError(getShell(), Messages.BaseApplicationModelWizard_Error, exception.getMessage());
			return false;
		}
	}

	protected abstract EObject createInitialModel();

	protected IFile getModelFile() throws CoreException {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName //$NON-NLS-1$
				+ "\" does not exist."); //$NON-NLS-1$
		}
		final IContainer container = (IContainer) resource;
		return container.getFile(new Path(fileName));
	}

	private void throwCoreException(String message) throws CoreException {
		final IStatus status = new Status(IStatus.ERROR, "org.eclipse.e4.tools.emf.editor3x", IStatus.OK, message, null); //$NON-NLS-1$
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 *
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	/**
	 * Adds other file to the build.properties file.
	 */
	private void adjustBuildPropertiesFile(IFile file)
		throws CoreException {
		final IProject project = file.getProject();
		final IFile buildPropertiesFile = PDEProject.getBuildProperties(project);
		if (buildPropertiesFile.exists()) {
			final WorkspaceBuildModel model = new WorkspaceBuildModel(buildPropertiesFile);
			final IBuildEntry entry = model.getBuild().getEntry(IBuildEntry.BIN_INCLUDES);
			final String token = file.getProjectRelativePath().toString();
			if (!entry.contains(token)) {
				entry.addToken(token);
			}
			model.save();
		}
	}

	/**
	 * Callback hook to allow for after-file-creation modifications. Default
	 * implementation does nothing.
	 *
	 * @param file
	 *            the file created by the wizard
	 */
	protected void adjustDependencies(IFile file) {
	}

	/**
	 * Add the required dependencies (org.eclipse.e4.ui.model.workbench) and
	 * register fragment.e4xmi at the required extension point
	 * (org.eclipse.e4.workbench.model)
	 */
	protected void adjustFragmentDependencies(IFile file) {
		final IProject project = file.getProject();
		final IFile pluginXml = PDEProject.getPluginXml(project);
		final IFile manifest = PDEProject.getManifest(project);

		final WorkspaceBundlePluginModel fModel = new WorkspaceBundlePluginModel(
			manifest, pluginXml);
		try {
			addWorkbenchDependencyIfRequired(fModel);
			registerWithExtensionPointIfRequired(project, fModel, file);
		} catch (final CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.BaseApplicationModelWizard_Error, e.getMessage());
		}
	}

	private void addWorkbenchDependencyIfRequired(
		WorkspaceBundlePluginModel fModel) throws CoreException {
		final IPluginImport[] imports = fModel.getPluginBase().getImports();

		final String WORKBENCH_IMPORT_ID = "org.eclipse.e4.ui.model.workbench"; //$NON-NLS-1$

		for (final IPluginImport iPluginImport : imports) {
			if (WORKBENCH_IMPORT_ID.equalsIgnoreCase(iPluginImport.getId())) {
				return;
			}
		}

		String version = ""; //$NON-NLS-1$
		final IPluginModelBase findModel = PluginRegistry
			.findModel(WORKBENCH_IMPORT_ID);
		if (findModel != null) {
			final BundleDescription bundleDescription = findModel
				.getBundleDescription();
			if (bundleDescription != null) {
				version = bundleDescription.getVersion().toString()
					.replaceFirst("\\.qualifier$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		final IPluginImport workbenchImport = fModel.getPluginFactory()
			.createImport();
		workbenchImport.setId(WORKBENCH_IMPORT_ID);
		workbenchImport.setVersion(version);
		workbenchImport.setMatch(IMatchRules.GREATER_OR_EQUAL);
		fModel.getPluginBase().add(workbenchImport);
		fModel.save();
	}

	/**
	 * Register the fragment.e4xmi with the org.eclipse.e4.workbench.model
	 * extension point, if there is not already a fragment registered.
	 */
	private void registerWithExtensionPointIfRequired(IProject project,
		WorkspaceBundlePluginModel fModel, IFile file) throws CoreException {
		final IPluginExtension[] extensions = fModel.getPluginBase().getExtensions();

		final String WORKBENCH_MODEL_EP_ID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$
		final String FRAGMENT = "fragment"; //$NON-NLS-1$

		for (final IPluginExtension iPluginExtension : extensions) {
			if (WORKBENCH_MODEL_EP_ID
				.equalsIgnoreCase(iPluginExtension.getId())) {
				final IPluginObject[] children = iPluginExtension.getChildren();
				for (final IPluginObject child : children) {
					if (FRAGMENT.equalsIgnoreCase(child.getName())) {
						return;
					}
				}
			}
		}

		final IPluginExtension extPointFragmentRegister = fModel.getPluginFactory()
			.createExtension();
		final IPluginElement element = extPointFragmentRegister.getModel()
			.getFactory().createElement(extPointFragmentRegister);
		element.setName(FRAGMENT);
		element.setAttribute("uri", file.getName()); //$NON-NLS-1$
		extPointFragmentRegister.setId(project.getName() + "." + FRAGMENT); //$NON-NLS-1$
		extPointFragmentRegister.setPoint(WORKBENCH_MODEL_EP_ID);
		extPointFragmentRegister.add(element);
		fModel.getPluginBase().add(extPointFragmentRegister);
		fModel.save();
	}
}