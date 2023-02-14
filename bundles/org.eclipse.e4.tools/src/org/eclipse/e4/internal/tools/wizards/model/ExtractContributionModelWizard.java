/*******************************************************************************
 * Copyright (c) 2010-2014 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sopot Cela <sopotcela@gmail.com> - initial API and implementation
 * Marco Descher <marco@descher.at> - Bug 434371
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class ExtractContributionModelWizard extends BaseApplicationModelWizard {

	private final List<MApplicationElement> oes = new ArrayList<>();

	public ExtractContributionModelWizard(MApplicationElement oe) {
		super();
		oes.add(oe);
	}

	public ExtractContributionModelWizard(List<MApplicationElement> oes) {
		this.oes.addAll(oes);
	}

	public ExtractContributionModelWizard() {
		super();
	}

	@Override
	public String getDefaultFileName() {
		return "fragment.e4xmi"; //$NON-NLS-1$
	}

	@Override
	protected EObject createInitialModel() {
		return (EObject) FragmentExtractHelper.createInitialModel(oes);
	}

	public void setup(IProject project) {
		init(PlatformUI.getWorkbench(), new StructuredSelection(project));
	}

	@Override
	protected NewModelFilePage createWizardPage(ISelection selection) {
		return new NewModelFilePage(selection, getDefaultFileName());
	}

	@Override
	protected void adjustDependencies(IFile file) {
		super.adjustFragmentDependencies(file);
	}

	@Override
	protected boolean handleFileExist() {
		return MessageDialog.openQuestion(getShell(), Messages.BaseApplicationModelWizard_FileExists,
				Messages.BaseApplicationModelWizard_TheFileAlreadyExists
				+ Messages.BaseApplicationModelWizard_AddExtractedNode);

	}

	@Override
	protected void mergeWithExistingFile(Resource resource, EObject rootObject) {

		final EObject existingRootObject = resource.getContents().get(0);
		if (!(existingRootObject instanceof MModelFragments)) {
			throw new IllegalStateException(Messages.ExtractContributionModelWizard_ExistingFragmentIsCorrupted);
		}

		final MModelFragments sourceFragments = (MModelFragments) rootObject;
		final MModelFragments targetFragments = (MModelFragments) existingRootObject;
		FragmentMergeHelper.merge(sourceFragments, targetFragments);


	}
}