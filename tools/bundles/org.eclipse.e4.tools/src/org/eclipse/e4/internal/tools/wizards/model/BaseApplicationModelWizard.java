/*******************************************************************************
 * Copyright (c) 2010, 2017 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Dmitry Spiridenok <d.spiridenok@gmail.com> - Bug 408712
 * Marco Descher <marco@descher.at> - Bug 434371
 * Olivier Prouvost <olivier.prouvost@opcoach.com> Bug 485723, Bug 436836
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IExtensionsModelFactory;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginExtensionPoint;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
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
				final boolean continueWithExistingFile = handleFileExist();
				if (!continueWithExistingFile) {
					return true;
				}

			}

			// Do the work within an operation.
			//
			final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
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

						final EObject rootObject = createInitialModel();

						if (rootObject == null) {
							throw new IllegalArgumentException(Messages.BaseApplicationModelWizard_ModelRootMustNotBeNull);
						}

						// If target file already exists, load its content
						//
						if (modelFile.exists()) {
							resource.load(null);

							mergeWithExistingFile(resource, rootObject);
						} else {
							// If target model is empty (file just created)
							// => add as is
							resource.getContents().add(rootObject);
						}

						// Save the contents of the resource to the file system.
						//
						final Map<Object, Object> options = new HashMap<>();
						resource.save(options);
						adjustBuildPropertiesFile(modelFile);
						adjustDependencies(modelFile);
					} catch (final Exception exception) {
						throw new RuntimeException(exception);
					} finally {
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
				getShell().getDisplay()
				.asyncExec(() -> ((ISetSelectionTarget) activePart).selectReveal(targetSelection));
			}

			// Open an editor on the new file.
			//
			try {
				page.openEditor(new FileEditorInput(modelFile),
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

	/**
	 * @return if the wizard should continue in case the file already exists
	 */
	protected boolean handleFileExist() {
		MessageDialog.openInformation(getShell(), Messages.BaseApplicationModelWizard_FileExists,
				Messages.BaseApplicationModelWizard_TheFileAlreadyExists);

		return false;
	}

	/**
	 * Creates the rootObject of the new model file. Must not be null.
	 *
	 * @return The root {@link EObject}
	 */
	protected abstract EObject createInitialModel();

	protected IFile getModelFile() throws CoreException {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IResource resource = root.findMember(IPath.fromOSString(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName //$NON-NLS-1$
					+ "\" does not exist."); //$NON-NLS-1$
		}
		final IContainer container = (IContainer) resource;
		return container.getFile(IPath.fromOSString(fileName));
	}

	private void throwCoreException(String message) throws CoreException {
		final IStatus status = new Status(IStatus.ERROR, "org.eclipse.e4.tools.emf.editor3x", IStatus.OK, message, //$NON-NLS-1$
				null);
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
	private void adjustBuildPropertiesFile(IFile file) throws CoreException {
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

		final WorkspaceBundlePluginModel fModel = new WorkspaceBundlePluginModel(manifest, pluginXml);
		try {
			addWorkbenchDependencyIfRequired(fModel);
			registerWithExtensionPointIfRequired(project, fModel, file);
		} catch (final CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.BaseApplicationModelWizard_Error, e.getMessage());
		}
	}

	private void addWorkbenchDependencyIfRequired(WorkspaceBundlePluginModel fModel) throws CoreException {
		final IPluginImport[] imports = fModel.getPluginBase().getImports();

		final String WORKBENCH_IMPORT_ID = "org.eclipse.e4.ui.model.workbench"; //$NON-NLS-1$

		for (final IPluginImport iPluginImport : imports) {
			if (WORKBENCH_IMPORT_ID.equalsIgnoreCase(iPluginImport.getId())) {
				return;
			}
		}

		String version = ""; //$NON-NLS-1$
		final IPluginModelBase findModel = PluginRegistry.findModel(WORKBENCH_IMPORT_ID);
		if (findModel != null) {
			final BundleDescription bundleDescription = findModel.getBundleDescription();
			if (bundleDescription != null) {
				version = bundleDescription.getVersion().toString().replaceFirst("\\.qualifier$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		final IPluginImport workbenchImport = fModel.getPluginFactory().createImport();
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
	protected void registerWithExtensionPointIfRequired(IProject project, WorkspaceBundlePluginModel fModel, IFile file)
			throws CoreException {

		final String WORKBENCH_MODEL_EP_ID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$
		final String FRAGMENT = "fragment"; //$NON-NLS-1$

		// Fix bug #436836 : the received fModel is an empty plugin model
		// without extension.
		// We must copy extensions found in registry plugin model into it
		// The registry plugin model is read only and must be copied inside the
		// new extension value..
		final BundlePluginModel registryModel = (BundlePluginModel) PluginRegistry.findModel(project.getName());
		// The registry Model is not modifiable and may be contains some
		// existing extensions.
		// Must copy them in the new created fModel
		for (final IPluginExtension e : registryModel.getPluginBase().getExtensions()) {
			final IPluginExtension clonedExtens = copyExtension(fModel.getFactory(), e);
			fModel.getPluginBase().add(clonedExtens);
		}

		// Fix 485723 Must do the same for extension points
		for (final IPluginExtensionPoint ep : registryModel.getPluginBase().getExtensionPoints()) {
			final IPluginExtensionPoint clonedExtensionPoint = copyExtensionPoint(fModel.getFactory(), ep);
			fModel.getPluginBase().add(clonedExtensionPoint);
		}

		// Can now check if we must add this extension (may be already inside).
		final IPluginExtension[] extensions = fModel.getPluginBase().getExtensions();
		for (final IPluginExtension iPluginExtension : extensions) {
			if (WORKBENCH_MODEL_EP_ID.equalsIgnoreCase(iPluginExtension.getPoint())) {
				final IPluginObject[] children = iPluginExtension.getChildren();
				for (final IPluginObject child : children) {
					if (FRAGMENT.equalsIgnoreCase(child.getName())) {
						return;
					}
				}
			}
		}

		final IPluginExtension extPointFragmentRegister = fModel.getPluginFactory().createExtension();
		final IPluginElement element = extPointFragmentRegister.getModel().getFactory()
				.createElement(extPointFragmentRegister);
		element.setName(FRAGMENT);
		element.setAttribute("uri", file.getName()); //$NON-NLS-1$
		// Bug 538922 set default value of apply to always
		element.setAttribute("apply", "always"); //$NON-NLS-1$ //$NON-NLS-2$
		extPointFragmentRegister.setId(project.getName() + "." + FRAGMENT); //$NON-NLS-1$
		extPointFragmentRegister.setPoint(WORKBENCH_MODEL_EP_ID);
		extPointFragmentRegister.add(element);
		fModel.getPluginBase().add(extPointFragmentRegister);
		fModel.save();
	}

	// Used to Fix bug #436836
	private IPluginExtension copyExtension(IExtensionsModelFactory factory, final IPluginExtension ext) {
		try {
			final IPluginExtension clonedExt = factory.createExtension();
			clonedExt.setPoint(ext.getPoint());
			final IPluginObject[] _children = ext.getChildren();
			for (final IPluginObject elt : _children) {
				if (elt instanceof IPluginElement) {
					final IPluginElement ipe = (IPluginElement) elt;
					final IPluginElement clonedElt = copyExtensionElement(factory, ipe, ext);
					clonedExt.add(clonedElt);
				}
			}
			return clonedExt;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	// Used to Fix bug #485723
	private IPluginExtensionPoint copyExtensionPoint(IExtensionsModelFactory factory, final IPluginExtensionPoint ep) {
		try {
			final IPluginExtensionPoint clonedExtPt = factory.createExtensionPoint();
			clonedExtPt.setId(ep.getId());
			clonedExtPt.setName(ep.getName());
			clonedExtPt.setSchema(ep.getSchema());

			return clonedExtPt;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	// Used to Fix bug #436836
	private IPluginElement copyExtensionElement(IExtensionsModelFactory factory, final IPluginElement elt,
			final IPluginObject parent) {
		try {
			final IPluginElement clonedElt = factory.createElement(parent);
			clonedElt.setName(elt.getName());
			for (final IPluginAttribute a : elt.getAttributes()) {
				clonedElt.setAttribute(a.getName(), a.getValue());
			}
			for (final IPluginObject e : elt.getChildren()) {
				if (e instanceof IPluginElement) {
					final IPluginElement ipe = (IPluginElement) e;
					final IPluginElement copyExtensionElement = copyExtensionElement(factory, ipe, clonedElt);
					clonedElt.add(copyExtensionElement);
				}
			}
			return clonedElt;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * @param resource
	 * @param rootObject
	 */
	protected void mergeWithExistingFile(Resource resource, EObject rootObject) {
		// do nothing

	}

}