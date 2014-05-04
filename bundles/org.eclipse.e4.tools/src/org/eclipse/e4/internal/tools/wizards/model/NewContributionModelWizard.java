/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 392907
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.project.PDEProject;

public class NewContributionModelWizard extends BaseApplicationModelWizard {

	@Override
	public String getDefaultFileName() {
		return "fragment.e4xmi";
	}

	@Override
	protected EObject createInitialModel() {
		return (EObject) MFragmentFactory.INSTANCE.createModelFragments();
	}

	@Override
	protected NewModelFilePage createWizardPage(ISelection selection) {
		return new NewModelFilePage(selection, getDefaultFileName());
	}

	/**
	 * Add the required dependencies (org.eclipse.e4.ui.model.workbench) and
	 * register fragment.e4xmi at the required extension point
	 * (org.eclipse.e4.workbench.model)
	 */
	@Override
	protected void adjustDependencies(IFile file) {
		IProject project = file.getProject();
		IFile pluginXml = PDEProject.getPluginXml(project);
		IFile manifest = PDEProject.getManifest(project);

		WorkspaceBundlePluginModel fModel = new WorkspaceBundlePluginModel(
				manifest, pluginXml);
		try {
			addWorkbenchDependencyIfRequired(fModel);
			registerWithExtensionPointIfRequired(project, fModel, file);
		} catch (CoreException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
	}

	private void addWorkbenchDependencyIfRequired(
			WorkspaceBundlePluginModel fModel) throws CoreException {
		IPluginImport[] imports = fModel.getPluginBase().getImports();

		final String WORKBENCH_IMPORT_ID = "org.eclipse.e4.ui.model.workbench"; //$NON-NLS-1$

		for (IPluginImport iPluginImport : imports) {
			if (WORKBENCH_IMPORT_ID.equalsIgnoreCase(iPluginImport.getId()))
				return;
		}

		String version = "";
		IPluginModelBase findModel = PluginRegistry
				.findModel(WORKBENCH_IMPORT_ID);
		if (findModel != null) {
			BundleDescription bundleDescription = findModel
					.getBundleDescription();
			if (bundleDescription != null)
				version = bundleDescription.getVersion().toString();
		}

		IPluginImport workbenchImport = fModel.getPluginFactory()
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
		IPluginExtension[] extensions = fModel.getPluginBase().getExtensions();

		final String WORKBENCH_MODEL_EP_ID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$
		final String FRAGMENT = "fragment";

		for (IPluginExtension iPluginExtension : extensions) {
			if (WORKBENCH_MODEL_EP_ID
					.equalsIgnoreCase(iPluginExtension.getId())) {
				IPluginObject[] children = iPluginExtension.getChildren();
				for (IPluginObject child : children) {
					if (FRAGMENT.equalsIgnoreCase(child.getName())) //$NON-NLS-1$
						return;
				}
			}
		}

		IPluginExtension extPointFragmentRegister = fModel.getPluginFactory()
				.createExtension();
		IPluginElement element = extPointFragmentRegister.getModel()
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